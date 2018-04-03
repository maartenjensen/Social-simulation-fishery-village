package fisheryvillage.municipality;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.Property;
import fisheryvillage.property.PropertyColor;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The Council divides money, that is earned through taxing, between the school, social care,
* elderly care and the factory.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Council extends Property {

	private double moneyForSchool = 0;
	private double moneyForSocialCare = 0;
	private double moneyForElderlyCare = 0;
	private double moneyForFactory = 0;
	
	public Council(int price, int maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 3, 3, Status.MAYOR, PropertyColor.COUNCIL);
		addToValueLayer();
		actionName = "Job mayor";
	}
	
	//TODO make this dependent on values of residents
	public void stepDistributeMoney() {
		
		double savingsPartition = getSavings() / 8;
		moneyForSchool = savingsPartition;
		moneyForSocialCare = savingsPartition;
		moneyForElderlyCare = savingsPartition;
		moneyForFactory = savingsPartition;
		
		if (SimUtils.getSchool().getSavings() < Constants.BUILDING_MONEY_DANGER_LEVEL)
			moneyForSchool += getSavings() / 2;
		else if (SimUtils.getSocialCare().getSavings() < Constants.BUILDING_MONEY_DANGER_LEVEL)
			moneyForSocialCare += getSavings() / 2;
		else if (SimUtils.getElderlyCare().getSavings() < Constants.BUILDING_MONEY_DANGER_LEVEL)
			moneyForElderlyCare += getSavings() / 2;
		else if (SimUtils.getFactory().getSavings() < Constants.BUILDING_MONEY_DANGER_LEVEL)
			moneyForFactory += getSavings() / 2;
		
		SimUtils.getSchool().addToSavings(moneyForSchool);
		SimUtils.getSocialCare().addToSavings(moneyForSocialCare);
		SimUtils.getElderlyCare().addToSavings(moneyForElderlyCare);
		SimUtils.getFactory().addToSavings(moneyForFactory);
		
		removeFromSavings(- (moneyForSchool + moneyForSocialCare + moneyForElderlyCare + moneyForFactory));
	}
	
	public boolean getVacancy() {
		if (hasMayor()) {
			return false;
		}
		return true;
	}
	
	public boolean hasMayor() {
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.MAYOR) {
				return true;
			}
		}
		return false;
	}
	
	public String getDate() {

		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		int year = (int) Math.floor(tick / Constants.TICKS_PER_YEAR) + 1;
		int month = (int) Math.floor((tick % Constants.TICKS_PER_YEAR) / Constants.TICKS_PER_MONTH) + 1;
		int tickInt = (int) (tick % Constants.TICKS_PER_MONTH) + 1;
		return "Year: " + year + ", month: " + month + ", tick:" + tickInt + "/" + Constants.TICKS_PER_MONTH;
	}
	
	@Override
	public VSpatial getSpatial() {
		if (hasMayor()) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getName() {
		return "Council";
	}
	
	@Override
	public String getLabel() {
		return "Council, $: " + Math.round(getSavings()) + "\nSchool $:" + Math.round(moneyForSchool) + "\nSocialCare $:" + Math.round(moneyForSocialCare) + 
				"\nElderlyCare $:" + Math.round(moneyForElderlyCare) + "\nFactory $:" + Math.round(moneyForFactory) + "\n\n"+getDate();
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
	
	public int getNumberOfCaptains() {
		return getNumberOfStatus(Status.CAPTAIN);		
	}

	public int getNumberOfElderlyCaretakers() {
		return getNumberOfStatus(Status.ELDERLY_CARETAKER);
	}
	
	public int getNumberOfWorkersOutside() {
		return getNumberOfStatus(Status.WORK_OUT_OF_TOWN);
	}
	
	public int getNumberOfMayor() {
		return getNumberOfStatus(Status.MAYOR);
	}
	
	public int getNumberOfBoss() {
		return getNumberOfStatus(Status.FACTORY_BOSS);
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
	
	public int getSocialStatusRange() {
		
		int min = Integer.MAX_VALUE;
		int max = 0;
		
		ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		for (Human human: humans) {
			min = Math.min(min, human.getSocialLevelCombined());
			max = Math.max(max, human.getSocialLevelCombined());
		}
		
		Logger.logDebug("Check unsatisfied universalist, min:" + min + ", max:" + max);
		return Math.abs(max - min);
	}
	
}