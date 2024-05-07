package ai.nets.samj.assessment.simulation;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

@SuppressWarnings("serial")

public class Region extends Polygon {

	public Region() {
		super();
	}
	
	public Region(Polygon polygon) {
		super();
		for(int i = 0; i<polygon.npoints; i++) {
			addPoint(polygon.xpoints[i], polygon.ypoints[i]);
		}
	}
	
	public Region(Rectangle rect) {
		super();
		addPoint(rect.x, rect.y);
		addPoint(rect.x+rect.width, rect.y);
		addPoint(rect.x+rect.width, rect.y+rect.height);
		addPoint(rect.x, rect.y+rect.height);
	}

	
	public Region(int x, int y, int w, int h, double angle) {
		super();
		double aradian = Math.toRadians(angle);
		double cosa = Math.cos(aradian);
		double sina = Math.sin(aradian);
		double px[] = {x - w/2, x + w/2, x + w/2, x - w/2};
		double py[] = {y - h/2, y - h/2, y + h/2, y + h/2};
		for(int i = 0; i<4; i++) {
			double dx = px[i] - x;
			double dy = py[i] - y;
			int ux = (int)(Math.round( dx * cosa + dy * sina) + x);
			int uy = (int)(Math.round(-dx * sina + dy * cosa) + y);
			addPoint(ux, uy);
		}
	}
	
	public Roi getRoi(Color c) {
		PolygonRoi roi = new PolygonRoi(this, PolygonRoi.POLYGON);
		roi.setStrokeColor(c);
		return roi;
	}
	
	public Rectangle rectangle(int extension) {
		Rectangle rect = getBounds();
		return new Rectangle(rect.x - extension, rect.y - extension, 
				rect.width + 2 * extension, rect.height + 2 * extension);
	}
	
	public void permuteXY() {
		for (int i = 0; i < npoints; i++) {
			int temp = ypoints[i];
			ypoints[i] = xpoints[i];
			xpoints[i] = temp;
		}
	}
	
	public void translate(int dx, int dy) {
		for (int i = 0; i < npoints; i++) {
			xpoints[i] += dx;
			ypoints[i] += dy;
		}
	}
		
	public double[][] increasePolygonSize(double[][] coords, double scaleFactor) {
        int n = coords[0].length;
        double[][] newCoords = new double[2][n];
        double xc = 0.0;
        double yc = 0.0;
        for (int i = 0; i < n; i++) {
            xc += coords[0][i];
            yc += coords[1][i];
        }
        xc /= n;
        yc /= n;
        for (int i = 0; i < n; i++) {
        	newCoords[0][i] = xc + (coords[0][i] - xc) * scaleFactor;
        	newCoords[1][i] = yc + (coords[1][i] - yc) * scaleFactor;
        }
        return newCoords;
    }
	
	public Region rotate() {
		int xc = 0;
		int yc = 0;
		for (int i = 0; i < npoints; i++) {
			xc += xpoints[i];
			yc += ypoints[i];
		}
		xc /= npoints;
		yc /= npoints;
		
		for (int i = 0; i < npoints; i++) {
			int x = xpoints[i] - xc;
			int y = ypoints[i] - yc;
			xpoints[i] = -y + xc;
			ypoints[i] = x + yc;
		}
		return this;
	}
}
