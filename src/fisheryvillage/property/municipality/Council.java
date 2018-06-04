package fisheryvillage.property.municipality;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Resident;
import fisheryvillage.population.Status;
import fisheryvillage.property.PropertyColor;
import fisheryvillage.property.Workplace;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The Council divides money, that is earned through taxing, between the school, social care,
* elderly care and the factory.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Council extends Workplace {

	private double moneyForSchool = 0;
	private double moneyForSocialCare = 0;
	private double moneyForElderlyCare = 0;
	private double moneyForFactory = 0;
	
	private int countUniversalism = 0;
	private int countTradition = 0;
	private int countPower = 0;
	private int countSelfDirection = 0;
	
	private int decideSavings = 0;
	
	private int schoolCount = 1;
	private int socialCount = 1;
	private int elderlyCount = 1;
	private int factoryCount = 1;
	
	public Council(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 3, 3, PropertyColor.COUNCIL);
		allJobs.add(Status.MAYOR);
		addToValueLayer();
	}
	
	public void resetCounts() {
		countUniversalism = 0;
		countTradition = 0;
		countPower = 0;
		countSelfDirection = 0;
	}
	
	//TODO make this dependent on values of residents
	public void stepDistributeMoney() {
		
		if (decideSavings >= Constants.TICKS_PER_YEAR) {
			
			decideSavings = 0;
			
			if (hasMayor()) {
				
				setBuildingSavingsCount();
			}
		}
		
		ArrayList<Double> moneyPart = new ArrayList<Double>();
		moneyPart.add(getSavings() / 2);
		moneyPart.add(getSavings() / 4);
		moneyPart.add(getSavings() / 8);
		moneyPart.add(getSavings() / 16);

		moneyForSchool = moneyPart.get(schoolCount);
		moneyForSocialCare = moneyPart.get(socialCount);
		moneyForElderlyCare = moneyPart.get(elderlyCount);
		moneyForFactory = moneyPart.get(factoryCount);
		
		SimUtils.getSchool().addSavings(moneyForSchool);
		SimUtils.getSocialCare().addSavings(moneyForSocialCare);
		SimUtils.getElderlyCare().addSavings(moneyForElderlyCare);
		SimUtils.getFactory().addSavings(moneyForFactory);
		
		addSavings(-1 * (moneyForSchool + moneyForSocialCare + moneyForElderlyCare + moneyForFactory));
		
		decideSavings ++;
	}
	
	public void setBuildingSavingsCount() {
		
		boolean share = false;
		if ((0.25 + getMayor().getUniversalismImportanceDistribution() * 0.75) > RandomHelper.nextDouble()) {
			share = true;
		}
		
		if (share) {
			
			schoolCount = 0;
			socialCount = 0;
			elderlyCount = 0;
			factoryCount = 0;
			
			double schoolMoney = SimUtils.getSchool().getSavings();
			double socialCareMoney = SimUtils.getSocialCare().getSavings();
			double elderlyCareMoney = SimUtils.getElderlyCare().getSavings();
			double factoryMoney = SimUtils.getFactory().getSavings();
			
			ArrayList<Integer> buildingSavings = new ArrayList<Integer>();
			buildingSavings.add((int) Math.ceil(schoolMoney));
			buildingSavings.add((int) Math.ceil(socialCareMoney));
			buildingSavings.add((int) Math.ceil(elderlyCareMoney));
			buildingSavings.add((int) Math.ceil(factoryMoney));
			
			for (int i = 0; i < 4; i ++) {
				if (schoolMoney > buildingSavings.get(i)) {
					schoolCount += 1;
				}
			}
			for (int i = 0; i < 4; i ++) {
				if (socialCareMoney > buildingSavings.get(i)) {
					socialCount += 1;
				}
			}
			for (int i = 0; i < 4; i ++) {
				if (elderlyCareMoney > buildingSavings.get(i)) {
					elderlyCount += 1;
				}
			}
			for (int i = 0; i < 4; i ++) {
				if (factoryMoney > buildingSavings.get(i)) {
					factoryCount += 1;
				}
			}
		}
		else {
			schoolCount = 1;
			socialCount = 1;
			elderlyCount = 1;
			factoryCount = 1;
		}
	}
	
	@Override
	public ArrayList<Status> getVacancy(boolean hasBeenFisher, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		if (!hasMayor()) {
			possibleJobs.add(Status.MAYOR);
		}
		return possibleJobs;
	}
	
	public Resident getMayor() {
		return getOwner();
	}
	
	public boolean hasMayor() {
		Resident resident = getOwner();
		if (resident != null) {
			if (resident.getStatus() == Status.MAYOR)
				return true;
			Logger.logError("H" + resident.getId() + " error in hasMayor ");
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
		return "Council" + " [" + getId() + "]";
	}
	
	@Override
	public String getLabel() {
		return "Council" + "[" + getId() + "], $: " + Math.round(getSavings()) + "\nSchool $:" + Math.round(moneyForSchool) + "\nSocialCare $:" + Math.round(moneyForSocialCare) + 
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
	
	public int getNumberOfAdultsAndElderlyYoung() {
		return getNumberOfAge(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_CARE_AGE);
	}
	
	public int getNumberOfPeople() {
		return getNumberOfAge(0, Integer.MAX_VALUE);
	}
	
	public void increaseUniversalism() {
		countUniversalism ++;
	}
	
	public void increaseTradition() {
		countTradition ++;
	}
	
	public void increasePower() {
		countPower ++;
	}
	
	public void increaseSelfDirection() {
		countSelfDirection ++;
	}
	
	public double getUniversalism() {
		int adultCount = getNumberOfAdultsAndElderlyYoung();
		return countUniversalism / (double) adultCount;
	}
	
	public double getTradition() {
		int adultCount = getNumberOfAdultsAndElderlyYoung();
		return countTradition / (double) adultCount;
	}
	
	public double getPower() {
		int adultCount = getNumberOfAdultsAndElderlyYoung();
		return countPower / (double) adultCount;
	}
	
	public double getSelfDirection() {
		int adultCount = getNumberOfAdultsAndElderlyYoung();
		return countSelfDirection / (double) adultCount;
	}
	
	public double getAvgUniversalism() {
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		int count = 0;
		double universalism = 0;
		for (Resident resident : residents) {
			if (resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_CARE_AGE) {
				count ++;
				universalism += resident.getLevelUniversalism();
			}
		}
		if (count > 0) {
			return universalism / count;
		}
		else {
			return 0;
		}
	}
	
	
	public double getAvgTradition() {
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		int count = 0;
		double tradition = 0;
		for (Resident resident : residents) {
			if (resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_CARE_AGE) {
				count ++;
				tradition += resident.getLevelTradition();
			}
		}
		if (count > 0) {
			return tradition / count;
		}
		else {
			return 0;
		}
	}
	
	public double getAvgPower() {
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		int count = 0;
		double power = 0;
		for (Resident resident : residents) {
			if (resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_CARE_AGE) {
				count ++;
				power += resident.getLevelPower();
			}
		}
		if (count > 0) {
			return power / count;
		}
		else {
			return 0;
		}
	}

	public double getAvgSelfDirection() {
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		int count = 0;
		double selfdirection = 0;
		for (Resident resident : residents) {
			if (resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_CARE_AGE) {
				count ++;
				selfdirection += resident.getLevelSelfDirection();
			}
		}
		if (count > 0) {
			return selfdirection / count;
		}
		else {
			return 0;
		}
	}
}