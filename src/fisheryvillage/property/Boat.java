package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

public class Boat extends Property {

	private int maxFishers = Constants.MAX_FISHERS_PER_BOAT;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	private int fishCaught = 0; //Kilograms
	private int fishSold = 0;
	ArrayList<Integer> fishersIds = new ArrayList<Integer>();
	
	public Boat(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 3, 2, Status.FISHER, PropertyColor.BOAT);
		addToValueLayer();
	}

	public int getFisherCount() {
		
		removeFishers();
		return fishersIds.size();
	}
	
	public boolean employeeOnBoat(int fisherId) {
		
		removeFishers();
		if (fishersIds.contains(fisherId)) {
			return true;
		}
		return false;
	}
	
	public boolean getVacancy() {
		
		int fishers = getFisherCount();
		if (fishers < maxFishers) {
			return true;
		}
		return false;
	}
	
	public void stepFish() { //TODO make this an influenceable step
		
		fishCaught = getFisherCount() * RandomHelper.nextIntFromTo(Constants.FISH_CATCH_AMOUNT_MIN_PP, Constants.FISH_CATCH_AMOUNT_MAX_PP);
	}
	
	public void stepSellFish() {
		
		Factory factory = SimUtils.getFactory();
		if (factory != null && fishCaught > 0) {
			fishSold = factory.calculatedFishToBuy(fishCaught);
			addToSavings(factory.buyFish(fishSold));
			//fishCaught -= fishSold; Don't remove it so it can be shown in the label
		}
	}
	
	public void addFisher(int id) {
		fishersIds.add(id);
	}
	
	public void removeFishers() {
		
		ArrayList<Integer> fishersIdsToRemove = new ArrayList<Integer>();
		for (final Integer fisherId: fishersIds) {
			Human human = HumanUtils.getHumanById(fisherId);
			if (human != null) {
				if (human.getStatus() != Status.FISHER) {
					fishersIdsToRemove.add(fisherId);
				}
			}
			else {
				fishersIdsToRemove.add(fisherId);
			}
		}
		if (fishersIdsToRemove.size() > 0) {
			fishersIds.removeAll(fishersIdsToRemove);
		}
	}
	
	/**
	 * Retrieves the payment of the fisher by dividing the total savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getFisherPayment() {
		
		if (paymentCount == 0) {
			paymentCount = getFisherCount();
			paymentAmount = getSavings() / paymentCount;
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
		else if (paymentCount < -1) {
			Logger.logErrorLn("Error in Boat, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else {
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
	}
	
	@Override
	public VSpatial getSpatial() {
		
		if (getFisherCount() >= 1) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getLabel() {
		return "Boat C:" + ", F:" + getFisherCount() + ", $:" + Math.round(getSavings()) + "\nFish caught (kg): " + fishCaught + "\nFish sold (kg): " + fishSold;
	}
}