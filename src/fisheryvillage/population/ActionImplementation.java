package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.Property;

public class ActionImplementation {
	
	public static void executeActionJob(String actionTitle, Human human) {
		
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		switch(actionTitle) {
		case "Job fisher":
			for (Boat boat : boats) {
				if (boat.getVacancy()) {
					actionWorkStartAt(human, boat);
					return ;
				}
			}
			Logger.logError("H" + human.getId() + " no boat found to be fisher at!");
			break;
		case "Job captain":
			for (Boat boat : boats) {
				if (!boat.hasCaptain()) {
					Logger.logAction("H" + human.getId() + " became a captain at : " + boat.getName());
					human.removeMoney(boat.getPrice());
					SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).addEdge(human, boat);
					human.setStatus(Status.CAPTAIN);
					return ;
				}
			}
			Logger.logError("H" + human.getId() + " no boat found to be captain at!");
			break;
		case "Job factory boss":
			human.removeMoney(SimUtils.getFactory().getPrice());
			SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).addEdge(human, SimUtils.getFactory());
			Logger.logAction("H" + human.getId() + " became the factory boss");
			human.setStatus(Status.FACTORY_BOSS);
			break;
		case "Job teacher":
			actionWorkStartAt(human, SimUtils.getSchool());
			break;
		case "Job factory worker":
			actionWorkStartAt(human, SimUtils.getFactory());
			break;
		case "Job work outside village":
			actionWorkStartAt(human, SimUtils.getCompanyOutside());
			break;
		case "Job mayor":
			actionWorkStartAt(human, SimUtils.getCompanyOutside());
			break;
		case "Job unemployed":
			Logger.logAction("H" + human.getId() + " became unemployed");
			human.setStatus(Status.UNEMPLOYED);
			break;
		default:
			Logger.logError("H" + human.getId() + " action '" + actionTitle + "' doesn't exist");
		}
	}	
	
	private static void actionWorkStartAt(Human human, Property property) {
		
		// Standard work start at
		Logger.logAction("H" + human.getId() + " took the job at : " + property.getName());
		human.setStatus(property.getJobStatus());
		if (property instanceof Boat) {
			Boat boat = (Boat) property;
			boat.addFisher(human.getId());
		}
	}
	
	public static void executeActionDonate(String actionTitle, Human human) {
		
		switch(actionTitle) {
		case "Donate nothing":
			Logger.logAction("H" + human.getId() + " donated nothing");
			break;
		case "Donate to council":
			Logger.logAction("H" + human.getId() + " donated $ 250" + " to council");
			actionDonate(human, SimUtils.getCouncil(), 250);
			break;
		}
	}
	
	public static void actionDonate(Human human, Property property, int amount) {
		
		Logger.logAction("H" + human.getId() + " donated money to : " + property.getName());
		human.removeMoney(amount);
		property.addToSavings(amount);
	}
	/*
	public void actionDonate(Property property, int amount) {
		Logger.logAction("H" + id + " donated money to : " + property.getName());
		if (money < amount) {
			Logger.logError("Error money smaller than amount, donation canceled");
			return ;
		}
		money -= amount;
		property.addToSavings(amount);
	}*/
}