package fisheryvillage.property;

import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

/**
* A house with space for 8 persons, comes
* in three price ranges.
*
* @author Maarten Jensen
*/
public class House extends Property {

	public House(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 4, 2, Status.NONE, PropertyColor.HOUSE);
		addToValueLayer();
	}

	@Override
	public String getLabel() {
		return "$:" + getPrice() + ", Cost $:" + getMaintenanceCost();
	}
}