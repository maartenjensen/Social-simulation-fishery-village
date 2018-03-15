package fisheryvillage.property;

import java.awt.Color;

import fisheryvillage.common.Constants;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class ElderlyCare extends Property {
	
	public ElderlyCare(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 6, Status.ELDERLY_CARETAKER);
		colorId = 5;
		addToValueLayer();
	}
	
	@Override
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}
	
	@Override
	public String getLabel() {
		return "Elderly care" + ", $:" + Math.round(getSavings());
	}
}