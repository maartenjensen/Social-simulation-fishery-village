package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* An abstraction of all types of work outside the village
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class CompanyOutside extends Property {

	private int maxEmployees = 100;
	
	public CompanyOutside(int price, int maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 10, 10, Status.WORK_OUT_OF_TOWN, PropertyColor.COMPANY);
		addToValueLayer();
		actionName = "Job work outside village";
	}

	public int getEmployeeCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		int employees = 0;
		for (final Human human: humans) {
			if (human.getStatus() == jobStatus) {
				employees ++;
			}
		}
		return employees;
	}
	
	public boolean getVacancy() {
		
		if (getEmployeeCount() < maxEmployees) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getName() {
		return "Company outside";
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "Company: W:" + getEmployeeCount() + "/" + maxEmployees;
	}	
}