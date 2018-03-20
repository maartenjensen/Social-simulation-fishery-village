package fisheryvillage.property;

import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The elderly care is the living place for humans above a certain age threshold.
*
* @author Maarten Jensen
*/
public class ElderlyCare extends Property {
	
	public ElderlyCare(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 6, Status.ELDERLY_CARETAKER, PropertyColor.ELDERLY_CARE);
		addToValueLayer();
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}
	
	@Override
	public String getLabel() {
		return "Elderly care" + ", $:" + Math.round(getSavings());
	}
}