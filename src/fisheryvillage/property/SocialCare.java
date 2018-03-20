package fisheryvillage.property;

import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* Social care houses people without a home that are to young for
* the elderly care. And it gives social benefits to people with
* no/too little income.
*
* @author Maarten Jensen
*/
public class SocialCare extends Property {
	
	public SocialCare(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 6, Status.NONE, PropertyColor.HOMELESS_CARE);
		addToValueLayer();
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "Social care $: " + Math.round(getSavings());
	}	
}