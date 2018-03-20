package fisheryvillage.common;

/**
* Logs stuff
*
* @author Maarten Jensen
*/
public final class Logger {

	// Initialize variables
	private static boolean logErrors = false;
	private static boolean logMain = false;
	private static boolean logAction = false;
	private static boolean logInfo = false;
	private static boolean logDebug = false;
	
	public static void enableLogger() {
		logErrors = true;
		logMain = true;
		logAction = true;
		logInfo = true;
		logDebug = true;
	}
	
	public static void setLogErrors(boolean logErrors) {
		Logger.logErrors = logErrors;
	}
	
	public static void logError(String error) {
		if (logErrors) {
			System.err.println(error);
		}
	}
	
	public static void logMain(String output) {
		if (logMain) {
			System.out.println(output);
		}
	}
	
	public static void logAction(String output) {
		if (logAction) {
			System.out.println("ACTION: " + output);
		}
	}
	
	public static void logInfo(String output) {
		if (logInfo) {
			System.out.println("INFO: " + output);
		}
	}
	
	public static void logDebug(String output) {
		if (logDebug) {
			System.out.println("DEBUG: " + output);
		}
	}
}