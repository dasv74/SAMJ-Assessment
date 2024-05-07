package ai.nets.samj.assessment.simulation;

import java.awt.Polygon;
import java.util.Collections;

public class RegionsFactory {

	static public Regions createTenBlocks(int nx, int ny) {
		int qx = nx / 20;
		int qy = ny / 20;
		Regions regions = new Regions();
		regions.add(new Region(10*qx, 2*qy, 18*qx, 2*qy, 0));
		regions.add(new Region(9*qx, 14*qy, 4*qx, 4*qy, 45));
		regions.add(new Region(3*qx, 9*qy, 3*qx, 8*qy, 0));
		regions.add(new Region(8*qx, 7*qy, 4*qx, 5*qy, 0));
		regions.add(new Region(14*qx, 7*qy, 3*qx, 3*qy, 45));
		regions.add(new Region(3*qx, 17*qy, 4*qx, 3*qy, 0));
		regions.add(new Region(17*qx, 12*qy, 1*qx, 3*qy, 45));
		regions.add(new Region(17*qx, 17*qy, 2*qx, 2*qy, 60));	
		regions.add(new Region(13*qx, 18*qy, 2*qx, 2*qy, 0));
		regions.add(new Region(14*qx, 11*qy, qx, qy, 30));
		Collections.shuffle(regions);
		return regions;
	}
	
	static public Regions createChevronRow(int nx, int ny, int nchevrons) {
		Regions regions = new Regions();

		for(int i=200; i<nx-200; i+=200)
			regions.add(chevron(i, 100, 30, 60));

		for(int i=100; i<nx-100; i+=100)
			regions.add(chevron(i, 300, 9, 18));

		return regions;
	}
	
	static public Regions createChevron(int nx, int ny) {
		Regions regions = new Regions();
		int w = 12;
		int h = 20;
		regions.add(chevron(20, 20, w, h));
		regions.add(chevron(20, ny-70, w, h).rotate());
		regions.add(chevron(nx-70, ny-70, w, h).rotate().rotate());
		regions.add(chevron(nx-70, 20, w, h).rotate().rotate().rotate());
		return regions;
	}
	
	static private Region chevron(int px, int py, int w, int h) {
		Polygon polygon = new Polygon();
		polygon.addPoint(px, py); px += w; 
		polygon.addPoint(px, py); py += h;
		polygon.addPoint(px, py); px += w; py += (h-w/2);
		polygon.addPoint(px, py); px += w; py -= (h-w/2);
		polygon.addPoint(px, py); 		   py -= h;
		polygon.addPoint(px, py); px += w;
		polygon.addPoint(px, py);            py += h + w;
		polygon.addPoint(px, py); px -= 2*w; py += h;
		polygon.addPoint(px, py); px -= 2*w; py -= h;
		polygon.addPoint(px, py); py += h+w;
		return new Region(polygon);
	}
		
}
