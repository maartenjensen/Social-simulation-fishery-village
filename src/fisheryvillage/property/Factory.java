package fisheryvillage.property;

import java.awt.Color;
import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class Factory extends Property {

	private int maxEmployees = 20;
	
	public Factory(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 10, 10, Status.FACTORY_WORKER);
		colorId = 5;
		addToValueLayer();
	}

	public int getFactoryWorkerCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		int workers = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.FACTORY_WORKER) {
				workers ++;
			}
		}
		return workers;
	}
	
	public boolean getVacancy() {
		
		if (getFactoryWorkerCount() < maxEmployees) {
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
		return "Factory: W:" + getFactoryWorkerCount() + "/" + maxEmployees + ", $:" + Math.round(getSavings());
	}
}