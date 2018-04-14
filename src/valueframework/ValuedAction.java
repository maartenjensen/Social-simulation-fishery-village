package valueframework;

import java.util.ArrayList;

public class ValuedAction {

	private String title;
	private ArrayList<String> valuesPositive;
	private ArrayList<String> valuesNegative;
	
	public ValuedAction(String title, ArrayList<String> valuesPositive, ArrayList<String> valuesNegative) {
		this.title = title;
		this.valuesPositive = valuesPositive;
		this.valuesNegative = valuesNegative;
	}
	
	public ArrayList<String> getValuesPositive() {
		return valuesPositive;
	}
	
	public ArrayList<String> getValuesNegative() {
		return valuesNegative;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String toString() {
		String returnString = "ValAct [" + title + "]:";
		for (String positive : valuesPositive) {
			returnString += " + " + positive;
		}
		for (String negative : valuesNegative) {
			returnString += " - " + negative;
		}
		return returnString;
	}
}
