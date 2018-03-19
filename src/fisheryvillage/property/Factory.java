package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

public class Factory extends Property {

	private int maxEmployees = 20;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	private int fishUnprocessedKg = 0;
	private int fishProcessedKg = 0; // Variable only for label

	public Factory(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 10, 10, Status.FACTORY_WORKER, PropertyColor.FACTORY);
		addToValueLayer();
	}

	public int calculatedFishToBuy(int fishProposed) {
		int fishBought = (int) (Math.min(getSavings(), fishProposed * Constants.PRICE_PER_KG_FISH_UNPROCESSED) / Constants.PRICE_PER_KG_FISH_UNPROCESSED);
		return fishBought;
	}

	/**
	 * Buy the amount of fish given in the parameter
	 * @return the amount of money
	 */
	public double buyFish(int fishBought) {

		double moneyPayedForFish = fishBought * Constants.PRICE_PER_KG_FISH_UNPROCESSED;
		fishUnprocessedKg += fishBought;
		removeFromSavings(-moneyPayedForFish);
		return moneyPayedForFish;
	}

	public void stepProcessFish() {
		
		int fishProcessedKg = getFactoryWorkerCount() * Constants.FISH_PROCESSING_AMOUNT_PP;
		fishUnprocessedKg -= fishProcessedKg;
		this.fishProcessedKg = fishProcessedKg;
		addToSavings(fishProcessedKg * Constants.PRICE_PER_KG_FISH_PROCESSED);
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
			Logger.logErrorLn("Error in Factory, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else {
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
	}
	
	public boolean getVacancy() {
		
		if (getFactoryWorkerCount() < maxEmployees) {
			return true;
		}
		return false;
	}
	
	@Override
	public VSpatial getSpatial() {
		
		if (getFactoryWorkerCount() >= 1) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getLabel() {
		return "Factory: W:" + getFactoryWorkerCount() + "/" + maxEmployees + ", $:" + Math.round(getSavings())
			 + "\nFish unprocessed (kg): " + fishUnprocessedKg + "\nFish processed (kg): " + fishProcessedKg;
	}
}