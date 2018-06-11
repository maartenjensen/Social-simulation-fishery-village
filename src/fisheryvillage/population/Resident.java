package fisheryvillage.population;

import java.util.ArrayList;
import java.util.List;

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
	private int childrenWanted = -1;
	private SocialStatus socialStatus = new SocialStatus();
	private String jobActionName = "none";
	private ValuedAction eventAction = null;
	private boolean canOrganize = false;

	public Resident(int id, boolean gender, boolean foreigner, int age, double money) {
		super(id, gender, foreigner, age, money);

		decisionMaker = new DecisionMaker();
		//initDecisionMakerWaterTanks();
	}

	public Resident(int id, boolean gender, boolean foreigner, boolean hasBeenFisher, int age, double money, int childrenWanted,
					double nettoIncome, double necessaryCost, String jobTitle, Status status, int workplaceId) {
		
		super(id, gender, foreigner, hasBeenFisher, age, money, nettoIncome, necessaryCost, status, workplaceId);

		this.childrenWanted = childrenWanted;
		this.jobActionName = jobTitle;
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
		updateValueThreshold();
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

		if (getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_CARE_AGE)
			decisionMaker.drainTanks();
	}

	public void stepWork() {
		
		retrieveAndShareSalary();
		if (getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_AGE) {
			socialStatus.setSocialStatusWork(getStatus());
			if (getStatus() == Status.CAPTAIN || getStatus() == Status.FISHER) {
				socialStatus.setSocialStatusBoat(SimUtils.getBoatByHumanId(getId()).getBoatType());
			}
		}
	}

	public void stepSelectWork() {
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_AGE) {
			return ;
		}

		ArrayList<String> possibleActions = new ArrayList<String>();
		if (jobActionName.equals("none") || Constants.HUMAN_PROB_SEARCH_NEW_JOB <= RandomHelper.nextDouble()) {
			possibleActions = getPossibleWorkActions(jobActionName);
		}
		else {
			possibleActions.add(jobActionName);
		}
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
			Logger.logError("H" + getId() + "Error no action to execute");
		}
	}

	public void stepRelation() {

		if (isSingle() && getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_CARE_AGE && RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_RELATION) {
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
		eventAction = null;
		canOrganize = false;
		
		//Create possible actions
		ArrayList<String> possibleActions = new ArrayList<String>();
		if (eventHall.getVacancyForNewEvent() && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
			possibleActions.add("Organize free event");
			possibleActions.add("Organize commercial event");
			canOrganize = true;
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
		ValuedAction selectedAction = socialStatus.getBestActionEvent(decisionMaker, filteredActions, canOrganize);
		String actionToDo = selectedAction.getTitle();

		if (actionToDo != null) {
			eventAction = selectedAction;
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

		ValuedAction selectedAction = socialStatus.getBestActionDonate(decisionMaker, filteredActions);
		String actionToDo = selectedAction.getTitle();

		if (actionToDo != null) {
			decisionMaker.agentExecutesValuedAction(selectedAction);
			ActionImplementation.executeActionDonate(actionToDo, this);
			socialStatus.setSocialStatusDonation(actionToDo);
		}
		else {
			Logger.logError("H " + getId() + " Error no action to execute");
		}
	}

	public void stepFamily() {

		if (!isSingle() && !isMan() && childrenWanted > 0 && getAge() < Constants.HUMAN_MAX_CHILD_GET_AGE && HumanUtils.isLivingTogetherWithPartner(this)
						&& RandomHelper.nextDouble() <= Constants.HUMAN_PROB_GET_CHILD) {
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
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE)
			return ;
		
		if (!getIsHappy() && RandomHelper.nextDouble() < 0.00001 * (2 + decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION)))
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
	}
	
	private void actionSellHouse(House myHouse) {
		
		Logger.logAction("H" + getId() + " sells house");
		removeAndSellProperty(myHouse.getId(), true);
	}
	
	public void actionSetPartner(Resident newPartner) {
		Logger.logAction("H" + getId() + " got a relation with H" + newPartner.getId());
		setPartner(newPartner);
		newPartner.setPartner(this);
		
		if (!isMan() && childrenWanted == -1) {
			childrenWanted = calculateChildrenWanted();
		}
		else if (newPartner.getChildrenWanted() == -1){
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
	
	public void actionEventOrganize(int profit) {
		if (eventAction != null) {
			decisionMaker.agentExecutesValuedAction(eventAction);
			socialStatus.setSocialStatusEvent(eventAction.getTitle(), canOrganize);
			addMoney(profit);
		}
		else {
			Logger.logError("H" + getId() + " has no event action");
		}
	}
	
	public void actionEventAttend(int fee) {
		if (eventAction != null) {
			decisionMaker.agentExecutesValuedAction(eventAction);
			socialStatus.setSocialStatusEvent(eventAction.getTitle(), canOrganize);
			addMoney(-1 * fee);
		}
		else {
			Logger.logError("H" + getId() + " has no event action");
		}
	}
	
	/*=========================================
	 * Other methods 
	 *=========================================
	 */
	/*
	private void initDecisionMakerWaterTanks() {
		
		String tradition = Integer.toString(RepastParam.getTradition());
		String power = Integer.toString(RepastParam.getPower());
		String universalism = Integer.toString(RepastParam.getUniversalism());
		String selfdirection = Integer.toString(RepastParam.getSelfDirection());
		
		List<String> data = new ArrayList<String>();
		data.add(0, Integer.toString(getId()));
		data.add(1, "TRADITION");
		data.add(2, tradition);
		data.add(3, tradition);
		data.add(4, "POWER");
		data.add(5, power);
		data.add(6, power);
		data.add(7, "UNIVERSALISM");
		data.add(8, universalism);
		data.add(9, universalism);
		data.add(10, "SELFDIRECTION");
		data.add(11, selfdirection);
		data.add(12, selfdirection);
		decisionMaker.setImportantWaterTankFromData(data);
	}*/
	
	private void updateValueThreshold() {
		
		if (getAge() < Constants.SCHWARTZ_CHANGE_MIN_AGE || getAge() > Constants.SCHWARTZ_MAX)
			return ;
		decisionMaker.adjustWaterTankThreshold(AbstractValue.SELFDIRECTION.name(), Constants.SCHWARTZ_CHANGE_SELF, Constants.SCHWARTZ_MIN, Constants.SCHWARTZ_MAX);
		decisionMaker.adjustWaterTankThreshold(AbstractValue.TRADITION.name(), Constants.SCHWARTZ_CHANGE_TRAD, Constants.SCHWARTZ_MIN, Constants.SCHWARTZ_MAX);
	}
	
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
		if (filteredActions.size() >= 1)
			return socialStatus.getBestActionFish(decisionMaker, filteredActions).getTitle();
		
		Logger.logError("H" + getId() + " no filtered actions");
		return "EMPTY";
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
		ValuedAction selectedAction = socialStatus.getBestActionWork(decisionMaker, filteredActions);
		
		// Keep the previous job if it is in the filteredActions
		if (!jobActionName.equals("Job unemployed") && RandomHelper.nextDouble() > Constants.HUMAN_PROB_KEEP_PREV_JOB) {
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
	
	public void setSocialStatusFromData(List<String> data) {
		socialStatus.setSocialStatusFromData(data);
	}

	public String socialStatusString() {
		return socialStatus.getSocialStatusString();
	}

	public void setImportantWaterTankFromData(List<String> data) {
		decisionMaker.setImportantWaterTankFromData(data);
	}
	
	public String importantWaterTankData() {
		return decisionMaker.importantData();
	}
	
	/*=========================================
	 * Getters and setters
	 *=========================================
	 */
	public int getChildrenWanted() {
		return childrenWanted;
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
	
	public double getSocialStatusBoat() {
		return socialStatus.getSocialStatusBoat();
	}
	
	public double getSocialStatusFishEcol() {
		return socialStatus.getSocialStatusFishEcol();
	}
	
	public double getSocialStatusFishEcon() {
		return socialStatus.getSocialStatusFishEcon();
	}
	
	public double getSocialStatusEvents() {
		return socialStatus.getSocialStatusEvents();
	}
	
	public double getSocialStatusEventsFree() {
		return socialStatus.getSocialStatusOrganizeFree();
	}
	
	public double getSocialStatusDonation() {
		return socialStatus.getSocialStatusDonation();
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

		return getId() + "," + (!isMan()) + "," + getForeigner() + "," + getHasBeenFisher() + "," + getAge() + "," + getMoney() + "," + childrenWanted +
			   "," + getNettoIncome() + "," + getNecessaryCost() + "," + jobActionName + "," + getStatus().name() + "," + getWorkplaceId(); 
	}
}