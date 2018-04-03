package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
/**
* Social status class that deals with a human's social status
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SocialStatus {

	int workLevel;
	int eventLevel;
	
	public void drainSocialStatus() {
		int drainAmount = 4;
		int amount = getCombinedLevel();
		workLevel = (int) Math.max(0, workLevel - ((double) workLevel / amount) * drainAmount);
		eventLevel = (int) Math.max(0, eventLevel - ((double) eventLevel / amount) * drainAmount);
	}
	
	public SocialStatus(int workLevel, int eventLevel) {
		this.workLevel = workLevel;
		this.eventLevel = eventLevel;
	}
	
	public void setSocialStatusWork(Status status) {
		if (workLevel < Constants.SOCIAL_STATUS_WORK.get(status)) {
			workLevel = Constants.SOCIAL_STATUS_WORK.get(status);
		}
	}
	
	public void setEventOrganizer() {
		if (eventLevel < Constants.SOCIAL_STATUS_EVENT_ORGANIZE) {
			eventLevel = Constants.SOCIAL_STATUS_EVENT_ORGANIZE;
		}
	}
	
	public void setEventAttendee() {
		if (eventLevel < Constants.SOCIAL_STATUS_EVENT_ATTEND) {
			eventLevel = Constants.SOCIAL_STATUS_EVENT_ATTEND;
		}
	}
	
	public int getWorkLevel() {
		return workLevel;
	}
	
	public int getEventLevel() {
		return eventLevel;
	}
	
	public int getCombinedLevel() {
		return workLevel + eventLevel;
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
	
	public boolean getUnsatisfiedUniversalist() {
		
		if (getSocialStatusRange() > 125) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public double getSocialLevel() {
		
		int summationLevel = 0;
		ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		for (Human human: humans) {
			if (human.getStatus() != Status.CHILD && human.getStatus() != Status.ELDEST && human.getStatus() != Status.DEAD) {
				summationLevel += human.getSocialStatus().getCombinedLevel();
			}
		}
		return (double) getCombinedLevel() / summationLevel;
	}
	
	public boolean getBelowAverage() {
		if (getSocialLevel() < (1.0 / SimUtils.getCouncil().getNumberOfAdults()))
			return true;
		return false;
	}
}