package valueframework.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import valueframework.*;

public final class FrameworkBuilder {
	
	private static ArrayList<Action> allActions = new ArrayList<Action>();
	private static ArrayList<Node> allConcreteValuesFromTrees = new ArrayList<Node>();
	private static ArrayList<String> allConcreteValuesNames = new ArrayList<String>();
	private static Map<String, RandomTree> globalValueTrees = new HashMap<String, RandomTree>();
	
	private static int valueNumber = 0;
	
	/**
	 * Private constructor since constructor should not be used
	 * in the static class
	 * @param numOfActions
	 */
	private FrameworkBuilder() {

	}
	
	public static int getNextValueNumber() {
		int newNumber = valueNumber;
		valueNumber ++;
		return newNumber;
	}
	
	/**
	 * Initialize the framework builder, always call this function
	 * first before using the value framework.
	 */
	public static void initialize() {
		
		Log.printLog("Initialize FrameworkBuilder");
		valueNumber = 0;
		
		allActions = new ArrayList<Action>();
		allConcreteValuesFromTrees = new ArrayList<Node>();
		allConcreteValuesNames = new ArrayList<String>();
		globalValueTrees = new HashMap<String, RandomTree>();
		
		try {
			//first create value files
			readValueTreeFile("inputFiles\\valueTree.txt");
			
			//then read actions from file
			readActionsFile("inputFiles\\actionList.txt");
			assignRelatedActionsToConcreteValues();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addConcreteValue(Node nd){
		
		if(allConcreteValuesFromTrees != null && !allConcreteValuesFromTrees.contains(nd)) {
			Log.printLog("FrameworkBuilder.addConcreteValue: " + nd.getValueName());
			allConcreteValuesFromTrees.add(nd);
		}
	}
	
	private static void readValueTreeFile(String filePath) throws IOException {
		
		Log.printLog("readValueTreeFile");
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(filePath));
		String line = reader.readLine();

		while (line != null) {
			
			if(line.startsWith("%")){//means that it is not comment
				line = reader.readLine();
				continue;
			}
			if (line.contains("value tree")){
				//This is water tank information						
				String waterTankInfo = reader.readLine();
				//it's value trees from now on
				line = reader.readLine();
				List<String> treeInfo = new ArrayList<String>();
				while (line != null && !line.contains("value tree") ){
					treeInfo.add(line);
					line = reader.readLine();
				}
				Log.printStars();
				RandomTree tree = createGlobalValueTrees(treeInfo, waterTankInfo);						
				globalValueTrees.put(tree.getRoot().getValueName(), tree);	
				Log.printLog(tree.getPrintableTree());
			}
			else {
				line = reader.readLine();
			}
		} 
		reader.close();
	}
	
	private static void readActionsFile(String filePath){
			
		Log.printStars();
		Log.printLog("readActionsFile");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
				if(!line.startsWith("%")){//means that it is not comment
					List<String> items = Arrays.asList(line.split("\\s*,\\s*"));//ignore while space after comma
					
					if (items.size() >= 1) {
						Action act = addActionFromString(items);
						ArrayList<Node> relatedCncrtValue = addConcreteValuesFromString(items.subList(1, items.size()));
						act.assignConcreteValues(relatedCncrtValue);
					}
					else {
						Log.printError("No elements in items or doesn't contain a concrete value:" + items);
					}
				}	
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void assignRelatedActionsToConcreteValues() {
		
		Log.printStars();
		Log.printLog("assignRelatedActionsToConcreteValues");
		for(Node nd : allConcreteValuesFromTrees){
			for(Action act : allActions){
				ArrayList<Node> ndList = act.getRelatedConcreteValues();
				if(ndList.contains(nd))
					nd.relatedActions.add(act);				
			}
		}
	}	

	private static ArrayList<Node> addConcreteValuesFromString(List<String> concreteValues) {
		
		ArrayList<Node> concreteValueNodes = new ArrayList<Node>();
		Node cncrtValueNode;
		for(String concreteValue : concreteValues){
			if(!allConcreteValuesNames.contains(concreteValue)){
				allConcreteValuesNames.add(concreteValue);
			}	
			cncrtValueNode = findInstanceOfValueWithName(concreteValue);
			if(cncrtValueNode == null)
				Log.printError("addConcreteValuesFromString(" + concreteValues + "):" + concreteValue +"\" is not in the list of concreteValues made from valueTree file" );
			else
				concreteValueNodes.add(cncrtValueNode);
		}
		return concreteValueNodes;
	}
	
	private static Node findInstanceOfValueWithName(String concreteValue) {
		for(Node nd : allConcreteValuesFromTrees){
			if(nd.getValueName().equals(concreteValue))
				return nd;
		}
		return null;
	}

	private static Action addActionFromString(List<String> actionAndConcreteValues) {
		
		String actionName = actionAndConcreteValues.get(0);
		ArrayList<String> concreteValues = new ArrayList<String>();
		for (int i = 1; i < actionAndConcreteValues.size(); i ++) {
			concreteValues.add(actionAndConcreteValues.get(i));
		}
		System.out.println("Add action: " + actionName + ", cvs: " + concreteValues);
		Action act = new Action(actionName, concreteValues);
		allActions.add(act);
		return act;
	}
	
	private static RandomTree createGlobalValueTrees(List<String> treeInfo, String waterTankInfo) {
		
		List<String> items = Arrays.asList(treeInfo.get(0).split("\\s*,\\s*"));		
		RandomTree tree = new RandomTree(items.get(0), treeInfo, waterTankInfo);
		globalValueTrees.put(tree.getRoot().getValueName(), tree);
		return tree;
	}

	public static ArrayList<Action> getAllPossibleActions() {
		return allActions;
	}

	public static Map<String, RandomTree> getGlobalValueTrees() {
		return globalValueTrees;
	}

	public static ArrayList<Node> getAllConcreteValues() {
		return allConcreteValuesFromTrees;
	}	
}