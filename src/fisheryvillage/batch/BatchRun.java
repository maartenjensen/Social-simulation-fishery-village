package fisheryvillage.batch;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fisheryvillage.common.Logger;
import fisheryvillage.common.RepastParam;
import fisheryvillage.common.SimUtils;

public class BatchRun {

	private static boolean enabled = false;
	private static boolean initialized = false;
	private static int runNumber = 0;
	
	private static int runPerParamCount = 0;
	private static int runsPerParamSetting = 1;
	
	private static List<String> initPopList;
	private static int initPopIndex = 0;
	
	private static ArrayList<BatchParameter> batchParameters;
	private static int parameterIndex = 0;
	
	private static boolean initFromFile = true;
	private static int endTickAt = 1920;
	
	private static int valuesSatisfiedForHappy = 2;
	
	private static List<String> ecosystemEndPopulation;
	private static List<String> migrationEndNumber;

	private static String actionListName = "inputFiles\\actionList.txt";
	private static RunningCondition runningCondition = RunningCondition.NO_EVENTHALL;
	private static String saveFilePath = "D:\\UniversiteitUtrecht\\7MasterThesis\\Repast-filesink\\fisheryvillage\\";
	private static String fileName = "BatchInfoEcosystem.txt";
	private static String fileName2 = "BatchInfoMigration.txt";

	public static boolean setEnable(boolean enable) {

		if (!enable)
			return true; // Initialize trees

		enabled = true;
		if (!initialized) {
			actionListName = "";
			initialized = true;
			runNumber = 0;
			runPerParamCount = 0;
			initPopIndex = 0;
			parameterIndex = 0;
			initDataArrays();
			actionListName = batchParameters.get(parameterIndex).getActionListPath();
			runningCondition = batchParameters.get(parameterIndex).getRunningCondition();
			Logger.logExtreme("------------------------------------------------------------------------------");
			Logger.logExtreme("Global parameters: init from file: " + initFromFile + ", end at tick: " + endTickAt);
			Logger.logExtreme("------------------------------------------------------------------------------");
			Logger.logExtreme("Batch: enable - init run: " + runNumber + ", parameters: " + parametersToString());
			return true;
		}
		else {
			runNumber ++;
			boolean initNewtree = setRunParameters();
			if (initNewtree) {
				Logger.logExtreme("------------------------------------------------------------------------------");
				Logger.logExtreme("Batch: enable - new parameter setting: " + runNumber + ", parameters: " + parametersToString()); 
			}
			else 
				Logger.logExtreme("Batch: enable - run: " + runNumber + ", population: " + initPopList.get(initPopIndex));
			return initNewtree;
		}
	}
	
	public static boolean setRunParameters() {
		
		boolean initNewtree = false;
		runPerParamCount ++;
		if (runPerParamCount == runsPerParamSetting) {
			runPerParamCount = 0;
			initPopIndex ++;
			if (initPopIndex == initPopList.size()) {
				initPopIndex = 0;
				parameterIndex ++;
				
				if (parameterIndex == batchParameters.size()) {
					endOfBatch();
					return false;
				}
				writeToFile(saveFilePath + "tempEco" + runNumber + ".txt", ecosystemEndPopulation);
				writeToFile(saveFilePath + "tempMigr" + runNumber + ".txt", migrationEndNumber);
				initNewtree = true;
				actionListName = batchParameters.get(parameterIndex).getActionListPath();
				runningCondition = batchParameters.get(parameterIndex).getRunningCondition();
			}
		}
		return initNewtree;
	}

	public static String parametersToString() {
		return initPopList.get(initPopIndex) + "," + batchParameters.get(parameterIndex).toString();
	}

	public static boolean getEnable() {
		return enabled;
	}

	public static RunningCondition getRunningCondition() {
		return runningCondition;
	}
	
	public static void setRepastParameters() {
		
		boolean pGenToFile = false;
		String pGenFileName = "Population";
		int pGenTickLimit = -1;
		
		boolean pInitFromFile = initFromFile;
		String pInitFileName = initPopList.get(initPopIndex);
		int pEndAtTick = endTickAt;
		
		int pPower = batchParameters.get(parameterIndex).getPower(1);
		int pSelf = batchParameters.get(parameterIndex).getSelfDir(1);
		int pUni = batchParameters.get(parameterIndex).getUniversalism(1);
		int pTrad = batchParameters.get(parameterIndex).getTradition(1);
		
		int pPower2 = batchParameters.get(parameterIndex).getPower(2);
		int pSelf2 = batchParameters.get(parameterIndex).getSelfDir(2);
		int pUni2 = batchParameters.get(parameterIndex).getUniversalism(2);
		int pTrad2 = batchParameters.get(parameterIndex).getTradition(2);
		
		RepastParam.setRepastParameters(pGenToFile, pGenFileName, pGenTickLimit, pInitFromFile, pInitFileName, pEndAtTick,
										pPower, pSelf, pUni, pTrad,
										pPower2, pSelf2, pUni2, pTrad2);
	}
	
