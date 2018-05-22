package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.municipality.Event;
import fisheryvillage.property.municipality.EventHall;
import repast.simphony.random.RandomHelper;
import valueframework.AbstractValue;
import valueframework.DecisionMaker;
import valueframework.ValuedAction;

/**
 * This class extends the human class with decision making options
 * @author Maarten
 *
 */
public final class Resident extends Human {

	// Variable declaration (initialization in constructor)
	private DecisionMaker decisionMaker;
	
	// Variable initialization
	private int homelessTick = 0; //TODO remove this
	private int childrenWanted = 0;
	private SocialStatus socialStatus = new SocialStatus();
	private String jobActionName = "none";
	
	public Resident(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money) {
		super(id, gender, foreigner, higherEducated, age, money);

		decisionMaker = new DecisionMaker();
	}
	
	public Resident(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money, int childrenWanted,
					int homelessTick, double nettoIncome, double necessaryCost, String jobTitle, Status status, int workplaceId) {
		
		super(id, gender, foreigner, higherEducated, age, money, nettoIncome, necessaryCost, status, workplaceId);

		this.childrenWanted = childrenWanted;
		this.jobActionName = jobTitle;
		this.homelessTick = homelessTick;
		socialStatus.setSocialStatusWork(status);
		if (status == Status.FISHER && workplaceId != -1) {
			
			Boat boat = (Boat) SimUtils.getPropertyById(workplaceId);
			boat.addFisher(id);
		}
		decisionMaker = new DecisionMaker();
	}

	/*=========================================
	 * Main human steps 
	 *=========================================
	 */
	
	public void stepAging() {
		addAge();
	}
	
	public void stepChildrenSchooling() {
		setPrimarySchool();
	}

	public void stepResetStandardCosts() {
		resetCostIndicators();
		equalizeMoneyWithPartner();
	}
	
	public void stepPayStandardCosts() {
		payStandardCosts();
	}
	
	public void stepDrainTanks() {

		decisionMaker.drainTanks();
	}

	public void stepWork() {
		
		retrieveAndShareSalary();
		if (getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_AGE) {
			socialStatus.setSocialStatusWork(getStatus());
		}
	}

	public void stepSelectWork() {
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_AGE) {
			return ;
		}

		ArrayList<String> possibleActions = getPossibleWorkActions(jobActionName);
		Logger.logInfo("H" + getId() + " possible actions: " + possibleActions);
		String actionToDo = null;
		ValuedAction selectedAction = selectActionFromPossibleActionsJob(possibleActions);
		if (selectedAction != null) {
			actionToDo = selectedAction.getTitle();
			decisionMaker.agentExecutesValuedAction(selectedAction);
		}
		
