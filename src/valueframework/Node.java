package valueframework;


import java.util.ArrayList;
import java.util.List;

public class Node
{
    private List<Node> children = null;
    private Node parent;
    private String valueName;

    private ArrayList<Action> positiveRelatedActions;
    private ArrayList<Action> negativeRelatedActions;
    
    public void addPositiveAction(Action posAction) {
    	positiveRelatedActions.add(posAction);
    }
    
    public void addNegativeAction(Action negAction) {
    	negativeRelatedActions.add(negAction);
    }
    
    public Node(String value, Node parentIn)
    {
        this.children = new ArrayList<>();
        this.setValueName(value);
        this.setParent(parentIn);
        
        positiveRelatedActions = new ArrayList<Action>();
        negativeRelatedActions = new ArrayList<Action>();
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

	public String toStringActions() {
		String print = valueName;
		print += ", +ACT:";
		for (Action pos : positiveRelatedActions) {
			print += pos.getTitle() + ", ";
		}
		print += ": -ACT:";
		for (Action neg : negativeRelatedActions) {
			print += neg.getTitle() + ", ";
		}
		return print;
	}
	
	public String toTreeStringActions(String stringActionStart) {
		String print = "";
		for (Action pos : positiveRelatedActions) {
			print += stringActionStart + "+" + pos.getTitle() + "\n";
		}
		for (Action neg : negativeRelatedActions) {
			print += stringActionStart + "-" + neg.getTitle() + "\n";
		}
		return print;
	}
}