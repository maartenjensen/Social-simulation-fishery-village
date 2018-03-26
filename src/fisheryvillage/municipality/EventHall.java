package fisheryvillage.municipality;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
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
* @since 2018-03-20
*/
public class EventHall extends Property {

	ArrayList<Event> events = new ArrayList<Event>();
	
	public EventHall(int price, int maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 5, 3, Status.NONE, PropertyColor.COUNCIL);
		addToValueLayer();
	}
	
	public boolean getVacancyForNewEvent() {
		
		int eventPeoplePotential = SimUtils.getCouncil().getNumberOfAdults() + SimUtils.getCouncil().getNumberOfElderlyYoung();
		int eventPeopleCapacity = 0;
		for (Event event : events) {
			eventPeopleCapacity += event.getMaxAttendees() + 1; // + 1 for the organizer
		}
		if (eventPeopleCapacity < eventPeoplePotential - 1) { // - 1 because of the organizer
			return true;
		}
		return false;
	}
	
	public int createEvent(String eventType, int organizerId) {
		
		Event event = new Event(eventType, organizerId);
		events.add(event);
		return event.getCostInitial();
	}
	
	public final ArrayList<Event> getEventsWithVacancy(int id) {
		
		ArrayList<Event> possibleEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (event.getVacancyForAttendee(id)) {
				possibleEvents.add(event);
			}
		}
		return possibleEvents;
	}
	
	public int joinEvent(Event event, int id) {
		
		event.addAttendee(id);
		return event.getAttendeeFee();
	}

	public void stepPerformSocialEvent() {
		
		for (Event event : events) {
			event.stepPerformEvent();
		}
	}
	
	public void stepResetEventHall() {
		events.clear();
	}
	
	@Override
	public String getLabel() {
		String label = "Event hall $: " + getSavings() + "\n";
		for (Event event : events) {
			label += event.toString() + "\n";
		}
		return label;
	}
	
	@Override
	public VSpatial getSpatial() {
		return spatialImagesOwned.get(true);
	}
}