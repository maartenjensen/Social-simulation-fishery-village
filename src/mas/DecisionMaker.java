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
import repast.simphony.random.RandomHelper;
import common.FrameworkBuilder;
import valueFramework.*;

public class DecisionMaker {
	private ArrayList<Action> possibleActions;
	private Map<String, RandomTree> valueTrees;
	private int Id;

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
		FrameworkBuilder.humanId++;		
		possibleActions = new ArrayList<Action>();
	}
	
	/*private void createYourOwnRandomValueTrees() {
		RandomTree tree = new RandomTree();
		Node root = tree.randomTreeBuilder(5, 3,
				Integer.toString(FrameworkBuilder.valueNumber));
		System.out.println("****** this is root " + root.getValueName());
		valueTrees.put(root.getValueName(), tree);
	}*/

	/*public RandomTree createValueTrees(List<String> treeInfo) {
		List<String> items = Arrays.asList(treeInfo.get(0).split("\\s*,\\s*"));//ignore while space after comma
		//TODO: check the split sql
		
		
		RandomTree tree = new RandomTree();
		Node root = tree.randomTreeBuilderFromFile(items.get(0), treeInfo);
		System.out.println("****** this is root " + root.getValueName());
		valueTrees.put(root.getValueName(), tree);
		return tree;
	}*/

	//TODO: now it is the same as global value trees in the FrameworkBuilder. 
	//But, it can be different for each agent
	public void createValueTrees(){
		for(String rootName: FrameworkBuilder.globalValueTrees.keySet()){
			valueTrees.put(rootName, FrameworkBuilder.globalValueTrees.get(rootName));
		}
		
	}
	// @SuppressWarnings("unchecked")
	// @ScheduledMethod(start = 1, interval = 1, shuffle = true)
	// TODO: call it in a loop in the main function for test
	public void step() {
		ArrayList<Action> possibleActions = new ArrayList<Action>();
//		Log.printActions(this.Id, possibleActions);

		possibleActions = selectPossibleActionsBasedonPerspective();
		ArrayList<RandomTree> relatedAbstractValue = selectAbstractValuesAccordingToActions(possibleActions);

		ArrayList<Action> filterdActions = new ArrayList<Action>();
		filterdActions = (ArrayList<Action>) filterActionsAccordingToTheMostImportantValue(
				possibleActions, relatedAbstractValue)[0];
		
		Object[] shouldChangeType = filterActionsAccordingToTheMostImportantValue(
				possibleActions, relatedAbstractValue);
		System.out.println("\n\n@@@@@\tfinal action set : ");
		ArrayList<Action>selectedActions = (ArrayList<Action>) shouldChangeType[0];
		RandomTree relatedValues = (RandomTree) shouldChangeType[1];
		Log.printActions(-1, selectedActions);
		if(relatedValues!=null)
			System.out.println("accoring to this value " + relatedValues.getRoot().getValueName());
		
		Action pickedAction = pickAnActionRandomly(selectedActions);
		System.out.println("[[[[[[[[ before increasing level of  " + relatedValues.getRoot().getValueName() + ", level is : "
				+ relatedValues.getWaterTank().getFilledLevel());
		relatedValues.getWaterTank().increasingLevel();
		System.out.println("]]]]]]]] after increasing level of  " + relatedValues.getRoot().getValueName() + ", level is : "
				+ relatedValues.getWaterTank().getFilledLevel());
	}

	public ArrayList<String> actionSelectionFromPossibleActions(ArrayList<String> possibleActionsIn) {
		ArrayList<Action> allActions = new ArrayList<Action>();
		ArrayList<Action> possibleActions = new ArrayList<Action>();

		allActions = selectPossibleActionsBasedonPerspective();

		for(Action action : allActions) {
			if (possibleActionsIn.contains(action.getTitle())) {
				System.out.println("add Action object:" + action.getTitle());
				possibleActions.add(action);
			}
		}
		ArrayList<RandomTree> relatedAbstractValue = selectAbstractValuesAccordingToActions(possibleActions);

		ArrayList<Action> filterdActions = new ArrayList<Action>();
		filterdActions = (ArrayList<Action>) filterActionsAccordingToTheMostImportantValue(
				possibleActions, relatedAbstractValue)[0];
		
		Object[] shouldChangeType = filterActionsAccordingToTheMostImportantValue(
				possibleActions, relatedAbstractValue);
		System.out.println("\n\n@@@@@\tfinal action set : ");
		ArrayList<Action>selectedActions = (ArrayList<Action>) shouldChangeType[0];
		RandomTree relatedValues = (RandomTree) shouldChangeType[1];
		Log.printActions(-1, selectedActions);
		if(relatedValues!=null)
			System.out.println("accoring to this value " + relatedValues.getRoot().getValueName());
		
		System.out.println("[[[[[[[[ before increasing level of  " + relatedValues.getRoot().getValueName() + ", level is : "
				+ relatedValues.getWaterTank().getFilledLevel());
		relatedValues.getWaterTank().increasingLevel();
		System.out.println("]]]]]]]] after increasing level of  " + relatedValues.getRoot().getValueName() + ", level is : "
				+ relatedValues.getWaterTank().getFilledLevel());
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
		ArrayList<RandomTree> prioritizedAbstractValues = prioritizingValues(selectedValues);
		System.out.println("prioritized abstract values : "
				+ selectedValues.size());
		Log.printAbstractValues(this.Id, prioritizedAbstractValues);
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
		
		System.out.println("\t\t	in filterAction Function : related actions are : ");
		Log.printActions(-1, returnedActions);
		Object[] returnedResults = new Object[] { returnedActions,
				returnedValues };
		return returnedResults;
	}

	private ArrayList<RandomTree> prioritizingValues(
			ArrayList<RandomTree> selectedValues) {
//		Collections.sort(selectedValues);		
		return Facility.sort(selectedValues);
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
		System.out
				.println("\npossibleActions in selectAbstractValuesAccordingToActions before selection: ");
		Log.printActions(this.Id, possibleActionsSet);
		ArrayList<RandomTree> outValues = new ArrayList<RandomTree>();
		for (int i = 0; i < possibleActionsSet.size(); i++) {
			ArrayList<RandomTree> val = findAbstractValues(possibleActionsSet
					.get(i));
			for (int j = 0; j < val.size(); j++) {
				if (!outValues.contains(val.get(j)))
					outValues.add(val.get(j));
			}
		}

		System.out
				.println("\nvalues : in selectAbstractValuesAccordingToActions after selection: ");
		Log.printAbstractValues(this.Id, outValues);

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

	private ArrayList<Action> selectActionsAccordingToTheMostImportantValue(
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
			if (tempLevel < lowestLevel /* && lowestValue < secondLowest */) {
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
	}

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

}
