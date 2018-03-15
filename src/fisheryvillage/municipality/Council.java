package fisheryvillage.municipality;

import java.awt.Color;
import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.Property;
import repast.simphony.space.grid.GridPoint;

public class Council extends Property {

	public Council(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 3, 3, Status.NONE);
		colorId = 5;
		addToValueLayer();
	}
	
	public void stepDistributeMoney() {
		
		double moneyForSchool = getSavings() / 10;
		Logger.logOutputLn("Money for school:" + Math.round(moneyForSchool));
		SimUtils.getSchool().addToSavings(moneyForSchool);
		//removeFromSavings(-moneyForSchool);
		removeFromSavings(-getSavings() * 0.5);
		Logger.logOutputLn("Does the error come afer this? Then it is in the steps of repast");
	}
	
	@Override
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}
	
	@Override
	public String getLabel() {
		return "Council, $:" + Math.round(getSavings());
	}
	
	public int getNumberOfStatus(Status status) {
		ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int count = 0;
		for (Human human : humans) {
			if (human.getStatus() == status) {
				count ++;
			}
		}
		return count;
	}
	
	public int getNumberOfUnemployed() {
		return getNumberOfStatus(Status.UNEMPLOYED);
	}
	
	public int getNumberOfTeachers() {
		return getNumberOfStatus(Status.TEACHER);
	}
	
	public int getNumberOfFactoryWorkers() {
		return getNumberOfStatus(Status.FACTORY_WORKER);
	}
	
	public int getNumberOfFishers() {
		return getNumberOfStatus(Status.FISHER);
	}
	
	public int getNumberOfHomelessCaretakers() {
		return getNumberOfStatus(Status.HOMELESS_CARERTAKER);
	}
	
	public int getNumberOfElderlyCaretakers() {
		return getNumberOfStatus(Status.ELDERLY_CARETAKER);
	}
	
	public int getNumberOfWorkersOutside() {
		return getNumberOfStatus(Status.WORK_OUT_OF_TOWN);
	}
	
	public int getNumberOfAge(int minimum, int maximum) {
		ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int count = 0;
		for (Human human : humans) {
			if (human.getAge() >= minimum && human.getAge() < maximum) {
				count ++;
			}
		}
		return count;
	}
	
	public int getNumberOfChildren() {
		return getNumberOfAge(0, Constants.HUMAN_ADULT_AGE);
	}
	
	public int getNumberOfAdultsYoung() {
		return getNumberOfAge(Constants.HUMAN_ADULT_AGE, 40);
	}
	
	public int getNumberOfAdultsOld() {
		return getNumberOfAge(40, Constants.HUMAN_ELDERLY_AGE);
	}
	
	public int getNumberOfElderlyYoung() {
		return getNumberOfAge(Constants.HUMAN_ELDERLY_AGE, Constants.HUMAN_ELDERLY_CARE_AGE);
	}
	
	public int getNumberOfElderlyOld() {
		return getNumberOfAge(Constants.HUMAN_ELDERLY_CARE_AGE, Integer.MAX_VALUE);
	}
}