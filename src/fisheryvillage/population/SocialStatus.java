package fisheryvillage.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fisheryvillage.property.BoatType;
import fisheryvillage.property.HouseType;
import repast.simphony.random.RandomHelper;
import valueframework.AbstractValue;
import valueframework.DecisionMaker;
import valueframework.ValuedAction;
/**
* Social status class that deals with a human's social status
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SocialStatus {

	double learningRate = 0.1;
	double noticeableDifference = 0.1;
	
	HashMap<StatusType, Double> statusMap = new HashMap<StatusType, Double>();
	
	public SocialStatus() {
		statusMap.put(StatusType.WEALTH_JOB, 0.5);
		statusMap.put(StatusType.WEALTH_HOUSE, 0.5);
		statusMap.put(StatusType.WEALTH_BOAT, 0.5);
		statusMap.put(StatusType.FISHER_ECON, 0.5);
		statusMap.put(StatusType.FISHER_ECOL, 0.5);
		statusMap.put(StatusType.ORGANIZE_FREE, 0.5);
		statusMap.put(StatusType.DONATION, 0.5);
		statusMap.put(StatusType.EVENTS, 0.5);
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
		updateStatus(StatusType.WEALTH_JOB, learningRate, status.getSocialStatusWork());
	}

	public void setSocialStatusHouse(HouseType houseType) {
		updateStatus(StatusType.WEALTH_HOUSE, learningRate, houseType.getSocialStatusHouse());
	}

	public void setSocialStatusBoat(BoatType boatType) {
		updateStatus(StatusType.WEALTH_BOAT, learningRate, boatType.getSocialStatusBoat());
	}

	public void setSocialStatusFisher(String fishingActionName) {
		
		FishingAction fishingAction = FishingAction.getEnumByString(fishingActionName);
		updateStatus(StatusType.FISHER_ECOL, learningRate, fishingAction.getFisherEcol());
		updateStatus(StatusType.FISHER_ECON, learningRate, fishingAction.getFisherEcon());
	}

	public void setSocialStatusEvent(String eventAction, boolean canOrganize) {
		if (eventAction.equals("Organize free event")) {
			updateStatus(StatusType.EVENTS, learningRate, 1);
			updateStatus(StatusType.ORGANIZE_FREE, learningRate, 1);
		}
		else if (eventAction.equals("Organize commercial event")) {
			updateStatus(StatusType.EVENTS, learningRate, 1);
			updateStatus(StatusType.ORGANIZE_FREE, learningRate, 0);
		}
		else if (eventAction.equals("Attend free event") || eventAction.equals("Attend commercial event")) {
			updateStatus(StatusType.EVENTS, learningRate, 0.5);
			if (canOrganize)
				updateStatus(StatusType.ORGANIZE_FREE, learningRate, 0);
		}
	}

	public ValuedAction getBestActionEvent(DecisionMaker decisionMaker, ArrayList<ValuedAction> eventActions, boolean canOrganize) {
		
		if (eventActions.size() == 1) 
			return eventActions.get(0);
		
		double sum = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) + decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM)
					 + decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
		double universalism = decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM) / sum;
		double tradition = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION) / sum;

		double organizeFree = statusMap.get(StatusType.ORGANIZE_FREE);
		if (canOrganize) {
			organizeFree = 0;
		}

		HashMap<ValuedAction, Double> statusMap = new HashMap<ValuedAction, Double>();
		for (ValuedAction eventAction : eventActions) {
			if (eventAction.getTitle().equals("Organize free event")) {
				statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), tradition * 1 + universalism * 1));
			}
			else if (eventAction.getTitle().equals("Organize commercial event")) {
				statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), tradition * 1 + universalism * 0));
			}
			else if (eventAction.getTitle().equals("Attend free event") || eventAction.getTitle().equals("Attend commercial event")) {
				statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), tradition * 0.5 + universalism * organizeFree));
			}
		}
		return selectBestAction(statusMap);
	}

	public ValuedAction getBestActionWork(DecisionMaker decisionMaker, ArrayList<ValuedAction> eventActions) {
		
		if (eventActions.size() == 1) 
			return eventActions.get(0);
		
		double sum = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) + decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM)
					 + decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
		double power = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) / sum;

		HashMap<ValuedAction, Double> statusMap = new HashMap<ValuedAction, Double>();
		for (ValuedAction eventAction : eventActions) {
			statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), power * ActionImplementation.getStatusFromString(eventAction.getTitle()).getSocialStatusWork() ));
		}
		return selectBestAction(statusMap);
	}

	public ValuedAction getBestActionDonate(DecisionMaker decisionMaker, ArrayList<ValuedAction> eventActions) {
		
		if (eventActions.size() == 1) 
			return eventActions.get(0);
		
		double sum = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) + decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM)
					 + decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
		double universalism = decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM) / sum;
		double tradition = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION) / sum;
		
		HashMap<ValuedAction, Double> statusMap = new HashMap<ValuedAction, Double>();
		for (ValuedAction eventAction : eventActions) {
			if (eventAction.getTitle().equals("Donate nothing")) {
				statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), 0));
			}
			else if (eventAction.getTitle().equals("Donate to council")) {
				statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), universalism * 1 + tradition * 1)); //TODO tradition
			}
		}
		return selectBestAction(statusMap);
	}

	public ValuedAction getBestActionFish(DecisionMaker decisionMaker, ArrayList<ValuedAction> eventActions) {
		
		if (eventActions.size() == 1) 
			return eventActions.get(0);
		
		double sum = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) + decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM)
					 + decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION);
		double power = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) / sum;
		double universalism = decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM) / sum;

		HashMap<ValuedAction, Double> statusMap = new HashMap<ValuedAction, Double>();
		for (ValuedAction eventAction : eventActions) {
			FishingAction fishingAction = FishingAction.getEnumByString(eventAction.getTitle());
			statusMap.put(eventAction, getCalculate(eventAction.getActionGoodness(), power * fishingAction.getFisherEcon() + universalism * fishingAction.getFisherEcol() ));
		}
		return selectBestAction(statusMap);
	}

	/**
	 * Calculate combined value of goodness (Schwartz based) and social
	 * The epsilon 0.01 is there to prevent a value to be zero. It can be the
	 * case that the valueGoodness of all actions is zero, using epsilon those actions
	 * can still be valued based on their social status influence.
	 * @param valueGoodness
	 * @param valueSocial
	 * @return
	 */
	private double getCalculate(double valueGoodness, double valueSocial) {
		return Math.max(valueGoodness + valueSocial, 0.01);
	}

	/**
	 * Takes the double and normalizes all the doubles. Then takes a random double
	 * and returns probability dependent a ValuedAction
	 * @param statusMap
	 * @return
	 */
	private ValuedAction selectBestAction(HashMap<ValuedAction, Double> statusMap) {

		double combinedValues = 0;
		for (ValuedAction key : statusMap.keySet()) {
			combinedValues += statusMap.get(key);
		}
		for (ValuedAction key : statusMap.keySet()) {
			statusMap.put(key, statusMap.get(key) / combinedValues);
		}
		double prob = RandomHelper.nextDouble();
		double value = 0;
		ValuedAction keyBackup = null;
		for (ValuedAction key : statusMap.keySet()) {
			value += statusMap.get(key);
			if (prob <= value)
				return key;
			keyBackup = key;
		}
		return keyBackup;
	}

	public void setSocialStatusDonation(String donationAction) {
		if (donationAction.equals("Donate to council")) {
			updateStatus(StatusType.DONATION, learningRate, 1);
		}
		else {
			updateStatus(StatusType.DONATION, learningRate, 0);
		}
	}

	/**
	 * Half of the social status is based on 
	 * @param status
	 * @return
	 */
	public double getSocialStatusPower(Status status) {
		
		double socialStatusPower = 0;
		double division = 2;
		socialStatusPower += statusMap.get(StatusType.WEALTH_JOB);
		socialStatusPower += statusMap.get(StatusType.WEALTH_HOUSE);
		if (status == Status.FISHER || status == Status.CAPTAIN) {
			division += 2;
			socialStatusPower += statusMap.get(StatusType.WEALTH_BOAT);
			socialStatusPower += statusMap.get(StatusType.FISHER_ECON);
		}
		return socialStatusPower / division;
	}
	
	public double getSocialStatusUniversalism(Status status) {
		double socialStatusUniversalism = 0;
		double division = 2;
		socialStatusUniversalism += statusMap.get(StatusType.DONATION);
		socialStatusUniversalism += statusMap.get(StatusType.ORGANIZE_FREE);
		if (status == Status.FISHER || status == Status.CAPTAIN) {
			division += 2;
			socialStatusUniversalism += statusMap.get(StatusType.FISHER_ECOL) * 2;
		}
		return socialStatusUniversalism / division;
	}

	public double getSocialStatusTradition() {

		return (statusMap.get(StatusType.EVENTS) + statusMap.get(StatusType.DONATION)) / 2;
	}
	
	public double getSocialStatusValue(DecisionMaker decisionMaker, Status status) {
		
		double powerWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.POWER) / 100;
		double universalismWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.UNIVERSALISM) / 100;
		double traditionWeight = decisionMaker.getAbstractValueThreshold(AbstractValue.TRADITION) / 100;
		
		double socialStatusValue =  powerWeight * getSocialStatusPower(status) +
									universalismWeight * getSocialStatusUniversalism(status) +
									traditionWeight * getSocialStatusTradition();
		double division = powerWeight + universalismWeight + traditionWeight;
		
		return socialStatusValue / division;
	}
	
	public double getSocialStatusWork() {
		return statusMap.get(StatusType.WEALTH_JOB);
	}

	public double getSocialStatusHouse() {
		return statusMap.get(StatusType.WEALTH_HOUSE);
	}
	
	public double getSocialStatusBoat() {
		return statusMap.get(StatusType.WEALTH_BOAT);
	}
	
	public double getSocialStatusFishEcol() {
		return statusMap.get(StatusType.FISHER_ECOL);
	}
	
	public double getSocialStatusFishEcon() {
		return statusMap.get(StatusType.FISHER_ECON);
	}

	public double getSocialStatusDonation() {
		return statusMap.get(StatusType.DONATION);
	}
	
	public double getSocialStatusEvents() {
		return statusMap.get(StatusType.EVENTS);
	}
	
	public double getSocialStatusOrganizeFree() {
		return statusMap.get(StatusType.ORGANIZE_FREE);
	}
	
	public void setSocialStatusFromData(List<String> data) {
		statusMap.put(StatusType.WEALTH_JOB, Double.parseDouble(data.get(1)));
		statusMap.put(StatusType.WEALTH_HOUSE, Double.parseDouble(data.get(2)));
		statusMap.put(StatusType.WEALTH_BOAT, Double.parseDouble(data.get(3)));
		statusMap.put(StatusType.FISHER_ECOL, Double.parseDouble(data.get(4)));
		statusMap.put(StatusType.FISHER_ECON, Double.parseDouble(data.get(5)));
		statusMap.put(StatusType.DONATION, Double.parseDouble(data.get(6)));
		statusMap.put(StatusType.EVENTS, Double.parseDouble(data.get(7)));
		statusMap.put(StatusType.ORGANIZE_FREE, Double.parseDouble(data.get(8)));
	}

	public String getSocialStatusString() {
		String string = "" + statusMap.get(StatusType.WEALTH_JOB);
		string += "," + statusMap.get(StatusType.WEALTH_HOUSE);
		string += "," + statusMap.get(StatusType.WEALTH_BOAT);
		string += "," + statusMap.get(StatusType.FISHER_ECOL);
		string += "," + statusMap.get(StatusType.FISHER_ECON);
		string += "," + statusMap.get(StatusType.DONATION);
		string += "," + statusMap.get(StatusType.EVENTS);
		string += "," + statusMap.get(StatusType.ORGANIZE_FREE);
		return string;
	}
}