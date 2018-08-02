package fisheryvillage.population;

import java.util.ArrayList;
import java.util.List;

import fisheryvillage.batch.BatchRun;
import fisheryvillage.batch.RunningCondition;
import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.Property;
import fisheryvillage.property.municipality.Event;
import fisheryvillage.property.municipality.EventHall;
import fisheryvillage.property.municipality.Factory;
import repast.simphony.engine.environment.RunEnvironment;
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
	private int graphDonateType = -1; //-1: undefined, 0: donation not possible, 1: not donate, 2: donate to council
	private int graphEventType = -1; //-1: undefined, 0: no action possible, 1:OF, 2:OC, 3:AF, 4:AC
	
	public Resident(int id, boolean gender, boolean foreigner, int age, double money) {
		super(id, gender, foreigner, age, money);

		decisionMaker = new DecisionMaker();
		//initDecisionMakerWaterTanks();
	}

	public Resident(int id, boolean gender, boolean foreigner, boolean hasBeenFisher, int age, double money, int childrenWanted,
					double nettoIncome, double necessaryCost, String jobTitle, Status status, int workplaceId, int notHappyTick) {
		
		super(id, gender, foreigner, hasBeenFisher, age, money, nettoIncome, necessaryCost, status, workplaceId, notHappyTick);

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
	public void stepSaveCurrentData() {
		
		if (!BatchRun.getEnable()) {
			double self_dir = decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION);
			double tradition = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
			double calculated_tick = 4 + (24 * Math.min(1, Math.max(0, (50 + tradition - self_dir) * 0.01)));
			
			String datum = getHumanVarsAsString() + "," + calculated_tick + "," + getSocialStatusValue() + "," + getPartnerId() + "," + getSalaryTaxedData() + "," + getHasEnoughMoney() + "," + HumanUtils.getChildrenUnder18(this).size() + "," + HumanUtils.getLivingPlaceType(this).name();
			datum += "," + getThresholds();
			int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			agentInfo.add(tick + "," + datum + "," + socialStatusString());
		}
	}
	
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

		if (getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_CARE_AGE) {
			
			setNotHappyTick(getIsHappy());
			decisionMaker.drainTanks();
		}
		else {
			setNotHappyTick(true);
		}
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

		//double self_dir = decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION);
		ArrayList<String> possibleActions = new ArrayList<String>();
		if (jobActionName.equals("none") || (Constants.HUMAN_PROB_SEARCH_NEW_JOB <= RandomHelper.nextDouble() && (!getIsHappy() || jobActionName.equals("Job unemployed")) ) )  { //getNotHappyTick() > 0.1 * (100 - self_dir) 
			possibleActions = getPossibleWorkActions(jobActionName); 
		}
		else {
			//possibleActions.add(jobActionName);
			possibleActions = getExtraWorkActions(jobActionName);
		}
		Logger.logInfo("H" + getId() + " possible actions: " + possibleActions);
		String actionToDo = null;
		ValuedAction selectedAction = selectActionFromPossibleActionsJob(possibleActions);
		if (selectedAction != null) {
			actionToDo = selectedAction.getTitle();
			decisionMaker.agentExecutesValuedAction(selectedAction, Constants.TICKS_PER_MONTH);
		}

		Logger.logInfo("H" + getId() + " selected action: " + actionToDo);
		if (actionToDo != null) {
			ActionImplementation.executeActionJob(actionToDo, this);
		}
		else {
			Logger.logError("H" + getId() + "Error no action to execute");
		}
	}

	private ArrayList<String> getExtraWorkActions(String jobName) {
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add(jobName);
		Property workplace = HumanUtils.getWorkingPlace(getWorkplaceId(), getStatus(), getSchoolType());
		if (workplace != null) {
			if (workplace instanceof Boat) {
				Boat boat = (Boat) workplace;
				ArrayList<Status> jobs = boat.getVacancy(false, getMoney());
				if (jobs.contains(Status.CAPTAIN) && jobName != "Job captain") {
					possibleActions.add("Job captain");
					Logger.logAction("H" + getId() + ", B" + boat.getId() + " add captain as possible job to fisher since there is no captain");
				}
			}
			else if (workplace instanceof Factory) {
				Factory factory = (Factory) workplace;
				ArrayList<Status> jobs = factory.getVacancy(false, getMoney());
				if (jobs.contains(Status.FACTORY_BOSS) && jobName != "Job factory boss") {
					possibleActions.add("Job factory boss");
					Logger.logAction("H" + getId() + ", Factory add Boss as possible job for factory worker since there is no Boss");
				}
			}
		}
		return possibleActions;
	}
	
	public void stepRelation() {

		if (isSingle() && getAge() >= Constants.HUMAN_ADULT_AGE && getAge() < Constants.HUMAN_ELDERLY_CARE_AGE && RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_RELATION && getPartnerId() != -2) {
			for (final Resident resident: SimUtils.getObjectsAllExcluded(Resident.class, this)) {
				if (isSingle() && HumanUtils.isPotentialCouple(resident, this) && getPartnerId() != -2) {
					if (!getAncestorsMatch(getAncestors(), resident.getAncestors())) {
						actionSetPartner(resident);
					}
					break;
				}
			}
		}
	}

	public void stepSocialEvent() {
		
		graphEventType = -1;
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		if ((getLeftoverMoney() <= 0 || getMoney() <= Constants.MONEY_DANGER_LEVEL) && getMoney() <= Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			graphEventType = 0;
			return ;
		}
		
		EventHall eventHall = SimUtils.getEventHall();
		ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(getId());
		eventAction = null;
		canOrganize = false;
		
		//Create possible actions
		ArrayList<String> possibleActions = new ArrayList<String>();
		if (eventHall.getVacancyForNewEvent() && getMoney() > Constants.MONEY_DANGER_LEVEL) {
			possibleActions.add("Organize free event");
			possibleActions.add("Organize commercial event");
			canOrganize = true;
		}
		for (Event event : possibleEvents) {
			if (event.getEventType().equals("Free") && !possibleActions.contains("Attend free event")) {
				possibleActions.add("Attend free event");
			}
			else if (event.getEventType().equals("Commercial") && !possibleActions.contains("Attend commercial event") && getMoney() > Constants.MONEY_DANGER_LEVEL) {
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
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE
				|| BatchRun.getRunningCondition() == RunningCondition.NO_DONATION
				|| BatchRun.getRunningCondition() == RunningCondition.NO_EV_AND_DON) {
			return ;
		}
		
		graphDonateType = -1;
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add("Donate nothing");
		if ((getLeftoverMoney() > 0 && getMoney() > Constants.MONEY_DANGER_LEVEL) || getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			possibleActions.add("Donate to council");
		}
		else {
			Logger.logInfo("H" + getId() + " donation not possible, not enough money or income");
			graphDonateType = 0;
			return ;
		}
		Logger.logAction("H" + getId() + " possible actions: " + possibleActions);

		ArrayList<ValuedAction> filteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);

		ValuedAction selectedAction = socialStatus.getBestActionDonate(decisionMaker, filteredActions);
		String actionToDo = selectedAction.getTitle();

		if (actionToDo != null) {
			decisionMaker.agentExecutesValuedAction(selectedAction, 1);
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
				if (getLeftoverMoney() < 0 && getMoney() < Constants.MONEY_DANGER_LEVEL) {
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
			SimUtils.getDataCollector().addDied();
			die();
			return ;
		}
		
		if (getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE)
			return ;
		
		double self_dir = decisionMaker.getAbstractValueThreshold(AbstractValue.SELFDIRECTION);
		double tradition = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
		double calculated_tick = 4 + (24 * Math.min(1, Math.max(0, (50 + tradition - self_dir) * 0.01)));
		Logger.logInfo("H" + getId() + ", self-dir:" + self_dir + ", tradition:" + tradition + ", not_happy_tick:" + getNotHappyTick() + ", calculated tick:" + calculated_tick);
		if (getNotHappyTick() >= calculated_tick && RandomHelper.nextDouble() <= Constants.MIGRATE_CHANCE)
		{
			Logger.logAction("H" + getId() + " moves out because he/she is not happy tick: " + getNotHappyTick() + ", self-dir:" + self_dir);
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
		
		if (!isMan()) {
			if (childrenWanted == -1) {
				childrenWanted = calculateChildrenWanted();
			}
		}
		else if (!newPartner.isMan()){
			if (newPartner.getChildrenWanted() == -1) {
				newPartner.setChildrenWanted(calculateChildrenWanted());
			}
		}
	}
	
	private void actionMigrateOutOfTown() {
		Logger.logAction("H" + getId() + " " + getStatus() + " migrates out of town");
		SimUtils.getDataCollector().addMigratorOut(true, getId());
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
		decisionMaker.agentExecutesValuedAction(evaluatedAction.get(0), 1);
		socialStatus.setSocialStatusFisher(fishingActionTitle);
	}
	
	public void actionEventOrganize(int profit) {
		if (eventAction != null) {
			decisionMaker.agentExecutesValuedAction(eventAction, 1);
			socialStatus.setSocialStatusEvent(eventAction.getTitle(), canOrganize);
			addMoney(profit);
		}
		else {
			Logger.logError("H" + getId() + " has no event action");
		}
	}
	
	public void actionEventAttend(int fee) {
		if (eventAction != null) {
			decisionMaker.agentExecutesValuedAction(eventAction, 1);
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

	public boolean getIsHappy() {
		if (socialStatus.getSocialStatusValue(decisionMaker, getStatus()) > 0.25 && decisionMaker.getSatisfiedValuesCount() >= BatchRun.getValuesSatisfiedForHappy()) {
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

		//double y = 7 - (1.0/15.0) * (double) SimUtils.getCouncil().getNumberOfPeople();
		//return Math.max(Constants.HUMAN_CHILDREN_WANTED_MIN, Math.min(Constants.HUMAN_CHILDREN_WANTED_MAX, (int) Math.round(y)));
		if (RandomHelper.nextDouble() < Constants.GET_NO_CHILDREN) {
			return 0;
		}
		else {
			return RandomHelper.nextIntFromTo(1, Constants.HUMAN_CHILDREN_WANTED_MAX);
		}
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
	
	public boolean getHasEnoughMoney() {
		if ((getLeftoverMoney() > 0 && getMoney() > Constants.MONEY_DANGER_LEVEL) || getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			return true;
		}
		else {
			return false;
		}
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
	
	public double getThreshold(AbstractValue abstractValue) {
		return decisionMaker.getWaterTankThreshold(abstractValue);
	}
	
	public int getSatisfiedValuesCount() {
		return decisionMaker.getSatisfiedValuesCount();
	}
	
	public void setChildrenWanted(int childrenWanted) {
		this.childrenWanted = childrenWanted;
	}
	
	/*=========================================
	 * Graph variables
	 * ========================================
	 */
	public void setGraphDonateType(int graphDonateType) {
		this.graphDonateType = graphDonateType;
	}
	
	public int getGraphDonateType() {
		return graphDonateType;
	}
	
	public void setGraphEventType(int graphEventType) {
		this.graphEventType = graphEventType;
	}
	
	public int getGraphEventType() {
		return graphEventType;
	}
	
	/*=========================================
	 * Print stuff
	 *=========================================
	 */
	public String getThresholds() {
		return decisionMaker.getThresholds();
	}
	
	public String getDcString() {
		return decisionMaker.toString();
	}
	
	public String getHumanVarsAsString() { 

		return getId() + "," + (!isMan()) + "," + getForeigner() + "," + getHasBeenFisher() + "," + getAge() + "," + getMoney() + "," + childrenWanted +
			   "," + getNettoIncome() + "," + getNecessaryCost() + "," + jobActionName + "," + getStatus().name() + "," + getWorkplaceId() + "," + getNotHappyTick(); 
	}
}