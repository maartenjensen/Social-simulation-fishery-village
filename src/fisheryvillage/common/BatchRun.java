package fisheryvillage.common;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BatchRun {

	private static boolean enabled = false;
	private static boolean initialized = false;
	private static int runNumber = 0;
	
	private static int runPerParamCount = 0;
	private static int runsPerParamSetting = 10;
	
	private static List<String> initPopList;
	private static int initPopIndex = 0;
	
	private static List<Integer> powerParameter;
	private static int powerIndex = 0;
	
	private static boolean initFromFile = true;
	private static int endTickAt = 1920;
	
	private static List<String> ecosystemEndPopulation;

	private static String actionListName = "inputFiles\\actionList.txt";
	
	public static boolean setEnable(boolean enable) {
		
		if (!enable)
			return true;

		enabled = true;
		if (!initialized) {
			actionListName = "inputFiles\\actionList.txt";
			initialized = true;
			runNumber = 0;
			runPerParamCount = -1;
			initPopIndex = 0;
			powerIndex = 0;
			initDataArrays();
			setRunParameters();
			Logger.logExtreme("------------------------------------------------------------------------------");
			Logger.logExtreme("Global parameters: init from file: " + initFromFile + ", end at tick: " + endTickAt);
			Logger.logExtreme("------------------------------------------------------------------------------");
			Logger.logExtreme("Batch: enable - init run: " + runNumber + ", parameters: " + parametersToString());
			return true;
		}
		else {
			runNumber ++;
			setRunParameters();
			Logger.logExtreme("Batch: enable - new run: " + runNumber + ", parameters: " + parametersToString());
			return false;
		}
	}
	
	public static void setRunParameters() {
		
		runPerParamCount ++;
		if (runPerParamCount == runsPerParamSetting) {
			runPerParamCount = 0;
			initPopIndex ++;
			if (initPopIndex == initPopList.size()) {
				initPopIndex = 0;
				powerIndex ++;
				writeToFile("D:\\UniversiteitUtrecht\\7MasterThesis\\Repast-filesink\\fisheryvillage\\BatchInfoEcosystemStandardTemp.txt", ecosystemEndPopulation);
				if (powerIndex == powerParameter.size()) {
					endOfBatch();
				}
			}
		}
	}

	public static String parametersToString() {
		return "init:" + initPopList.get(initPopIndex) + ", power: " + powerParameter.get(powerIndex);
	}
	
	public static void initDataArrays() {
		
		initPopList = new ArrayList<String>();
		initPopList.add("pop1");
		initPopList.add("pop2"); 
		initPopList.add("pop3");
		initPopList.add("pop4"); 
		initPopList.add("pop5");
		initPopIndex = 0;
		
		powerParameter = new ArrayList<Integer>();
		powerParameter.add(50);
		powerParameter.add(60);
		powerParameter.add(70);
		powerParameter.add(80);
		powerParameter.add(90);
		powerParameter.add(100);
		powerIndex = 0;
		
		ecosystemEndPopulation = new ArrayList<String>();
		ecosystemEndPopulation.add("Init pop,Power,Fish end");
	}

	public static boolean getEnable() {
		return enabled;
	}

	public static void setRepastParameters() {
		
		boolean pGenToFile = false;
		String pGenFileName = "Population";
		int pGenTickLimit = -1;
		
		boolean pInitFromFile = initFromFile;
		String pInitFileName = initPopList.get(initPopIndex);
		int pEndAtTick = endTickAt;
		
		int pPower = powerParameter.get(powerIndex);
		int pSelf = 45;
		int pUni = 45;
		int pTrad = 45;
		
		RepastParam.setRepastParameters(pGenToFile, pGenFileName, pGenTickLimit, pInitFromFile, pInitFileName, pEndAtTick, pPower, pSelf, pUni, pTrad);
	}
	
	public static String getActionlistPath() {
		return actionListName;
	}
	
	public static int getRunNumber() {
		return runNumber;
	}
	
	public static void saveRunData() {
		String datum =  initPopList.get(initPopIndex) + "," + powerParameter.get(powerIndex) + "," + SimUtils.getEcosystem().getFish();
		ecosystemEndPopulation.add(datum);
	}

	public static void endOfBatch() {
		Logger.logExtreme("End batch run: number of runs: " + runNumber);
		saveBatchData();
	}

	public static void saveBatchData() {
		Logger.logExtreme("Saving data...");
		writeToFile("D:\\UniversiteitUtrecht\\7MasterThesis\\Repast-filesink\\fisheryvillage\\BatchInfoEcosystemStandard.txt", ecosystemEndPopulation);
		Logger.logExtreme("Saving complete!");
		Logger.logExtreme("Exiting program...");
		System.exit(0);
	}
	
	public static void writeToFile(String filePathAndName, List<String> data) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePathAndName, "UTF-8");
			for (String datum : data) {
				writer.println(datum);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}