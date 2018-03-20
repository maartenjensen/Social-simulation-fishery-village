package fisheryvillage.property;

import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

/**
* Not used at the moment
*
* @author Maarten Jensen
*/
public class Graveyard extends Property {
	
	public Graveyard(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 2, 2, Status.NONE, PropertyColor.BOAT);
		addToValueLayer();
	}

	@Override
	public String getLabel() {
		return "Graveyard";
	}
}