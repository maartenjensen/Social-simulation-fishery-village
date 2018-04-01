package valueFramework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import common.Constants;
import common.Log;
import common.FrameworkBuilder;

public class Action {
//	private ArrayList<Object[]> relatedConcreteValues;//arraylist of list[2] : [node as a concrete value, +/- contribution]
	private ArrayList<Node> relatedConcreteValues;
	private String title;
	public Action() {
//		relatedConcreteValues = new ArrayList<Object[]>();	
		relatedConcreteValues = new ArrayList<Node>();
		title = Integer.toString(Constants.ID_ACTION);
		Constants.ID_ACTION++;
	}
	
	
	public Action(String titleIn, ArrayList<String> concreteValuesInn) {
		title = titleIn;
		relatedConcreteValues= new ArrayList<Node>();
//		relatedConcreteValues = concreteValuesInn;
		Constants.ID_ACTION++;
	}

	public void addRelatedConcreteValue(Node concreteValue, Boolean contribution){
//		Object[] input = {concreteValue, contribution};
		relatedConcreteValues.add(concreteValue);
//		System.out.println(input[0]);
	}
	
	public void assignRandomConcreteValues(){
		ArrayList<Node> cncrValues = FrameworkBuilder.allConcreteValues;
		Collections.shuffle(cncrValues);
    	int randomNum = ThreadLocalRandom.current().nextInt(0, cncrValues.size());
    	for(int i = 0 ; i < randomNum; i++){
    		boolean randomCont = ThreadLocalRandom.current().nextBoolean();
    		
    		relatedConcreteValues.add(cncrValues.get(i));
    	}
	
    	System.out.println("in assignRandomConcreteValues action  " + this.title + " : ");
    	Log.printConcreteValues(this.title, relatedConcreteValues);
	}
	
	public ArrayList<Node> getRelatedConcreteValues(){
		return relatedConcreteValues;
	}
	public int checkRalatedValueInValueTree(Node root, boolean contributionType) {
		//look into the value tree until you get the concrete values and check if the concrete values are in the list of relatedValues of the actionC
		return sweepTree(root);
		 
	}

	private int sweepTree(Node valueIn) {
		int numOfRelatedConcreteValues = 0;
		if(valueIn.getChildren().size() ==0)//it's a leaf
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
	
}