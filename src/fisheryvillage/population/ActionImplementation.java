package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.BoatType;
import fisheryvillage.property.Property;
import fisheryvillage.property.Workplace;
import fisheryvillage.property.municipality.Event;
import fisheryvillage.property.municipality.EventHall;
import fisheryvillage.property.municipality.Factory;

public class ActionImplementation {
	
	public static void executeActionJob(String actionTitle, Resident resident) {
		
		if (resident.getJobActionName().contains(actionTitle)) {
			Logger.logAction("H" + resident.getId() + " " + actionTitle + " keeps his job as " + resident.getStatus() + ", good for him/her");
			return ;
		}
		
		resident.stopWorkingAtWorkplace();
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		switch(actionTitle) {
		case "Job fisher":
			for (Boat boat : boats) {
				if (boat.getVacancy(false, resident.getMoney()).contains(Status.FISHER)) {
					actionWorkStartAt(resident, boat, Status.FISHER);
					resident.setHasBeenFisher();
					return ;
				}
			}
			Logger.logError("H" + resident.getId() + " no boat found to be fisher at!");
			break;
		case "Job captain":
			for (Boat boat : boats) {
				if (!boat.hasCaptain() && !boat.getDisabled()) {
					if (resident.getMoney() > BoatType.LARGE.getPrice()) {
						boat.setBoatType(BoatType.LARGE);
					}
					else if (resident.getMoney() > BoatType.MEDIUM.getPrice()) {
						boat.setBoatType(BoatType.MEDIUM);
					}
					else if (resident.getMoney() > BoatType.SMALL.getPrice()) {
						boat.setBoatType(BoatType.SMALL);
					}
					else {
						Logger.logError("H" + resident.getId() + " could not pay the boat!");
					}
					resident.addMoney(-1 * boat.getPrice());
					resident.connectProperty(boat.getId());
					resident.setJobActionName(actionTitle);
					resident.setStatus(Status.CAPTAIN);
					resident.setWorkplaceId(boat.getId());
					resident.setHasBeenFisher();
					Logger.logAction("H" + resident.getId() + " became a captain at : " + boat.getName());
					return ;
				}
			}
			Logger.logError("H" + resident.getId() + " no boat found to be captain at!");
			break;
		case "Job factory boss":
			Factory factory = SimUtils.getFactory();
			resident.addMoney(-1 * factory.getPrice());
			resident.connectProperty(factory.getId());
			Logger.logAction("H" + resident.getId() + " became the factory boss");
			resident.setJobActionName(actionTitle);
			resident.setStatus(Status.FACTORY_BOSS);
			resident.setWorkplaceId(factory.getId());
			break;
		case "Job teacher":
			actionWorkStartAt(resident, SimUtils.getSchool(), Status.TEACHER);
			break;
		case "Job factory worker":
			actionWorkStartAt(resident, SimUtils.getFactory(), Status.FACTORY_WORKER);
			break;
		case "Job elderly caretaker":
			actionWorkStartAt(resident, SimUtils.getElderlyCare(), Status.ELDERLY_CARETAKER);
			break;
		case "Job work outside village":
			actionWorkStartAt(resident, SimUtils.getCompanyOutside(), Status.WORK_OUT_OF_TOWN);
			break;
		case "Job mayor":
			actionWorkStartAt(resident, SimUtils.getCouncil(), Status.MAYOR);
			resident.connectProperty(SimUtils.getCouncil().getId());
			break;
		case "Job unemployed":
			Logger.logAction("H" + resident.getId() + " became unemployed");
			resident.setJobActionName(actionTitle);
			resident.setStatus(Status.UNEMPLOYED);
			break;
		default:
			Logger.logError("H" + resident.getId() + " action '" + actionTitle + "' doesn't exist");
		}
	}	
	
	private static void actionWorkStartAt(Resident resident, Workplace workplace, Status jobStatus) {
		
		// Standard work start at
		Logger.logAction("H" + resident.getId() + " took the job at : " + workplace.getName());
		resident.setStatus(jobStatus);
		resident.setJobActionName(jobStatus.getJobActionName());
		resident.setWorkplaceId(workplace.getId());
		if (workplace instanceof Boat) {
			Boat boat = (Boat) workplace;
			boat.addFisher(resident.getId());
		}
	}
	
