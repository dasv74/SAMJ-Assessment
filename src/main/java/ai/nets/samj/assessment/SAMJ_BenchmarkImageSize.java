package ai.nets.samj.assessment;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.compress.archivers.ArchiveException;

import ai.nets.samj.assessment.simulation.ImageTest;
import ai.nets.samj.assessment.simulation.Regions;
import ai.nets.samj.assessment.simulation.RegionsFactory;
import ai.nets.samj.assessment.tools.Encoding;
import ai.nets.samj.assessment.tools.Experiment;
import ai.nets.samj.communication.model.SAMModel;
import ai.nets.samj.communication.model.SAMModels;
import ij.ImageJ;
import ij.plugin.PlugIn;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;

/**
 * Assessment of SAMJ. Test the impact of the image size.
 * 
 * @author dsage
 *
 */

public class SAMJ_BenchmarkImageSize implements PlugIn {
	
	private String nameExperiment = "ImageSize";
	private int nIterations = 1;
	private int nModels = 1; // Number of models 1 ... 6
	private int outerRectPrompt = 10; // Number of pixel around the rectangular prompt
	private final boolean display;
	
	private SAMJ_BenchmarkImageSize(boolean display) {
		this.display = display;
	}
	
	@Override
	public void run(String arg) {
		ArrayList<Dimension> dims = new ArrayList<Dimension>();
		for(int i=1; i<=20; i++) {
			dims.add(new Dimension(200*i, 200*i));
		}
		ArrayList<int[]> params = new ArrayList<int[]>();
		// 1 run params: [iteration, numModel, nx, ny, levelNoise]
		for (int levelNoise = 128; levelNoise<=128;  levelNoise += 16)  // level of noise vs. 256
			for(int iter = 0; iter<nIterations; iter++)
				for(int numModel = 0; numModel<nModels; numModel++)
					for(int i=0; i<dims.size(); i++) 
						params.add(new int[] {iter, numModel, dims.get(i).width, dims.get(i).height, levelNoise});
		Collections.shuffle(params);

		Experiment experiment = new Experiment(nameExperiment, Encoding.Mode.WHOLE);
		SAMModels modelsList;
		try {
			modelsList = new SAMModels();
		} catch (IOException | RuntimeException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		for(int[] param : params) {
			int nx = param[2];
			int ny = param[3];
			int levelNoise = param[4];
			Regions regions = RegionsFactory.createChevron(nx, ny);
			ImageTest image = new ImageTest("test", nx, ny, regions, 255-levelNoise);
			image.addUniformNoise(levelNoise, 1);
			if (this.display)
				image.test.show();
			SAMModel model;
			try {
				model = modelsList.get(param[1]);
				if (!model.getInstallationManger().checkEverythingInstalled())
					model.getInstallationManger().installEverything();
				experiment.run(image, regions, param[0], model, outerRectPrompt, levelNoise);
			} catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException | MambaInstallException e) {
				e.printStackTrace();
			}
			image.test.close();
			image.gt.close();
			break;
		}
		experiment.save();
	}

	public static void main(String[] args) {
		new ImageJ();
		boolean display = true;
		SAMJ_BenchmarkImageSize bench = new SAMJ_BenchmarkImageSize(display);
		bench.run("");
	}
}