		Logger.logInfo("H" + getId() + " selected action: " + actionToDo);
		if (actionToDo != null) {
			ActionImplementation.executeActionJob(actionToDo, this);
		}
		else {
			Logger.logError("Error no action to execute");
		}
	}
	
	public void stepRelation() {

		if (isSingle() && getAge() >= Constants.HUMAN_ADULT_AGE && RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_RELATION) {
			for (final Resident resident: SimUtils.getObjectsAllExcluded(Resident.class, this)) {
				if (isSingle() && HumanUtils.isPotentialCouple(resident, this)) {
					if (!getAncestorsMatch(getAncestors(), resident.getAncestors())) {
						actionSetPartner(resident);
					}
					break;
				}
			}
		}
	}

	public void stepSocialEvent() {
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		EventHall eventHall = SimUtils.getEventHall();
		ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(getId());
		
		//Create possible actions
		ArrayList<String> possibleActions = new ArrayList<String>();
		if (eventHall.getVacancyForNewEvent() && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
			possibleActions.add("Organize free event");
			possibleActions.add("Organize commercial event");
		}
		for (Event event : possibleEvents) {
			if (event.getEventType().equals("Free") && !possibleActions.contains("Attend free event")) {
				possibleActions.add("Attend free event");
			}
			else if (event.getEventType().equals("Commercial") && !possibleActions.contains("Attend commercial event") && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
				possibleActions.add("Attend commercial event");
			}
		}
		
		if (possibleActions.size() == 0) {
			Logger.logInfo("H" + getId() + " event no possible actions");
			return ;
		}
		else {
			Logger.logInfo("H" + getId() + " event possible actions: " + possibleActions);
		}
		
		ArrayList<ValuedAction> filteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);
		ValuedAction selectedAction = filteredActions.get(0);
		String actionToDo = selectedAction.getTitle();

		if (actionToDo != null) {
			decisionMaker.agentExecutesValuedAction(selectedAction);
			ActionImplementation.executeActionEvent(actionToDo, this);
		}
		else {
			Logger.logError("H " + getId() + " Error no action to execute");
		}	
	}

	public void stepDonate() {
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add("Donate nothing");
		if ((getLeftoverMoney() > 0 && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) || getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			possibleActions.add("Donate to council");
		}
		Logger.logInfo("H" + getId() + " possible actions: " + possibleActions);

		ArrayList<ValuedAction> filteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);

		ValuedAction selectedAction = filteredActions.get(0);
		String actionToDo = selectedAction.getTitle();

		if (actionToDo != null) {
			decisionMaker.agentExecutesValuedAction(selectedAction);
			ActionImplementation.executeActionDonate(actionToDo, this);
		}
		else {
			Logger.logError("H " + getId() + " Error no action to execute");
		}
	}

	public void stepFamily() {

		if (!isSingle() && !isMan() && childrenWanted > 0 && getAge() < Constants.HUMAN_MAX_CHILD_GET_AGE && HumanUtils.isLivingTogetherWithPartner(this)
						&& RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_CHILD) {
			Human partner = getPartner();
			if (partner == null) {
				Logger.logError("Human.stepFamily(): partner = null");
			}
			actionGetChild(partner);
		}
	}
	
	public void stepHousing() {
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			
			socialStatus.setSocialStatusHouse(HumanUtils.getLivingPlaceType(this));
			return ;
		}
		
		if (RandomHelper.nextDouble() > Constants.HUMAN_PROB_GET_HOUSE) {
		
			if (!HumanUtils.isOwningHouse(this) && getStatus() != Status.UNEMPLOYED) {
				homelessTick ++;
				for (House house : SimUtils.getPropertyAvailableAllRandom(House.class)) {
					if (getMoney() > house.getPrice()) {
						actionBuyHouse(house);
						return;
					}
				}
			}
			
			House ownedHouse = HumanUtils.getOwnedHouse(this);
			if (ownedHouse != null) {
				// Sell house if in relationship and not single and owns a house
				if (!isSingle() && !HumanUtils.isLivingTogetherWithPartner(this)) { 
					actionSellHouse(ownedHouse);
					return;
				}
				if (getLeftoverMoney() < 0 && getMoney() < Constants.HUMAN_MONEY_DANGER_LEVEL) {
					if (ownedHouse.getHouseType() != HouseType.CHEAP) {
						actionSellHouse(ownedHouse);
					}
				}
			}
		}
		Logger.logDebug("H" + getId() + " check this human");
		socialStatus.setSocialStatusHouse(HumanUtils.getLivingPlaceType(this));
	}
	
	/**
	 * Removes the agent from the context and controls loosing of a partner
	 * etc.
	 */
	public void stepRemove() {
	
		if (doesHumanDie(getAge())) {		
			die();
			return ;
		}
		else if (getAge() == Constants.HUMAN_ELDERLY_CARE_AGE) {
			
			goToElderlyCare();
			return ;
		}
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE)
			return ;
		
		if (homelessTick >= Constants.HOMELESS_TICK) {
			actionMigrateOutOfTown();
			return ;
		}
		if (!decisionMaker.getIsSatisfied() && RandomHelper.nextDouble() < 0.00001 * (2 + decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION)))
		{
			Logger.logAction("H" + getId() + " moves out because he/she is not happy : " + 0.00001 * (2 + decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION)));
			Logger.logInfo("H" + getId() + getDcString());
			actionMigrateOutOfTown();
		}
	}
	
	public void stepLocation() {
		
		updateLocation();
	}

	/*=========================================
	 * Actions
	 *========================================
	 */
	
	private void actionBuyHouse(House house) {
		
		addMoney(-1 * house.getPrice());
		connectProperty(house.getId());
		Logger.logAction("H" + getId() + " bought house:" + HumanUtils.getOwnedHouse(this));
		homelessTick = 0;
	}
	
	private void actionSellHouse(House myHouse) {
		
		Logger.logAction("H" + getId() + " sells house");
		removeAndSellProperty(myHouse.getId(), true);
	}
	
	public void actionSetPartner(Resident newPartner) {
		Logger.logAction("H" + getId() + " got a relation with H" + newPartner.getId());
		setPartner(newPartner);
		newPartner.setPartner(this);
		
		if (!isMan()) {
			childrenWanted = calculateChildrenWanted();
		}
		else {
			newPartner.setChildrenWanted(calculateChildrenWanted());
		}
	}
	
	private void actionMigrateOutOfTown() {
		Logger.logAction("H" + getId() + " " + getStatus() + " migrates out of town");
		migrateOutOfTown();
	}
	
	public void actionGetChild(Human partner) {
		
		HumanUtils.spawnChild(this, partner);
		Logger.logAction("H" + getId() + "and H" + partner.getId() + " got a child");
		childrenWanted--;
	}

	public void actionFish(String fishingActionTitle) {
		
		ArrayList<String> fishingActions = new ArrayList<String>();
		fishingActions.add(fishingActionTitle);
		ArrayList<ValuedAction> evaluatedAction = decisionMaker.agentFilterActionsBasedOnValues(fishingActions);
		decisionMaker.agentExecutesValuedAction(evaluatedAction.get(0));
		socialStatus.setSocialStatusFisher(fishingActionTitle);
	}
	
	/*=========================================
	 * Other methods 
	 *=========================================
	 */
	
	public boolean getIsHappy() {
		if (socialStatus.getSocialStatusValue(decisionMaker, getStatus()) > 0.25 && decisionMaker.getSatisfiedValuesCount() >= 2) {
			return true;
		}
		return false;
	}
	
	/**
	 * Select fishing action based on the decisionMaker filter on actions
	 * from the filteredActions, the best actions are selected (so the actions with the highest
	 * same value). A random action from this best actions is choosen
	 * @return
	 */
	public String selectFishingAction() {
		
		ArrayList<String> possibleActions = getFishingActions();
		ArrayList<ValuedAction> filteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);
		ArrayList<ValuedAction> bestActions = new ArrayList<ValuedAction>();
		for (ValuedAction valuedAction : filteredActions) {
			if (bestActions.size() == 0) {
				bestActions.add(valuedAction);
			}
			else if (bestActions.get(0).getActionGoodness() == valuedAction.getActionGoodness()) {
				bestActions.add(valuedAction);
			}
		}
		if (filteredActions.size() > 0) {
			return bestActions.get(RandomHelper.nextIntFromTo(0, bestActions.size() - 1)).getTitle();
		}
		Logger.logError("H" + getId() + " no selected actions from possible actions:" + possibleActions);
		return "ERROR";
	}

	public ArrayList<String> getFishingActions() {
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		if (!SimUtils.getEcosystem().fishInDanger()) {
			possibleActions.add("Fish a lot");
			possibleActions.add("Fish medium");
			possibleActions.add("Fish less");
		}
		else {
			possibleActions.add("Fish a lot danger");
			possibleActions.add("Fish medium danger");
			possibleActions.add("Fish less danger");
		}
		return possibleActions;
	}

	private int calculateChildrenWanted() {
		double y = 7 - (1.0/15.0) * (double) SimUtils.getCouncil().getNumberOfPeople();
		return Math.max(Constants.HUMAN_CHILDREN_WANTED_MIN, Math.min(Constants.HUMAN_CHILDREN_WANTED_MAX, (int) Math.round(y)));
	}
	
	private ValuedAction selectActionFromPossibleActionsJob(ArrayList<String> possibleActions) {
		
		ArrayList<ValuedAction> filteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);
		//Remove unemployed if there are more options
		if (filteredActions.size() >= 2) {
			for (ValuedAction valuedAction : filteredActions) {
				if (valuedAction.getTitle().equals("Job unemployed")) {
					filteredActions.remove(valuedAction);
					break;
				}
			}
		}
		
		ValuedAction selectedAction = null;
		// Select based on highest social status
		double bestValue = -1;
		for (ValuedAction filteredAction : filteredActions) {
			if (Status.getEnumByString(filteredAction.getTitle()).getSocialStatusWork() > bestValue) {
				selectedAction = filteredAction;
				bestValue = Status.getEnumByString(filteredAction.getTitle()).getSocialStatusWork();
			}
		}
		// Keep the previous job if it is in the filteredActions
		if (jobActionName != "Job unemployed" && RandomHelper.nextDouble() > Constants.HUMAN_PROB_KEEP_PREV_JOB) {
			for (ValuedAction valuedAction : filteredActions) {
				if (valuedAction.getTitle().equals(jobActionName)) {
					selectedAction = valuedAction;
					break;
				}
			}
		}
		
		Logger.logInfo("H" + getId() + " jobTitle: " + jobActionName + ", selected action:" + selectedAction + " from actions: " + filteredActions);
		return selectedAction;
	}
	
	/*=========================================
	 * Getters and setters
	 *=========================================
	 */
	public int getChildrenWanted() {
		return childrenWanted;
	}
	
	public int getHomelessTick() {
		return homelessTick;
	}
	
	public double getSocialStatusValue() {
		return socialStatus.getSocialStatusValue(decisionMaker, getStatus());
	}
	
	public double getSocialStatusWork() {
		return socialStatus.getSocialStatusWork();
	}
	
	public double getSocialStatusHouse() {
		return socialStatus.getSocialStatusHouse();
	}
	
	public double getSocialStatusFishEcol() {
		return socialStatus.getSocialStatusFishEcol();
	}
	
	public double getSocialStatusFishEcon() {
		return socialStatus.getSocialStatusFishEcon();
	}
	
	public SocialStatus getSocialStatus() {
		return socialStatus;
	}
	
	public String getJobActionName() {
		return jobActionName;
	}
	
	public void setJobActionName(String jobActionName) {
		this.jobActionName = jobActionName;
	}
	
	public double getUniversalismImportanceDistribution() {
		return decisionMaker.getUniversalismImportanceDistribution();
	}
	
	public double getLevelUniversalism() {
		return decisionMaker.getWaterTankLevel(AbstractValue.UNIVERSALISM.name());
	}
	
	public double getLevelTradition() {
		return decisionMaker.getWaterTankLevel(AbstractValue.TRADITION.name());
	}
	
	public double getLevelSelfDirection() {
		return decisionMaker.getWaterTankLevel(AbstractValue.SELFDIRECTION.name());
	}
	
	public double getLevelPower() {
		return decisionMaker.getWaterTankLevel(AbstractValue.POWER.name());
	}
	
	public int getSatisfiedValuesCount() {
		return decisionMaker.getSatisfiedValuesCount();
	}
	
	public void setChildrenWanted(int childrenWanted) {
		this.childrenWanted = childrenWanted;
	}

	/*=========================================
	 * Print stuff
	 *=========================================
	 */
	public String getDcString() {
		return decisionMaker.toString();
	}
	
	public String getHumanVarsAsString() { 

		return getId() + "," + (!isMan()) + "," + getForeigner() + "," + getHigherEducated() + "," + getAge() + "," + getMoney() + "," + childrenWanted +
			   "," + homelessTick + "," + getNettoIncome() + "," + getNecessaryCost() + "," + jobActionName + "," + getStatus().name() + "," + getWorkplaceId(); 
	}
}