	public static void executeActionDonate(String actionTitle, Resident resident) {
		
		switch(actionTitle) {
		case "Donate nothing":
			Logger.logAction("H" + resident.getId() + " donated nothing");
			resident.setGraphDonateType(1);
			break;
		case "Donate to council":
			actionDonate(resident, SimUtils.getCouncil());
			resident.setGraphDonateType(2);
			break;
		default:
			Logger.logError("H" + resident.getId() + " action '" + actionTitle + "' doesn't exist");
		}
	}

	public static void executeActionEvent(String actionTitle, Resident resident) {
		
		int moneyEvent = 0;
		EventHall eventHall = SimUtils.getEventHall();
		ArrayList<Event> possibleEvents = eventHall.getEventsWithVacancy(resident.getId());
		switch(actionTitle) {
		case "Organize free event":
			moneyEvent = eventHall.createEvent("Free", resident.getId());
			resident.addMoney(-1 * moneyEvent);
			resident.setGraphEventType(1);
			Logger.logAction("CREATE EVENT F - H" + resident.getId() + " event cost: " + moneyEvent);
			break;
		case "Organize commercial event":
			moneyEvent = eventHall.createEvent("Commercial", resident.getId());
			resident.addMoney(-1 * moneyEvent);
			resident.setGraphEventType(2);
			Logger.logAction("CREATE EVENT C - H" + resident.getId() + " event cost: " + moneyEvent);
			
			break;
		case "Attend free event":
			for (Event event : possibleEvents) {
				if (event.getEventType().equals("Free")) {
					Logger.logAction("ATTEND EVENT F - H" + resident.getId());
					eventHall.joinEvent(event, resident.getId());
					resident.setGraphEventType(3);
					break;
				}
			}
			break;
		case "Attend commercial event":
			for (Event event : possibleEvents) {
				if (event.getEventType().equals("Commercial")) {
					Logger.logAction("ATTEND EVENT C - H" + resident.getId());
					eventHall.joinEvent(event, resident.getId());
					resident.setGraphEventType(4);
					break;
				}
			}
			break;
		default:
			Logger.logError("H" + resident.getId() + " action '" + actionTitle + "' doesn't exist");
		}
	}
	
	public static void actionDonate(Resident resident, Property property) {
		
		if (resident.getNettoIncome() >= resident.getNecessaryCost()) {
			double amount = (resident.getNettoIncome() - resident.getNecessaryCost()) * Constants.DONATE_FACTOR_OF_LEFT_OVER_MONEY;
			resident.addMoney(-1 * amount);
			property.addSavings(amount);
			Logger.logAction("H" + resident.getId() + " donated " + amount + " money to : " + property.getName() + ", total money is " + resident.getMoney());
		}
		else if (resident.getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			resident.addMoney(-1 * Constants.DONATE_MONEY_WITHOUT_INCOME);
			property.addSavings(Constants.DONATE_MONEY_WITHOUT_INCOME);
			Logger.logAction("H" + resident.getId() + " donated " + Constants.DONATE_MONEY_WITHOUT_INCOME + " money to : " + property.getName() + ", total money is " + resident.getMoney());
		}
		else
		{
			Logger.logError("H" + resident.getId() + " netto income: " + resident.getNettoIncome() + " not exceeding necessary cost: " + resident.getNecessaryCost());
		}
	}
	
	public static Status getStatusFromString(String actionTitle) {
		
		switch(actionTitle) {
		case "Job fisher":
			return Status.FISHER;
		case "Job captain":
			return Status.CAPTAIN;
		case "Job factory boss":
			return Status.FACTORY_BOSS;
		case "Job teacher":
			return Status.TEACHER;
		case "Job factory worker":
			return Status.FACTORY_WORKER;
		case "Job elderly caretaker":
			return Status.ELDERLY_CARETAKER;
		case "Job work outside village":
			return Status.WORK_OUT_OF_TOWN;
		case "Job mayor":
			return Status.MAYOR;
		case "Job unemployed":
			return Status.UNEMPLOYED;
		default:
			return Status.NONE;
		}
	}	
}