package mas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import common.Facility;
import common.Log;
import fisheryvillage.common.Logger;
import repast.simphony.random.RandomHelper;
import common.FrameworkBuilder;
import valueFramework.*;

public class DecisionMaker {
	private ArrayList<Action> possibleActions;
	private Map<String, RandomTree> valueTrees;
	private Map<String, WaterTank> waterTanks;

	/*public HumanAgent() {
		Id = FrameworkBuilder.humanId;
		valueTrees = new HashMap<String, RandomTree>();
		FrameworkBuilder.humanId++;
		for (int i = 0; i < FrameworkBuilder.numOfAbstractValues; i++)
			createYourOwnRandomValueTrees();
		possibleActions = new ArrayList<Action>();
	}
*/
	public DecisionMaker(int id) {
//		Id = Variables.humanId;
		valueTrees = new HashMap<String, RandomTree>();
		waterTanks = new HashMap<String, WaterTank>();
		FrameworkBuilder.humanId++;		
		possibleActions = new ArrayList<Action>();
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

	//TODO: now it is the same as global value trees in the FrameworkBuilder. 
	//But, it can be different for each agent
	public void createValueTrees(){
		for(String rootName: FrameworkBuilder.globalValueTrees.keySet()){
			
			RandomTree rt = FrameworkBuilder.globalValueTrees.get(rootName);
			rt.getWaterTank().setRelatedAbstractValue(rt.getRoot().getValueName());
			WaterTank wt = waterCopyWaterTank(rt.getWaterTank());
			valueTrees.put(rootName, rt);
			waterTanks.put(rootName, wt);
		}
	}
	
	public void drainTanks() {
		for (String key : waterTanks.keySet()) {
			waterTanks.get(key).draining();
		}
	}
	
	public WaterTank waterCopyWaterTank(WaterTank wt) {
		WaterTank newWaterTank = new WaterTank(wt.getCapacity(), RandomHelper.nextIntFromTo(0, (int) wt.getCapacity()), 
											   wt.getThreshould(), wt.getDrainingAmount(), wt.getRelatedAbstractValue());

		Logger.logDebug("WaterTank " + newWaterTank.getRelatedAbstractValue() + ", dra:" + newWaterTank.getDrainingAmount() + ", incr:" + newWaterTank.getIncreasingAmount() + ", cap:" + newWaterTank.getCapacity() + ", thr: " + newWaterTank.getThreshould());
		return newWaterTank;
	}
	
	// @SuppressWarnings("unchecked")
	// @ScheduledMethod(start = 1, interval = 1, shuffle = true)
	// TODO: call it in a loop in the main function for test
	public void step() {
		
		ArrayList<Action> possibleActions = new ArrayList<Action>();
		possibleActions = selectPossibleActionsBasedonPerspective();
		ArrayList<RandomTree> relatedAbstractValue = selectAbstractValuesAccordingToActions(possibleActions);

		ArrayList<Action> filterdActions = new ArrayList<Action>();
		filterdActions = (ArrayList<Action>) filterActionsAccordingToTheMostImportantValue(possibleActions, relatedAbstractValue)[0];
		
		Object[] shouldChangeType = filterActionsAccordingToTheMostImportantValue(possibleActions, relatedAbstractValue);
		//System.out.println("Final action set : ");
		ArrayList<Action>selectedActions = (ArrayList<Action>) shouldChangeType[0];
		RandomTree selectedVal = (RandomTree) shouldChangeType[1];
		//Log.printActions(selectedActions);
		//if(selectedVal!=null)
			//System.out.println("accoring to this value " + selectedVal.getRoot().getValueName());
		
		Action pickedAction = pickAnActionRandomly(selectedActions);

		waterTanks.get(selectedVal.getRoot().getValueName()).increasingLevel();
		System.out.println("after increasing level of  " + waterTanks.get(selectedVal.getRoot().getValueName()).getRelatedAbstractValue() + ", level is : "
				+ waterTanks.get(selectedVal.getRoot().getValueName()).getFilledLevel());
	}

	public ArrayList<String> actionSelectionFromPossibleActions(ArrayList<String> possibleActionsIn) {
		ArrayList<Action> allActions = new ArrayList<Action>();
		ArrayList<Action> possibleActions = new ArrayList<Action>();

		allActions = selectPossibleActionsBasedonPerspective();

		for(Action action : allActions) {
			if (possibleActionsIn.contains(action.getTitle())) {
				possibleActions.add(action);
			}
		}
		ArrayList<RandomTree> relatedAbstractValue = selectAbstractValuesAccordingToActions(possibleActions);

		
		Object[] shouldChangeType = filterActionsAccordingToTheMostImportantValue(
				possibleActions, relatedAbstractValue);
		//System.out.println("Final action set : ");
		ArrayList<Action>selectedActions = (ArrayList<Action>) shouldChangeType[0];
		RandomTree selectedVal = (RandomTree) shouldChangeType[1];
		//Log.printActions(selectedActions);
		//if(selectedVal!=null)
			//System.out.println("accoring to this value " + selectedVal.getRoot().getValueName());
		
		waterTanks.get(selectedVal.getRoot().getValueName()).increasingLevel();
		
		System.out.println("After increasing level of  " + waterTanks.get(selectedVal.getRoot().getValueName()).getRelatedAbstractValue() + ", level is : "
				+ waterTanks.get(selectedVal.getRoot().getValueName()).getFilledLevel());
		//System.out.println("Final decided action:" + pickedAction.getTitle());
		ArrayList<String> selectedActionsTitles = new ArrayList<String>();
		for (Action selectedAction : selectedActions) {
			selectedActionsTitles.add(selectedAction.getTitle());
		}
		return selectedActionsTitles;
	}
	
	private Action pickAnActionRandomly(ArrayList<Action> selectedActions) {
		Random rand = new Random();
		if (selectedActions.size() > 0) {
			return selectedActions.get(RandomHelper.nextIntFromTo(0, selectedActions.size() - 1));
		}
		return null;
	}

	private Object[] filterActionsAccordingToTheMostImportantValue(
			ArrayList<Action> possibleActionsSet,
			ArrayList<RandomTree> selectedValues) {
		// TODO: filter the input list, apply the formula and return the final
		// action set that is filtered by
		// the most important values.
		// 1.Prioritize values
		// 2.select the highest priorities
		// 3.select actions related to that
		// 4.return the arraylist <object> in which object is {arraylist<action>
		// and arrayList<values>}
		ArrayList<Action> returnedActions = new ArrayList<Action>();
		RandomTree returnedValues = null;
		if (selectedValues == null | possibleActionsSet == null)
			return null;
		ArrayList<RandomTree> prioritizedAbstractValues = prioritizingWaterTanks(selectedValues);
		
		//System.out.println("prioritized abstract values : "	+ selectedValues.size());
		//Log.printAbstractValues(prioritizedAbstractValues);
		/*double prvPrio = -100;
		double crrPrio = prioritizedAbstractValues.get(0).getWaterTank()
				.getPriorityPercentage();*/
		
		
		//this part of code only considers the highest priority
		for(RandomTree rt: prioritizedAbstractValues){
			ArrayList<Action> relatedActions = filterActions(possibleActionsSet, rt);
			if (relatedActions.size()!=0){
				returnedValues = rt;
				for(Action tmpAct : relatedActions){
					if(!returnedActions.contains(tmpAct))
						returnedActions.addAll(relatedActions);
				}
				break;			
			}
				
		}
		
		//System.out.println("\t\t	in filterAction Function : related actions are : ");
		//Log.printActions(returnedActions);
		Object[] returnedResults = new Object[] { returnedActions,	returnedValues };
		return returnedResults;
	}

	
	public ArrayList<RandomTree> prioritizingWaterTanks(ArrayList<RandomTree> rts) {
		
		ArrayList<WaterTank> unsortedWaterTanks = new ArrayList<WaterTank>();
		for(int i = 0; i < rts.size(); i++) {			
			if(waterTanks.keySet().contains(rts.get(i).getRoot().getValueName())) {
				unsortedWaterTanks.add(waterTanks.get(rts.get(i).getRoot().getValueName()));
				//System.out.println("WT " + unsortedWaterTanks.get(i).getRelatedAbstractValue() + " :" + unsortedWaterTanks.get(i).getFilledLevel());
			}
		}
		
		ArrayList<RandomTree> sortedValueTrees = new ArrayList<RandomTree>();
		ArrayList<WaterTank>  sortedWaterTanks = Facility.sort(unsortedWaterTanks);
		for(int i =0; i < sortedWaterTanks.size(); i++) {
			sortedValueTrees.add(valueTrees.get(sortedWaterTanks.get(i).getRelatedAbstractValue()));
			//Logger.logDebug("a new value tree has been added to the sortedValueTrees : " + sortedWaterTanks.get(i).getRelatedAbstractValue() );
			//Logger.logDebug(", and the root is : " + valueTrees.get(sortedWaterTanks.get(i).getRelatedAbstractValue()).getRoot().getValueName());
		}
		return sortedValueTrees;
	}
	
	public WaterTank mostImportantValue() {
		
		/*ArrayList<WaterTank> wt = new ArrayList<WaterTank>();
		for (String k: waterTanks.keySet()) {
			 wt.add(waterTanks.get(k));
		}
		
		return Facility.sort(wt).get(0);*/
		return Facility.sort(new ArrayList<WaterTank>(waterTanks.values())).get(0);
	}

	private ArrayList<Action> selectPossibleActionsBasedonPerspective() {
		// TODO: it should select all the possible actions that are active in
		// this perspective
		// so TODO: add perspective and link it to actions. But, for now it can
		// return actions randomly
		// ArrayList<Action> possibleActions = new ArrayList<Action>();
		return possibleActions;
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

	/*private ArrayList<Action> selectActionsAccordingToTheMostImportantValue(
			ArrayList<Action> possibleActionsSet) {
		ArrayList<Action> filteredActions = new ArrayList<Action>();
		// TODO: first select only values that are linked to the possibleActions
		// start with finding the two lowest water level tanks
		double lowestLevel = Double.MAX_VALUE;
		RandomTree lowestValue = null;
		double secondLowestLevel = Double.MAX_VALUE;
		RandomTree secondLowestValue = null;
		double tempLevel;
		for (RandomTree node : valueTrees.values()) {
			tempLevel = node.getWaterTank().getFilledLevel();
			if (tempLevel < lowestLevel  && lowestValue < secondLowest ) {
				lowestLevel = tempLevel;
				lowestValue = node;
				secondLowestLevel = lowestLevel;
				secondLowestValue = lowestValue;
			} else if (lowestLevel > tempLevel & tempLevel > secondLowestLevel) {
				secondLowestLevel = tempLevel;
				secondLowestValue = node;
			}
		}

		// *NOTE : the importance of a value is a complement of its level.
		// Meaning that if a water tank has the lowest level of water, it has
		// the highest priority.

		// comparing the priorities
		// TODO: without considering perspective.
		// there are three modes : if v1 >> v2, if v1 > v2, if v1 == v2. TODO:
		// we skip the second condition for now.
		if (lowestLevel < secondLowestLevel) {
			// return lowestValue;
			filteredActions = filterActions(possibleActions, lowestValue);
		} else if (lowestLevel == secondLowestLevel) {
			// find how many of
			for (int i = 0; i < possibleActions.size(); i++) {
				int numOfPossitiveContibutedValues_lowestLevel = possibleActions
						.get(i).checkRalatedValueInValueTree(
								lowestValue.getRoot(), true);
				int numOfPossitiveContibutedValues_secondLowestLevel = possibleActions
						.get(i).checkRalatedValueInValueTree(
								lowestValue.getRoot(), true);
				if (numOfPossitiveContibutedValues_lowestLevel > numOfPossitiveContibutedValues_secondLowestLevel) {
					filteredActions = filterActions(possibleActions,
							lowestValue);
					break;
				} else if (numOfPossitiveContibutedValues_lowestLevel < numOfPossitiveContibutedValues_secondLowestLevel) {
					filteredActions = filterActions(possibleActions,
							secondLowestValue);
					break;// TODO: check if it breaks from for loop;
				} else if (numOfPossitiveContibutedValues_lowestLevel == numOfPossitiveContibutedValues_secondLowestLevel) {// TODO:
																															// check
																															// if
																															// it's
																															// correct
					int numOfNegativeContibutedValues_secondLowestLevel = possibleActions
							.get(i).checkRalatedValueInValueTree(
									lowestValue.getRoot(), false);
					int numOfNegativeContibutedValues_lowestLevel = possibleActions
							.get(i).checkRalatedValueInValueTree(
									lowestValue.getRoot(), false);
					filteredActions = filterActions(possibleActions,
							secondLowestValue);
				}
			}

		} else {
			System.err
					.println("lowest level cannot be greater that second lowest levle! level = "
							+ lowestLevel
							+ " for  "
							+ lowestValue.getRoot().getValueName()
							+ ", second lowest level = "
							+ secondLowestLevel
							+ " for "
							+ secondLowestValue.getRoot().getValueName());
		}
		return null;
	}*/

	private ArrayList<Action> filterActions(ArrayList<Action> possibleActions,
			RandomTree lowestValue) {
		// check all the possibleActions that are related to the given abstract value
		ArrayList<Action> filteredActions = new ArrayList<Action>();
		for (int i = 0; i < possibleActions.size(); i++) {
			int numOfPossitiveContibutedValues = possibleActions.get(i)
					.checkRalatedValueInValueTree(lowestValue.getRoot(), true);
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

	public void assignPossibleActions(ArrayList<Action> alist) {
		possibleActions = alist;
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
