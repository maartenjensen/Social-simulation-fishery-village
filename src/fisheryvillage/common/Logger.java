package fisheryvillage.common;

public final class Logger {

	// Initialize variables
	private static boolean logErrors = false;
	private static boolean logOutput = false;
	
	public static void enableLogger() {
		logErrors = true;
		logOutput = true;
	}
	
	public static void setLogErrors(boolean logErrors) {
		Logger.logErrors = logErrors;
	}
	
	public static void logErrorLn(String error) {
		if (logErrors) {
			System.err.println(error);
		}
	}
	
	public static void setLogOutput(boolean logOutput) {
		Logger.logOutput = logOutput;
	}
	
	public static void logOutputLn(String output) {
		if (logOutput) {
			System.out.println(output);
		}
	}
}