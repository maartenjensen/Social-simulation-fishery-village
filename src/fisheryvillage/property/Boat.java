package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.batch.BatchRun;
import fisheryvillage.batch.RunningCondition;
import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.ecosystem.Ecosystem;
import fisheryvillage.population.Human;
import fisheryvillage.population.Resident;
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
	private boolean disabled = false;
	private boolean enoughMoneyActionChooser = false;

	ArrayList<Integer> fishersIds = new ArrayList<Integer>();
	
	// Pure for analysis
	private int fishToCatchData = 0;
	private String valuesOfActionChooser = "0,0,0,0";
	
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
		if (getFisherCount() > maxFishers) {
			fireEmployees(getFisherCount() - maxFishers);
			Logger.logInfo(getName() + "to many fishers, firing fishers");
		}
		
		resetLayer();
		addToValueLayer();
	}

	public void fireEmployees(int number) {

		ArrayList<Integer> temporaryFisherIds = new ArrayList<Integer>();
		temporaryFisherIds.addAll(fishersIds);
		for (Integer fisherId : temporaryFisherIds) {
			Human human = HumanUtils.getHumanById(fisherId);
			if (human.getStatus() == Status.FISHER) {
				human.stopWorkingAtWorkplace();
				number -= 1;
			}
			else
				Logger.logError(getName() + " error in fire employees with " + fisherId);
			if (number == 0)
				return ;
		}
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
	public ArrayList<Status> getVacancy(boolean hasBeenFisher, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		if (hasBeenFisher || !SimUtils.getEcosystem().getFishAlive() || allJobs.size() == 0) {
			return possibleJobs;
		}
		
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

	public void stepFish() {
		
		fishThrownAway = 0;
		fishSold = 0;
		fishToCatchData = 0;
		valuesOfActionChooser = "0,0,0,0,0,0,0,0";
		enoughMoneyActionChooser = false;
		
		if (getFisherAndCaptainCount() == 0) {
			SimUtils.getDataCollector().addFishingData(getId(), toString());
			return ;
		}
		
		String fishingAction = "none";
		if (hasCaptain()) {
			fishingAction = getOwner().selectFishingAction();
			Logger.logAction(getName() + " captain chooses action " + fishingAction);
			getOwner().actionFish(fishingAction);
			valuesOfActionChooser = getOwner().getThresholds();
			enoughMoneyActionChooser = getOwner().getHasEnoughMoney();
		}
		else {
			Resident fisher = HumanUtils.getResidentById( fishersIds.get(RandomHelper.nextIntFromTo(0, fishersIds.size() - 1)) );
			fishingAction = fisher.selectFishingAction();
			Logger.logAction(getName() + " fisher chooses action " + fishingAction);
			valuesOfActionChooser = fisher.getThresholds();
			enoughMoneyActionChooser = fisher.getHasEnoughMoney();
		}
		
		for (int fisherId : fishersIds) { // Update values for each fisher
			HumanUtils.getResidentById(fisherId).actionFish(fishingAction);
		}
		
		int fishToCatch = actionFish(fishingAction);
		fishCaught += SimUtils.getEcosystem().fishFish(fishToCatch);
		
		SimUtils.getDataCollector().addFishingData(getId(), toString());
	}
	
	public int actionFish(String fishingAction) {
		
		Ecosystem ecosystem = SimUtils.getEcosystem();
		int amountPerFisher = 0;
		switch (fishingAction) {
		case "Fish a lot":
			amountPerFisher = Constants.FISH_CATCH_AMOUNT_MAX_PP;
			break;
		case "Fish a lot danger":
			amountPerFisher = Constants.FISH_CATCH_AMOUNT_MAX_PP;
			break;
		case "Fish medium":
			amountPerFisher = Constants.FISH_CATCH_AMOUNT_MEDIUM_PP;
			break;
		case "Fish medium danger":
			amountPerFisher = Constants.FISH_CATCH_AMOUNT_MEDIUM_PP;
			break;
		case "Fish less":
			amountPerFisher = Math.min(ecosystem.getAllowedToCatchLess(), Constants.FISH_CATCH_AMOUNT_MIN_PP);
			break;
		case "Fish less danger":
			amountPerFisher = Math.min(ecosystem.getAllowedToCatchLess(), Constants.FISH_CATCH_AMOUNT_MIN_PP);
			break;
		default:
			Logger.logError("Error in fish catch");
		}
		fishToCatchData = amountPerFisher;
		int fishToCatch = getFisherAndCaptainCount() * amountPerFisher;
		if (!hasCaptain())
			fishToCatch *= (100 - Constants.FISH_CAUGHT_NO_CAPTAIN_DECREASE) / 100.0;
		
		return fishToCatch;
	}
	
	public void stepSellFish() {
		
		Factory factory = SimUtils.getFactory();
		if (factory != null && fishCaught > 0) {
			
			if (BatchRun.getRunningCondition() != RunningCondition.NO_FACTORY) {
				fishSold = factory.calculatedFishToBuy(fishCaught);
				addSavings(factory.buyFish(fishSold));
			}
			else {
				fishSold = fishCaught;
				addSavings(fishSold * 4);
			}
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
		
		if (!SimUtils.getEcosystem().getFishAlive())
			fireFishers();
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
		
		Logger.logAction("Boat fires all employees, since there is no fish to catch");
		final ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		for (final Resident resident : residents) {
			if (resident.getWorkplaceId() == getId()) {
				resident.stopWorkingAtWorkplace();
			}
		}
	}
	
	public void disableBoat() {
		
		fireFishers();
		allJobs.remove(0);
		allJobs.remove(0);
		disabled = true;
	}
	
	public boolean getDisabled() {
		return disabled;
	}
	
	/**
	 * Retrieves the payment of the fisher by dividing the total savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getFisherPayment(int fisherId) {
		
		Logger.logInfo("Boat " + getId() + " pay fisher " + fisherId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		paymentCount -= 1;
		addSavings(-1 * paymentAmount);
		return paymentAmount;
	}
	
	/**
	 * Retrieves the payment of the captain by dividing the total savings
	 * Should only be called in the work function of Human
	 * Captain gets payed double.
	 * @return
	 */
	public double getCaptainPayment(int captainId) {
		
		Logger.logInfo("Boat " + getId() + " pay captain " + captainId + ", count:" + paymentCount + ", fishercount:" + getFisherCount());
		paymentCount -= Constants.SALARY_MULTIPLIER_CAPTAIN;
		addSavings(-1 * paymentAmount * Constants.SALARY_MULTIPLIER_CAPTAIN);
		return paymentAmount * Constants.SALARY_MULTIPLIER_CAPTAIN;
	}
	
	public String getFishersIdsString() {
		String string = "";
		for (int i = 0; i < fishersIds.size(); i ++) {
			if (i >= 1)
				string += ",";
			string += fishersIds.get(i);
		}
		return string;
	}
	
	public BoatType getBoatType() {
		return boatType;
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

	@Override
	public String toString() {
		//Employee capacity, fisher count, has captain, value of person p, s, u, t
		return boatType.getEmployeeCapacity() + "," + getFisherCount() + "," + hasCaptain() + "," + fishToCatchData + "," + valuesOfActionChooser + "," + enoughMoneyActionChooser;
	}

	public double getNormalizedFishToCatchPerPerson() {
		return (((double) fishToCatchData) / Constants.FISH_CATCH_AMOUNT_MAX_PP) * 8;
	}

	public int getCaptainIndex() {
		if (hasCaptain())
			return 1;
		else
			return 0;
	}
}