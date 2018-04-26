package valueframework;

import java.util.ArrayList;
import java.util.HashMap;

public class ValuedAction implements Comparable<ValuedAction> {

	private String title;
	private HashMap<String, Integer> evaluatedValues;
	private double actionGoodness;
	
	public ValuedAction(String title) {
		this.title = title;
		this.evaluatedValues = new HashMap<String, Integer>();
		actionGoodness = 0;
	}
	
	public ValuedAction(String title, HashMap<String, Integer> evaluatedValues, double actionGoodness) {
		this.title = title;
		this.evaluatedValues = evaluatedValues;
		this.actionGoodness = actionGoodness;
	}
	
	public ArrayList<String> getValuesPositive() {
		ArrayList<String> valuesPositive = new ArrayList<String>();
		for (String key : evaluatedValues.keySet()) {
			if (evaluatedValues.get(key) == 1) {
				valuesPositive.add(key);
			}
		}
		return valuesPositive;
	}
	
	public ArrayList<String> getValuesNegative() {
		ArrayList<String> valuesNegative = new ArrayList<String>();
		for (String key : evaluatedValues.keySet()) {
			if (evaluatedValues.get(key) == -1) {
				valuesNegative.add(key);
			}
		}
		return valuesNegative;
	}
	
	public double getActionGoodness() {
		return actionGoodness;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String toString() {
		String returnString = "ValAct [" + title + "]:";
		for (String key : evaluatedValues.keySet()) {
			if (evaluatedValues.get(key) == 1) {
				returnString += " +" + key.charAt(0);
			}
			else if (evaluatedValues.get(key) == -1) {
				returnString += " -" + key.charAt(0);
			}
		}

		returnString += ", Good: " + actionGoodness;
		return returnString;
	}

	@Override
	public int compareTo(ValuedAction other) {

		if (this.getActionGoodness() > other.getActionGoodness()) return -1;
		if (this.getActionGoodness() < other.getActionGoodness()) return 1;
		return 0;
	}
}
