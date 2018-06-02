package fisheryvillage.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.House;
import fisheryvillage.property.Property;
import fisheryvillage.property.Workplace;
import fisheryvillage.property.municipality.Council;
import fisheryvillage.property.municipality.School;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The human class, without it the village would be a ghost town
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Human {

	// Variable declaration (initialization in constructor)
	private final int id;
	private final boolean gender; // man = false; woman = true;
	private final boolean foreigner;
	private boolean higherEducated;
	private int age;
	private double money;
	private Status status;
	
	// Variable initialization
	private ArrayList<Integer> childrenIds = new ArrayList<Integer>();
	private ArrayList<Integer> propertyIds = new ArrayList<Integer>();
	private ArrayList<GridPoint> ancestors = new ArrayList<GridPoint>();
	private HashMap<Status, VSpatial> spatialImages = new HashMap<Status, VSpatial>();
	private SchoolType schoolType = SchoolType.NO_SCHOOL;
	private double nettoIncome = 0;
	private double necessaryCost = 0;
	private double salaryUntaxed = 0;
	private int partnerId = -1;
	private int workplaceId = -1;

	protected Human(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money) {

		this.id = id;
		this.gender = gender;
		this.foreigner = foreigner;
		this.higherEducated = higherEducated;
		this.age = age;
		this.money = money;
		this.schoolType = SchoolType.NO_SCHOOL;
		this.status = Status.UNEMPLOYED;
		
		setStatusByAge();
		addToContext();
	}

	protected Human(int id, boolean gender, boolean foreigner, boolean higherEducated, int age, double money,
				 double nettoIncome, double necessaryCost, Status status, int workplaceId) {
		
		this.id = id;
		this.gender = gender;
		this.foreigner = foreigner;
		this.higherEducated = higherEducated;
		this.age = age;
		this.money = money;
		this.schoolType = SchoolType.NO_SCHOOL;
		this.status = status;
		this.workplaceId = workplaceId;
		this.necessaryCost = necessaryCost;
		this.nettoIncome = nettoIncome;
		
		addToContext();
	}

	private void addToContext() {
		
		SimUtils.getContext().add(this);
		
		final NdPoint pt = SimUtils.getSpace().getLocation(this);
		if (!SimUtils.getGrid().moveTo(this, (int) pt.getX(), (int) pt.getY())) {
			Logger.logError("Human could not be placed, coordinate: " + pt.toString());
		}
	}
	
	/*=========================================
	 * Main functions
	 *=========================================
	 */
	protected void addAge() {
		age++;
		setStatusByAge();
	}

	protected void setPrimarySchool() {
		
		if (status == Status.CHILD) {
			School school = SimUtils.getSchool();
			if (school.getPupilVacancy())
				schoolType = SchoolType.INSIDE_VILLAGE;
			else 
				schoolType = SchoolType.OUTSIDE_VILLAGE;
		}
		else {
			schoolType = SchoolType.NO_SCHOOL;
		}
	}
	
	protected void equalizeMoneyWithPartner() {
		if (partnerId >= 0) {
			Human partner = getPartner();
			double dividedMoney = (money + partner.getMoney()) / 2;
			setMoney(dividedMoney);
			partner.setMoney(dividedMoney);
		}
	}
	
	protected void resetCostIndicators() {
		
		necessaryCost = 0;
		nettoIncome = 0;
		salaryUntaxed = 0;
	}

	/**
	 * Get salary, pay tax and share it with partner
	 */
	protected void retrieveAndShareSalary() {
		
		this.salaryUntaxed = calculateSalary();
		double salary = payTax(salaryUntaxed);
		double benefits = calculateBenefits();
		double bankrupt_benefits = calculateBankruptBenefits();
		Human partner = getPartner();
		if (partner != null) {
			salary /= 2;
			benefits /= 2;
			partner.giveIncomeToPartner(salary + benefits);
		}
		nettoIncome += salary + benefits;
		money += salary + benefits + bankrupt_benefits;
	}

	public void payStandardCosts() {
		
		// For children
		if (status == Status.CHILD) {
			money -= Constants.LIVING_COST_CHILD;
			necessaryCost += Constants.LIVING_COST_CHILD;
			return;
		}
		
		// For adults
		double partnerMultiplier = 1;
		Human partner = getPartner();
		if (partner != null)
			partnerMultiplier = 0.5;
		
		money -= Constants.LIVING_COST_ADULT;
		necessaryCost += Constants.LIVING_COST_ADULT;

		payChildren(partnerMultiplier);
		payPropertyMaintenance(partnerMultiplier);
	}
	
	/**
	 * Pay costs for children this contains living cost and school cost
	 * The partnerMultiplier is 1 if the agent has no partner and
	 * 0.5 if he/she has a partner
	 * @param partnerMultiplier
	 */
	private void payChildren(double partnerMultiplier) {
		
		// Pay children
		double childPayment = Constants.LIVING_COST_CHILD * partnerMultiplier;
		for (Human child : HumanUtils.getChildrenUnder18(this)) {
			Logger.logProb("H" + id + " payed child " + child.getId() + " : " + childPayment, 0.05);
			money -= childPayment;
			necessaryCost += childPayment;
			child.addMoney(childPayment);
			// Pay school
			if (child.getSchoolType() == SchoolType.INSIDE_VILLAGE) {
				money -= Constants.COST_SCHOOL_INSIDE * partnerMultiplier;
				SimUtils.getSchool().addSavings(Constants.COST_SCHOOL_INSIDE * partnerMultiplier);
				necessaryCost += Constants.COST_SCHOOL_INSIDE * partnerMultiplier;
			}
			else if (child.getSchoolType() == SchoolType.OUTSIDE_VILLAGE) {
				money -= Constants.COST_SCHOOL_OUTSIDE * partnerMultiplier;
				necessaryCost += Constants.COST_SCHOOL_OUTSIDE * partnerMultiplier;
			}
		}
	}
	
	/**
	 * Pay maintenance cost for property
	 * The partnerMultiplier is 1 if the agent has no partner and
	 * 0.5 if he/she has a partner
	 * @param partnerMultiplier
	 */
	private void payPropertyMaintenance(double partnerMultiplier) {
		
		// Pay property TODO let husband/wife who doesn't own house pay their partner
		for (Property property : HumanUtils.getOwnedProperty(this)) {
			double maintenanceCost = property.getMaintenanceCost();
			money -= maintenanceCost * partnerMultiplier;
			necessaryCost += maintenanceCost * partnerMultiplier;
			if (partnerId >= 0) {
				getPartner().getNecessaryMoneyFromPartner(maintenanceCost * partnerMultiplier);
			}
		}
	}
	
	/**
	 * Return all job vacancies, can't become a fisher if you are a captain already
	 * @return
	 */
	protected ArrayList<String> getPossibleWorkActions(String currentJobTitle) {
		
		ArrayList<String> possibleActions = new ArrayList<String>();
		possibleActions.add("Job unemployed");

		ArrayList<Workplace> workplaces = SimUtils.getObjectsAllRandom(Workplace.class);
		for (final Workplace workplace : workplaces) {

			ArrayList<Status> vacancies = workplace.getVacancy(getHigherEducated(), getMoney());
			for (Status vacancy : vacancies) {
				if (!possibleActions.contains(vacancy.getJobActionName())) {
					possibleActions.add(vacancy.getJobActionName());
				}
			}
		}
		if (!possibleActions.contains(currentJobTitle) && !currentJobTitle.equals("none")) {
			possibleActions.add(currentJobTitle);
		}
		if (currentJobTitle.equals("Job captain") && possibleActions.contains("Job fisher")) {
			possibleActions.remove("Job fisher");
		}
		return possibleActions;
	}
	
	/**
	 * TODO for now this function makes sure Humans are moved when they are on top of
	 * each other
	 */
	public void updateLocation() {
		
		final Grid<Object> grid = SimUtils.getGrid();
		GridPoint newLocation = grid.getLocation(this);

		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (currentTick % 2 == 0 | currentTick < 0) {
			Property livingPlace = HumanUtils.getLivingPlace(this);
			if (livingPlace != null) {
				newLocation = livingPlace.getFreeLocationExcluded(this);
				if (newLocation == null)  {
					livingPlace.getLocation();
					Logger.logInfo("H" + id + " living place is full: " + livingPlace.getName());
				}
			}
			else {
				Logger.logError("H" + id + " has no living place");
			}
		}
		else {
			Property workingPlace = HumanUtils.getWorkingPlace(workplaceId, status, schoolType);
			if (workingPlace != null){
				if (workingPlace.getFreeLocationExcluded(this) != null) {
					newLocation = workingPlace.getFreeLocationExcluded(this);
				}
				else {
					Logger.logError("H " + id + " status " + status + " no room in working place to put agent");
				}
			}
		}
		grid.moveTo(this, newLocation.getX(), newLocation.getY());
	}
	
	/**
	 * Remove person because it died
	 */
	public void die() {
		Logger.logAction("H" + id + " died at age : " + age);
		removeSelf();
	}
	
	/**
	 * Removes the agent from the context, the house is sold or given to the partner (if he/she has
	 * no house) after that the money is given to the partner. If the person has no partner then it 
	 * is shared among the children. The person is also removed from the childrenIds in his/her parents
	 */
	public void removeSelf() {
		
		stopWorkingAtWorkplace();
		status = Status.DEAD;
		
		// Remove this person from parent id
		for (Human parent : HumanUtils.getParents(this)) {
			parent.removeChild(id);
		}
		
		Logger.logInfo("H" + id + " is removed");
		Human partner = getPartner();
		if (partner != null && HumanUtils.getOwnedHouse(this) != null) {
			if (HumanUtils.getOwnedHouse(partner) == null) {
				removeAllPropertyExceptHouse();
				int houseId = HumanUtils.getOwnedHouse(this).getId();
				removeProperty(houseId, true);
				partner.connectProperty(houseId);
				Logger.logInfo("H" + id + " gave house " + houseId + " to partner H" + partner.getId());
			}
		}
		
		removeAndSellAllProperty();
		shareMoney();
		
		breakUpWithPartner();
		Logger.logDebug("ContextUtils.remove" + getId());
		SimUtils.getContext().remove(this);
	}
	
	protected void migrateOutOfTown() {
	
		SimUtils.getGrid().moveTo(this,  RandomHelper.nextIntFromTo(1, Constants.GRID_VILLAGE_START - 2), 
				RandomHelper.nextIntFromTo(0, Constants.GRID_HEIGHT - 1));
		status = Status.DEAD;
		// Remove partner
		if (getPartner() != null) {
			Logger.logInfo("and takes partner H" + getPartner().getId() + " with her/him");
			getPartner().removeSelf();
		}
		// Also remove children (in removeSelf the child removes the childrenIds id in the parent.
		for (Human child : HumanUtils.getChildrenUnder18(this)) {
			Logger.logInfo("and also child H" + child.getId());
			child.removeSelf();
		}
		removeSelf();
	}
	
	public void breakUpWithPartner() {
		if (partnerId >= 0) {
			getPartner().resetPartnerId();
			resetPartnerId();
		}
	}

	/**
	 * Remove all property but share the house with husband or wife
	 */
	protected void goToElderlyCare() {
		
		Human partner = getPartner();
		if (partner != null && HumanUtils.getOwnedHouse(this) != null) {
			if (HumanUtils.getOwnedHouse(partner) == null) {
				removeAllPropertyExceptHouse();
				int houseId = HumanUtils.getOwnedHouse(this).getId();
				removeProperty(houseId, true);
				partner.connectProperty(houseId);
				Logger.logInfo("H" + id + " gave house " + houseId + " to partner H" + partner.getId());
			}
		}
		removeAndSellAllProperty();
	}
	
	private void shareMoney() {
		
		Human partner = getPartner();
		if (partner != null) {
			partner.addMoney(money);
			money = 0;
		}
		else if (childrenIds.size() > 0) {
			double sharedMoney = money / childrenIds.size();
			for (int childId : childrenIds) {
				HumanUtils.getHumanById(childId).addMoney(sharedMoney);
			}
			money = 0;
		}
	}
	
	/*=========================================
	 * Getters and setters with logic 
	 *========================================
	 */
	public boolean isSingle() {
		
		if (partnerId >= 0) {
			return false;
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
	
	protected double calculateSalary() {
		
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
			if (SimUtils.getBoatByHumanId(id) != null) {
				return Math.round(SimUtils.getBoatByHumanId(id).getFisherPayment(id));
			}
			Logger.logError("Human.calculateSalary: H" + id + " no boat for fisher");
			return 0;
		case CAPTAIN:
			if (SimUtils.getBoatByHumanId(id) != null) {
				return Math.round(SimUtils.getBoatByHumanId(id).getCaptainPayment(id));
			}
			Logger.logError("Human.calculateSalary: H" + id + " no boat for captain");
			return 0;
		case ELDERLY_CARETAKER:
			return Math.round(SimUtils.getElderlyCare().getCaretakerPayment());
		default: // You get nothing
			return 0;
		}
	}
	
	protected double calculateBenefits() {
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
	
	protected double calculateBankruptBenefits() {
		
		if (money < 0) {
			if (SimUtils.getSocialCare().getSavings() > 0) {
				
				double amountOfMoney = Math.max(0, Math.min(Constants.BENEFIT_UNEMPLOYED, SimUtils.getSocialCare().getSavings()));
				SimUtils.getSocialCare().addSavings(-1 * amountOfMoney);
				return amountOfMoney;
			}
		}
		return 0;
	}

	//TODO in this function change the parameter to something that is defined before hand
	protected double payTax(double salary) {
		double payedAsTax = salary * ((Constants.TAX_PERCENTAGE) / 100);
		salary -= payedAsTax;
		if (status != Status.WORK_OUT_OF_TOWN) {
			
			Council council = SimUtils.getObjectsAll(Council.class).get(0);
			council.addSavings(payedAsTax * (Constants.PERC_FROM_TAX_TO_COUNCIL / 100));
			
			return salary;
		}
		return salary;
	}

	public void connectProperty(int propertyId) {
		
		if (!propertyIds.contains(propertyId)) {
			SimUtils.getPropertyById(propertyId).setOwner(id);
			propertyIds.add(propertyId);
			return;
		}
		Logger.logError("Property " + propertyId + " already contained in H" + id);
	}
	
	public void stopWorkingAtWorkplace() {
		
		Logger.logInfo("H" + id + ", status: " + status + " stops working at: " + workplaceId);
		if (workplaceId >= 0) {
			Workplace workplace = (Workplace) SimUtils.getPropertyById(workplaceId);
			if (propertyIds.contains(workplaceId)) {
				removeAndSellProperty(workplaceId, true);
			}
			else if (workplace instanceof Boat) {
				((Boat) workplace).removeFisher(getId());
			}
			status = Status.UNEMPLOYED;
			workplaceId = -1;
		}
	}
	
	/**
	 * Remove and sells property
	 * @param propertyId
	 */
	public void removeAndSellProperty(int propertyId, boolean removeFromArray) {
		
		Logger.logDebug("H" + id + " remove property: " + propertyId);
		if (removeFromArray) 
			propertyIds.remove(propertyIds.indexOf(propertyId));
		Property property = SimUtils.getPropertyById(propertyId);
		money += property.getPrice() * ((double) Constants.PROPERTY_SELL_PERCENTAGE / 100);
		property.removeOwner(id);
	}
	
	/**
	 * Remove without selling
	 * @param propertyId
	 */
	public void removeProperty(int propertyId, boolean removeFromArray) {
		
		Logger.logDebug("H" + id + " remove property: " + propertyId);
		if (removeFromArray)
			propertyIds.remove(propertyIds.indexOf(propertyId));
		Property property = SimUtils.getPropertyById(propertyId);
		property.removeOwner(id);
	}
	
	
	protected void removeAndSellAllProperty() {
		
		for (int propertyId : propertyIds) {
			removeAndSellProperty(propertyId, false);
		}
		propertyIds.clear();
	}
	
	protected void removeAllPropertyExceptHouse() {
		
		int houseId = -1;
		for (int propertyId : propertyIds) {
			if (!(SimUtils.getPropertyById(propertyId) instanceof House)) {
				removeAndSellProperty(propertyId, false);
			}
			else {
				houseId = propertyId;
			}
		}
		if (houseId >= 0) {
			propertyIds.clear();
			propertyIds.add(houseId);
		}
	}
	
	/*=========================================
	 * Standard getters and setters
	 *=========================================
	 */
	private void resetPartnerId() {
		partnerId = -1;
	}
	
	public void setPartner(Human newPartner) {
		partnerId = newPartner.getId();
	}
	
	protected void setStatusByAge() {
		
		if (age < Constants.HUMAN_ADULT_AGE) {
			if (status != Status.CHILD)
				status = Status.CHILD;
		}
		else if (age >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			if (status != Status.ELDEST) {
				status = Status.ELDEST;
				schoolType = SchoolType.NO_SCHOOL;
				removeAndSellAllProperty(); //Maybe don't sell house when partner still lives there
			}
		}
		else if (age >= Constants.HUMAN_ELDERLY_AGE) {
			if (status != Status.ELDER) {
				stopWorkingAtWorkplace();
				status = Status.ELDER;
				schoolType = SchoolType.NO_SCHOOL;
			}
		}
		else if (status == Status.CHILD) {
			status = Status.UNEMPLOYED;
			schoolType = SchoolType.NO_SCHOOL;
		}
	}
	
	public boolean doesHumanDie(int age) {
		double prob = Math.pow((1.0/125) * age, 5) * (1.0/24);
		if (prob > RandomHelper.nextDouble())
			return true;
		return false;
	}
	
	public double getMoney() {
		return money;
	}
	
	public void getNecessaryMoneyFromPartner(double cost) {
		money -= cost;
		necessaryCost += cost;
	}
	
	public void addMoney(double money) {
		this.money += money;
	}
	
	public void setMoney(double money) {
		this.money = money;
	}

	public void addChild(int childId) {
		if (!childrenIds.contains(childId)) {
			childrenIds.add(childId);
		}
		else {
			Logger.logError("H" + id + " child already in childrenIds : " + childId);
		}
	}
	
	public void addParent(int parentId) {
		
		GridPoint parentAncestor = new GridPoint(1, parentId);
		if (!ancestors.contains(parentAncestor)) {
			ancestors.add(parentAncestor);
		}
		else {
			Logger.logError("H" + id + " parent already in ancestors : " + parentId);
		}
	}
	
	public void removeChild(int childId) {
		if (childrenIds.contains(childId)) {
			Logger.logDebug("H" + id + " remove child " + childId + " from parent");
			childrenIds.remove(childrenIds.indexOf(childId));
		}
		else {
			Logger.logError("H" + id + " child not in childrenIds : " + childId);
		}
	}
	
	public void giveIncomeToPartner(double income) {
		money += income;
		nettoIncome += income;
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

	public int getId() {
		return id;
	}

	public boolean getMigrated() {
		return foreigner;
	}
	
	public boolean getForeigner() {
		return foreigner;
	}
	
	public double getNettoIncome() {
		return nettoIncome;
	}

	public double getNecessaryCost() {
		return necessaryCost;
	}
	
	public double getLeftoverMoney() {
		return nettoIncome - necessaryCost;
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
	
	public boolean getHigherEducated() {
		return higherEducated;
	}
	
	public ArrayList<GridPoint> getAncestors() {
		return ancestors;
	}
	
	public Human getPartner() {
		if (partnerId == -1)
			return null;
		Human partner = HumanUtils.getHumanById(partnerId);
		if (partner != null)
			return partner;
		Logger.logError("H"+ id + " has no partner:" + partnerId);
		return null;
	}
	
	public int getPartnerId() {
		return partnerId;
	}
	
	public int getWorkplaceId() {
		return workplaceId;
	}
	
	public void setWorkplaceId(int workplaceId) {
		this.workplaceId = workplaceId;
	}
	
	public ArrayList<Integer> getParentsIds() {
		ArrayList<Integer> parentsIds = new ArrayList<Integer>();
		for (GridPoint ancestor : ancestors) {
			if (ancestor.getX() == 1) {
				parentsIds.add(ancestor.getY());
			}
		}
		return parentsIds;
	}
	
	public String getChildrenIdsString() {
		
		String datum = "";
		for (Integer childId : childrenIds) {
			if (datum.equals("")) {
				datum += Integer.toString(childId);
			}
			else {
				datum += "," + Integer.toString(childId);
			}
		}		
		return datum;
	}
	
	public String getPropertyIdsString() {
		
		String datum = "";
		for (Integer propertyId : propertyIds) {
			if (datum.equals("")) {
				datum += Integer.toString(propertyId);
			}
			else {
				datum += "," + Integer.toString(propertyId);
			}
		}		
		return datum;
	}
	
	public ArrayList<Integer> getChildrenIds() {
		return childrenIds;
	}
	
	public ArrayList<Integer> getPropertyIds() {
		return propertyIds;
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