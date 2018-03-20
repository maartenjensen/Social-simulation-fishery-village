package fisheryvillage.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.municipality.Council;
import fisheryvillage.property.Boat;
import fisheryvillage.property.House;
import fisheryvillage.property.Property;
import fisheryvillage.property.School;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The human class, without it the village would be a ghost town
*
* @author Maarten Jensen
*/
public class Human {

	// Variable declaration (initialization in constructor)
	private final boolean gender; // man = false; woman = true;
	private final int id;
	private int age;
	private int yearTick;
	private double money;
	private int childrenWanted;
	private final boolean foreigner;
	private boolean liveOutOfTown;
	
	// Variable initialization
	private ArrayList<GridPoint> ancestors = new ArrayList<GridPoint>();
	private HashMap<Status, VSpatial> spatialImages = new HashMap<Status, VSpatial>();
	private int homelessTick = 0;
	private Status status = Status.UNEMPLOYED;
	private SchoolType schoolType = SchoolType.NO_SCHOOL;
	private double nettoIncome = 0;
	private double necessaryCost = 0;

	public Human(boolean gender, int age, int id, double money, boolean foreigner, boolean liveOutOfTown) {
		this.gender = gender;
		this.age = age;
		this.id = id;
		this.money = money;
		this.foreigner = foreigner;
		this.liveOutOfTown = liveOutOfTown;
		this.childrenWanted = RandomHelper.nextIntFromTo(Constants.HUMAN_MIN_CHILDREN_WANTED, Constants.HUMAN_MAX_CHILDREN_WANTED);
		this.yearTick = 1;
		setStatusByAge();
		this.schoolType = SchoolType.NO_SCHOOL;
		
		SimUtils.getContext().add(this);
		
		final NdPoint pt = SimUtils.getSpace().getLocation(this);
		if (!SimUtils.getGrid().moveTo(this, (int) pt.getX(), (int) pt.getY())) {
			Logger.logError("Human could not be placed, coordinate: " + pt.toString());
		}
	}

	/*=========================================
	 * Main human steps 
	 *========================================
	 */
	
	public void stepReset() {
		necessaryCost = 0;
		nettoIncome = 0;
		
		yearTick ++;
		if (yearTick > 1) {
			yearTick = 1;
			age++;
			setStatusByAge();
		}
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

	public void stepWork() {
		
		double salary = payTax(getSalary());
		Human partner = HumanUtils.getPartner(this);
		if (partner != null) {
			salary /= 2;
			partner.giveSalaryToPartner(salary);
		}
		nettoIncome += salary;
		money += salary;
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
			Logger.logDebug("H" + id + " payed child " + child.getId() + " : " + childPayment);
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
		 
		if (status == Status.UNEMPLOYED && RandomHelper.nextDouble() < 0.5) { //TODO this probability
			
			ArrayList<Property> properties = SimUtils.getObjectsAllRandom(Property.class); //TODO this is not efficient, look only through job specific buildings
			for (final Property property : properties) {
				if (property.getVacancy()) {
					actionWorkStartAt(property);
					break;
				}
			}
		}
	}
	
	public void stepRelation() {

		if (isSingle() && age >= Constants.HUMAN_ADULT_AGE && RandomHelper.nextDouble() < Constants.HUMAN_PROB_GET_RELATION) {
			Logger.logInfo("Human" + id + "is single");
			for (final Human human: SimUtils.getObjectsAllExcluded(Human.class, this)) {
				if (isSingle() && HumanUtils.isPotentialCouple(human, this) && !human.isLivingOutOfTown()) {
					if (!getAncestorsMatch(ancestors, human.getAncestors())) {
						actionGetPartner(human);
					}
					break;
				}
			}
		}
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

		if (RandomHelper.nextDouble() > Constants.HUMAN_PROB_GET_HOUSE) {
			return ;
		}
		if (age >= Constants.HUMAN_ADULT_AGE && age < Constants.HUMAN_ELDERLY_CARE_AGE) {
			
			if (!HumanUtils.isOwningHouse(this)) {
				homelessTick ++;
				for (House house : SimUtils.getPropertyAvailableAllRandom(House.class)) {
					if (money > house.getPrice()) {
						actionBuyHouse(house);
						break;
					}
				}
			}
			else if ((!isSingle() && !HumanUtils.isLivingTogetherWithPartner(this))) { // Sell house if in relationship and not single and owns a house
				actionSellHouse(HumanUtils.getOwnedHouse(this));
			}
			House ownedHouse = HumanUtils.getOwnedHouse(this);
			if (age >= Constants.HUMAN_ELDERLY_CARE_AGE && ownedHouse != null) {
				actionSellHouse(HumanUtils.getOwnedHouse(this));
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
		
		if (homelessTick >= 20 && liveOutOfTown == false) {
			actionMigrateOutOfTown();
		}
	}
	
	public void removeSelf() {
		
		status = Status.DEAD;
		liveOutOfTown = true;
		Logger.logInfo("H" + id + " is removed");
		Human partner = HumanUtils.getPartner(this);
		if (partner != null) {
			Logger.logInfo("H" + partner.getId() + " is the partner and gets the property of H" + id);
			Network<Object> networkProperty = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
			Iterable<RepastEdge<Object>> propertyEdges = networkProperty.getOutEdges(this);
			for (RepastEdge<Object> propertyEdge : propertyEdges) {
				networkProperty.addEdge(partner, propertyEdge.getTarget());
			}
		}

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
		
		Logger.logAction("H" + id + "sells house");
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
	
	public void actionWorkStartAt(Property property) {
		
		Logger.logAction("H" + id + " took the job at : " + property.getLabel());
		status = property.getJobStatus();
		if (property instanceof Boat) {
			Boat boat = (Boat) property;
			boat.addFisher(id);
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
		liveOutOfTown = true;
		SimUtils.getGrid().moveTo(this,  RandomHelper.nextIntFromTo(1, Constants.GRID_VILLAGE_START - 2), 
				RandomHelper.nextIntFromTo(0, Constants.GRID_HEIGHT - 1));
				status = Status.OUT_OF_TOWN;
		
		if (HumanUtils.getPartner(this) != null) {
			HumanUtils.getPartner(this).removeSelf();
		}
		removeSelf();
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
	
	public double getSalary() {
		switch(status) {
		case FACTORY_WORKER:
			return Constants.SALARY_FACTORY_WORKER;
		case TEACHER:
			return Math.round(SimUtils.getSchool().getTeacherPayment());
		case WORK_OUT_OF_TOWN:
			return Constants.SALARY_OUTSIDE_WORK;
		case FISHER:
			return Math.round(SimUtils.getBoat(id).getFisherPayment());
		default: // You get nothing
			return 0;
		}
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
	
	public void giveSalaryToPartner(double salary) {
		money += salary;
		nettoIncome += salary;
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

	public boolean isLivingOutOfTown() {
		return liveOutOfTown;
	}
	
	public ArrayList<GridPoint> getAncestors() {
		return ancestors;
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
	
	@Override
	public String toString() {
		return String.format("Human (" + id + "), location %s", SimUtils.getGrid().getLocation(this));
	}
	
	public String getLabel() {

		return Integer.toString(id) + "|" + age;
	}

	public VSpatial getSpatialImage() {
		return spatialImages.get(status);
	}

	public void setSpatialImages(HashMap<Status, VSpatial> spatialImages) {
		this.spatialImages = spatialImages;	
	}
}