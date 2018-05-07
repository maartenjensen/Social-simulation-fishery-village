package fisheryvillage.property.municipality;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Property;
import fisheryvillage.property.PropertyColor;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* Social care houses people without a home that are to young for
* the elderly care. And it gives social benefits to people with
* no/too little income.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SocialCare extends Property {
	
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	public SocialCare(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 11, 8, PropertyColor.SOCIAL_CARE);
		addToValueLayer();
	}
	
	/**
	 * Retrieves the social benefit for unemployed
	 * @return
	 */
	public double getUnemployedBenefit() {
		
		if (paymentCount < 0) {
			Logger.logError("Error in Unemployed, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else if (paymentCount == 0) {
			paymentCount = SimUtils.getCouncil().getNumberOfUnemployed();
			paymentAmount = Math.max(0, Math.min(Constants.BENEFIT_UNEMPLOYED, getSavings() / paymentCount));
		}
		paymentCount -= 1;
		addSavings(-1 * paymentAmount);
		return paymentAmount;
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getName() {
		return "SocialCare [" + getId() + "]";
	}
	
	@Override
	public String getLabel() {
		return "Social care [" + getId() + "] $: " + Math.round(getSavings());
	}	
}