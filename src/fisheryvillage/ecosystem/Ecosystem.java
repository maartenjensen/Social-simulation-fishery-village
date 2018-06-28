package fisheryvillage.ecosystem;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import repast.simphony.space.grid.GridPoint;

/**
* Regulates the fishes
*
* @author Maarten Jensen
* @since 2018-03-24
*/
public class Ecosystem {
	
	private double fish;
	private int allowedLessFish = 0;
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
			Logger.logInfo("Ecosystem, there is no fish left!");
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
	
	public double getAmountFished() {
		return amountFished;
	}
	
	public double getAmountRepopulated() {
		return amountRepopulated;
	}
	
	public String getParametersString() {
		return Double.toString(fish);
	}
	
	public void setParameters(double fish) {
		this.fish = fish;
	}
	
	/**
	 * Increase is based on the max number of boats, times the medium amount that can be caught
	 */
	public void stepEcosystem() {
		
		amountRepopulated = getAmountRepopulate();
		int fishers = 0;
		for (Boat boat : SimUtils.getObjectsAll(Boat.class)) {
			fishers += boat.getFisherAndCaptainCount();
		}
		allowedLessFish = (int) Math.round(amountRepopulated / (2 * fishers));
		fish += amountRepopulated;
		Logger.logInfo("Ecosystem fish: " + fish + ", increased amount: " + amountRepopulated + ", allowed to catch: " + allowedLessFish);
		amountFished = 0;
	}
	
	private double getAmountRepopulate() {
		
		double repopulationMultiplier = 1;
		if (fish < Constants.ECOSYSTEM_MAX_REPOPULATE_LOWER) {
			repopulationMultiplier = Math.max(0, fish / (Constants.ECOSYSTEM_MAX_REPOPULATE_LOWER));
		}
		else if (fish > Constants.ECOSYSTEM_MAX_REPOPULATE_UPPER) {
			repopulationMultiplier = Math.max(0, (Constants.ECOSYSTEM_STABLE_FISH - fish) / (Constants.ECOSYSTEM_STABLE_FISH - Constants.ECOSYSTEM_MAX_REPOPULATE_UPPER) );
		}
		return Constants.ECOSYSTEM_REPOPULATE_AMOUNT * repopulationMultiplier;
	}

	public int getAllowedToCatchLess() {
		return allowedLessFish;
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
	
	public boolean getFishAlive() {
		if (fish > 0)
			return true;
		return false;
	}
}