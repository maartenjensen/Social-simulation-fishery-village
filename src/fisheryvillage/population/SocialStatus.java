package fisheryvillage.population;

import java.util.HashMap;

import fisheryvillage.property.HouseType;
import valueframework.AbstractValue;
import valueframework.DecisionMaker;
/**
* Social status class that deals with a human's social status
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SocialStatus {

	HashMap<StatusType, Double> statusMap = new HashMap<StatusType, Double>();
	
	public SocialStatus() {
		statusMap.put(StatusType.WEALTH_JOB, 0.0);
		statusMap.put(StatusType.WEALTH_HOUSE, 0.0);
		statusMap.put(StatusType.FISHER_ECOL, 0.0);
		statusMap.put(StatusType.FISHER_ECON, 0.0);
		//donation, boat wealth, social life event, free event
	}
	
	/**
	 * Formula from The Dynamics of Reinforcement Learning in Cooperative Multiagent Systems – Caroline Claus
	 * Section 2.3 Reinforcement learning
	 * @param statusType
	 * @param updateRate double between 0 and 1, a higher value will discard the past more, a value of 1 will completely discard the past
	 * @param value 
	 */
	private void updateStatus(StatusType statusType, double updateRate, double value) {
		
		double s = statusMap.get(statusType);
		double newS = s + updateRate * (value - s);
		statusMap.put(statusType, newS);
	}
	
	public void setSocialStatusWork(Status status) {
		updateStatus(StatusType.WEALTH_JOB, 0.1, status.getSocialStatusWork());
	}
	
	public void setSocialStatusHouse(HouseType houseType) {
		updateStatus(StatusType.WEALTH_HOUSE, 0.1, houseType.getSocialStatusHouse());
	}
	
	public void setSocialStatusFisher(String fishingActionName) {
		
		FishingAction fishingAction = FishingAction.getEnumByString(fishingActionName);
		updateStatus(StatusType.FISHER_ECOL, 0.1, fishingAction.getFisherEcol());
		updateStatus(StatusType.FISHER_ECON, 0.1, fishingAction.getFisherEcon());
	}
	
	public void setSocialStatusEvent(String eventAction) {
		//updateStatus(StatusType.WEALTH_HOUSE, 0.1, houseType.getSocialStatusHouse());
	}
	
	public double getSocialStatusValue(DecisionMaker decisionMaker, Status status) {
		
		double socialStatusValue = 0;
		double weights = 0;
		double powerWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) / 100;
		double universalismWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM) / 100;
		//double traditionWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION) / 100;
		
		socialStatusValue += statusMap.get(StatusType.WEALTH_JOB) * powerWeight;
		weights += powerWeight;
		socialStatusValue += statusMap.get(StatusType.WEALTH_HOUSE) * powerWeight;
		weights += powerWeight;
		
		if (status == Status.FISHER || status == Status.CAPTAIN) {
			socialStatusValue += statusMap.get(StatusType.FISHER_ECOL) * universalismWeight;
			weights += universalismWeight;
			socialStatusValue += statusMap.get(StatusType.FISHER_ECON) * powerWeight;
			weights += powerWeight;
		}

		socialStatusValue = socialStatusValue / weights;
		return socialStatusValue;
	}
	
	public double getSocialStatusWork() {
		return statusMap.get(StatusType.WEALTH_JOB);
	}
	
	public double getSocialStatusHouse() {
		return statusMap.get(StatusType.WEALTH_HOUSE);
	}
	
	public double getSocialStatusFishEcol() {
		return statusMap.get(StatusType.FISHER_ECOL);
	}
	
	public double getSocialStatusFishEcon() {
		return statusMap.get(StatusType.FISHER_ECON);
	}
}