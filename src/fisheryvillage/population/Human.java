package fisheryvillage.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import common.FrameworkBuilder;
import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.municipality.Council;
import fisheryvillage.municipality.Event;
import fisheryvillage.municipality.EventHall;
import fisheryvillage.property.Boat;
import fisheryvillage.property.Factory;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.Property;
import fisheryvillage.property.School;
import mas.DecisionMaker;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;
import valueFramework.WaterTank;

/**
* The human class, without it the village would be a ghost town
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Human {

	// Variable declaration (initialization in constructor)
	private final boolean gender; // man = false; woman = true;
	private final int id;
	private int age;
	private double money;
	private int childrenWanted;
	private final boolean foreigner;
	private DecisionMaker decisionMaker;

	// Variable initialization
	private ArrayList<GridPoint> ancestors = new ArrayList<GridPoint>();
	private HashMap<Status, VSpatial> spatialImages = new HashMap<Status, VSpatial>();
	private int homelessTick = 0;
	private Status status = Status.UNEMPLOYED;
	private SchoolType schoolType = SchoolType.NO_SCHOOL;
	private double nettoIncome = 0;
	private double necessaryCost = 0;
	private double salaryUntaxed = 0;
	private SocialStatus socialStatus = new SocialStatus(0, 0);
	private String jobTitle = "NONE";

	public Human(boolean gender, int age, int id, double money, boolean foreigner) {
		
		this.gender = gender;
		this.age = age;
		this.id = id;
		this.money = money;
		this.foreigner = foreigner;
		this.childrenWanted = RandomHelper.nextIntFromTo(Constants.HUMAN_MIN_CHILDREN_WANTED, Constants.HUMAN_MAX_CHILDREN_WANTED);
		setStatusByAge();
		this.schoolType = SchoolType.NO_SCHOOL;

		decisionMaker = new DecisionMaker(id);
		FrameworkBuilder.decisionMakerList.add(decisionMaker);
		
		decisionMaker.assignPossibleActions(FrameworkBuilder.allPossibleActions);
		decisionMaker.createValueTrees();
		
		SimUtils.getContext().add(this);
		
		final NdPoint pt = SimUtils.getSpace().getLocation(this);
		if (!SimUtils.getGrid().moveTo(this, (int) pt.getX(), (int) pt.getY())) {
			Logger.logError("Human could not be placed, coordinate: " + pt.toString());
		}
	}
	
	public Human(boolean gender, int age, int id, double money, boolean foreigner, int childrenWanted,
				 int homelessTick, double nettoIncome, double necessaryCost, String jobTitle, Status status, int boatId) {
		
		this.gender = gender;
		this.age = age;
		this.id = id;
		this.money = money;
		this.foreigner = foreigner;
		this.childrenWanted = childrenWanted;
		setStatusByAge();
		this.schoolType = SchoolType.NO_SCHOOL;
		this.homelessTick = homelessTick;
		this.nettoIncome = nettoIncome;
		this.necessaryCost = necessaryCost;
		this.jobTitle = jobTitle;
		this.status = status;
		socialStatus.setSocialStatusWork(status);
		if (status == Status.FISHER && boatId != -1) {
			
			Boat boat = (Boat) SimUtils.getPropertyById(boatId);
			boat.addFisher(id);
		}
		
		decisionMaker = new DecisionMaker(id);
		FrameworkBuilder.decisionMakerList.add(decisionMaker);
		
		decisionMaker.assignPossibleActions(FrameworkBuilder.allPossibleActions);
		decisionMaker.createValueTrees();
		
		SimUtils.getContext().add(this);
		
		final NdPoint pt = SimUtils.getSpace().getLocation(this);
		if (!SimUtils.getGrid().moveTo(this, (int) pt.getX(), (int) pt.getY())) {
			Logger.logError("Human could not be placed, coordinate: " + pt.toString());
		}
	}

	/*=========================================
	 * Main human steps 
	 *=========================================
	 */
	public void stepAging() {
		
		age++;
		setStatusByAge();
	}

	public void stepChildrenSchooling() {
		
		if (status == Status.CHILD) {
			School school = SimUtils.getSchool();
			if (school.getPupilVacancy()) {
				schoolType = SchoolType.INSIDE_VILLAGE;
			}
			else {
				schoolType = SchoolType.OUTSIDE_VILLAGE;
			}
		}
		else {
			schoolType = SchoolType.NO_SCHOOL;
		}
	}

	public void stepResetStandardCosts() {
		
		necessaryCost = 0;
		nettoIncome = 0;
		salaryUntaxed = 0;
	}

	public void stepSocialStatusDrain() {
		socialStatus.drainSocialStatus();
	}
	
	public void stepWork() {
		
		this.salaryUntaxed = calculateSalary();
		double salary = payTax(salaryUntaxed);
		double benefits = calculateBenefits();
		double extra_benefits = calculateExtraBenefits();
		Human partner = HumanUtils.getPartner(this);
		if (partner != null) {
			salary /= 2;
			benefits /= 2;
			partner.giveIncomeToPartner(salary + benefits);
		}
		nettoIncome += salary + benefits;
		money += salary + benefits + extra_benefits;
		
		// Set social status
		socialStatus.setSocialStatusWork(status);
	}

	public void stepPayStandardCosts() {
		
		double multiplier = 1;
		Human partner = HumanUtils.getPartner(this);
		if (partner != null)
			multiplier = 0.5;
		
		if (status == Status.CHILD) {
			money -= Constants.LIVING_COST_CHILD;
			necessaryCost += Constants.LIVING_COST_CHILD;
			return;
		}
		
		money -= Constants.LIVING_COST_ADULT;
		necessaryCost += Constants.LIVING_COST_ADULT;

		// Pay children
		double childPayment = Constants.LIVING_COST_CHILD * multiplier;
		for (Human child : HumanUtils.getChildrenUnder18(this)) {
			Logger.logProb("H" + id + " payed child " + child.getId() + " : " + childPayment, 0.05);
			money -= childPayment;
			necessaryCost += childPayment;
			child.giveMoney(childPayment);
			// Pay school
			if (child.getSchoolType() == SchoolType.INSIDE_VILLAGE) {
				money -= Constants.COST_SCHOOL_INSIDE * multiplier;
				SimUtils.getSchool().addToSavings(Constants.COST_SCHOOL_INSIDE * multiplier);
				necessaryCost += Constants.COST_SCHOOL_INSIDE * multiplier;
			}
			else if (child.getSchoolType() == SchoolType.OUTSIDE_VILLAGE) {
				money -= Constants.COST_SCHOOL_OUTSIDE * multiplier;
				SimUtils.getSchool().addToSavings(Constants.COST_SCHOOL_OUTSIDE * multiplier);
				necessaryCost += Constants.COST_SCHOOL_INSIDE * multiplier;
			}
		}
		// Pay property TODO let husband/wife who doesn't own house pay their partner
		for (Property property : HumanUtils.getOwnedProperty(this)) {
			double maintenanceCost = property.getMaintenanceCost();
			money -= maintenanceCost * multiplier;
			necessaryCost += maintenanceCost * multiplier;
			if (partner != null) {
				partner.getNecessaryMoneyFromPartner(maintenanceCost * multiplier);
			}
			//property.addToSavings(maintenanceCost); //TODO change it that property savings are affected
		}
	}

	public void stepSelectWork() {
		
		if (age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_AGE) {
			return ;
		}
		
		if (SimUtils.getInitializationPhase()) {
			boolean searchWork = false;
			if (status == Status.WORK_OUT_OF_TOWN && RandomHelper.nextDouble() < 0.1) {
				searchWork = true;
			}
			else if (status == Status.UNEMPLOYED && RandomHelper.nextDouble() < 0.5) {
				searchWork = true;
			}
			else if (nettoIncome < necessaryCost && money < Constants.HUMAN_MONEY_DANGER_LEVEL && RandomHelper.nextDouble() < 0.25) {
				searchWork = true;
			}
			if (!searchWork) {
				return ;
			}
		}

		ArrayList<String> possibleActions = createWorkPossibleActions();
		Logger.logInfo("H" + id + " possible actions: " + possibleActions);
		String actionToDo = null;
		if (!SimUtils.getInitializationPhase()) {
			actionToDo = selectActionFromPossibleActionsJob(possibleActions);
		}
		else {
			actionToDo = selectActionFromPossibleActions(possibleActions);
		}
		//Logger.logInfo("H" + id + " selected action: " + actionToDo);
		if (actionToDo != null) {
			ActionImplementation.executeActionJob(actionToDo, this);
		}
		else {
			Logger.logError("Error no action to execute");
		}
	}

	public void stepDrainTanks() {
		
		Logger.logProb("VALUES BEFOR H" + id + " - " + getDcString(), 0.05);
		decisionMaker.drainTanks();
		Logger.logProb("VALUES AFTER H" + id + " - " + getDcString(), 0.05);
	}
	
	private String selectActionFromPossibleActionsJob(ArrayList<String> possibleActions) {
		
		ArrayList<String> selectedActions = decisionMaker.actionSelectionFromPossibleActions(possibleActions);
		if (selectedActions.size() >= 2 && selectedActions.contains("Job unemployed")) {
			selectedActions.remove("Job unemployed");
		}
		String selectedAction = selectedActions.get(RandomHelper.nextIntFromTo(0, selectedActions.size() - 1));
		if (selectedActions.contains(jobTitle) && jobTitle != "Job unemployed") {
			selectedAction = jobTitle;
		}
		Logger.logInfo("H" + id + " jobTitle: " + jobTitle + ", selected action:" + selectedAction + " from actions: " + selectedActions);
		return selectedAction;
	}
	
	private String selectActionFromPossibleActions(ArrayList<String> possibleActions) {
		
		if (possibleActions.size() > 0) {
			return possibleActions.get(RandomHelper.nextIntFromTo(0, possibleActions.size() - 1));
		}
		else {
			return null;
		}
	}
	
	public ArrayList<String> createWorkPossibleActions() {
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add("Job unemployed");
		
		ArrayList<Property> properties = SimUtils.getObjectsAllRandom(Property.class); //TODO this is not efficient, look only through job specific buildings
		for (final Property property : properties) {
			// Different rules for boat since it can be owned
			if (property instanceof Boat && status != Status.CAPTAIN) {
				if (!((Boat) property).hasCaptain()) {
					if (money > property.getPrice()) {
						if (!possibleActions.contains("Job captain")) {
							possibleActions.add("Job captain");
						}
					}
				}
			}
			if (property instanceof Factory && status != Status.FACTORY_BOSS) {
				if (!((Factory) property).hasBoss()) {
					if (money > property.getPrice()) {
						if (!possibleActions.contains("Job factory boss")) {
							possibleActions.add("Job factory boss");
						}
					}
				}
			}
			if (property.getVacancy()) {
				possibleActions.add(property.getActionName());
			}
		}
		
		if (!possibleActions.contains(jobTitle) && !jobTitle.equals("NONE")) {
			possibleActions.add(jobTitle);
			Logger.logInfo("H" + id + ", status=" + status + ", title=" + jobTitle);
		}
		return possibleActions;
	}
	
	public void stepRelation() {

		if (isSingle() && age >= Constants.HUMAN_ADULT_AGE && RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_RELATION) {
			Logger.logInfo("Human" + id + "is single");
			for (final Human human: SimUtils.getObjectsAllExcluded(Human.class, this)) {
				if (isSingle() && HumanUtils.isPotentialCouple(human, this)) {
					if (!getAncestorsMatch(ancestors, human.getAncestors())) {
						actionGetPartner(human);
					}
					break;
				}
			}
		}
	}
	 
	public void stepSocialEvent() {
		
		if (age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		WaterTank waterTank = decisionMaker.mostImportantValue();
		EventHall eventHall = SimUtils.getEventHall();
		ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(id);
		
		switch(waterTank.getRelatedAbstractValue()) {
		case "Tradition":
			if (possibleEvents.size() >= 1) {
				Logger.logAction("JOIN EVENT - H" + id + " Tradition");
				Event eventToJoin = possibleEvents.get(RandomHelper.nextIntFromTo(0, possibleEvents.size() - 1));
				money -= eventHall.joinEvent(eventToJoin, id);
				waterTank.increasingLevel(0.2);
				eventHall.increaseTradition();
			}
			break;
		case "Power":
			if (socialStatus.getBelowAverage()) {
				
				if (eventHall.getVacancyForNewEvent() && money > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
					Logger.logAction("CREATE EVENT C - H" + id + " Power");
					money -= eventHall.createEvent("Commercial", id);
					waterTank.increasingLevel(2);
					eventHall.increasePower();
				}
				else {
					if (possibleEvents.size() >= 1) {
						Logger.logAction("JOIN EVENT - H" + id + " Power");
						Event eventToJoin = possibleEvents.get(RandomHelper.nextIntFromTo(0, possibleEvents.size() - 1));
						money -= eventHall.joinEvent(eventToJoin, id);
						waterTank.increasingLevel(0.2);
						eventHall.increasePower();
					}
				}
			}
			break;
		case "Self-direction":
			if (eventHall.getVacancyForNewEvent() && money > Constants.DONATE_MONEY_MINIMUM_SAVINGS) {
				if (RandomHelper.nextDouble() < 0.5) {
					Logger.logAction("CREATE EVENT F - H" + id + " Self-direction");
					money -= eventHall.createEvent("Free", id);
				}
				else {
					Logger.logAction("CREATE EVENT C - H" + id + " Self-direction");
					money -= eventHall.createEvent("Commercial", id);	
				}
				eventHall.increaseSelfDirection();
				waterTank.increasingLevel(2);
			}
			break;
		case "Universalism":
			if (socialStatus.getUnsatisfiedUniversalist() && eventHall.getVacancyForNewEvent()) {
				Logger.logAction("CREATE EVENT F - H" + id + " Universalism");
				money -= eventHall.createEvent("Free", id);
				waterTank.increasingLevel(2);
				eventHall.increaseUniversalism();
			}
			break;
		}		
	}
	
	public void stepSocialEventOld() {
		
		if (age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		EventHall eventHall = SimUtils.getEventHall();
		
		if (eventHall.getVacancyForNewEvent() && RandomHelper.nextDouble() < 0.2) {
			if (RandomHelper.nextDouble() > 0.5) {
				Logger.logAction("CREATE EVENT C - H" + id);
				money -= eventHall.createEvent("Commercial", id);
			}
			else {
				Logger.logAction("CREATE EVENT F - H" + id);
				money -= eventHall.createEvent("Free", id);
			}
		}
		else if (RandomHelper.nextDouble() < 0.9) {
			ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(id);
			if (possibleEvents.size() >= 1) {
				Logger.logAction("JOIN EVENT - H" + id);
				Event eventToJoin = possibleEvents.get(RandomHelper.nextIntFromTo(0, possibleEvents.size() - 1));
				money -= eventHall.joinEvent(eventToJoin, id);
			}
		}
	}
	
	public void stepDonate() {
		
		if (age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add("Donate nothing");
		if ((nettoIncome > necessaryCost && money > Constants.DONATE_MONEY_MINIMUM_SAVINGS) || money > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			possibleActions.add("Donate to council");
		}
		Logger.logInfo("H" + id + " possible actions: " + possibleActions);
		String actionToDo = "Donate nothing";
		if (SimUtils.getInitializationPhase()) {
			actionToDo = selectActionFromPossibleActions(possibleActions);
		}
		else {
			actionToDo = selectActionFromPossibleActionsDonate(possibleActions);
		}
		if (actionToDo != null) {
			ActionImplementation.executeActionDonate(actionToDo, this);
		}
		else {
			Logger.logError("Error no action to execute");
		}
	}
	
	private String selectActionFromPossibleActionsDonate(ArrayList<String> possibleActions) {
		
		if (possibleActions.size() == 0) {
			return null;
		}
		
		WaterTank waterTank = decisionMaker.mostImportantValue();
		if (possibleActions.size() == 1){
			if (waterTank.getRelatedAbstractValue().equals("Power")) {
				Logger.logInfo("DONATE NOT - H" + id + " Power (no money)");
				waterTank.increasingLevel(0.2);
				SimUtils.getCouncil().increasePower();
			}
			else if (waterTank.getRelatedAbstractValue().equals("Self-direction")) {
				Logger.logInfo("DONATE NOT - H" + id + " Self-direction (no money)");
				waterTank.increasingLevel(0.2);
				SimUtils.getCouncil().increaseSelfDirection();
			}
			return "Donate nothing";
		}
		
		switch(waterTank.getRelatedAbstractValue()) {
		case "Tradition":
			Logger.logInfo("DONATE - H" + id + " Tradition");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseTradition();
			return "Donate to council";
		case "Power":
			Logger.logInfo("DONATE NOT - H" + id + " Power");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increasePower();
			return "Donate nothing";
		case "Self-direction":
			Logger.logInfo("DONATE NOT - H" + id + " Self-direction");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseSelfDirection();
			return "Donate nothing";
		case "Universalism":
			Logger.logInfo("DONATE - H" + id + " Universalism");
			waterTank.increasingLevel(0.2);
			SimUtils.getCouncil().increaseUniversalism();
			return "Donate to council";
		default:
			Logger.logError("Error no correct value from watertank: " + waterTank.getRelatedAbstractValue());
		}	
		return "Donate nothing"; 
	}
	
	public void stepFamily() {
		
		if (!isSingle() && !isMan() && childrenWanted > 0 && age < Constants.HUMAN_MAX_CHILD_GET_AGE && HumanUtils.isLivingTogetherWithPartner(this)
						&& RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_CHILD) {
			Human partner = HumanUtils.getPartner(this);
			if (partner == null) {
				Logger.logError("Human.stepFamily(): partner = null");
			}
			actionGetChild(partner);
		}
	}

	public void stepHousing() {
				
		if (RandomHelper.nextDouble() > Constants.HUMAN_PROB_GET_HOUSE || age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return ;
		}
		
		if (!HumanUtils.isOwningHouse(this) && status != Status.UNEMPLOYED) {
			homelessTick ++;
			for (House house : SimUtils.getPropertyAvailableAllRandom(House.class)) {
				if (money > house.getPrice()) {
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
			if (nettoIncome < necessaryCost && money < Constants.HUMAN_MONEY_DANGER_LEVEL) {
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
	public void stepDeath() {
	
		if (age > Constants.HUMAN_MAX_LIVING_AGE) { //TODO make this a proper distribution
			
			removeSelf();
		}
		else if (age == Constants.HUMAN_ELDERLY_CARE_AGE) {
			
			actionSellAllProperty();
		}
		
		if (age < Constants.HUMAN_ADULT_AGE || age >= Constants.HUMAN_ELDERLY_CARE_AGE)
			return ;
		
		if (homelessTick >= Constants.HOMELESS_TICK) {
			actionMigrateOutOfTown();
			return ;
		}
		if (!SimUtils.getInitializationPhase()) {
			if (!decisionMaker.getIsSatisfied() && RandomHelper.nextDouble() < 0.00001 * (2 + decisionMaker.getSelfDirectionThreshold()))
			{
				Logger.logAction("H" + id + " moves out because he/she is not happy : " + 0.00001 * (2 + decisionMaker.getSelfDirectionThreshold()));
				Logger.logInfo("H" + id + getDcString());
				actionMigrateOutOfTown();
			}
		}
		else {
			if (status == Status.WORK_OUT_OF_TOWN && RandomHelper.nextDouble() < (1.0 / 300)) {
				Logger.logAction("H" + id + " moves out because he/she has an outside job");
				actionMigrateOutOfTown();
			}
			else if (status == Status.UNEMPLOYED && RandomHelper.nextDouble() < (1.0 / (4 * 1200))) {
				Logger.logAction("H" + id + " moves out because he/she is unemployed");
				actionMigrateOutOfTown();
			}
		}
	}
	
	/**
	 * TODO for now this function makes sure Humans are moved when they are on top of
	 * each other
	 */
	public void stepLocation() {
		
		final Grid<Object> grid = SimUtils.getGrid();
		GridPoint newLocation = grid.getLocation(this);

		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (currentTick % 2 == 0 | currentTick < 0) {
			Property livingPlace = HumanUtils.getLivingPlace(this);
			if (livingPlace != null) {
				newLocation = livingPlace.getFreeLocationExcluded(this);
			}
			else {
				Logger.logError("Human"+getId()+" has no living place");
			}
		}
		else {
			Property workingPlace = HumanUtils.getWorkingPlace(id, status, schoolType);
			if (workingPlace != null){
				if (workingPlace.getFreeLocationExcluded(this) != null) {
					newLocation = workingPlace.getFreeLocationExcluded(this);
				}
				else {
					Logger.logError("No room in working place to put agent");
				}
			}
		}
		grid.moveTo(this, newLocation.getX(), newLocation.getY());
	}
	
	public void removeSelf() {
		
		status = Status.DEAD;
		Logger.logInfo("H" + id + " is removed");
		Human partner = HumanUtils.getPartner(this);
		
		Network<Object> networkProperty = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
		
		if (partner != null) {
			Logger.logInfo("H" + partner.getId() + " is the partner and gets the property of H" + id);
			
			Iterable<RepastEdge<Object>> propertyEdges = networkProperty.getOutEdges(this);
			for (RepastEdge<Object> propertyEdge : propertyEdges) {
				networkProperty.addEdge(partner, propertyEdge.getTarget());
			}
		}
		
		Logger.logDebug("HumanUtils.removeAllEdges" + getId());
		HumanUtils.removeAllEdges(this);
		Logger.logDebug("ContextUtils.remove" + getId());
		SimUtils.getContext().remove(this);
	}
	
	/*=========================================
	 * Actions
	 *========================================
	 */
	
	public void actionBuyHouse(House house) {
		
		money -= house.getPrice();
		SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).addEdge(this, house);
		Logger.logAction("H" + id + " bought house:" + HumanUtils.getOwnedHouse(this));
		homelessTick = 0;
	}
	
	public void actionSellHouse(House myHouse) {
		
		Logger.logAction("H" + id + " sells house");
		Network<Object> networkProperty = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
		money += myHouse.getPrice(); // Increase money
		RepastEdge<Object> houseEdge = networkProperty.getEdge(this, myHouse);
		networkProperty.removeEdge(houseEdge);
	}
	
	public void actionSellAllProperty() {
		Logger.logAction("H" + id + " sells all property");
		Network<Object> networkProperty = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
		Iterable<RepastEdge<Object>> propertyEdges = networkProperty.getEdges(this);
		for (RepastEdge<Object> propertyEdge : propertyEdges) {
			networkProperty.removeEdge(propertyEdge);
		}
	}
	
	public void actionGetPartner(Human newPartner) {
		Logger.logAction("H" + id + " got a relation with H" + newPartner.getId());
		SimUtils.getNetwork(Constants.ID_NETWORK_COUPLE).addEdge(this, newPartner);
	}
	
	public void actionGetChild(Human partner) {
		
		HumanUtils.spawnChild(this, partner);
		Logger.logAction("H" + id + "and H" + partner.getId() + " got a child");
		childrenWanted--;
	}
	
	public void actionMigrateOutOfTown() {
		
		Logger.logAction("H" + id + " migrates out of town");
		SimUtils.getGrid().moveTo(this,  RandomHelper.nextIntFromTo(1, Constants.GRID_VILLAGE_START - 2), 
				RandomHelper.nextIntFromTo(0, Constants.GRID_HEIGHT - 1));
				status = Status.DEAD;
		// Remove partner
		if (HumanUtils.getPartner(this) != null) {
			Logger.logInfo("and takes partner H" + HumanUtils.getPartner(this).getId() + " with her/him");
			HumanUtils.getPartner(this).removeSelf();
		}
		// Also remove children
		for (Human child : HumanUtils.getChildrenUnder18(this)) {
			Logger.logInfo("and also child H" + child.getId());
			child.removeSelf();
		}
		removeSelf();
	}
	
	public void actionSocialEventAttend() {
		socialStatus.setEventAttendee();
	}
	
	public void actionSocialEventOrganize() {
		socialStatus.setEventOrganizer();
	}
	
	/*=========================================
	 * Getters and setters with logic 
	 *========================================
	 */

	public boolean isSingle() {
		
		if (SimUtils.getNetwork(Constants.ID_NETWORK_COUPLE).getDegree(this) == 1) {
			return false;
		}
		if (SimUtils.getNetwork(Constants.ID_NETWORK_COUPLE).getDegree(this) > 1) {
			Logger.logError("Human.isSingle() to much networks!!!");
		}
		return true;
	}
	
	/**
	 * Calculates the number of ancestors to take into account, dependent on the
	 * Constants.HUMAN_ANCESTORS_LAYERS 0:none, 1:parents, 2:grandparents, 3:great-grandparents, 4:great-great-grandparents, 5:etc
	 * @param motherId
	 * @param fatherId
	 * @param motherAncestors
	 * @param fatherAncestors
	 */
	@SuppressWarnings("unused")
	public void setAncestors(int motherId, int fatherId, final ArrayList<GridPoint> motherAncestors,
														 final ArrayList<GridPoint> fatherAncestors) {
		ancestors = new ArrayList<GridPoint>();
		if (Constants.HUMAN_ANCESTORS_LAYERS == 0)
			return ;
		
		ancestors.add(new GridPoint(1, motherId));
		ancestors.add(new GridPoint(1, fatherId));
		if (Constants.HUMAN_ANCESTORS_LAYERS == 1)
			return ;

		for (GridPoint ancestor : motherAncestors) {
			if (ancestor.getX() + 1 <= Constants.HUMAN_ANCESTORS_LAYERS) {
				ancestors.add(new GridPoint(ancestor.getX() + 1, ancestor.getY()));
			}
		}
		for (GridPoint ancestor : fatherAncestors) {
			if (ancestor.getX() + 1 <= Constants.HUMAN_ANCESTORS_LAYERS) {
				ancestors.add(new GridPoint(ancestor.getX() + 1, ancestor.getY()));
			}
		}
		Logger.logDebug("H" + id + " ancestors : " + ancestors.toString());
	}

	public boolean getAncestorsMatch(final ArrayList<GridPoint> ancestors1, final ArrayList<GridPoint> ancestors2) {
		
		Logger.logDebug("An1:"+ancestors1.toString() + ", An2:"+ancestors2.toString());
		ArrayList<Integer> ancestorsId = new ArrayList<Integer>();
		for (GridPoint ancestor : ancestors1) {
			ancestorsId.add(ancestor.getY());
		}
		for (GridPoint ancestor : ancestors2) {
			ancestorsId.add(ancestor.getY());
		}
		Set<Integer> ancestorsIdSet = new HashSet<Integer>(ancestorsId);
		if (ancestorsIdSet.size() != ancestorsId.size()) {
			Logger.logDebug("#Matching ancestors");
			return true;
		}
		Logger.logDebug("#No matching ancestors");
		return false;
	}
	
	public double calculateSalary() {
		
		switch(status) {
		case FACTORY_WORKER:
			return Math.round(SimUtils.getFactory().getFactoryWorkerPayment());
		case FACTORY_BOSS:
			return Math.round(SimUtils.getFactory().getFactoryBossPayment());
		case TEACHER:
			return Math.round(SimUtils.getSchool().getTeacherPayment());
		case WORK_OUT_OF_TOWN:
			return Constants.SALARY_OUTSIDE_WORK;
		case FISHER:
			if (SimUtils.getBoat(id) != null) {
				return Math.round(SimUtils.getBoat(id).getFisherPayment());
			}
			Logger.logError("Human.calculateSalary: H" + id + " no boat for fisher");
			return 0;
		case CAPTAIN:
			if (SimUtils.getBoat(id) != null) {
				return Math.round(SimUtils.getBoat(id).getCaptainPayment());
			}
			Logger.logError("Human.calculateSalary: H" + id + " no boat for captain");
			return 0;
		case ELDERLY_CARETAKER:
			return Math.round(SimUtils.getElderlyCare().getCaretakerPayment());
		default: // You get nothing
			return 0;
		}
	}
	
	public double calculateBenefits() {
		switch(status) {
		case UNEMPLOYED:
			return Math.round(SimUtils.getSocialCare().getUnemployedBenefit());
		case ELDER:
			return Math.round(SimUtils.getElderlyCare().getPension());
		case ELDEST:
			return Math.round(SimUtils.getElderlyCare().getPension());
		default:
			return 0;
		}
	}
	
	public double calculateExtraBenefits() {
		
		if (money < 0) {
			if (SimUtils.getSocialCare().getSavings() > 0) {
				
				double amountOfMoney = Math.min(Constants.BENEFIT_UNEMPLOYED, SimUtils.getSocialCare().getSavings());
				SimUtils.getSocialCare().removeFromSavings(amountOfMoney);
				return amountOfMoney;
				
			}
		}
		return 0;
	}
	
	//TODO in this function change the parameter to something that is defined before hand
	public double payTax(double salary) {
		if (status != Status.WORK_OUT_OF_TOWN) {
			
			Council council = SimUtils.getObjectsAll(Council.class).get(0);
			council.addToSavings(salary * ((double) RunEnvironment.getInstance().getParameters().getValue(Constants.PARAMETER_TAX_TO_COUNCIL) / 100));
			
			return salary * ((100 - (double) RunEnvironment.getInstance().getParameters().getValue(Constants.PARAMETER_PERCENTAGE_TAX)) / 100);//(Constants.NETTO_INCOME_PERCENTAGE / 100);
		}
		return salary * ((100 - (double) RunEnvironment.getInstance().getParameters().getValue(Constants.PARAMETER_PERCENTAGE_TAX)) / 100);
	}
	
	/*=========================================
	 * Standard getters and setters
	 *=========================================
	 */
	private void setStatusByAge() {
		
		if (age < Constants.HUMAN_ADULT_AGE) {
			status = Status.CHILD;
		}
		else if (age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			status = Status.ELDEST;
			schoolType = SchoolType.NO_SCHOOL;
		}
		else if (age >= Constants.HUMAN_ELDERLY_AGE) {
			status = Status.ELDER;
			schoolType = SchoolType.NO_SCHOOL;
		}
		else if (status == Status.CHILD) {
			status = Status.UNEMPLOYED;
			schoolType = SchoolType.NO_SCHOOL;
		}
	}
	
	public double getMoney() { // TODO the rounding may be problematic
		return Math.round(money);
	}
	
	public void getNecessaryMoneyFromPartner(double cost) {
		money -= cost;
		necessaryCost += cost;
	}
	
	public void giveMoney(double gift) {
		money += gift;
	}
	
	public void removeMoney(double remove) {
		money += remove;
	}
	
	public void giveIncomeToPartner(double income) {
		money += income;
		nettoIncome += income;
	}
	
	public int getChildrenWanted() {
		return childrenWanted;
	}
	
	public boolean isAdult() {
		if (age >= Constants.HUMAN_ADULT_AGE) {
			return true;
		}
		return false;
	}
	
	public int getAge() {
		return age;
	}
	
	public int getHomelessTick() {
		return homelessTick;
	}

	public int getId() {
		return id;
	}

	public boolean getMigrated() {
		return foreigner;
	}
	
	public double getNettoIncome() {
		return nettoIncome;
	}

	public double getNecessaryCost() {
		return necessaryCost;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public SchoolType getSchoolType() {
		return schoolType;
	}
	
	public void setSchoolType(SchoolType schoolType) {
		this.schoolType = schoolType;
	}
	
	public boolean isMan() {
		return (!gender);
	}
	
	public ArrayList<GridPoint> getAncestors() {
		return ancestors;
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
	
	public String getJobTitle() {
		return jobTitle;
	}
	
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
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
	/*=========================================
	 * Graphics and information
	 *========================================
	 */
	
	public String getStatusString() {
		return status.toString();
	}
	
	public String getSchoolTypeString() {
		return schoolType.toString();
	}
	
	public String getDcString() {
		return decisionMaker.toString();
	}
	
	@Override
	public String toString() {
		return String.format("Human (" + id + "), location %s", SimUtils.getGrid().getLocation(this));
	}
	
	public String getLabel() {

		return Integer.toString(id) + "|" + age;
	}
	//
	public String getHumanVarsAsString() {
		int boatId = -1;
		if (status == Status.FISHER) {
			boatId = SimUtils.getBoat(id).getId();
		}
		return id + "," + age + "," + gender + "," + money + "," + childrenWanted + "," + foreigner +
			   "," + homelessTick + "," + nettoIncome + "," + necessaryCost + "," + jobTitle + "," + status.name() + "," + boatId; 
	}

	public VSpatial getSpatialImage() {

		return spatialImages.get(status);
	}

	public void setSpatialImages(HashMap<Status, VSpatial> spatialImages) {
		this.spatialImages = spatialImages;	
	}
}