package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.municipality.Factory;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridValueLayer;
import saf.v3d.scene.VSpatial;

/**
* Boat: this class extends Workplace and has the possibility for the jobs CAPTAIN and FISHER
* This is what the fishers use to fish
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Boat extends Workplace {

	private BoatType boatType;
	private int maxFishers = 0;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	private int fishCaught = 0; //Kilograms
	private int fishSold = 0;
	private int fishThrownAway = 0;
	ArrayList<Integer> fishersIds = new ArrayList<Integer>();

	public Boat(int id, BoatType boatType, double money, GridPoint location) {
		super(id, boatType.getPrice(), boatType.getMaintenanceCost(), money, location, boatType.getDimensions().getX(), boatType.getDimensions().getY(), PropertyColor.BOAT);
		allJobs.add(Status.CAPTAIN);
		allJobs.add(Status.FISHER);
		this.boatType = boatType;
		maxFishers = this.boatType.getEmployeeCapacity() - 1;
		addToValueLayer();
	}

	public void setBoatType(BoatType boatType) {
		this.boatType = boatType;
		setPrice(this.boatType.getPrice());
		setMaintenanceCost(this.boatType.getMaintenanceCost());
		setDimensions(this.boatType.getDimensions());
		maxFishers = this.boatType.getEmployeeCapacity() - 1;
		resetLayer();
		addToValueLayer();
	}

	protected void resetLayer() {

		GridValueLayer valueLayer = SimUtils.getValueLayer();
		if (valueLayer == null) {
			Logger.logError("Error valueLayer is null");
			return ;
		}
		for (int i = 0; i < 8; i ++) {
			for (int j = 0; j < 2; j ++) {
				valueLayer.set(RandomHelper.nextDoubleFromTo(1.9, 1.99), getX() + i, getY() + j);
			}
		}
	}	

	public int getFisherCount() {
		
		return fishersIds.size();
	}

	public int getFisherAndCaptainCount() {
		
		if (getOwner() != null) {
			return fishersIds.size() + 1;
		}
		return fishersIds.size();
	}

	public boolean employeeOnBoat(int fisherId) {
		
		if (fishersIds.contains(fisherId)) {
			return true;
		}
		else if (getOwnerId() == fisherId) {
			return true;
		}
		return false;
	}
	
	@Override
	public ArrayList<Status> getVacancy(boolean higherEducated, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		if (hasCaptain()) {
			int fishers = getFisherCount();
			if (fishers < maxFishers) {
				possibleJobs.add(Status.FISHER);
			}
		}
		else {
			if (money > BoatType.SMALL.getPrice()) {
				possibleJobs.add(Status.CAPTAIN);
			}
		}
		return possibleJobs;
	}

	public void stepFish() { //TODO make this an influenceable step
		
		fishThrownAway = 0;
		fishSold = 0;
		int fishToCatch = (getFisherCount() + 1) * RandomHelper.nextIntFromTo(Constants.FISH_CATCH_AMOUNT_MIN_PP, Constants.FISH_CATCH_AMOUNT_MAX_PP);
		fishCaught += SimUtils.getEcosystem().fishFish(fishToCatch);
	}
	
	public void stepSellFish() {
		
		Factory factory = SimUtils.getFactory();
		if (factory != null && fishCaught > 0) {
			fishSold = factory.calculatedFishToBuy(fishCaught);
			addSavings(factory.buyFish(fishSold));
			fishThrownAway = fishCaught - fishSold;
		}
		else {
			fishThrownAway = 0;
			fishSold = 0;
		}
		fishCaught = 0;
		
		paymentCount = getFisherCount();
		if (hasCaptain()) {
			paymentCount += Constants.SALARY_MULTIPLIER_CAPTAIN;
		}
		paymentAmount = getSavings() / paymentCount;
	}
	
	public void addFisher(int id) {
		fishersIds.add(id);
	}
	
	public void removeFisher(int fisherId) {
		
		if (fishersIds.contains(fisherId)) {
			Human human = HumanUtils.getHumanById(fisherId);
			if (human != null) {
				if (human.getStatus() == Status.FISHER) {
					human.setStatus(Status.UNEMPLOYED);
				}
			}
			fishersIds.remove(fishersIds.lastIndexOf(fisherId));
		}
	}
	
	/**
	 * Checks whether the boat has an owner (captain)
	 * if not it fires all the fishers. If the owner 
	 * is not a fisher it also fires all the fishers
	 * and deletes the connection with the captain
	 * @return
	 */
	public boolean hasCaptain() {
		
		Human owner = getOwner();
		if (owner == null) {
			return false;
		}
		else if (owner.getStatus() != Status.CAPTAIN) {
			Logger.logError("Owner " + getOwner().getId() + " is not a captain!");
			Logger.logDebug("Remove property: " + getId());
			getOwner().removeAndSellProperty(getId(), true);
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
	public double getFisherPayment(int fisherId) {
		
		Logger.logInfo("Boat " + getId() + " pay fisher " + fisherId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		paymentCount -= 1;
		return paymentAmount;
		/*
		if (paymentCount < 0) {
			
			Logger.logError("Error in Boat " + getId() + ", paymentCount lower than zero: " + paymentCount);
			return 0;
		}
		else if (paymentCount == 0) {
			
			paymentCount = getFisherCount();
			if (hasCaptain()) {
				paymentCount += Constants.SALARY_MULTIPLIER_CAPTAIN;
			}
			paymentAmount = getSavings() / paymentCount;
		}
		paymentCount -= 1;
		addSavings(-1 * paymentAmount);
		Logger.logInfo("Boat " + getId() + " pay fisher " + fisherId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		return paymentAmount;*/
	}

	/**
	 * Retrieves the payment of the captain by dividing the total savings
	 * Should only be called in the work function of Human
	 * Captain gets payed double.
	 * @return
	 */
	public double getCaptainPayment(int captainId) {
		
		Logger.logInfo("Boat " + getId() + " pay fisher " + captainId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		paymentCount -= Constants.SALARY_MULTIPLIER_CAPTAIN;
		return paymentAmount * Constants.SALARY_MULTIPLIER_CAPTAIN;
		/*
		if (paymentCount < 0) {
			Logger.logError("Error in Boat " + getId() + ", captain exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else if (paymentCount == 0) {
			
			paymentCount = getFisherCount() + Constants.SALARY_MULTIPLIER_CAPTAIN;
			paymentAmount = getSavings() / paymentCount;
		}
		paymentCount -= Constants.SALARY_MULTIPLIER_CAPTAIN;
		addSavings(-1 * paymentAmount * Constants.SALARY_MULTIPLIER_CAPTAIN);
		Logger.logInfo("Boat " + getId() + " pay captain " + captainId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		return paymentAmount * Constants.SALARY_MULTIPLIER_CAPTAIN;*/
	}
	
	@Override
	public String getName() {
		
		if (getOwner() != null) {
			return "Boat" + " [" + getId() + "] C:" + getOwner().getId() + ", F:" + fishersIds.toString();
		}
		return "Boat" + " [" + getId() + "] C: #, F:" + fishersIds.toString();
	}
	
	@Override
	public VSpatial getSpatial() {
		
		if (hasCaptain()) {
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
		return "Boat " + boatType + " ["+ getId() +"] price:" + getPrice() + "\nCaptain ID:" + captainId + ", F:" + fishersIds.toString() + "\nF:" + getFisherCount() + ", $:" + Math.round(getSavings()) + "\nFish caught (kg):" + fishCaught + "\nFish sold (kg): " + fishSold + "\nFish thrown away (kg): " + fishThrownAway;
	}
}