package fisheryvillage;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Resident;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class DataCollector {

	public DataCollector(GridPoint location) {
		
		SimUtils.getContext().add(this);
		if (!SimUtils.getGrid().moveTo(this, location.getX(), location.getY())) {
			Logger.logError("DataCollector could not be placed, coordinate: " + location);
		}
	}
	
	/**
	 * Average satisfied values times 0.25 which means the percentage of satisfied values is shown
	 * @param ageMin
	 * @param ageMaxAddOne
	 * @return
	 */
	public double averageSatisfiedValues(int ageMin, int ageMaxAddOne, Status status) {
		
		int valuesSatisfied = 0;
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getAge() >= ageMin && resident.getAge() < ageMaxAddOne && (status == Status.NONE || resident.getStatus() == status)) {
				valuesSatisfied += resident.getSatisfiedValuesCount();
				count ++;
			}
		}
		
		if (count > 0) {
			return (((double) valuesSatisfied) / count) * 0.25;
		}
		else {
			return -0.05;
		}
	}

	public double getAverageSatisfiedValuesElderly() {
		return averageSatisfiedValues(Constants.HUMAN_ELDERLY_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getAverageSatisfiedValuesAdult() {
		return averageSatisfiedValues(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE, Status.NONE);
	}
	
	public double getAverageSatisfiedValuesAdultElderly() {
		return averageSatisfiedValues(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getAverageSatisfiedValuesCaptain() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.CAPTAIN);
	}
	
	public double getAverageSatisfiedValuesCaretaker() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.ELDERLY_CARETAKER);
	}
	
	public double getAverageSatisfiedValuesBoss() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_BOSS);
	}
	
	public double getAverageSatisfiedValuesWorker() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_WORKER);
	}
	
	public double getAverageSatisfiedValuesFisher() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FISHER);
	}
	
	public double getAverageSatisfiedValuesMayor() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.MAYOR);
	}

	public double getAverageSatisfiedValuesTeacher() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.TEACHER);
	}
	
	public double getAverageSatisfiedValuesUnemployed() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.UNEMPLOYED);
	}
	
	public double getAverageSatisfiedValuesWorkOutOfTown() {
		return averageSatisfiedValues(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.WORK_OUT_OF_TOWN);
	}
	
	public double averageSocialStatus(int ageMin, int ageMaxAddOne, Status status) {
		
		double valuesSatisfied = 0;
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getAge() >= ageMin && resident.getAge() < ageMaxAddOne && (status == Status.NONE || resident.getStatus() == status)) {
				valuesSatisfied += resident.getSocialStatusValue();
				count ++;
			}
		}
		
		if (count > 0) {
			return valuesSatisfied / count;
		}
		else {
			return -0.05;
		}
	}

	public double getAverageSocialStatusElderly() {
		return averageSocialStatus(Constants.HUMAN_ELDERLY_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getAverageSocialStatusAdult() {
		return averageSocialStatus(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE, Status.NONE);
	}
	
	public double getAverageSocialStatusAdultElderly() {
		return averageSocialStatus(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getAverageSocialStatusCaptain() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.CAPTAIN);
	}
	
	public double getAverageSocialStatusCaretaker() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.ELDERLY_CARETAKER);
	}
	
	public double getAverageSocialStatusBoss() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_BOSS);
	}
	
	public double getAverageSocialStatusWorker() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_WORKER);
	}
	
	public double getAverageSocialStatusFisher() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FISHER);
	}
	
	public double getAverageSocialStatusMayor() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.MAYOR);
	}

	public double getAverageSocialStatusTeacher() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.TEACHER);
	}
	
	public double getAverageSocialStatusUnemployed() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.UNEMPLOYED);
	}
	
	public double getAverageSocialStatusWorkOutOfTown() {
		return averageSocialStatus(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.WORK_OUT_OF_TOWN);
	}
	
	public double happyPercentage(int ageMin, int ageMaxAddOne, Status status) {
		
		int happy = 0;
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getAge() >= ageMin && resident.getAge() < ageMaxAddOne && (status == Status.NONE || resident.getStatus() == status)) {
				count ++;
				if (resident.getIsHappy()) {
					happy ++;
				}
			}
		}
		if (count > 0) {
			return ((double) happy) / count;
		}
		else {
			return -0.05;
		}
	}
	
	public double notHappyPercentage(int ageMin, int ageMaxAddOne, Status status) {
		
		int notHappy = 0;
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getAge() >= ageMin && resident.getAge() < ageMaxAddOne && (status == Status.NONE || resident.getStatus() == status)) {
				count ++;
				if (!resident.getIsHappy()) {
					notHappy ++;
				}
			}
		}
		return ((double) notHappy) / count;
	}
	
	public double getHappyPercentageAdult() {
		return happyPercentage(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE, Status.NONE);
	}
	
	public double getHappyPercentageElderly() {
		return happyPercentage(Constants.HUMAN_ELDERLY_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getHappyPercentageAdultElderly() {
		return happyPercentage(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_CARE_AGE, Status.NONE);
	}
	
	public double getHappyPercentageCaptain() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.CAPTAIN);
	}
	
	public double getHappyPercentageCaretaker() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.ELDERLY_CARETAKER);
	}
	
	public double getHappyPercentageBoss() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_BOSS);
	}
	
	public double getHappyPercentageWorker() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FACTORY_WORKER);
	}
	
	public double getHappyPercentageFisher() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.FISHER);
	}
	
	public double getHappyPercentageMayor() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.MAYOR);
	}

	public double getHappyPercentageTeacher() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.TEACHER);
	}
	
	public double getHappyPercentageUnemployed() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.UNEMPLOYED);
	}
	
	public double getHappyPercentageWorkOutOfTown() {
		return happyPercentage(0, Constants.HUMAN_ELDERLY_CARE_AGE, Status.WORK_OUT_OF_TOWN);
	}

	/**
	 * Retrieves a distribution
	 * @param number
	 * @return
	 */
	public double satisfiedValuesCount(int number) {
		int total = 0;
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_AGE) {
				total ++;
				if (resident.getSatisfiedValuesCount() == number) {
					count ++;
				}
			}
		}
		return ((double) count) / total;
	}
	
	public double getSatisfiedValues0() {
		return satisfiedValuesCount(0);
	}

	public double getSatisfiedValues1() {
		return satisfiedValuesCount(1);
	}

	public double getSatisfiedValues2() {
		return satisfiedValuesCount(2);
	}
	
	public double getSatisfiedValues3() {
		return satisfiedValuesCount(3);
	}
	
	public double getSatisfiedValues4() {
		return satisfiedValuesCount(4);
	}
	
	public String getLabel() {
		
		return "DataCollector";
	}
}
