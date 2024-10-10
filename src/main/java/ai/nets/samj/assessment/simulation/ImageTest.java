package ai.nets.samj.assessment.simulation;

import java.util.Random;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.GaussianBlur3D;
import ij.process.Blitter;
import ij.process.ImageProcessor;

public class ImageTest {

	private int nx = 512;
	private int ny = 512;
	public ImagePlus gt;	// Ground-truth
	public ImagePlus test;	// Test image
	
	public ImageTest(String title, int nx, int ny, Regions regions, double value) {
		this.nx = nx;
		this.ny = ny;
		gt = create(regions, (float)value);
		gt.setTitle("GT");
		test = gt.duplicate();
		test.setTitle(title);
	}
	
	public int area() {
		return gt.getWidth() * gt.getHeight();
	}
	
	private ImagePlus create(Regions regions, float value) {
		ImagePlus imp = IJ.createImage("simulation", nx, ny, 1, 8);
		ImageProcessor ip = imp.getProcessor();
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				for (Region region : regions)
					if (region.contains(i, j))
						ip.putPixelValue(i, j, value);
			}
		}
		return imp;
	}

	public void addUniformNoise(double noise, double scale) {
		Random rand = new Random(1234);
		ImagePlus noisy = IJ.createImage("Image-" + nx + "x" + ny, nx, ny, 1, 8);
		ImageProcessor ip = noisy.getProcessor();
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < ny; j++)
				ip.putPixelValue(i, j, rand.nextDouble() * noise);

		if (scale > 0)
			GaussianBlur3D.blur(noisy, scale, scale, 0);
		test.getProcessor().copyBits(ip, 0, 0, Blitter.ADD);
	}
	
	public void save(String path, String modelName) {
		IJ.save(gt, path + "gt-" + modelName + "-" + area() + ".png");
		IJ.run("Flatten");
		IJ.save(test, path + "results-" + modelName + "-" + area() + ".png");
		IJ.runMacro("close();");
	}
	
}
