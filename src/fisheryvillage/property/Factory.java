package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The factory that buys fish from fishers and processes it to sell it again
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Factory extends Property {

	private int maxEmployees = Constants.FACTORY_INITIAL_MAX_EMPLOYEES;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	private int fishUnprocessedKg = 0;
	private int fishProcessedKg = 0; // Variable only for label

	public Factory(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 10, 10, Status.FACTORY_WORKER, PropertyColor.FACTORY);
		addToValueLayer();
		actionName = "Job factory worker";
	}
	
	public int getFishUnprocessed() {
		return fishUnprocessedKg;
	}
	
	public int getMaxEmployees() {
		return maxEmployees;
	}
	
	public void setVariables(int maxEmployees, int fishUnprocessed) {
		
		this.maxEmployees = maxEmployees;
		this.fishUnprocessedKg = fishUnprocessed;
	}

	public int calculatedFishToBuy(int fishProposed) {
		if (getOwner() != null) { //&& getSavings() > 0
			//int fishBought = (int) (Math.min(getSavings(), fishProposed * Constants.PRICE_PER_KG_FISH_UNPROCESSED) / Constants.PRICE_PER_KG_FISH_UNPROCESSED);
			int fishBought = fishProposed;
			return fishBought;
		}
		return 0;
	}

	/**
	 * Buy the amount of fish given in the parameter
	 * @return the amount of money
	 */
	public double buyFish(int fishBought) {

		if (getOwner() != null) {
			double moneyPayedForFish = fishBought * Constants.PRICE_PER_KG_FISH_UNPROCESSED;
			fishUnprocessedKg += fishBought;
			removeFromSavings(-moneyPayedForFish);
			return moneyPayedForFish;
		}
		return 0;
	}

	public void stepProcessFish() {
		
		int fishProcessedKg = getFactoryWorkerCount() * Constants.FISH_PROCESSING_AMOUNT_PP;
		if (fishProcessedKg <= fishUnprocessedKg) {
			fishUnprocessedKg -= fishProcessedKg;
			this.fishProcessedKg = fishProcessedKg;
			if (getFactoryWorkerCount() == maxEmployees && maxEmployees < Constants.FACTORY_MAX_EMPLOYEES) {
				maxEmployees ++;
			}
		}
		else {
			fishProcessedKg = fishUnprocessedKg;
			fishUnprocessedKg = 0;
			this.fishProcessedKg = fishProcessedKg;
			fireEmployees(1);
			if (maxEmployees > Constants.FACTORY_MIN_EMPLOYEES) {
				maxEmployees --;
			}
		}
		addToSavings(fishProcessedKg * Constants.PRICE_PER_KG_FISH_PROCESSED);
	}
	
	public void fireEmployees(int number) {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.FACTORY_WORKER) {
				human.setStatus(Status.UNEMPLOYED);
				number -= 1;
			}
			if (number == 0)
				return ;
		}
	}
	
	public int getFactoryWorkerCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		int workers = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.FACTORY_WORKER) {
				workers ++;
			}
		}
		return workers;
	}

	public double getFactoryBossPayment() {

		double bossPayment = Math.min(Constants.SALARY_FACTORY_BOSS, getSavings());
		removeFromSavings(-bossPayment);
		return bossPayment;
	}
	
	/**
	 * Retrieves the payment of the factory worker and subtracts it from the savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getFactoryWorkerPayment() {
		
		if (paymentCount == 0) {
			paymentCount = getFactoryWorkerCount();
			paymentAmount = Math.min(Constants.SALARY_FACTORY_WORKER, getSavings() / paymentCount);
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
		else if (paymentCount < -1) {
			Logger.logError("Error in Factory, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else {
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
	}
	
	public boolean getVacancy() {
		
		if (getFactoryWorkerCount() < maxEmployees && hasBoss()) {
			return true;
		}
		return false;
	}
	
	public boolean hasBoss() {
		Human owner = getOwner();
		if (owner == null) {
			Logger.logInfo("There is no boss! Fire everyone");
			//fireEmployees();
			return false;
		}
		else if (getOwner().getStatus() != Status.FACTORY_BOSS) {
			Logger.logDebug("Boss " + getOwner().getId() + " is not a boss!");
			//fireEmployees();
			Network<Object> network = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
			RepastEdge<Object> bossEdge = network.getEdge(getOwner(), this);
			Logger.logDebug("Remove edge: " + bossEdge);
			SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY).removeEdge(bossEdge);
			return false;
		}
		return true;
	}
	
	public void fireEmployees() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.FACTORY_WORKER) {
				human.setStatus(Status.UNEMPLOYED);
			}
		}
	}
	
	@Override
	public String getName() {
		return "Factory";
	}
	
	@Override
	public VSpatial getSpatial() {
		
		if (hasBoss()) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getLabel() {
		int bossId = -1;
		if (getOwner() != null) {
			bossId = getOwner().getId();
		}
		return "Factory: Boss: " + bossId + ", W:" + getFactoryWorkerCount() + "/" + maxEmployees + ", $:" + Math.round(getSavings())
			 + "\nFish unprocessed (kg): " + fishUnprocessedKg + "\nFish processed (kg): " + fishProcessedKg;
		// System.out.printf("Value with 3 digits after decimal point %.3f %n", PI); To format a floating decimal number use : .3f
	}
}