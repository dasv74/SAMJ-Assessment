package ai.nets.samj.assessment.tools;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
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
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class Experiment  {

	private String path;
	private String name;
	private long annotationTime;
	private ResultsTable table;
	private IJLogger logger = new IJLogger();
	
	public Experiment( String name, Encoding.Mode mode) {
		this.path = Tools.getDesktopPath() + File.separator + "SAMJ-Experiment" + File.separator ;
		this.name = name;
		new File(this.path).mkdir();
		this.path = this.path + File.separator + name + File.separator;
		new File(this.path).mkdir();
		this.table = new ResultsTable();
	}

	public void run(ImageTest image, Regions regions, int iter, SAMModel model,  int margin, int levelNoise) throws IOException, RuntimeException, InterruptedException {
		long encodingTime = encode(model, image);
		model.setReturnOnlyBiggest(true);
		Overlay overlay = new Overlay();
		MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		String machine = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");

		for(Region region : regions) {
			Rectangle prompt = region.rectangle(margin);				
			Region sam;
			try {
				sam = promptAnnotation(prompt, model);
			} catch (IOException | RuntimeException | InterruptedException e) {
				continue;
			}
			logger.info("Final SAM>>> " + region.getRoi(Color.BLACK).getBounds());
			
			overlay.add(new Roi(prompt));
			overlay.add(region.getRoi(Color.GREEN));
			overlay.add(sam.getRoi(Color.RED));
			Metric metric = new Metric(region, sam);
			String msg = String.format("%1.3f", metric.IoU);
			overlay.add(new TextRoi(prompt.x, prompt.y, msg));
			
			table.incrementCounter();
			table.addValue("Model", model.getName());
			table.addValue("Iteration", (iter+1));
			table.addValue("Image", image.area());
			table.addValue("Noise", String.format("%3.2f", (levelNoise/256.0)));
			table.addValue("Prompt", (prompt.width*prompt.height));
			table.addValue("Encoding", String.format("%3.5f", encodingTime / 1000.0));
			table.addValue("Annotation", String.format("%3.5f", annotationTime / 1000.0));
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
	}
	
	private long encode(SAMModel model, ImageTest im) throws IOException, RuntimeException, InterruptedException {
		long tt = System.currentTimeMillis();
		model.setImage(ImageJFunctions.wrap(im.test), logger);
		return System.currentTimeMillis() - tt;
	}
	
	private Region promptAnnotation(Rectangle prompt, SAMModel model) throws IOException, RuntimeException, InterruptedException {
		long[] pos = new long[] { prompt.x, prompt.y };
		long[] shape = new long[] { prompt.x + prompt.width - 1, prompt.y + prompt.height - 1 };
		Interval rectInterval = new FinalInterval(pos, shape);			
		long tt = System.currentTimeMillis();
		Polygon polygon = model.fetch2dSegmentation(rectInterval).get(0);
		annotationTime = System.currentTimeMillis() - tt;
		logger.info("Polygon " + polygon.npoints + " points");
		Region sam = new Region(polygon);
		return sam;
	}
	
	public void save() {
		table.show(name);
		table.save(path + "result-" + name + ".csv");
	}
	
}
