package common;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mas.DecisionMaker;
import valueFramework.Action;
import valueFramework.Node;
import valueFramework.RandomTree;

public class FrameworkBuilder {
	public static ArrayList<Action> allPossibleActions= new ArrayList<Action>();
	public static ArrayList<Node> allConcreteValues = new ArrayList<Node>();
	public static ArrayList<String> concreteValuesName = new ArrayList<String>();
	public static ArrayList<DecisionMaker> decisionMakerList = new ArrayList<DecisionMaker>();
	public static Map<String, RandomTree> allValueTrees = new HashMap<String, RandomTree>();
	public static Map<String, RandomTree> globalValueTrees = new HashMap<String, RandomTree>();
	
	public static final int numOfAbstractValues = 4;
	public static int valueNumber = 0;
	public static int humanId = 0;
	//TODO: when we want to use the tree as an actual argumentation, the name should be meaningful. So, childnumber won't be necessary any more.
			
	public static void createRandomAction(int numOfActions){
		for (int i = 0; i < numOfActions; i++) {
			Action act = new Action();
			allPossibleActions.add(act);			
		}
	}
	
	public static void readActionsFromFile(String filePath){
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
						System.err.println("No elements in items or doesn't contain a concrete value:" + items);
					}
				}	
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
public static ArrayList<Node> addConcreteValuesFromString(List<String> concreteValues) {
	ArrayList<Node> concreteValueNodes = new ArrayList<Node>();
	Node cncrtValueNode;
		for(String concreteValue : concreteValues){
			if(!concreteValuesName.contains(concreteValue)){
				System.out.println("Add concrete value: " + concreteValue);
				concreteValuesName.add(concreteValue);
				
			}	
			cncrtValueNode = findInstanceOfValueWithName(concreteValue);
			if(cncrtValueNode == null)
				System.err.println("\"" + concreteValue +"\" is not in the list of concreteValues made from valueTree file" );
			else
				concreteValueNodes.add(cncrtValueNode);
		}
		return concreteValueNodes;
	}
	

private static Node findInstanceOfValueWithName(String concreteValue) {
	for(Node nd : allConcreteValues){
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
		allPossibleActions.add(act);
		return act;
	}

	public static void addConcreteValue(Node nd){
		System.out.println("node : " + nd);
		System.out.println("list of concretevalues " + allConcreteValues);
		if(allConcreteValues != null && !allConcreteValues.contains(nd))
			allConcreteValues.add(nd);
	}

	public static void readValueTreeFile() throws IOException {
		BufferedReader reader;
			reader = new BufferedReader(new FileReader(
					"inputFiles\\valueTree example.txt"));
			String line = reader.readLine();
//			HumanAgent hmn;
			while (line != null) {
				if(line.startsWith("%")){//means that it is not comment
					line = reader.readLine();
					continue;
				}
						//this line contains info about a human
//						int tmpid = Integer.valueOf(Arrays.asList(line.split(" = ")).get(1));
//						hmn = new HumanAgent();
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
//						RandomTree tree = humnaAgentList.get(0).createValueTrees(treeInfo);
						RandomTree tree = createGlobalValueTrees(treeInfo, waterTankInfo);						
						allValueTrees.put(tree.getRoot().getValueName(), tree);	
		//							line = reader.readLine();
							
					}	
//				}	
//				line = reader.readLine();
			
			
		} 
			reader.close();
	}

	public static void assginRelatedActionsToConcreteValues() {
		for(Node nd : allConcreteValues){
			for(Action act : allPossibleActions){
				ArrayList<Node> ndList = act.getRelatedConcreteValues();
				if(ndList.contains(nd))
					nd.relatedActions.add(act);				
			}
		}
	}
	
	public static RandomTree createGlobalValueTrees(List<String> treeInfo, String waterTankInfo) {
		List<String> items = Arrays.asList(treeInfo.get(0).split("\\s*,\\s*"));//ignore while space after comma
		//TODO: check the split sql
		
		
		RandomTree tree = new RandomTree();
		Node root = tree.randomTreeBuilderFromFile(items.get(0), treeInfo, waterTankInfo);
		System.out.println("****** this is root " + root.getValueName());
		globalValueTrees.put(root.getValueName(), tree);
		return tree;
	}

	
}
