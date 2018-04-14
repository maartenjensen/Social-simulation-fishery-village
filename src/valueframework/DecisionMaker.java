package valueframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import valueframework.common.Facility;
import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class DecisionMaker {
	
	private ArrayList<Action> allActions;
	private Map<String, RandomTree> valueTrees;
	private Map<String, WaterTank> waterTanks;

	public DecisionMaker() {
		
		valueTrees = new HashMap<String, RandomTree>();
		waterTanks = new HashMap<String, WaterTank>();	
		allActions = new ArrayList<Action>();
		
		assignAllActions(FrameworkBuilder.getAllPossibleActions());
		createValueTrees();
	}
	
	public double getUniversalismImportanceDistribution() {
		return waterTanks.get("Universalism").getThreshould() / (waterTanks.get("Power").getThreshould() + waterTanks.get("Universalism").getThreshould());
	}
	
	public double getWaterTankLevel(String abstractValue) {
		if (waterTanks.containsKey(abstractValue)) {
			return waterTanks.get(abstractValue).getFilledLevel();
		}
		return -1;
	}

	public void drainTanks() {
		for (String key : waterTanks.keySet()) {
			waterTanks.get(key).draining();
		}
	}
	
	/**
	 * Here we save the reference of the global value tree and create
	 * new waterTanks for the decisionmaker.
	 */
	private void createValueTrees(){
		
		for(String rootName: FrameworkBuilder.getGlobalValueTrees().keySet()){
			RandomTree rt = FrameworkBuilder.getGlobalValueTrees().get(rootName);
			rt.getWaterTank().setRelatedAbstractValue(rt.getRoot().getValueName());
			WaterTank wt = waterCopyWaterTank(rt.getWaterTank());
			valueTrees.put(rootName, rt);
			waterTanks.put(rootName, wt);
		}
	}
	
	private WaterTank waterCopyWaterTank(WaterTank wt) {

		Random r = new Random();
		double filledLevel = Math.round(wt.getCapacity() * r.nextDouble());
		WaterTank newWaterTank = new WaterTank(wt.getCapacity(), filledLevel, 
											   wt.getThreshould(), wt.getDrainingAmount(), wt.getRelatedAbstractValue());
		return newWaterTank;
	}

	/**
	 * Filters the given actions based on the watertanks
	 * @param possibleActionsIn
	 * @return
	 */
	public ArrayList<ValuedAction> agentFilterActionsBasedOnValues(ArrayList<String> possibleActionTitlesIn) {
		
		// Return if input is an array list of zero
		if (possibleActionTitlesIn.size() == 0) {
			return new ArrayList<ValuedAction>();
		}
		
		// Convert action titles to actions and get the trees that belong to the trees
		ArrayList<Action> possibleActions = convertActionTitlesToActions(possibleActionTitlesIn);
		ArrayList<RandomTree> relatedAbstractValue = selectAbstractValuesAccordingToActions(possibleActions);
		
		// Retrieve the actions that belong to the highest priority value
		ArrayList<ValuedAction> selectedValuedActions = getActionsWithHighestPriorityValue(possibleActions, relatedAbstractValue);
		Log.printValuedActions(selectedValuedActions);

		return selectedValuedActions;
	}

	/**
	 * Updates the watertanks of the corresponding values
	 * @param selectedActionTitle
	 */
	public void agentExecutesValuedAction(ValuedAction selectedActionTitle) {
		
		for(String positiveValue : selectedActionTitle.getValuesPositive()) {
			waterTanks.get(positiveValue).increaseLevel();
		}
		
		for(String negativeValue : selectedActionTitle.getValuesNegative()) {
			waterTanks.get(negativeValue).decreaseLevel();
		}
	}

	private ArrayList<Action> convertActionTitlesToActions(ArrayList<String> possibleActionTitlesIn) {
		
		ArrayList<Action> possibleActions = new ArrayList<Action>();
		for(Action action : allActions) {
			if (possibleActionTitlesIn.contains(action.getTitle())) {
				possibleActions.add(action);
			}
		}
		if (possibleActionTitlesIn.size() != possibleActions.size()) {
			Log.printError("ArrayLists do not match in size actionTitles.size(): " + possibleActionTitlesIn.size() + ", actions.size():" + possibleActions.size());
		}
		return possibleActions;
	}
	
	private ArrayList<ValuedAction> getActionsWithHighestPriorityValue(ArrayList<Action> possibleActionsSet, ArrayList<RandomTree> selectedValues) {
		
		// There should be at least some possibleActions and selectedValues
		if (possibleActionsSet.size() == 0 || selectedValues.size() == 0)
			return new ArrayList<ValuedAction>();
		
		// Order value trees based on priority
		ArrayList<RandomTree> orderedAbstractValues = prioritizingWaterTanks(selectedValues);
		
		ArrayList<ValuedAction> returnedValuedActions = new ArrayList<ValuedAction>();
		// Search through the trees in order and return the first abstract value that has 1 or more actions available
		for(RandomTree rt: orderedAbstractValues) {
			
			ArrayList<Action> relatedActions = filterActions(possibleActionsSet, rt);
			if (relatedActions.size() >= 1) {
				for(Action tmpAct : relatedActions){
					//TODO this uses only positively influenced abstract values
					ArrayList<String> positiveValues = new ArrayList<String>();
					positiveValues.add(rt.getRoot().getValueName());
					returnedValuedActions.add(new ValuedAction(tmpAct.getTitle(), positiveValues, new ArrayList<String>()));
				}
				break;			
			}
		}
		return returnedValuedActions;
	}
	
	private ArrayList<RandomTree> prioritizingWaterTanks(ArrayList<RandomTree> rts) {
		
		ArrayList<WaterTank> unsortedWaterTanks = new ArrayList<WaterTank>();
		for(int i = 0; i < rts.size(); i++) {			
			if(waterTanks.keySet().contains(rts.get(i).getRoot().getValueName())) {
				unsortedWaterTanks.add(waterTanks.get(rts.get(i).getRoot().getValueName()));
			}
		}
		
		ArrayList<RandomTree> sortedValueTrees = new ArrayList<RandomTree>();
		ArrayList<WaterTank>  sortedWaterTanks = Facility.sort(unsortedWaterTanks);
		for(int i =0; i < sortedWaterTanks.size(); i++) {
			sortedValueTrees.add(valueTrees.get(sortedWaterTanks.get(i).getRelatedAbstractValue()));
		}
		return sortedValueTrees;
	}
	
	public WaterTank mostImportantValue() {
		
		return Facility.sort(new ArrayList<WaterTank>(waterTanks.values())).get(0);
	}

	private ArrayList<RandomTree> selectAbstractValuesAccordingToActions(
			ArrayList<Action> possibleActionsSet) {
		// TODO: picks values that are linked to the possible actions. and then
		// apply the priority function on them
		// then returns the values and their importance .
		// priority is a signed calculated like this : (level-thres)/thres *100;
		// this list contains at the values with the same priority.
		// TODO: check if the list is empty write a message that values are not
		// applicable here.
		/*System.out
				.println("\npossibleActions in selectAbstractValuesAccordingToActions before selection: ");
		Log.printActions(possibleActionsSet);*/
		ArrayList<RandomTree> outValues = new ArrayList<RandomTree>();
		for (int i = 0; i < possibleActionsSet.size(); i++) {
			ArrayList<RandomTree> val = findAbstractValues(possibleActionsSet
					.get(i));
			for (int j = 0; j < val.size(); j++) {
				if (!outValues.contains(val.get(j)))
					outValues.add(val.get(j));
			}
		}

		/*System.out
				.println("\nvalues : in selectAbstractValuesAccordingToActions after selection: ");
		Log.printAbstractValues(outValues);*/

		return outValues;
	}

	private ArrayList<RandomTree> findAbstractValues(Action action) {
		ArrayList<Node> absValues = new ArrayList<Node>();
		ArrayList<RandomTree> rndTrees = new ArrayList<RandomTree>();
		ArrayList<Node> concreteValues = action.getRelatedConcreteValues();
		for (int i = 0; i < concreteValues.size(); i++) {
			Node crrPrnt = concreteValues.get(i);
			Node prvPrnt = crrPrnt;
			while (crrPrnt != null) {
				prvPrnt = crrPrnt;
				crrPrnt = crrPrnt.getParent();
			}
			if (!absValues.contains(prvPrnt))
				absValues.add(prvPrnt);
		}

		/*
		 * Iterator it = valueTrees.entrySet().iterator(); while (it.hasNext())
		 * { Map.Entry pair = (Map.Entry)it.next();
		 * if(absValues.contains(((RandomTree)pair.getValue()).getRoot()))
		 * rndTrees.add((RandomTree) pair.getValue()); it.remove(); // avoids a
		 * ConcurrentModificationException }
		 */
		for (RandomTree value : valueTrees.values()) {
			if (absValues.contains(value.getRoot()))
				rndTrees.add(value);
		}

		return rndTrees;
	}

	private ArrayList<Action> filterActions(ArrayList<Action> possibleActions,
			RandomTree lowestValue) {
		// check all the possibleActions that are related to the given abstract value
		ArrayList<Action> filteredActions = new ArrayList<Action>();
		for (int i = 0; i < possibleActions.size(); i++) {
			int numOfPossitiveContibutedValues = possibleActions.get(i)
					.checkRelatedValueInValueTree(lowestValue.getRoot(), true);
			// int numOfNegativeContibutedValues =
			// possibleActions.get(i).checkRalatedValueInValueTree(lowestValue.getRoot(),
			// false);
			if (numOfPossitiveContibutedValues != 0) {
				if(!filteredActions.contains(possibleActions.get(i)))
				filteredActions.add(possibleActions.get(i));
			}
		}
		return filteredActions;
	}

	public void assignAllActions(ArrayList<Action> alist) {
		allActions = alist;
	}

	public Map<String, RandomTree> getValueTrees() {
		return valueTrees;
	}

	public void setValueTrees(Map<String, RandomTree> valueTrees) {
		this.valueTrees = valueTrees;
	}

	public double getSelfDirectionThreshold() {
		return waterTanks.get("Self-direction").getThreshould();
	}
	
	public boolean getIsSatisfied() {
		int satisfiedValues = 0;
		for (String key: waterTanks.keySet()) {
			WaterTank wt = waterTanks.get(key);
			if (wt.getFilledLevel() > wt.getThreshould()) {
				satisfiedValues ++;
			}
		}
		if (satisfiedValues > waterTanks.size() / 2) {
			return true;
		}
		return false;
	}
	
	public boolean isSelfDirectionSatisfied() {

		if (waterTanks.get("Self-direction").getFilledLevel() >= waterTanks.get("Self-direction").getThreshould()) {
			return true;
		}
		return false;
	}
	
	public double getTankDrainAmount() {
		
		for (String key: waterTanks.keySet()) {
			return waterTanks.get(key).getDrainingAmount();
		}
		return 0;
	}
	
	@Override
	public String toString() {
		String string = "";
		for (String key: waterTanks.keySet()) {
			string += waterTanks.get(key).getRelatedAbstractValue().charAt(0) + ":" +
					waterTanks.get(key).getFilledLevel() + "/" + waterTanks.get(key).getThreshould() + ", ";
		}
		return string;
	}
}
