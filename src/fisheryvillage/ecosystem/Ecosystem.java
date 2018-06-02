package fisheryvillage.ecosystem;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.BoatType;
import repast.simphony.space.grid.GridPoint;

/**
* Regulates the fishes
*
* @author Maarten Jensen
* @since 2018-03-24
*/
public class Ecosystem {
	
	private double fish;
	private double amountFished = 0; //only used for data collection
	private double amountRepopulated = 0; //only used for data collection
	
	public Ecosystem(int initialFish, GridPoint location) {
		
		fish = initialFish;
		SimUtils.getContext().add(this);
		if (!SimUtils.getGrid().moveTo(this, location.getX(), location.getY())) {
			Logger.logError("Ecosystem could not be placed, coordinate: " + location);
		}
	}
	
	public int fishFish(int amountFish) {
		
		if (amountFish <= fish) {
			fish -= amountFish;
			amountFished += amountFish;
			return amountFish;
		}
		else {
			System.out.println("Ecosystem, there is no fish left!");
			amountFish = (int) fish;
			amountFished += amountFish;
			fish = 0;
			return amountFish;
		}
	}
	
	public double getFish() {
		return fish;
	}
	
	public double getAmountFishedX10() {
		return amountFished * 10;
	}
	
	public double getAmountRepopulatedX10() {
		return amountRepopulated * 10;
	}
	
	public String getParametersString() {
		return Double.toString(fish);
	}
	
	public void setParameters(int fish) {
		this.fish = fish;
	}
	
	/**
	 * Increase is based on the max number of boats, times the medium amount that can be caught
	 */
	public void stepEcosystem() {
		
		double repopulationMultiplier = 1;
		if (fish < Constants.ECOSYSTEM_MAX_REPOPULATE_LOWER) {
			repopulationMultiplier = Math.max(0, fish / (Constants.ECOSYSTEM_MAX_REPOPULATE_LOWER));
		}
		else if (fish > Constants.ECOSYSTEM_MAX_REPOPULATE_UPPER) {
			repopulationMultiplier = Math.max(0, (Constants.ECOSYSTEM_STABLE_FISH - fish) / (Constants.ECOSYSTEM_STABLE_FISH - Constants.ECOSYSTEM_MAX_REPOPULATE_UPPER) );
		}
		amountRepopulated = Constants.FISH_CATCH_AMOUNT_MEDIUM_PP * Constants.NUMBER_OF_BOATS * BoatType.LARGE.getEmployeeCapacity() * repopulationMultiplier;
		fish += amountRepopulated;
		amountFished = 0;
	}

	public String getLabel() {
		
		String label = "Ecosystem\nFish:" + Double.toString(fish);
		if (fishInDanger()) {
			return label + " DANGER";
		}
		return label;
	}
	
	public boolean fishInDanger() {
		if (fish < Constants.ECOSYSTEM_IN_DANGER) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * This value times 10
	 * @return
	 */
	public double getFishInDangerLevel() {
		return Constants.ECOSYSTEM_IN_DANGER;
	}
}