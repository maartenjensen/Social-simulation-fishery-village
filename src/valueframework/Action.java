package valueframework;

import java.util.ArrayList;
import java.util.List;

public class Action {
	
	private ArrayList<Node> positiveConcreteValues;
	private ArrayList<Node> negativeConcreteValues;
	private String title;
	
	public Action(String titleIn, ArrayList<Node> positiveConcreteValues, ArrayList<Node> negativeConcreteValues) {
		title = titleIn;
		this.positiveConcreteValues = new ArrayList<Node>();
		this.positiveConcreteValues.addAll(positiveConcreteValues);
		this.negativeConcreteValues = new ArrayList<Node>();
		this.negativeConcreteValues.addAll(negativeConcreteValues);
	}

	public ArrayList<Node> getPositiveRelatedConcreteValues() {
		return positiveConcreteValues;
	}
	
	public ArrayList<Node> getNegativeRelatedConcreteValues() {
		return negativeConcreteValues;
	}
	
	public int checkPositiveRelatedValueInValueTree(Node root) {
		return sweepTreePositive(root);
	}

	public int checkNegativeRelatedValueInValueTree(Node root) {
		return sweepTreeNegative(root);
	}

	/**
	 * look into the value tree until you get the concrete values and check if the concrete values are in the list of positivelyRelatedValues of the actionC
	 * @param valueIn
	 * @return
	 */
	private int sweepTreePositive(Node valueIn) {
		int numOfRelatedConcreteValues = 0;
		if(valueIn.getChildren().size() == 0)//it's a leaf
		{
			if(positiveConcreteValues.contains(valueIn))
				numOfRelatedConcreteValues++;
		}
		else{
			List<Node> children = valueIn.getChildren();
			for (int i = 0; i < children.size(); i++) {
				numOfRelatedConcreteValues = numOfRelatedConcreteValues + sweepTreePositive(children.get(i)); 
			}			
		}
		return numOfRelatedConcreteValues;
	}
	
	/**
	 * look into the value tree until you get the concrete values and check if the concrete values are in the list of negativeRelatedValues of the actionC
	 * @param valueIn
	 * @return
	 */
	private int sweepTreeNegative(Node valueIn) {
		int numOfRelatedConcreteValues = 0;
		if(valueIn.getChildren().size() == 0)//it's a leaf
		{
			if(negativeConcreteValues.contains(valueIn))
				numOfRelatedConcreteValues++;
		}
		else{
			List<Node> children = valueIn.getChildren();
			for (int i = 0; i < children.size(); i++) {
				numOfRelatedConcreteValues = numOfRelatedConcreteValues + sweepTreeNegative(children.get(i)); 
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