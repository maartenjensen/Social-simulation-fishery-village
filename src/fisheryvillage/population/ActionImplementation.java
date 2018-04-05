package fisheryvillage.population;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.Property;

public class ActionImplementation {
	
	public static void executeActionJob(String actionTitle, Human human) {
		
		if (human.getJobTitle().contains(actionTitle)) {
			Logger.logAction("H" + human.getId() + " " + actionTitle + " keeps his job as " + human.getStatus() + ", good for him/her");
			return ;
		}
		
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		switch(actionTitle) {
		case "Job fisher":
			for (Boat boat : boats) {
				if (boat.getVacancy()) {
					actionWorkStartAt(human, boat, actionTitle);
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
					human.setJobTitle(actionTitle);
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
			human.setJobTitle(actionTitle);
			human.setStatus(Status.FACTORY_BOSS);
			break;
		case "Job teacher":
			actionWorkStartAt(human, SimUtils.getSchool(), actionTitle);
			break;
		case "Job factory worker":
			actionWorkStartAt(human, SimUtils.getFactory(), actionTitle);
			break;
		case "Job elderly caretaker":
			actionWorkStartAt(human, SimUtils.getElderlyCare(), actionTitle);
			break;
		case "Job work outside village":
			actionWorkStartAt(human, SimUtils.getCompanyOutside(), actionTitle);
			break;
		case "Job mayor":
			actionWorkStartAt(human, SimUtils.getCouncil(), actionTitle);
			break;
		case "Job unemployed":
			Logger.logAction("H" + human.getId() + " became unemployed");
			human.setJobTitle(actionTitle);
			human.setStatus(Status.UNEMPLOYED);
			break;
		default:
			Logger.logError("H" + human.getId() + " action '" + actionTitle + "' doesn't exist");
		}
	}	
	
	private static void actionWorkStartAt(Human human, Property property, String jobTitle) {
		
		// Standard work start at
		Logger.logAction("H" + human.getId() + " took the job at : " + property.getName());
		human.setStatus(property.getJobStatus());
		human.setJobTitle(jobTitle);
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
			actionDonate(human, SimUtils.getCouncil());
			break;
		}
	}
	
	public static void actionDonate(Human human, Property property) {
		
		if (human.getNettoIncome() >= human.getNecessaryCost()) {
			double amount = (human.getNettoIncome() - human.getNecessaryCost()) * Constants.DONATE_FACTOR_OF_LEFT_OVER_MONEY;
			human.removeMoney(-amount);
			property.addToSavings(amount);
			Logger.logAction("H" + human.getId() + " donated " + amount + " money to : " + property.getName() + ", total money is " + human.getMoney());
		}
		else if (human.getMoney() > Constants.DONATE_MONEY_MINIMUM_SAVINGS_WITHOUT_INCOME) {
			human.removeMoney(-Constants.DONATE_MONEY_WITHOUT_INCOME);
			property.addToSavings(Constants.DONATE_MONEY_WITHOUT_INCOME);
			Logger.logAction("H" + human.getId() + " donated " + Constants.DONATE_MONEY_WITHOUT_INCOME + " money to : " + property.getName() + ", total money is " + human.getMoney());
		}
		else
		{
			Logger.logError("H" + human.getId() + " netto income: " + human.getNettoIncome() + " not exceeding necessary cost: " + human.getNecessaryCost());
		}
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