	public static String getActionlistPath() {
		return actionListName;
	}
	
	public static int getRunNumber() {
		return runNumber;
	}
	
	public static void saveRunData() {
		
		String datum = initPopList.get(initPopIndex) + "," + batchParameters.get(parameterIndex).toString()  + "," + SimUtils.getEcosystem().getFish();
		ecosystemEndPopulation.add(datum);

		String datum2 = initPopList.get(initPopIndex) + "," + batchParameters.get(parameterIndex).toString()  + ","
						+ SimUtils.getDataCollector().getMigratedOutSelf() + "," + SimUtils.getDataCollector().getMigratedOutWith() + "," + SimUtils.getDataCollector().getMigratedOutChildren();
		migrationEndNumber.add(datum2);
	}

	public static void endOfBatch() {
		Logger.logExtreme("End batch run: number of runs: " + runNumber);
		saveBatchData();
		Logger.logExtreme("Exiting program...");
		System.exit(0);
	}

	public static void saveBatchData() {
		Logger.logExtreme("Saving data...");
		writeToFile(saveFilePath + fileName, ecosystemEndPopulation);
		writeToFile(saveFilePath + fileName2, migrationEndNumber);
		Logger.logExtreme("Saving complete!");
	}
	
	public static int getValuesSatisfiedForHappy() {
		return valuesSatisfiedForHappy;
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
	
	public static void initDataArrays() {
		
		initPopList = new ArrayList<String>();
		initPopList.add("pop1");
		initPopList.add("pop2"); 
		initPopList.add("pop3");
		initPopList.add("pop4"); 
		//initPopList.add("pop5");
		
		batchParameters = getHypo1BasicPower();
		
		ecosystemEndPopulation = new ArrayList<String>();
		ecosystemEndPopulation.add("Init pop,Tree,Condition,p1,t1,u1,s1,p2,t2,u2,s2,Fish end");
		
		migrationEndNumber = new ArrayList<String>();
		migrationEndNumber.add("Init pop,Tree,Condition,p1,t1,u1,s1,p2,t2,u2,s2,MigrateSelf,MigrateWith,MigrateChildren");
	}
	
	private static ArrayList<BatchParameter> getHypo1BasicPower() {
		
		saveFilePath = "D:\\UniversiteitUtrecht\\7MasterThesis\\Repast-filesink\\fisheryvillage\\";
		ArrayList<BatchParameter> tempParameters = new ArrayList<BatchParameter>();
		/*
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 45, 45, 45, 60, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 45, 45, 45, 70, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 45, 45, 45, 80, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 45, 45, 45, 90, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 45, 45, 45, 100, 45, 45, 45));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 60, 45, 45, 45, 70, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 60, 45, 45, 45, 80, 45, 45, 45));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 60, 45, 45, 45, 90, 45, 45, 45));*/

		for (int i = 100; i <= 100; i += 10) {
			tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", RunningCondition.NONE, i, 45, 45, 45));
			tempParameters.add(new BatchParameter("inputFiles\\actionListPowerEvent.txt", 	RunningCondition.NONE, i, 45, 45, 45));
			tempParameters.add(new BatchParameter("inputFiles\\actionListPowerFish.txt", 	RunningCondition.NONE, i, 45, 45, 45));	
		}
		
		return tempParameters;
	}
	
	private static ArrayList<BatchParameter> getHypo2Basic() {
		
		saveFilePath = "D:\\UniversiteitUtrecht\\7MasterThesis\\Repast-filesink\\fisheryvillage\\";
		ArrayList<BatchParameter> tempParameters = new ArrayList<BatchParameter>();
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NONE, 50, 50, 50, 70));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_SCHOOL, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_SCHOOL, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_SCHOOL, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_SCHOOL, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_SCHOOL, 50, 50, 50, 70));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EVENTHALL, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EVENTHALL, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EVENTHALL, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EVENTHALL, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EVENTHALL, 50, 50, 50, 70));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_FACTORY, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_FACTORY, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_FACTORY, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_FACTORY, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_FACTORY, 50, 50, 50, 70));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_DONATION, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_DONATION, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_DONATION, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_DONATION, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_DONATION, 50, 50, 50, 70));
		
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EV_AND_DON, 50, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EV_AND_DON, 70, 50, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EV_AND_DON, 50, 70, 50, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EV_AND_DON, 50, 50, 70, 50));
		tempParameters.add(new BatchParameter("inputFiles\\actionList.txt", 			RunningCondition.NO_EV_AND_DON, 50, 50, 50, 70));
		return tempParameters;
	}
}