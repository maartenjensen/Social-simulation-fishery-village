package fisheryvillage.property;

import java.awt.Color;

import fisheryvillage.common.Constants;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class HomelessCare extends Property {
	
	public HomelessCare(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 6, Status.HOMELESS_CARERTAKER);
		colorId = 5;
		addToValueLayer();
	}
	
	@Override
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}
	
	@Override
	public String getLabel() {
		return "Homeless care" + Math.round(getSavings());
	}
}