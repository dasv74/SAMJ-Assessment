package ai.nets.samj.assessment.tools;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Tools {
	
	static public String getDesktopPath() {
		String home = System.getProperty("user.home");
		return home + File.separator + "Desktop" + File.separator;	
	}
	
	static public Rectangle permuteXY(Rectangle source) {
		Rectangle rect = new Rectangle(source.y, source.x, source.height, source.width);
		return rect;
	}

	static public PolygonRoi permuteXY(PolygonRoi source) {
		Polygon p = source.getPolygon();
		Polygon pp = new Polygon();
		for (int i = 0; i < p.npoints; i++) {
			pp.addPoint(p.ypoints[i], p.xpoints[i]);
		}
		return new PolygonRoi(pp, PolygonRoi.POLYGON);
	}
		
	static public ImagePlus createGroundTruth(int nx, int ny, ArrayList<Rectangle> rects, double signalValue) {
		ImagePlus imp = IJ.createImage("GT-" + nx + "x" + ny, nx, ny, 1, 8);
		ImageProcessor ip = imp.getProcessor();
		ip.setColor(signalValue);
		for (Rectangle rect : rects) {
			ip.setRoi(new Roi(rect));
			ip.fill();
		}
		return imp;
	}

	static public double iou(Roi roi1, Roi roi2) {
		Rectangle r1 = roi1.getBounds();
		Rectangle r2 = roi2.getBounds();
		int x1 = Math.min(r1.x, r2.x);
		int y1 = Math.min(r1.y, r2.y);
		int x2 = Math.max(r1.x+r1.width, r2.x+r2.width);
		int y2 = Math.max(r1.y+r1.height, r2.y+r2.height);
		int intersection = 0;
		int union = 0;
		for (int i = x1-1; i <= x2+1; i++) {
			for (int j = y1-1; j <= y2+1; j++) {
				boolean a1 = roi1.contains(i, j);
				boolean a2 = roi2.contains(i, j);
				if (a1 && a2) intersection++;
				if (a1 || a2) union++;
			}
		}
		double iou = (double) intersection / (double) union;
		return iou;
	}
	

	

}
