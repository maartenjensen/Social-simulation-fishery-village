package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

public class CompanyOutside extends Property {

	private int maxEmployees = 100;
	
	public CompanyOutside(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 10, 10, Status.WORK_OUT_OF_TOWN, PropertyColor.COMPANY);
		addToValueLayer();
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
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "Company: W:" + getEmployeeCount() + "/" + maxEmployees;
	}	
}