package ai.nets.samj.assessment.simulation;

import java.awt.Rectangle;

public class Metric {

	public double IoU;	
	public int TP;
	public int FP;
	public int TN;
	public int CardinalA;
	public int CardinalB;
	public int intersection = 0;
	public int union = 0;
	public Metric(Region A, Region B) {

		Rectangle r1 = A.getBounds();
		Rectangle r2 = B.getBounds();
		int x1 = Math.min(r1.x, r2.x);
		int y1 = Math.min(r1.y, r2.y);
		int x2 = Math.max(r1.x+r1.width, r2.x+r2.width);
		int y2 = Math.max(r1.y+r1.height, r2.y+r2.height);

		for (int i = x1-1; i <= x2+1; i++) {
			for (int j = y1-1; j <= y2+1; j++) {
				boolean a = A.contains(i, j);
				boolean b = B.contains(i, j);
				if (a) CardinalA++;
				if (b) CardinalB++;
				if (a && !b) TN++;
				if (!a && b) FP++;
				if (a && b) intersection++;
				if (a || b) union++;
			}
		}
		IoU = (double) intersection / (double) union;
		TP = intersection;
	}

}
