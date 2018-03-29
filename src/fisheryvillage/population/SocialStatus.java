package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
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
	
	public SocialStatus(int workLevel, int eventLevel) {
		this.workLevel = workLevel;
		this.eventLevel = eventLevel;
	}
	
	public void setSocialStatusWork(Status status) {
		workLevel = Constants.SOCIAL_STATUS_WORK.get(status);
	}
	
	public void setEventOrganizer() {
		eventLevel = Constants.SOCIAL_STATUS_EVENT_ORGANIZE;
	}
	
	public void setEventAttendee() {
		eventLevel = Constants.SOCIAL_STATUS_EVENT_ATTEND;
	}
	
	public void setEventNone() {
		eventLevel = Constants.SOCIAL_STATUS_EVENT_NONE;
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
}