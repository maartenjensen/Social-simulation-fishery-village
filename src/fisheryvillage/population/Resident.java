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
import valueframework.DecisionMaker;
import valueframework.ValuedAction;
import valueframework.WaterTank;

/**
 * This class extends the human class with decision making options
 * @author Maarten
 *
 */
public final class Resident extends Human {

	// Variable declaration (initialization in constructor)
	private DecisionMaker decisionMaker;
	
	// Variable initialization
	private int homelessTick = 0;
	private int childrenWanted = 0;
	private SocialStatus socialStatus = new SocialStatus(0, 0);
	private String jobActionName = "none";
	
	public Resident(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money) {
		super(id, gender, foreigner, higherEducated, age, money);

		decisionMaker = new DecisionMaker();
	}
	
	public Resident(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money, int childrenWanted,
					int homelessTick, double nettoIncome, double necessaryCost, String jobTitle, Status status, int boatId) {
		
		super(id, gender, foreigner, higherEducated, age, money, homelessTick, childrenWanted, nettoIncome, necessaryCost, jobTitle, status);

		this.childrenWanted = childrenWanted;
		socialStatus.setSocialStatusWork(status);
		if (status == Status.FISHER && boatId != -1) {
			
			Boat boat = (Boat) SimUtils.getPropertyById(boatId);
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
	
	public void stepSocialStatusDrain() {
		socialStatus.drainSocialStatus();
	}
	
	public void stepDrainTanks() {
		 	
		//Logger.logProb("VALUES BEFOR H" + id + " - " + getDcString(), 0.05);
		decisionMaker.drainTanks();
		//Logger.logProb("VALUES AFTER H" + id + " - " + getDcString(), 0.05);
	}
	
	public void stepWork() {
		
		retrieveAndShareSalary();
		// Set social status
		//socialStatus.setSocialStatusWork(status);
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
		
		WaterTank waterTank = decisionMaker.mostImportantValue();
		EventHall eventHall = SimUtils.getEventHall();
		ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(getId());
		
		switch(waterTank.getRelatedAbstractValue()) {
		case "Tradition":
			if (possibleEvents.size() >= 1) {
				Logger.logAction("JOIN EVENT - H" + getId() + " Tradition");
				Event eventToJoin = possibleEvents.get(RandomHelper.nextIntFromTo(0, possibleEvents.size() - 1));
				addMoney(-1 * eventHall.joinEvent(eventToJoin, getId()));
				waterTank.increasingLevel(0.2);
				eventHall.increaseTradition();
			}
			break;
		case "Power":
			if (socialStatus.getBelowAverage()) {
				
				if (eventHall.getVacancyForNewEvent() && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
					Logger.logAction("CREATE EVENT C - H" + getId() + " Power");
					addMoney(-1 * eventHall.createEvent("Commercial", getId()));
					waterTank.increasingLevel(2);
					eventHall.increasePower();
				}
				else {
					if (possibleEvents.size() >= 1) {
						Logger.logAction("JOIN EVENT - H" + getId() + " Power");
						Event eventToJoin = possibleEvents.get(RandomHelper.nextIntFromTo(0, possibleEvents.size() - 1));
						addMoney(-1 * eventHall.joinEvent(eventToJoin, getId()));
						waterTank.increasingLevel(0.2);
						eventHall.increasePower();
					}
				}
			}
			break;
		case "Self-direction":
			if (eventHall.getVacancyForNewEvent() && getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
				if (RandomHelper.nextDouble() < 0.5) {
					Logger.logAction("CREATE EVENT F - H" + getId() + " Self-direction");
					addMoney(-1 * eventHall.createEvent("Free", getId()));
				}
				else {
					Logger.logAction("CREATE EVENT C - H" + getId() + " Self-direction");
					addMoney(-1 * eventHall.createEvent("Commercial",  getId()));	
				}
				eventHall.increaseSelfDirection();
				waterTank.increasingLevel(2);
			}
			break;
		case "Universalism":
			if (socialStatus.getUnsatisfiedUniversalist() && eventHall.getVacancyForNewEvent()) {
				Logger.logAction("CREATE EVENT F - H" + getId() + " Universalism");
				addMoney(-1 * eventHall.createEvent("Free", getId()));
				waterTank.increasingLevel(2);
				eventHall.increaseUniversalism();
			}
			break;
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
		String actionToDo = "Donate nothing";
		actionToDo = selectActionFromPossibleActionsDonate(possibleActions);

		if (actionToDo != null) {
			ActionImplementation.executeActionDonate(actionToDo, this);
		}
		else {
			Logger.logError("Error no action to execute");
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
		
		if (RandomHelper.nextDouble() > Constants.HUMAN_PROB_GET_HOUSE || getAge() < Constants.HUMAN_ADULT_AGE || getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE)
			return ;
		
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
	
	/**
	 * Removes the agent from the context and controls loosing of a partner
	 * etc.
	 */
	public void stepRemove() {
	
		if (getAge() > Constants.HUMAN_MAX_LIVING_AGE) { //TODO make this a proper distribution
			
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
		if (!decisionMaker.getIsSatisfied() && RandomHelper.nextDouble() < 0.00001 * (2 + decisionMaker.getSelfDirectionThreshold()))
		{
			Logger.logAction("H" + getId() + " moves out because he/she is not happy : " + 0.00001 * (2 + decisionMaker.getSelfDirectionThreshold()));
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
	
	public void actionSocialEventAttend() {
		socialStatus.setEventAttendee();
	}
	
	public void actionSocialEventOrganize() {
		socialStatus.setEventOrganizer();
	}
	
	public void actionGetChild(Human partner) {
		
		HumanUtils.spawnChild(this, partner);
		Logger.logAction("H" + getId() + "and H" + partner.getId() + " got a child");
		childrenWanted--;
	}
	
	/*=========================================
	 * Other methods 
	 *=========================================
	 */
	
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
		// Select a random job from the filtered jobs, but keep previous job if it is not unemployed
		ValuedAction selectedAction = filteredActions.get(RandomHelper.nextIntFromTo(0, filteredActions.size() - 1));
		if (jobActionName != "Job unemployed") {
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
	
	private String selectActionFromPossibleActionsDonate(ArrayList<String> possibleActions) {
		
		if (possibleActions.size() == 0) {
			return null;
		}
		
		WaterTank waterTank = decisionMaker.mostImportantValue();
		if (possibleActions.size() == 1){
			if (waterTank.getRelatedAbstractValue().equals("Power")) {
				Logger.logInfo("DONATE NOT - H" + getId() + " Power (no money)");
				waterTank.increasingLevel(0.2);
				SimUtils.getCouncil().increasePower();
			}
			else if (waterTank.getRelatedAbstractValue().equals("Self-direction")) {
				Logger.logInfo("DONATE NOT - H" + getId() + " Self-direction (no money)");
				waterTank.increasingLevel(0.2);
				SimUtils.getCouncil().increaseSelfDirection();
			}
			return "Donate nothing";
		}
		
		switch(waterTank.getRelatedAbstractValue()) {
		case "Tradition":
			Logger.logInfo("DONATE - H" + getId() + " Tradition");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseTradition();
			return "Donate to council";
		case "Power":
			Logger.logInfo("DONATE NOT - H" + getId() + " Power");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increasePower();
			return "Donate nothing";
		case "Self-direction":
			Logger.logInfo("DONATE NOT - H" + getId() + " Self-direction");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseSelfDirection();
			return "Donate nothing";
		case "Universalism":
			Logger.logInfo("DONATE - H" + getId() + " Universalism");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseUniversalism();
			return "Donate to council";
		default:
			Logger.logError("Error no correct value from watertank: " + waterTank.getRelatedAbstractValue());
		}	
		return "Donate nothing"; 
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
	
	public double getSocialLevel() {
		return socialStatus.getSocialLevel();
	}
	
	public double getSocialLevelWork() {
		return socialStatus.getWorkLevel();
	}
	
	public double getSocialLevelEvent() {
		return socialStatus.getEventLevel();
	}
	
	public int getSocialLevelCombined() {
		return socialStatus.getCombinedLevel();
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
		return decisionMaker.getWaterTankLevel("Universalism");
	}
	
	public double getLevelTradition() {
		return decisionMaker.getWaterTankLevel("Tradition");
	}
	
	public double getLevelSelfDirection() {
		return decisionMaker.getWaterTankLevel("Self-direction");
	}
	
	public double getLevelPower() {
		return decisionMaker.getWaterTankLevel("Power");
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
		int boatId = -1;
		if (getStatus() == Status.FISHER) {
			boatId = SimUtils.getBoatByHumanId(getId()).getId();
		}
		return getId() + "," + isMan() + "," + getForeigner() + "," + getHigherEducated() + "," + getAge() + "," + getMoney() + "," + childrenWanted +
			   "," + homelessTick + "," + getNettoIncome() + "," + getNecessaryCost() + "," + jobActionName + "," + getStatus().name() + "," + boatId; 
	}
}
