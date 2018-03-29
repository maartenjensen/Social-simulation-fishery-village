package fisheryvillage.population;

import fisheryvillage.property.Property;

public class ActionImplementation {
	
	public static void executeAction(Human human, String actionTitle) {
		
		switch(actionTitle) {
		case "job fisher":
			
			break;
		case "job captain":
			break;
		}
	}
	
	public void actionWorkStartAt(Human human, Property property) {
		
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