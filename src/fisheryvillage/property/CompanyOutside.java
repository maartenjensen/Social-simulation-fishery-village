package fisheryvillage.property;

import java.awt.Color;
import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class CompanyOutside extends Property {

	private int maxEmployees = 100;
	
	public CompanyOutside(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 10, 10, Status.WORK_OUT_OF_TOWN);
		colorId = 5;
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
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}

	@Override
	public String getLabel() {
		return "Company: W:" + getEmployeeCount() + "/" + maxEmployees;
	}
}