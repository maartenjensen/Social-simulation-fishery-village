package fisheryvillage.common;

import java.util.ArrayList;

import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import fisheryvillage.property.CompanyOutside;
import fisheryvillage.property.ElderlyCare;
import fisheryvillage.property.Factory;
import fisheryvillage.property.HomelessCare;
import fisheryvillage.property.House;
import fisheryvillage.property.Property;
import fisheryvillage.property.School;
import fisheryvillage.property.SchoolOutside;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

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
				return getOwnedHouse(getPartner(human));
			}
		}
		else { 
			House parentsHouse = getParentsHouse(human);
			if (parentsHouse != null) {
				return parentsHouse;
			}
			else { // Go to homeless care
				return SimUtils.getObjectsAll(HomelessCare.class).get(0);
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
		Human partner = getPartner(human);
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
		Human partner = getPartner(human);
		if (partner != null) {
			if (getOwnedHouse(partner) != null) {
				return true;
			}
		}
		return false;
	}
	
	public static Human getPartner(Human human) {
		
		final Iterable<RepastEdge<Object>> partnerEdges = SimUtils.getNetwork(Constants.ID_NETWORK_COUPLE).getEdges(human);
		for (final RepastEdge<Object> partnerEdge : partnerEdges) {
			if (partnerEdge.getSource() != human) {
				return (Human) partnerEdge.getSource();
			}
			else {
				return (Human) partnerEdge.getTarget();
			}
		}
		return null;
	}
	
	public static ArrayList<Human> getChildrenUnder18(Human human) {
		
		ArrayList<Human> children = new ArrayList<Human>();
		final Iterable<RepastEdge<Object>> childrenEdges = SimUtils.getNetwork(Constants.ID_NETWORK_CHILDREN).getOutEdges(human);
		for (final RepastEdge<Object> childrenEdge : childrenEdges) {
			if (!((Human) childrenEdge.getTarget()).isAdult()) {
				children.add((Human) childrenEdge.getTarget());
			}
		}
		return children;
	}
	
	public static ArrayList<Human> getParents(Human human) {
		
		final Iterable<RepastEdge<Object>> parentsEdges = SimUtils.getNetwork(Constants.ID_NETWORK_CHILDREN).getInEdges(human); //In edges are edges of this parents
		ArrayList<Human> parents = new ArrayList<Human>();
		for (final RepastEdge<Object> parentEdge : parentsEdges) {
			parents.add((Human) parentEdge.getSource());
		}
		return parents;
	}

	public static House getParentsHouse(Human human) {
		
		ArrayList<Human> parents = HumanUtils.getParents(human);
		if (parents.size() >= 1) {
			for (final Human parent : parents) {
				if (HumanUtils.getOwnedHouse(parent) != null) {
					return HumanUtils.getOwnedHouse(parent);
				}
			}
		}
		return null;
	}
	
	public static Property getWorkingPlace(Status status, SchoolType schoolType) {
		switch(status) {
		case TEACHER:
			return SimUtils.getObjectsAll(School.class).get(0);
		case FACTORY_WORKER:
			return SimUtils.getObjectsAll(Factory.class).get(0);
		case WORK_OUT_OF_TOWN:
			return SimUtils.getObjectsAll(CompanyOutside.class).get(0);
		case CHILD:
			if (schoolType == SchoolType.INSIDE_VILLAGE) {
				return SimUtils.getObjectsAll(School.class).get(0);
			}
			else if (schoolType == SchoolType.OUTSIDE_VILLAGE) {
				return SimUtils.getObjectsAll(SchoolOutside.class).get(0);
			}
		default:
			return null;
		}
	}

	public static House getOwnedHouse(Human human) {
		
		final Iterable<RepastEdge<Object>> propertyEdges = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).getOutEdges(human);
		for (final RepastEdge<Object> propertyEdge : propertyEdges) {
			if (propertyEdge.getTarget() instanceof House) {
				return (House) propertyEdge.getTarget();
			}
		}
		return null;
	}

	public static ArrayList<Property> getOwnedProperty(Human human) {
		
		final Iterable<RepastEdge<Object>> propertyEdges = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).getOutEdges(human);
		ArrayList<Property> properties = new ArrayList<Property>();
		for (final RepastEdge<Object> propertyEdge : propertyEdges) {
			properties.add((Property) propertyEdge.getTarget());
		}
		return properties;
	}
	
	public static void spawnChild(Human mother, Human father) {

		Context<Object> context = SimUtils.getContext();
		Logger.logOutputLn("m." + mother.getId() + ", f." + father.getId() + "spawnChild()");
		final Human child = new Human(SimUtils.getRandomBoolean(), 0, getNewHumanId(), 0, false, false);
		System.out.println("Pre child.setAncestors(), An moth:" + mother.getAncestors() + ", an fath:" + father.getAncestors());
		child.setAncestors(mother.getId(), father.getId(), mother.getAncestors(), father.getAncestors());
		// Make connection with parents
		@SuppressWarnings("unchecked")
		Network<Object> networkChildren = (Network<Object>) context.getProjection(Constants.ID_NETWORK_CHILDREN);
		networkChildren.addEdge(mother, child);
		networkChildren.addEdge(father, child);
		// Put child on the grid
		@SuppressWarnings("unchecked")
		Grid<Object> grid = (Grid<Object>) context.getProjection(Constants.ID_GRID);
		GridPoint newLocation = HumanUtils.getParentsHouse(child).getFreeLocationExcluded(child);
		grid.moveTo(child, newLocation.getX(), newLocation.getY());
		
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
