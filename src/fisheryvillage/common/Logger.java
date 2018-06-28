package fisheryvillage.common;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;

/**
* Logs stuff
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public final class Logger {

	// Initialize variables
	private static boolean logErrors = false;
	private static boolean logMain = false;
	private static boolean logAction = false;
	private static boolean logInfo = false;
	private static boolean logDebug = false;
	private static boolean logProb = false;
	
	public static void enableLogger() {
		logErrors = true;
		logMain = true;
		logAction = true;
		logInfo = true;
		logDebug = true;
		logProb = true;
	}
	
	public static void disableLogger() {
		logErrors = true;
		logMain = false;
		logAction = false;
		logInfo = false;
		logDebug = false;
		logProb = false;
	}
	
	public static void setLoggerAll(boolean logErrors, boolean logMain, boolean logAction, boolean logInfo, boolean logDebug, boolean logProb) {
		Logger.logErrors = logErrors;
		Logger.logMain = logMain;
		Logger.logAction = logAction;
		Logger.logInfo = logInfo;
		Logger.logDebug = logDebug;
		Logger.logProb = logProb;
	}
	
	public static void setLogErrors(boolean logErrors) {
		Logger.logErrors = logErrors;
	}
	
	public static void logError(String error) {
		if (logErrors) {
			System.err.println("Error: " + error);
			new Exception().printStackTrace();
			RunEnvironment.getInstance().endRun();
		}
	}
	
	public static void logMain(String output) {
		if (logMain) {
			System.out.println(output);
		}
	}
	
	public static void logAction(String output) {
		if (logAction) {
			System.out.println("---- ACTION: " + output);
		}
	}
	
	public static void logInfo(String output) {
		if (logInfo) {
			System.out.println("----- INFO: " + output);
		}
	}
	
	public static void logDebug(String output) {
		if (logDebug) {
			System.out.println("------ DEBUG: " + output);
		}
	}
	
	public static void logProb(String output, double prob) {
		if (logProb && RandomHelper.nextDouble() < prob) {
			System.out.println("------ PROB" + prob + ": " + output);
		}
	}
}