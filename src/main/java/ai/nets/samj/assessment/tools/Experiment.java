package ai.nets.samj.assessment.tools;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.sql.Timestamp;

import ai.nets.samj.assessment.simulation.ImageTest;
import ai.nets.samj.assessment.simulation.Metric;
import ai.nets.samj.assessment.simulation.Region;
import ai.nets.samj.assessment.simulation.Regions;
import ai.nets.samj.communication.model.SAMModel;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.ResultsTable;

public class Experiment  {

	private String path;
	private String name;
	private Encoding.Mode mode;
	private ResultsTable table;
	
	public Experiment( String name, Encoding.Mode mode) {
		this.path = Tools.getDesktopPath() + File.separator + "SAMJ-Experiment" + File.separator ;
		this.name = name;
		this.mode = mode;
		new File(this.path).mkdir();
		this.path = this.path + File.separator + name + File.separator;
		new File(this.path).mkdir();
		this.table = new ResultsTable();
	}

	public void run(ImageTest image, Regions regions, int iter, SAMModel model,  int margin, int levelNoise) {
		Encoding encoder = new Encoding(model, image.test, mode);
		int nx = image.gt.getWidth();
		int ny = image.gt.getHeight();
		encoder.annotate(new Rectangle(nx/2, ny/2, nx/8, ny/8)); // TODO First Annotation
		Overlay overlay = new Overlay();
		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		String machine = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");

		for(Region region : regions) {
			Rectangle prompt = region.rectangle(margin);
			Region sam = encoder.annotate(prompt);
			boolean error = sam == null;
			if (error) sam = new Region(prompt);
			
			overlay.add(new Roi(prompt));
			overlay.add(region.getRoi(Color.GREEN));
			overlay.add(sam.getRoi(Color.RED));
			Metric metric = new Metric(region, sam);
			String msg = String.format("%1.3f", metric.IoU);
			if (error) msg ="Error";
			overlay.add(new TextRoi(prompt.x, prompt.y, msg));
			
			table.incrementCounter();
			table.addValue("Model", model.getName());
			table.addValue("Iteration", (iter+1));
			table.addValue("Image", image.area());
			table.addValue("Noise", String.format("%3.2f", (levelNoise/256.0)));
			table.addValue("Prompt", (prompt.width*prompt.height));
			table.addValue("Encoding", String.format("%3.5f", encoder.getEncodingTime()));
			table.addValue("Annotation", String.format("%3.5f", encoder.getAnnotationTime()));
			table.addValue("IoU", String.format("%1.5f", metric.IoU));
			table.addValue("TP", ""+ metric.TP);
			table.addValue("FP", ""+ metric.FP);
			table.addValue("TN", ""+ metric.TN);
			table.addValue("Memory", heapMemoryUsage.getUsed());
			table.addValue("Uptime", String.format("%1.2f", (rt.getUptime()*0.001)));
			table.addValue("Machine", machine);
			table.addValue("TimeStamp", new Timestamp(System.currentTimeMillis()).toString());
		}
		for(Rectangle rect : encoder.getTiles()) {
			Roi roi = new Roi(rect);
			roi.setStrokeColor(Color.blue);
			overlay.add(roi);
		}
		image.test.setOverlay(overlay);
			
		image.save(path, model.getName() + "-" + levelNoise);
		encoder.close();
	}
	
	public void save() {
		table.show(name);
		table.save(path + "result-" + name + ".csv");
	}
	
	/*
	public void run(ImageTest image, Regions regions, int iter, SAMModel model,  int margin, int levelNoise) {
		InstanceModel instance = new InstanceModel(model, image.test);
		int nx = image.gt.getWidth();
		int ny = image.gt.getHeight();
		instance.annotate(new Rectangle(nx/2, ny/2, nx/8, ny/8)); // TODO First Annotation
		Overlay overlay = new Overlay();
		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		String machine = System.getProperty("os.name") + " " + 
						 System.getProperty("os.version") + " " + 
						 System.getProperty("os.arch");
		
		for(Region region : regions) {
			Rectangle prompt = region.rectangle(margin);
			Region sam = instance.annotate(prompt);
			overlay.add(new Roi(prompt));
			overlay.add(region.getRoi(Color.GREEN));
			overlay.add(sam.getRoi(Color.RED));
			Metric metric = new Metric(region, sam);
			TextRoi text = new TextRoi(prompt.x, prompt.y, String.format("%1.3f", metric.IoU));
			overlay.add(text);
			
			table.incrementCounter();
			table.addValue("Model", model.getName());
			table.addValue("Iteration", (iter+1));
			table.addValue("Image", image.area());
			table.addValue("Noise", String.format("%3.2f", (levelNoise/256.0)));
			table.addValue("Prompt", (prompt.width*prompt.height));
			table.addValue("Encoding", String.format("%3.5f", instance.getEncodingTime()));
			table.addValue("Annotation", String.format("%3.5f", instance.getAnnotationTime()));
			table.addValue("IoU", String.format("%1.5f", metric.IoU));
			table.addValue("TP", ""+ metric.TP);
			table.addValue("FP", ""+ metric.FP);
			table.addValue("TN", ""+ metric.TN);
			table.addValue("Memory", heapMemoryUsage.getUsed());
			table.addValue("Uptime", String.format("%1.2f", (rt.getUptime()*0.001)));
			table.addValue("Machine", machine);
			table.addValue("TimeStamp", new Timestamp(System.currentTimeMillis()).toString());
		}
		image.test.setOverlay(overlay);
		image.save(path, model.getName() + "-" + levelNoise);
		instance.close();
	}*/
	
}
