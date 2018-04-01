package valueFramework;


import java.nio.ReadOnlyBufferException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Node
{
    private List<Node> children = null;
    private Node parent;
    private String valueName;

    public ArrayList<Action> relatedActions;
    //this list has only value when the node is a leaf in a value tree; otherwise, it is null.
    
	
    /*public Node(String value)
    {
        this.children = new ArrayList<>();
        this.setValueName(value);
    }*/
    
    public void randomlyAssignActions(ArrayList<Action> alist){
    	Collections.shuffle(alist);
    	int randomNum;
		if(alist.size()!=0){
    		randomNum = ThreadLocalRandom.current().nextInt(0, alist.size());			
    	relatedActions = new ArrayList<Action>(alist.subList(0, randomNum));
		}
		else{
			relatedActions = null;
			System.err.println("in randomlyAassingActions, input list is null");
		}
    	//TODO: check if it works fine
    }
    
    public Node(String value, Node parentIn)
    {
        this.children = new ArrayList<>();
        this.setValueName(value);
        this.setParent(parentIn);
        
        relatedActions = new ArrayList<Action>();
    }

    public void addChild(Node child)
    {
        children.add(child);
    }

	public String getValueName() {
		return valueName;
	}

	public void setValueName(String value) {
		this.valueName = value;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

}