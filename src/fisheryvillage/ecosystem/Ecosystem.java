package fisheryvillage.ecosystem;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import repast.simphony.space.grid.GridPoint;

/**
* Regulates the fishes
*
* @author Maarten Jensen
* @since 2018-03-24
*/
public class Ecosystem {
	
	private int fish;
	
	public Ecosystem(int initialFish, GridPoint location) {
		
		fish = initialFish;
		SimUtils.getContext().add(this);
		if (!SimUtils.getGrid().moveTo(this, location.getX(), location.getY())) {
			Logger.logError("Ecosystem could not be placed, coordinate: " + location);
		}
	}
	
	public int fishFish(int amountFish) {
		
		double fishFactor = (double) fish / (Constants.ECOSYSTEM_INITIAL_FISH / 2.0);
		amountFish *= 0.25 + Math.min(0.75, fishFactor * 0.75);
		
		if (amountFish <= fish) {
			fish -= amountFish * (0.25 + Math.min(0.75, fishFactor * 0.75)); //TODO put into constants
			return amountFish;
		}
		else {
			System.out.println("Ecosystem, there is no fish left!");
			amountFish = fish;
			fish = 0;
			return amountFish;
		}
	}
	
	public int getFish() {
		return fish;
	}
	
	public void stepEcosystem() {
		
		//double fishFactor = (double) fish / (Constants.ECOSYSTEM_INITIAL_FISH);
		fish += ((Constants.FISH_CATCH_AMOUNT_MIN_PP + Constants.FISH_CATCH_AMOUNT_MAX_PP) * 12)/ 2.0;
		//TODO grows with rate of 15 fishers there is a max of 12 fishers
	}
	
	public String getLabel() {
		
		String label = "Ecosystem\nFish:" + Integer.toString(fish);
		if (fishInDanger()) {
			return label + " DANGER";
		}
		return label;
	}
	
	public boolean fishInDanger() {
		if (fish < Constants.ECOSYSTEM_INITIAL_FISH) {
			return true;
		}
		else {
			return false;
		}
	}
}