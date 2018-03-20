package fisheryvillage.municipality;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.Property;
import fisheryvillage.property.PropertyColor;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The Council divides money, that is earned through taxing, between the school, social care,
* elderly care and the factory.
*
* @author Maarten Jensen
*/
public class Council extends Property {

	private int year = 2000;
	private double moneyForSchool = 0;
	private double moneyForSocialCare = 0;
	private double moneyForElderlyCare = 0;
	private double moneyForFactory = 0;
	
	public Council(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 3, 3, Status.NONE, PropertyColor.COUNCIL);
		addToValueLayer();
	}
	
	//TODO make this dependent on values of residents
	public void stepDistributeMoney() {
		
		double savingsPartition = getSavings() / 4;
		moneyForSchool = savingsPartition;
		moneyForSocialCare = savingsPartition;
		moneyForElderlyCare = savingsPartition;
		moneyForFactory = savingsPartition;
		
		SimUtils.getSchool().addToSavings(moneyForSchool);
		SimUtils.getSocialCare().addToSavings(moneyForSocialCare);
		SimUtils.getElderlyCare().addToSavings(moneyForElderlyCare);
		SimUtils.getFactory().addToSavings(moneyForFactory);
		
		removeFromSavings(-getSavings());
		Logger.logDebug("Does the error come afer this? Then it is in the steps of repast");
	}
	
	public String getDate() {
		year ++;
		return Integer.toString(year);
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}
	
	@Override
	public String getLabel() {
		return "Council, $: " + Math.round(getSavings()) + "\nSchool $:" + Math.round(moneyForSchool) + "\nSocialCare $:" + Math.round(moneyForSocialCare) + 
				"\nElderlyCare $:" + Math.round(moneyForElderlyCare) + "\nFactory $:" + Math.round(moneyForFactory) + "\n\nYear: "+getDate();
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
		return 0;
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
	
	public int getNumberOfAdults() {
		return getNumberOfAge(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE);
	}
	
	public int getNumberOfElderlyYoung() {
		return getNumberOfAge(Constants.HUMAN_ELDERLY_AGE, Constants.HUMAN_ELDERLY_CARE_AGE);
	}
	
	public int getNumberOfElderlyOld() {
		return getNumberOfAge(Constants.HUMAN_ELDERLY_CARE_AGE, Integer.MAX_VALUE);
	}
}