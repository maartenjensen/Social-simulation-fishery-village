package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* Boat: although not a building it is property.
* This is what the fishers use to fish
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Boat extends Property {

	private int maxFishers = Constants.MAX_FISHERS_PER_BOAT;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	private int fishCaught = 0; //Kilograms
	private int fishSold = 0;
	private int fishThrownAway = 0;
	ArrayList<Integer> fishersIds = new ArrayList<Integer>();
	
	public Boat(int price, int maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 3, 2, Status.FISHER, PropertyColor.BOAT);
		addToValueLayer();
	}

	public int getFisherCount() {
		
		removeFishersIds();
		return fishersIds.size();
	}
	
	public boolean employeeOnBoat(int fisherId) {
		
		removeFishersIds();
		if (fishersIds.contains(fisherId)) {
			return true;
		}
		return false;
	}
	
	public boolean getVacancy() {
		
		int fishers = getFisherCount();
		if (fishers < maxFishers && hasCaptain()) {
			return true;
		}
		return false;
	}

	public void stepFish() { //TODO make this an influenceable step
		
		fishThrownAway = 0;
		fishSold = 0;
		int fishToCatch = getFisherCount() * RandomHelper.nextIntFromTo(Constants.FISH_CATCH_AMOUNT_MIN_PP, Constants.FISH_CATCH_AMOUNT_MAX_PP);
		fishCaught += SimUtils.getEcosystem().fishFish(fishToCatch);
	}
	
	public void stepSellFish() {
		
		Factory factory = SimUtils.getFactory();
		if (factory != null && fishCaught > 0) {
			fishSold = factory.calculatedFishToBuy(fishCaught);
			addToSavings(factory.buyFish(fishSold));
			fishThrownAway = fishCaught - fishSold;
		}
		else {
			fishThrownAway = 0;
			fishSold = 0;
		}
		fishCaught = 0;
	}
	
	public void addFisher(int id) {
		fishersIds.add(id);
	}
	
	public void removeFishersIds() {
		
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
	 * Checks whether the boat has an owner (captain)
	 * if not it fires all the fishers. If the owner 
	 * is not a fisher it also fires all the fishers
	 * and deletes the connection with the captain
	 * @return
	 */
	public boolean hasCaptain() { //TODO make some inheritance for the boat property
		
		Human owner = getOwner();
		if (owner == null) {
			Logger.logDebug("There is no captain! Fire fishers");
			fireFishers();
			return false;
		}
		else if (getOwner().getStatus() != Status.FISHER) {
			Logger.logDebug("Captain " + getOwner().getId() + " is not a fisher!");
			fireFishers();
			Network<Object> network = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
			RepastEdge<Object> captainEdge = network.getEdge(getOwner(), this);
			Logger.logDebug("Remove edge: " + captainEdge);
			SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).removeEdge(captainEdge);
			return false;
		}
		return true;
	}
	
	/**
	 * Fire the fishers that belong to this boat
	 */
	public void fireFishers() {
		
		for (final Integer fisherId: fishersIds) {
			Human human = HumanUtils.getHumanById(fisherId);
			if (human != null) {
				if (human.getStatus() == Status.FISHER) {
					human.setStatus(Status.UNEMPLOYED);
				}
			}
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
			Logger.logError("Error in Boat, exceeded paymentCount : " + paymentCount);
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
		int captainId = -1;
		int fishCaught = this.fishCaught;
		if (fishSold + fishThrownAway > 0) {
			fishCaught = fishSold + fishThrownAway;
		}
		if (getOwner() != null) {
			captainId = getOwner().getId();
		}
		return "Boat Captain ID:" + captainId + "\nF:" + getFisherCount() + ", $:" + Math.round(getSavings()) + "\nFish caught (kg):" + fishCaught + "\nFish sold (kg): " + fishSold + "\nFish thrown away (kg): " + fishThrownAway;
	}
}