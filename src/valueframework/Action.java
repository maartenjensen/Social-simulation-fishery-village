package valueframework;

import java.util.ArrayList;
import java.util.List;

public class Action {
	
	private ArrayList<Node> relatedConcreteValues;
	private String title;
	
	public Action(int id) {
		relatedConcreteValues = new ArrayList<Node>();
		title = Integer.toString(id);
	}
	
	public Action(String titleIn, ArrayList<String> concreteValuesInn) {
		title = titleIn;
		relatedConcreteValues= new ArrayList<Node>();
	}

	public void addRelatedConcreteValue(Node concreteValue, Boolean contribution){
		relatedConcreteValues.add(concreteValue);
	}
	
	public ArrayList<Node> getRelatedConcreteValues(){
		return relatedConcreteValues;
	}
	
	public int checkRelatedValueInValueTree(Node root, boolean contributionType) {
		//look into the value tree until you get the concrete values and check if the concrete values are in the list of relatedValues of the actionC
		return sweepTree(root);
	}
	
	public void assignConcreteValues(ArrayList<Node> relatedCncrtValue) {
		if(relatedCncrtValue!=null){
			for(Node nd : relatedCncrtValue)
				if(!this.relatedConcreteValues.contains(nd))
					this.relatedConcreteValues.addAll(relatedCncrtValue);
		}
		else{
			System.err.println("returned concreteValue list is null");
		}
	}

	private int sweepTree(Node valueIn) {
		int numOfRelatedConcreteValues = 0;
		if(valueIn.getChildren().size() == 0)//it's a leaf
		{
			if(relatedConcreteValues.contains(valueIn))
				numOfRelatedConcreteValues++;
		}
		else{
			List<Node> children = valueIn.getChildren();
			for (int i = 0; i < children.size(); i++) {
				numOfRelatedConcreteValues = numOfRelatedConcreteValues + sweepTree(children.get(i)); 
			}			
		}
		return numOfRelatedConcreteValues;
	}
	
	@Override
    public String toString(){
        return "action "+ title +", done" ;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}