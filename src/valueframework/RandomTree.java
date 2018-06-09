package valueframework;

import java.util.Arrays;
import java.util.List;

import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class RandomTree {

	private Node root;
	private WaterTank waterTank;
	
	public RandomTree(String rootTitle,	List<String> treeInfo, String waterTankInfo) {
		
		Log.printLog("Build value tree: " + rootTitle);
		waterTank = new WaterTank(waterTankInfo, rootTitle);
		randomTreeBuilderFromFile(rootTitle, treeInfo);
	}
	
	public RandomTree(Node rt, WaterTank wt){
		root = rt;
		waterTank = wt;
	}
	
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public WaterTank getWaterTank() {
		return waterTank;
	}
	
	public void setWaterTank(WaterTank wt) {
		this.waterTank = wt;
	}
	
	public Node randomTreeBuilderFromFile(String rootTitle,	List<String> treeInfo) {
		
		root = new Node(rootTitle, null);
		Node crrNode;
		int numOfChild;
		String nodeTitle;
		List<String> items;
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
				Log.printError("randomTreeBuilderFromFile: cannot find " + nodeTitle + " in the tree. Please make sure that u defined it in a children list of some other values before");
				return root;
			}
	
			int startingIndexOfChild = 1;
			numOfChild = items.size() - startingIndexOfChild;

			if(numOfChild == 0){
				FrameworkBuilder.addConcreteValue(crrNode);
				Log.printLog("Added concrete value: " + crrNode.getValueName());
			}
			for (int nc = startingIndexOfChild; nc < items.size(); nc++){
				Node newChild = new Node(items.get(nc), crrNode);
				crrNode.addChild(newChild);
				Log.printLog("New node: " + items.get(nc) + ", parent: " + crrNode.getValueName() );
				FrameworkBuilder.getNextValueNumber();			
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
	
	public String getPrintableTree() {
		return getPrintableNode("", root, 0);
	}

	private String getPrintableNode(String print, Node node, int depth) {
		String printableTree = print;
		for (int i = 0; i < depth - 1; i ++) {
			printableTree += "     ";
		}
		if (depth > 0) {
			printableTree += "=====";
		}
		printableTree += node.getValueName() + "\n";
		
		//Print positive and negative influencing actions
		String stringActionStart = "";
		for (int i = 0; i < depth; i ++) {
			stringActionStart += "     ";
		}
		if (depth > 0) {
			stringActionStart += "--- ";
		}
		printableTree += node.toTreeStringActions(stringActionStart);
		
		for (Node child : node.getChildren()) {
			printableTree += getPrintableNode("", child, depth + 1);
		}

		return printableTree;
	}
}
