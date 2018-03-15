package fisheryvillage.property;

import java.awt.Color;
import fisheryvillage.common.Constants;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class Graveyard extends Property {
	
	public Graveyard(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 2, 2, Status.NONE);
		colorId = 5;
		addToValueLayer();
	}
	
	@Override
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}
	
	@Override
	public String getLabel() {
		return "Graveyard";
	}
}