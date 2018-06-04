package fisheryvillage.common;

import java.util.ArrayList;

import fisheryvillage.population.Human;
import fisheryvillage.population.Resident;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.Property;
import fisheryvillage.property.municipality.ElderlyCare;
import fisheryvillage.property.municipality.School;
import fisheryvillage.property.other.SchoolOutside;
import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

/**
* Supports the Human class with relevant getter functions
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public final strictfp class HumanUtils {

	private static int newHumanId = 0;

	/**
	 * This returns a new Id. ++ is used after the variable to make sure
	 * the current newHumanId is returned
	 * @return a new unused id for a resident
	 */
	public static int getNewHumanId() {
		return newHumanId++;
	}

	public static void resetHumanId() {
		newHumanId = 0;
	}
	
	public static void setHumanId(int newHumanId) {
		HumanUtils.newHumanId = newHumanId;
	}
	
	public static Human getHumanById(int id) {
		
		ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (Human human : humans) {
			if (human.getId() == id) {
				return human;
			}
		}
		Logger.logError("Human with id:" + id + " does not exist");
		return null;
	}
	
	/**
	 * The same as getHumanById but gives no error message when the human is not found
	 * @param id
	 * @return
	 */
	public static Human getHumanByIdNoException(int id) {
		
		ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (Human human : humans) {
			if (human.getId() == id) {
				return human;
			}
		}
		return null;
	}
	
	public static Resident getResidentById(int id) {
		
		ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		for (Resident resident : residents) {
			if (resident.getId() == id) {
				return resident;
			}
		}
		Logger.logError("Resident with id:" + id + " does not exist");
		return null;
	}
	
	/**
	 * Returns the house the agents lives in and other wise
	 * the homelesscare
	 * @return
	 */
	public static Property getLivingPlace(Human human) {

		if (human.getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return SimUtils.getObjectsAll(ElderlyCare.class).get(0);
		}
		
		// Has a house OR partner has a house
		if (isOwningHouse(human)) { 
			if (getOwnedHouse(human) != null) {
				return getOwnedHouse(human);
			}
			else {
				return getOwnedHouse(human.getPartner());
			}
		}
		else { // If doesn't have a house go to parents house
			House parentsHouse = getParentsHouse(human);
			if (parentsHouse != null) {
				return parentsHouse;
			}
			else { // Go to homeless care
				return SimUtils.getSocialCare();
			}
		}
	}
	
	/**
	 * Returns the house the agents lives in and other wise
	 * the homeless care
	 * @return
	 */
	public static HouseType getLivingPlaceType(Human human) {

		if (human.getAge() >= Constants.HUMAN_ELDERLY_CARE_AGE) {
			return HouseType.WITH_OTHERS;
		}
		
		// Has a house OR partner has a house
		if (isOwningHouse(human)) { 
			if (getOwnedHouse(human) != null) {
				return getOwnedHouse(human).getHouseType();
			}
			else {
				Human partner = human.getPartner();
				if (partner != null) {
					House house = getOwnedHouse(partner);
					if (house != null)
						return house.getHouseType();
					else {
						Logger.logError("H" + human.getId() + " house does not exist: H" + partner.getId() + " should own properties: " + partner.getPropertyIdsString());
						return null;
					}
				}
				else {
					Logger.logError("H" + human.getId() + " is owning house but partner does not exist, partnerId: " + human.getPartnerId());
					return null;
				}
			}
		}
		else { 
			House parentsHouse = getParentsHouse(human);
			if (parentsHouse != null) {
				return HouseType.WITH_OTHERS;
			}
			else { // Go to homeless care
				return HouseType.HOMELESS;
			}
		}
	}
	
	public static boolean isPotentialCouple(Human human1, Human human2) {
		
		if (human1.getAge() >= Constants.HUMAN_ADULT_AGE && human1.isSingle()) {
			if (human1.isMan()) {
				if (!human2.isMan() && (human2.getAge() + Constants.HUMAN_RELATIVE_WIFE_MIN_AGE) >= human1.getAge()
										&& (human2.getAge() - Constants.HUMAN_RELATIVE_WIFE_MAX_AGE) <= human1.getAge()) {
					return true;
				}
			}
			else {
				if (human2.isMan()  && human2.getAge() >= (human1.getAge() + Constants.HUMAN_RELATIVE_WIFE_MIN_AGE)
										&& human2.getAge() <= (human1.getAge() - Constants.HUMAN_RELATIVE_WIFE_MAX_AGE)) {
					return true;
				}
			}	
		}
		return false;
	}
	
	public static boolean isLivingTogetherWithPartner(Human human) {
		Human partner = human.getPartner();
		if (partner != null) {
			if (getOwnedHouse(human) != null ^ getOwnedHouse(partner) != null) { //Exclusive OR used (^)
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOwningHouse(Human human) {

		if (getOwnedHouse(human) != null) {
			return true;
		}
		Human partner = human.getPartner();
		if (partner != null) {
			if (getOwnedHouse(partner) != null) {
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<Human> getChildrenUnder18(Human human) {
		
		ArrayList<Human> children = new ArrayList<Human>();
		for (Integer childId : human.getChildrenIds()) {
			Human child = HumanUtils.getHumanById(childId);
			if (child.getAge() < 18) {
				children.add(child);
			}
		}
		return children;
	}
	
	public static ArrayList<Human> getParents(Human human) {
		
		ArrayList<Human> parents = new ArrayList<Human>();
		for (Integer parentId : human.getParentsIds()) {
			Human parent = HumanUtils.getHumanByIdNoException(parentId);
			if (parent != null)
				parents.add(parent);
		}
		return parents;
	}

	public static House getParentsHouse(Human human) {
		
		ArrayList<Human> parents = HumanUtils.getParents(human);
		if (parents.size() >= 1) {
			for (final Human parent : parents) {
				if (isOwningHouse(parent)) { 
					if (getOwnedHouse(parent) != null) {
						return getOwnedHouse(parent);
					}
					else {
						return getOwnedHouse(parent.getPartner());
					}
				}				
			}
		}
		return null;
	}
	
	public static Property getWorkingPlace(int workplaceId, Status status, SchoolType schoolType) {
		
		if (status == Status.CHILD) {
			if (schoolType == SchoolType.INSIDE_VILLAGE) {
				return SimUtils.getObjectsAll(School.class).get(0);
			}
			else if (schoolType == SchoolType.OUTSIDE_VILLAGE) {
				return SimUtils.getObjectsAll(SchoolOutside.class).get(0);
			}
			return null;
		}
		else {
			Property workplace = SimUtils.getPropertyById(workplaceId);
			if (workplace != null) {
				return workplace;
			}
			else {
				return null;
			}
		}
	}

	public static House getOwnedHouse(Human human) {
		
		for (Integer propertyId : human.getPropertyIds()) {
			Property property = SimUtils.getPropertyById(propertyId);
			if (property instanceof House) {
				return (House) property;
			}
		}
		return null;
	}

	public static ArrayList<Property> getOwnedProperty(Human human) {
		
		ArrayList<Property> properties = new ArrayList<Property>();
		for (Integer propertyId : human.getPropertyIds()) {
			properties.add(SimUtils.getPropertyById(propertyId));
		}
		return properties;
	}
	
	public static int spawnChild(Human mother, Human father) {

		Context<Object> context = SimUtils.getContext();
		Logger.logDebug("m." + mother.getId() + ", f." + father.getId() + "spawnChild()");
		final Human child = new Resident(getNewHumanId(), SimUtils.getRandomBoolean(), false, 0, Constants.HUMAN_INIT_STARTING_MONEY);
		Logger.logDebug("Pre child.setAncestors(), An moth:" + mother.getAncestors() + ", an fath:" + father.getAncestors());
		child.setAncestors(mother.getId(), father.getId(), mother.getAncestors(), father.getAncestors());
		// Add child to parents
		mother.addChild(child.getId());
		father.addChild(child.getId());
		// Put child on the grid
		@SuppressWarnings("unchecked")
		Grid<Object> grid = (Grid<Object>) context.getProjection(Constants.ID_GRID);
		GridPoint newLocation = HumanUtils.getParentsHouse(child).getFreeLocationExcluded(child);
		grid.moveTo(child, newLocation.getX(), newLocation.getY());
		return child.getId();
	}
	
	public static boolean cellFreeOfHumans(GridPoint cellLocation) {

		Grid<Object> grid = (Grid<Object>) SimUtils.getGrid();
		Iterable<Object> objectsOnGrid = grid.getObjectsAt(cellLocation.getX(), cellLocation.getY());
		for (final Object object : objectsOnGrid) {
			if (object instanceof Human) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean cellFreeOfHumansExcluded(GridPoint cellLocation, Human humanExcluded) {
		
		Grid<Object> grid = (Grid<Object>) SimUtils.getGrid();
		Iterable<Object> objectsOnGrid = grid.getObjectsAt(cellLocation.getX(), cellLocation.getY());
		for (final Object object : objectsOnGrid) {
			if (object instanceof Human) {
				if ((Human) object != humanExcluded) {
					return false;
				}
			}
		}
		return true;
	}
}
