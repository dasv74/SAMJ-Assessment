package ai.nets.samj.assessment.tools;

public class Chrono {

	static private double	chrono	= 0;

	/**
	 * Register the current time.
	 */
	public static void start() {
		chrono = System.nanoTime();
	}

	/**
	 */
	public static double stop() {
		return System.nanoTime() - chrono;
	}

	public static double nanoseconds() {
		return (System.nanoTime() - chrono);
	}
	
	public static double microseconds() {
		return (System.nanoTime() - chrono) * 0.001;
	}
	
	public static double millseconds() {
		return (System.nanoTime() - chrono) * 0.000001;
	}
	
	public static double seconds() {
		return (System.nanoTime() - chrono) * 0.000000001;
	}
}