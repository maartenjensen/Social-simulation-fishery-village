package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class Workplace extends Property {

	protected ArrayList<Status> allJobs = new ArrayList<Status>();
	
	public Workplace(int id, int price, int maintenanceCost, double savings, GridPoint location, int width, int height, PropertyColor propertyColor) {
		super(id, price, maintenanceCost, savings, location, width, height, propertyColor);
	}
	
	public ArrayList<Status> getVacancy(boolean hasBeenFisher, double money) {
		return new ArrayList<Status>();
	}
	
	public int getEmployeeCount(Status jobStatus) {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		int employees = 0;
		for (final Human human: humans) {
			if (human.getStatus() == jobStatus && human.getWorkplaceId() == getId()) {
				employees ++;
			}
		}
		return employees;
	}
}