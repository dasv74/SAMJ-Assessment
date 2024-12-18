package ai.nets.samj.assessment.tools;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.assessment.simulation.Region;
import ai.nets.samj.communication.model.SAMModel;
import ij.ImagePlus;
import ij.gui.Roi;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class Encoding {

	public enum Mode { WHOLE, TILING };	// Encoding mode
	
	private int SAM_IMAGE_SIZE = 512;
	
	private ArrayList<SAMModel> instances = new ArrayList<SAMModel>();
	private ArrayList<Rectangle> tiles = new ArrayList<Rectangle>();

	private double timeEncoding = 0;
	private double timeAnnotation = 0;
	private IJLogger logger = new IJLogger();

	public Encoding(SAMModel model, ImagePlus imp, Mode mode) {

		int nx = imp.getWidth();
		int ny = imp.getHeight();
		timeEncoding = System.nanoTime();
		
		if (mode == Mode.WHOLE) {
			addTile(imp, 0, 0, nx, ny);
			addInstance(model, imp);
		}
		
		else {
			int mx = 1 + (nx-1) / SAM_IMAGE_SIZE;
			int my = 1 + (ny-1) / SAM_IMAGE_SIZE;
			int overlapx = mx > 1 ? (mx*SAM_IMAGE_SIZE - nx)/(mx-1) : 0;
			int overlapy = my > 1 ? (my*SAM_IMAGE_SIZE - ny)/(my-1) : 0;
			int px = 0;
			for(int tx=0; tx<mx; tx++) {
				int sx = px + SAM_IMAGE_SIZE < nx ? SAM_IMAGE_SIZE : nx-px;
				int py = 0;
				for(int ty=0; ty<my; ty++) {
					int sy = py + SAM_IMAGE_SIZE < ny ? SAM_IMAGE_SIZE : ny-py;
					addTile(imp, px, py, sx, sy);
					ImagePlus crop = new ImagePlus(""+tx+"-"+ty, imp.getProcessor().crop());
					addInstance(model, crop);
					py += SAM_IMAGE_SIZE - overlapy;
				}
				px += SAM_IMAGE_SIZE - overlapx;
			}
		}
		timeEncoding = (System.nanoTime() - timeEncoding) * 0.000000001;
	}
	
	private void addTile(ImagePlus imp, int px, int py, int sx, int sy) {
		Roi roi = new Roi(px, py, sx, sy);
		roi.setStrokeColor(Color.blue);
		imp.setRoi(roi);
		Rectangle tile = new Rectangle(px, py, sx, sy);
		tiles.add(tile);
	}
	
	private void addInstance(SAMModel model, ImagePlus imp) {
		Img<?> img = ImageJFunctions.wrap(imp);
		try {
			model.setImage(Cast.unchecked(img), logger);
			model.setReturnOnlyBiggest(true);
			instances.add(model);
		} catch (Exception ex) {
			System.out.println(ex);
			logger.error(ex.toString());
		}
	}

	public ArrayList<Rectangle> getTiles() {
		return tiles;
	}
	
	public void close() {
		for (SAMModel instance : instances)
			instance.closeProcess();
	}

	public Region annotate(Rectangle rect) {
		timeAnnotation = System.nanoTime();
		Region region = new Region(rect);
		int tileNumber = getTileNumber(rect);
		
		if (tileNumber < 0) {
			timeAnnotation = (System.nanoTime() - timeAnnotation) * 0.000000001;
			return region;
		}
		
		Rectangle tile = tiles.get(tileNumber);
		SAMModel instance = instances.get(tileNumber);
		int px = rect.x - tile.x;
		int py = rect.y - tile.y;
		Rectangle rectTiled = new Rectangle(px, py, rect.width, rect.height);
		Rectangle prompt = Tools.permuteXY(rectTiled);

		try {
			long[] pos = new long[] { prompt.x, prompt.y };
			long[] shape = new long[] { prompt.x + prompt.width - 1, prompt.y + prompt.height - 1 };
			Interval rectInterval = new FinalInterval(pos, shape);					
			List<Mask> masks = instance.fetch2dSegmentation(rectInterval);
			for (Mask mask : masks) {
				logger.info("Polygon " + mask.getContour().npoints + " points");
				region = new Region(mask.getContour());
				region.permuteXY(); // TODO Permutation Axis XY
				logger.info("Translate>>> " + tile.x + " "+  tile.y);
				region.translate(tile.x, tile.y);
				logger.info("Final SAM>>> " + region.getRoi(Color.BLACK).getBounds());
			}

		} catch (Exception ex) {
			ex.printStackTrace();;
			logger.error(ex.toString());
			return null;
		}
		timeAnnotation = (System.nanoTime() - timeAnnotation) * 0.000000001;
		return region;
	}

	private int getTileNumber(Rectangle rect) {
		int maxIntersectionArea = -1;
		int tileNumber = 0;
		for(int i=0; i<tiles.size(); i++) {
			Rectangle tile = tiles.get(i);
			Rectangle intersection = tile.intersection(rect);  // Permute
			int area = intersection.width * intersection.height;
			if (area > maxIntersectionArea) {
				maxIntersectionArea = area;
				tileNumber = i;
			}
			logger.info("Intersection i=" + i + " (" + rect + " in " + tile + ") = " + area);
		}
		logger.info("Intersection imax=" + tileNumber + " " + maxIntersectionArea);
		if (maxIntersectionArea < 1)
			return -1;

		logger.info(">> " + tileNumber + " " + tiles.size() + " " + instances.size());
		logger.info(">> " + (tiles.get(tileNumber) == null));
		logger.info(">> " + (instances.get(tileNumber) == null));
		return tileNumber;
	}
	
	public double getEncodingTime() {
		return timeEncoding;
	}

	public double getAnnotationTime() {
		return timeAnnotation;
	}
	
	/*
	public static void main(String[] args) {
		new ImageJ();
		SAMModels models = new SAMModels();
		ImagePlus imp = IJ.createImage("", 510, 1000, 1, 8);
		
		InstanceTiledModel instance = new InstanceTiledModel(models.get(1), imp);
		instance.annotate(new Rectangle(100, 500, 100, 200));
	}
	*/

}
