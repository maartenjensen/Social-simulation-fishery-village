package valueFramework;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

import common.FrameworkBuilder;


public class RandomTree  implements Comparable<RandomTree>{
	private WaterTank waterTank;
	private Node root;
	
	
	public void createChildren(int depth, int numOfChildren, Node parent){		
		if (depth == 0){
			parent.randomlyAssignActions(FrameworkBuilder.allPossibleActions);
			FrameworkBuilder.addConcreteValue(parent);
			System.out.println(parent.getValueName() + " as a concrete value adds possible actions " + parent.relatedActions.size());
			return;
		}
		Random rand = new Random();
		for (int i = 0; i < numOfChildren; i++){
			int numofGrandChildren= rand.nextInt(3) + 1;
			//TODO: limitation is 10 children. It can be more or less, just by changing the input arg of nextInt function
			Node newChild = new Node(Integer.toString(FrameworkBuilder.valueNumber), parent);
			parent.addChild(newChild);
			System.out.println("My name " + FrameworkBuilder.valueNumber + "\tparent : " + parent.getValueName() + "\tdepth : " + depth + "\tnumOfChildren : " + numofGrandChildren);
			FrameworkBuilder.valueNumber++;
			createChildren(depth-1, numofGrandChildren, newChild);
		}				
	}
	
	public Node randomTreeBuilder(int maxDepth, int maxNumOfChildren, String title){
		setWaterTank(new WaterTank());
		root = new Node(title, null);
		Random rand = new Random();
	    int depth = rand.nextInt(maxDepth) + 1;
	    int numofChildren = rand.nextInt(maxNumOfChildren) + 1;
	    createChildren(depth, numofChildren, root);
	    return root;
	}

	public WaterTank getWaterTank() {
		return waterTank;
	}

	public void setWaterTank(WaterTank waterTank) {
		this.waterTank = waterTank;
	}
			
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	@Override
	public int compareTo(RandomTree other) {
		return Double.compare(waterTank.getPriorityPercentage(),other.waterTank.getPriorityPercentage());
	}

	public Node randomTreeBuilder(int depth, String rootTitle,
			List<String> childrenNames) {
		setWaterTank(new WaterTank());
		root = new Node(rootTitle, null);
		Random rand = new Random();
	    createChildren(depth, childrenNames.size(), root);
	    return root;
	}

	public Node randomTreeBuilderFromFile(String rootTitle,
			List<String> treeInfo, String waterTankInfo) {
		
		setWaterTank(new WaterTank(waterTankInfo));
		root = new Node(rootTitle, null);
		Node crrNode;
		int depth;
//		List<String> childrenList ;
		int numOfChild;
		String nodeTitle;
		List<String> items;
		System.out.println("\n\n*******************************making tree *************************");
		for(int i = 0; i < treeInfo.size(); i ++){	
			String line = treeInfo.get(i);
			line = line.replaceAll("\\[", "");
			line = line.replaceAll("\\]", "");
			items = Arrays.asList(line.split("\\s*,\\s*"));//ignore while space after comma
			nodeTitle = items.get(0);
			//TODO: check the split sql
			if(i == 0)
				crrNode = root;	
			else{
				crrNode = findInTreeWithTitle(root, nodeTitle);
			}
			if(crrNode == null){
				System.err.println("i cannot find the node " + nodeTitle + " in the tree. Please make sure that u defined it in a children list of some other values before");
				return root;
			}
//			depth = Integer.valueOf(items.get(1));
//			String tmpChildren =  items.get(2);		
			int startingIndexOfChild = 1;
			numOfChild = items.size() - startingIndexOfChild;

			if(numOfChild == 0){
//				crrNode.randomlyAssignActions(FrameworkBuilder.allPossibleActions);
				FrameworkBuilder.addConcreteValue(crrNode);
				System.out.println(crrNode.getValueName() + " as a concrete value added");
			}
			for (int nc = startingIndexOfChild; nc < items.size(); nc++){
				Node newChild = new Node(items.get(nc), crrNode);
				crrNode.addChild(newChild);
				System.out.println("My name " + items.get(nc) + "\tparent : " + crrNode.getValueName() );
				FrameworkBuilder.valueNumber++;				
			}				             
		}
		
	    return root;
	}

	private Node findInTreeWithTitle(Node crrNode, String nodeTitle) {
		List<Node> childList;
		childList = crrNode.getChildren();
		for(Node chl : childList){
			if(chl.getValueName().endsWith(nodeTitle))
				return chl;
			Node retrunedVal = findInTreeWithTitle(chl, nodeTitle);
			if( retrunedVal != null)
				return retrunedVal;
		}	
		return null;
			
	}
	
	/*if (depth == 0){
		parent.randomlyAssignActions(Variables.allPossibleActions);
		Variables.addConcreteValue(parent);
		System.out.println(parent.getValueName() + " as a concrete value adds possible actions " + parent.relatedActions.size());
		return;
	}
	Random rand = new Random();
	for (int i = 0; i < numOfChildren; i++){
		int numofGrandChildren= rand.nextInt(3) + 1;
		//TODO: limitation is 10 children. It can be more or less, just by changing the input arg of nextInt function
		Node newChild = new Node(Integer.toString(Variables.valueNumber), parent);
		parent.addChild(newChild);
		System.out.println("My name " + Variables.valueNumber + "\tparent : " + parent.getValueName() + "\tdepth : " + depth + "\tnumOfChildren : " + numofGrandChildren);
		Variables.valueNumber++;
		createChildren(depth-1, numofGrandChildren, newChild);
	}			*/
	
	
}
