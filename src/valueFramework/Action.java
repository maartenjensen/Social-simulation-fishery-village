package valueFramework;

import java.util.ArrayList;

public class Action {

	private String title;
	private ArrayList<String> relatedConcreteValues;
	
	public Action(String titleIn, ArrayList<String> concreteValuesIn){
		
		title = titleIn;
		relatedConcreteValues = concreteValuesIn;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ArrayList<String> getRelatedConcreteValues() {
		return relatedConcreteValues;
	}
}