package fisheryvillage.property.municipality;

import java.util.ArrayList;

import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;

public class Event {
	
	private int costInitial = 30;
	private int maxAttendees = 10;
	private int costAttendee = 5;
	private int attendeeFee;
	private String eventType = "NONE";
	
	int organizerId = -1;
	ArrayList<Integer> attendeesIds = new ArrayList<Integer>();
	
	Event(String eventType, int organizerId) {
		if (eventType == "Free") {
			attendeeFee = 0;
		}
		else if (eventType == "Commercial") {
			attendeeFee = 15;
		}
		this.eventType = eventType;
		setOrganizer(organizerId);
	}
	
	public void stepPerformEvent() {
		
		Logger.logInfo("PERFORM " + toString());
		if (organizerId != -1) {
			HumanUtils.getResidentById(organizerId).actionSocialEventOrganize();
			for (int attendee : attendeesIds) {
				 HumanUtils.getResidentById(attendee).actionSocialEventAttend();
			}
		}
		
	}
	
	public boolean getVacancyForAttendee(int id) {
		if (attendeesIds.size() < maxAttendees && organizerId != id && organizerId != -1) {
			return true;
		}
		return false;
	}
	
	public void addAttendee(int id) {
		if (attendeesIds.size() < maxAttendees) {
			attendeesIds.add(id);
		}
		else {
			Logger.logError("Error Event.addAttendee: exceeded capacity of event at H" + id);
		}
	}
	
	public void setOrganizer(int organizerId) {
		if (this.organizerId == -1) { 
			this.organizerId = organizerId;
		}
		else {
			Logger.logError("Error EventType.setOrganizer: original organizer:" + this.organizerId + ", new organizer:" + organizerId);
		}
	}
	
	public int getCostInitial() {
		return costInitial;
	}
	
	public int getMaxAttendees() {
		return maxAttendees;
	}
	
	public int getCostAttendee() {
		return costAttendee;
	}
	
	public int getAttendeeFee() {
		return attendeeFee;
	}
	
	public String toString() {
		
		//String string = "Event " + eventType + " - organizer: " + organizerId + "\nAttendees [" + attendeesIds.size() + "/" + maxAttendees + "]: ";
		String string = eventType + " - O:" + organizerId + ", A[" + attendeesIds.size() + "/" + maxAttendees + "]: ";
		for (int attendee : attendeesIds) {
			string += Integer.toString(attendee) + ",";
		}
		return string;
	}
}