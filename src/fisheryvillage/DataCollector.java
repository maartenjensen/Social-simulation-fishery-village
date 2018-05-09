package fisheryvillage;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Resident;
import repast.simphony.space.grid.GridPoint;

public class DataCollector {

	public DataCollector(GridPoint location) {
		
		SimUtils.getContext().add(this);
		if (!SimUtils.getGrid().moveTo(this, location.getX(), location.getY())) {
			Logger.logError("DataCollector could not be placed, coordinate: " + location);
		}
	}
	
	public int satisfiedValuesCount(int number) {
		int count = 0;
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			if (resident.getSatisfiedValuesCount() == number && resident.getAge() >= Constants.HUMAN_ADULT_AGE && resident.getAge() < Constants.HUMAN_ELDERLY_AGE)
				count ++;
		}
		return count;
	}
	
	public int getSatisfiedValues0() {
		return satisfiedValuesCount(0);
	}

	public int getSatisfiedValues1() {
		return satisfiedValuesCount(1);
	}

	public int getSatisfiedValues2() {
		return satisfiedValuesCount(2);
	}
	
	public int getSatisfiedValues3() {
		return satisfiedValuesCount(3);
	}
	
	public int getSatisfiedValues4() {
		return satisfiedValuesCount(4);
	}
	
	public String getLabel() {
		
		return "DataCollector";
	}
}
