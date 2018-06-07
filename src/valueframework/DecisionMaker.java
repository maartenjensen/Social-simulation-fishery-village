package valueframework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
		createWaterTanks();
		Log.printDebug("Making a new agent ");
		Log.printDebug("the input threshold is : ");
		FrameworkBuilder.printThresholds(valueTrees);
		ValueAssignment.checkInitialConditions(valueTrees);
		updateValueTrees(ValueAssignment.getImportanceRange());
		Log.printDebug("the assigned threshold is : ");
		FrameworkBuilder.printThresholds(valueTrees);
		printValueTrees();
	}

	private void updateValueTrees(ArrayList<String> importanceRange) {
		for (String valInfo : importanceRange) {
			System.out.println(importanceRange);
			WaterTank wt = waterCopyWaterTank(waterTanks.get(ValueAssignment
					.getValueName(valInfo)));
			wt.setThreshold(ValueAssignment.getThreshold(valInfo));
			waterTanks.put(wt.getRelatedAbstractValue(), wt);
			RandomTree rt = valueTrees.get(wt.getRelatedAbstractValue());
			rt.setWaterTank(wt);
			valueTrees.put(wt.getRelatedAbstractValue(), rt);
		}
	}

	private void printValueTrees() {
		for (String key : valueTrees.keySet()) {
			System.out.println(key + ": threshold "
					+ valueTrees.get(key).getWaterTank().getThreshold());
		}

	}

	public double getUniversalismImportanceDistribution() {
		return waterTanks.get(AbstractValue.UNIVERSALISM.name()).getThreshold()
				/ (waterTanks.get(AbstractValue.POWER.name()).getThreshold() + waterTanks
						.get(AbstractValue.UNIVERSALISM.name()).getThreshold());
	}

	public double getWaterTankLevel(String abstractValue) {
		if (waterTanks.containsKey(abstractValue)) {
			return waterTanks.get(abstractValue).getFilledLevel();
		}
		return -1;
	}

	public void setWaterTankThreshold(String valueName, double threshold) {
		waterTanks.get(valueName).setThreshold(threshold);
	}

	public void adjustWaterTankThreshold(String valueName, double change,
			double min, double max) {
		waterTanks.get(valueName).adjustThreshold(change, min, max);
	}

	public void drainTanks() {
		for (String key : waterTanks.keySet()) {
			waterTanks.get(key).draining();
		}
	}

	/**
	 * Here we save the reference of the global value tree and create new
	 * waterTanks for the decisionmaker.
	 */
	private void createWaterTanks() {
		for (String rootName : FrameworkBuilder.getGlobalValueTrees().keySet()) {
			RandomTree rt = copyRadnomTree(FrameworkBuilder
					.getGlobalValueTrees().get(rootName));
			valueTrees.put(rootName, rt);
			waterTanks.put(rootName, rt.getWaterTank());
		}
	}

	private RandomTree copyRadnomTree(RandomTree rt) {
		Node newRoot = new Node(rt.getRoot().getValueName(), null);
		Node newTreeCrrNode, oldTreeCrrNode;
		newTreeCrrNode = newRoot;
		oldTreeCrrNode = rt.getRoot();

		copyChildren(newTreeCrrNode, oldTreeCrrNode);

		RandomTree randomTree = new RandomTree(newTreeCrrNode,
				waterCopyWaterTank(rt.getWaterTank()));
		return randomTree;
	}

	private void copyChildren(Node newTreeCrrNode, Node oldTreeCrrNode) {

		for (Node ch : oldTreeCrrNode.getChildren()) {
			Node newChild = new Node(ch.getValueName(), newTreeCrrNode);
			newTreeCrrNode.addChild(ch);
			copyChildren(newChild, ch);
		}
		if (oldTreeCrrNode.getChildren().size() == 0)
			return;
	}

	private WaterTank waterCopyWaterTank(WaterTank wt) {

		Random r = new Random();
		double filledLevel = Math.round(wt.getCapacity() * r.nextDouble());
		WaterTank newWaterTank = new WaterTank(wt.getCapacity(), filledLevel,
				wt.getThreshold(), wt.getDrainingAmount(),
				wt.getRelatedAbstractValue());
		return newWaterTank;
	}

	/**
	 * Filters the given actions based on the watertanks
	 * 
	 * @param possibleActionsIn
	 * @return
	 */
	public ArrayList<ValuedAction> agentFilterActionsBasedOnValues(
			ArrayList<String> possibleActionTitlesIn) {

		// Return if input is an array list of zero
		if (possibleActionTitlesIn.size() == 0) {
			return new ArrayList<ValuedAction>();
		}

		// Convert action titles to actions and get the trees that belong to the
		// trees
		ArrayList<Action> possibleActions = convertActionTitlesToActions(possibleActionTitlesIn);
		Map<String, Double> weightedValues = getWeightedAbstractValueList();
		if (weightedValues.size() == 0) {
			Log.printLog("No value is important, returning all possible actions");
			return convertActionsToValuedActions(possibleActions);
		}
		Log.printLog("Print weighted values" + weightedValues.toString());
		ArrayList<ValuedAction> possibleValuedActions = evaluateActionsAccordingToValues(
				possibleActions, weightedValues);
		Log.printValuedActions("Evaluated VA: ", possibleValuedActions);

		// Select actions
		ArrayList<ValuedAction> selectedValuedActions = filterValuedActions(possibleValuedActions);

		return selectedValuedActions;
	}

	/**
	 * First sorts the possibleValuedActions, then adds all of them that have a
	 * positive action goodness If there are none which have a goodness above
	 * zero then the ones with the highest goodness are returned e.g. a set of
	 * valuedActions with goodness 0 or maybe -0.1
	 * 
	 * @param possibleValuedActions
	 * @return
	 */
	private ArrayList<ValuedAction> filterValuedActions(
			ArrayList<ValuedAction> possibleValuedActions) {

		Collections.sort(possibleValuedActions);
		ArrayList<ValuedAction> choosenValuedActions = new ArrayList<ValuedAction>();
		double highestValue = -1000; // Double.MIN_NORMAL; doesn't work the
										// value is to low to compare
		for (ValuedAction valuedAction : possibleValuedActions) {

			if (valuedAction.getActionGoodness() > 0) {
				choosenValuedActions.add(valuedAction);
				highestValue = 1;
			} else {
				if (highestValue <= valuedAction.getActionGoodness()) {
					choosenValuedActions.add(valuedAction);
					highestValue = valuedAction.getActionGoodness();
				}
			}
		}
		return choosenValuedActions;
	}

	private ArrayList<ValuedAction> evaluateActionsAccordingToValues(
			ArrayList<Action> possibleActions,
			Map<String, Double> weightedValues) {

		ArrayList<ValuedAction> valuedActions = new ArrayList<ValuedAction>();
		for (Action action : possibleActions) {

			double actionGoodness = 0;
			HashMap<String, Integer> evaluatedValues = new HashMap<String, Integer>();
			ArrayList<String> positiveAbstractValues = findPositiveAbstractValues(action);
			ArrayList<String> negativeAbstractValues = findNegativeAbstractValues(action);
			for (String key : weightedValues.keySet()) {
				if (positiveAbstractValues.contains(key)) {
					evaluatedValues.put(key, 1);
					actionGoodness += weightedValues.get(key);
				} else if (negativeAbstractValues.contains(key)) {
					evaluatedValues.put(key, -1);
					actionGoodness -= weightedValues.get(key);
				}
			}
			valuedActions.add(new ValuedAction(action.getTitle(),
					evaluatedValues, actionGoodness));
		}
		return valuedActions;
	}

	private Map<String, Double> getWeightedAbstractValueList() {

		Map<String, Double> priorityOfValues = new HashMap<String, Double>();
		double sigma = 0;
		for (String wtKey : waterTanks.keySet()) {
			double priority = waterTanks.get(wtKey).getPriorityPercentage();
			if (priority > 0) {
				priorityOfValues.put(wtKey, priority);
				sigma += priority;
			}
		}
		Map<String, Double> weightedValues = new HashMap<String, Double>();
		for (String prioKey : priorityOfValues.keySet()) {
			double weight = priorityOfValues.get(prioKey) / sigma;
			weightedValues.put(prioKey, weight);
		}
		return weightedValues;
	}

	/**
	 * Updates the watertanks of the corresponding values
	 * 
	 * @param selectedActionTitle
	 */
	public void agentExecutesValuedAction(ValuedAction selectedActionTitle) {

		for (String positiveValue : selectedActionTitle.getValuesPositive()) {
			waterTanks.get(positiveValue).increaseLevel(0.25);
		}

		for (String negativeValue : selectedActionTitle.getValuesNegative()) {
			waterTanks.get(negativeValue).decreaseLevel(0.5);
		}
	}

	private ArrayList<Action> convertActionTitlesToActions(
			ArrayList<String> possibleActionTitlesIn) {

		ArrayList<Action> possibleActions = new ArrayList<Action>();
		for (Action action : allActions) {
			if (possibleActionTitlesIn.contains(action.getTitle())) {
				possibleActions.add(action);
			}
		}
		if (possibleActionTitlesIn.size() != possibleActions.size()) {
			Log.printError("ArrayLists do not match in size actionTitles.size(): "
					+ possibleActionTitlesIn.size()
					+ ", actions.size():"
					+ possibleActions.size());
		}
		return possibleActions;
	}

	private ArrayList<ValuedAction> convertActionsToValuedActions(
			ArrayList<Action> actions) {
		ArrayList<ValuedAction> valuedActions = new ArrayList<ValuedAction>();
		for (Action action : actions) {
			valuedActions.add(new ValuedAction(action.getTitle()));
		}
		return valuedActions;
	}

	public WaterTank mostImportantValue() {

		return Facility.sort(new ArrayList<WaterTank>(waterTanks.values()))
				.get(0);
	}

	private ArrayList<String> findPositiveAbstractValues(Action action) {
		ArrayList<Node> absValues = new ArrayList<Node>();
		ArrayList<String> rndTrees = new ArrayList<String>();
		// add all the related values including negative and positive related
		// ones.
		ArrayList<Node> concreteValues = action
				.getPositiveRelatedConcreteValues();
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

		for (RandomTree value : valueTrees.values()) {
			if (absValues.contains(value.getRoot()))
				rndTrees.add(value.getRoot().getValueName());
		}

		return rndTrees;
	}

	private ArrayList<String> findNegativeAbstractValues(Action action) {
		ArrayList<Node> absValues = new ArrayList<Node>();
		ArrayList<String> rndTrees = new ArrayList<String>();
		// add all the related values including negative and positive related
		// ones.
		ArrayList<Node> concreteValues = action
				.getNegativeRelatedConcreteValues();
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

		for (RandomTree value : valueTrees.values()) {
			if (absValues.contains(value.getRoot()))
				rndTrees.add(value.getRoot().getValueName());
		}

		return rndTrees;
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

	public double getAbstractValueThreshold(AbstractValue abstractValue) {
		return waterTanks.get(abstractValue.name()).getThreshold();
	}

	public int getSatisfiedValuesCount() {
		int satisfiedValues = 0;
		for (String key : waterTanks.keySet()) {
			WaterTank wt = waterTanks.get(key);
			if (wt.getFilledLevel() >= wt.getThreshold()) {
				satisfiedValues++;
			}
		}
		return satisfiedValues;
	}

	public boolean getIsSatisfied() {

		if (getSatisfiedValuesCount() > waterTanks.size() / 2) {
			return true;
		}
		return false;
	}

	public boolean isSelfDirectionSatisfied() {

		if (waterTanks.get(AbstractValue.SELFDIRECTION.name()).getFilledLevel() >= waterTanks
				.get(AbstractValue.SELFDIRECTION.name()).getThreshold()) {
			return true;
		}
		return false;
	}

	public double getTankDrainAmount() {

		for (String key : waterTanks.keySet()) {
			return waterTanks.get(key).getDrainingAmount();
		}
		return 0;
	}

	public void setImportantWaterTankFromData(List<String> data) {
		for (int i = 1; i < data.size(); i += 3) {
			waterTanks.get(data.get(i)).setLevelAndThreshold(
					Double.parseDouble(data.get(i + 1)),
					Double.parseDouble(data.get(i + 2)));
		}
	}

	public String importantData() {
		String string = "";
		boolean first = true;
		for (String key : waterTanks.keySet()) {
			if (!first) {
				string += ",";

			}
			string += waterTanks.get(key).getRelatedAbstractValue() + ","
					+ waterTanks.get(key).getFilledLevel() + ","
					+ waterTanks.get(key).getThreshold();
			first = false;
		}
		return string;
	}

	@Override
	public String toString() {
		String string = "";
		for (String key : waterTanks.keySet()) {
			string += waterTanks.get(key).getRelatedAbstractValue().charAt(0)
					+ ":" + waterTanks.get(key).getFilledLevel() + "/"
					+ waterTanks.get(key).getThreshold() + ", ";
		}
		return string;
	}
}