package valueframework.example;

import java.util.ArrayList;
import java.util.Random;

import valueframework.DecisionMaker;
import valueframework.ValuedAction;
import valueframework.common.Log;

public class Agent {
	
	private int id = -1;
	private DecisionMaker decisionMaker;
	
	public Agent(int id) {
		
		Log.printStars();
		this.id = id;
		Log.printLog("Agent " + this.id + " with DecisionMaker");
		decisionMaker = new DecisionMaker();
	}
	
	public void stepAction() {
		
		ArrayList<String> possibleActions = getPossibleActions();
		Log.printLog("Agent " + id + " possible actions: " + possibleActions);
		ArrayList<ValuedAction> valueFilteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);
		Random r = new Random();
		if (valueFilteredActions.size() >= 1) {
			
			ValuedAction selectedAction = valueFilteredActions.get(r.nextInt(valueFilteredActions.size()));
			Log.printLog("Agent " + id + " executes action: " + selectedAction.getTitle());
			decisionMaker.agentExecutesValuedAction(selectedAction, 1);
		}
		else {
			Log.printLog("Agent " + id + " doesn't perform an action");
		}
	}
	
	public void stepDrainTanks() {
		decisionMaker.drainTanks();
		Log.printLog("Agent " + id + " tanks after drain of " + decisionMaker.getTankDrainAmount() + " - " + decisionMaker.toString());
	}
	
	/**
	 * Retrieves the titles of all actions
	 * @return all action titles
	 */
	private ArrayList<String> getAllActions() {
		
		ArrayList<String> allActions = new ArrayList<String>();
		allActions.add("Job fisher");
		allActions.add("Job captain");
		allActions.add("Job teacher");
		allActions.add("Job factory worker");
		allActions.add("Job factory boss");
		allActions.add("Job elderly caretaker");
		allActions.add("Job work outside village");
		allActions.add("Job unemployed");
		return allActions;
	}
	
	/**
	 * Retrieves a subset of all actions
	 * @return some action titles
	 */
	private ArrayList<String> getPossibleActions() {
		
		Random r = new Random();
		ArrayList<String> possibleActions = new ArrayList<String>();
		for (String actionTitle : getAllActions()) {
			if (r.nextDouble() < 0.3) {
				possibleActions.add(actionTitle);
			}
		}
		return possibleActions;
	}

}
