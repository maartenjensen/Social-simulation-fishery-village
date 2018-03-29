package valueFramework;

import java.util.ArrayList;

import repast.simphony.random.RandomHelper;

public class DecisionMaker {

	
	public DecisionMaker() {
		
	}
	
	public Action filterActionsAccordingToTheMostImportantValue(ArrayList<Action> actions) {
		if (actions.size() >= 1) {
			return actions.get(RandomHelper.nextIntFromTo(0, actions.size() - 1));
		}
		else {
			return null;
		}
	}
}