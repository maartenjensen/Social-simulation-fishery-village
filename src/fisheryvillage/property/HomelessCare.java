package fisheryvillage.property;

import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

public class HomelessCare extends Property {
	
	public HomelessCare(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 6, Status.HOMELESS_CARERTAKER, PropertyColor.HOMELESS_CARE);
		addToValueLayer();
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "Homeless care" + Math.round(getSavings());
	}	
}