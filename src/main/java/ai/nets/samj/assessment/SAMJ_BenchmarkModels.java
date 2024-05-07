package ai.nets.samj.assessment;

import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Collections;

import ai.nets.samj.assessment.simulation.ImageTest;
import ai.nets.samj.assessment.simulation.Regions;
import ai.nets.samj.assessment.simulation.RegionsFactory;
import ai.nets.samj.assessment.tools.Encoding;
import ai.nets.samj.assessment.tools.Experiment;
import ai.nets.samj.communication.model.SAMModel;
import ai.nets.samj.communication.model.SAMModels;
import ij.ImageJ;
import ij.plugin.PlugIn;

/**
 * Assessment of SAMJ. Test the effect of the models.
 * 
 * @author dsage
 *
 */

public class SAMJ_BenchmarkModels implements PlugIn {
	
	private String nameExperiment = "Variability-Models";
	private int nIterations = 1;
	private int nModels = 1; // Number of models 1 ... 6
	private int outerRectPrompt = 10; // Number of pixel around the rectangular prompt
	
	@Override
	public void run(String arg) {
		ArrayList<Dimension> dims = new ArrayList<Dimension>();
		//dims.add(new Dimension(200, 200));
		dims.add(new Dimension(400, 400));
		ArrayList<int[]> params = new ArrayList<int[]>();
		// 1 run params: [iteration, numModel, nx, ny, levelNoise]
		for (int levelNoise = 128; levelNoise<=128;  levelNoise += 16)  // level of noise vs. 256
			for(int iter = 0; iter<nIterations; iter++)
				for(int numModel = 0; numModel<nModels; numModel++)
					for(int i=0; i<dims.size(); i++) 
						params.add(new int[] {iter, numModel, dims.get(i).width, dims.get(i).height, levelNoise});
		Collections.shuffle(params);

		Experiment experiment = new Experiment(nameExperiment, Encoding.Mode.WHOLE);
		for(int[] param : params) {
			int nx = param[2];
			int ny = param[3];
			int levelNoise = param[4];
			Regions regions = RegionsFactory.createTenBlocks(nx, ny);
			ImageTest image = new ImageTest("test", nx, ny, regions, 255-levelNoise);
			image.addUniformNoise(levelNoise, 1);
			image.test.show();
			SAMModel model = new SAMModels().get(param[1]);
			experiment.run(image, regions, param[0], model, outerRectPrompt, levelNoise);
			image.test.close();
			image.gt.close();
		}
		experiment.save();
	}

	public static void main(String[] args) {
		new ImageJ();
		new SAMJ_BenchmarkModels().run("");
	}
}
