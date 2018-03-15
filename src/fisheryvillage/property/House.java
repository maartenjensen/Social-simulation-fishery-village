package fisheryvillage.property;

import java.awt.Color;

import fisheryvillage.common.Constants;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class House extends Property {

	public House(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 4, 2, Status.NONE);
		colorId = 4;
		addToValueLayer();
	}

	@Override
	public Color getColor() {
		return Constants.COLOR_HOUSE;
	}

	@Override
	public String getLabel() {
		return "H, $:" + getSavings();
	}
}