package ai.nets.samj.assessment.tools;

import ai.nets.samj.ui.SAMJLogger;
import ij.IJ;

public class IJLogger implements SAMJLogger{

	@Override
	public void info(String text) {
		IJ.log(text);
	}
	
	@Override
	public void warn(String text) {
		IJ.log(text);
	}
	
	@Override
	public void error(String text) {
		IJ.log(text);
	}
}
