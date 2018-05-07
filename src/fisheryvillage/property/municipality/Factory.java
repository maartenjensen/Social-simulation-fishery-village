package fisheryvillage.property.municipality;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.PropertyColor;
import fisheryvillage.property.Workplace;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The factory that buys fish from fishers and processes it to sell it again
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Factory extends Workplace {

	private int maxEmployees = Constants.FACTORY_INITIAL_MAX_EMPLOYEES;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	private int fishUnprocessedKg = 0;
	private int fishProcessedKg = 0; // Variable only for label

	public Factory(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 10, 10, PropertyColor.FACTORY);
		allJobs.add(Status.FACTORY_BOSS);
		allJobs.add(Status.FACTORY_WORKER);
		addToValueLayer();
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
			addSavings(-1 * moneyPayedForFish);
			return moneyPayedForFish;
		}
		return 0;
	}

	public void stepProcessFish() {
		
		int fishProcessedKg = getEmployeeCount(Status.FACTORY_WORKER) * Constants.FISH_PROCESSING_AMOUNT_PP;
		if (fishProcessedKg <= fishUnprocessedKg) {
			fishUnprocessedKg -= fishProcessedKg;
			this.fishProcessedKg = fishProcessedKg;
			if (getEmployeeCount(Status.FACTORY_WORKER) == maxEmployees && maxEmployees < Constants.FACTORY_MAX_EMPLOYEES) {
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
		addSavings(fishProcessedKg * Constants.PRICE_PER_KG_FISH_PROCESSED);
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
	
	public double getFactoryBossPayment() {

		double bossPayment = Math.max(0, Math.min(Constants.SALARY_FACTORY_BOSS, getSavings()));
		addSavings(-1 * bossPayment);
		return bossPayment;
	}
	
	/**
	 * Retrieves the payment of the factory worker and subtracts it from the savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getFactoryWorkerPayment() {
		
		if (paymentCount < 0) {
			Logger.logError("Error in Factory, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else if (paymentCount == 0) {
			paymentCount = getEmployeeCount(Status.FACTORY_WORKER);
			paymentAmount = Math.max(0, Math.min(Constants.SALARY_FACTORY_WORKER, getSavings() / paymentCount));
		}
		
		paymentCount -= 1;
		addSavings(-1 * paymentAmount);
		return paymentAmount;
	}
	
	@Override
	public ArrayList<Status> getVacancy(boolean higherEducated, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		if (hasBoss()) {
			if (getEmployeeCount(Status.FACTORY_WORKER) < maxEmployees) {
				possibleJobs.add(Status.FACTORY_WORKER);
			}
		}
		else {
			if (money > getPrice()) {
				possibleJobs.add(Status.FACTORY_BOSS);
			}
		}
		return possibleJobs;
	}
	
	public boolean hasBoss() {

		Human owner = getOwner();
		if (owner == null) {
			return false;
		}
		else if (getOwner().getStatus() != Status.FACTORY_BOSS) {
			Logger.logDebug("Boss " + getOwner().getId() + " is not a boss!");
			Logger.logDebug("Remove property: " + getId());
			getOwner().removeAndSellProperty(getId(), true);
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
		return "Factory [" + getId() + "]";
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
		return "Factory [" + getId() + "]: Boss: " + bossId + ", W:" + getEmployeeCount(Status.FACTORY_WORKER) + "/" + maxEmployees + ", $:" + Math.round(getSavings())
			 + "\nFish unprocessed (kg): " + fishUnprocessedKg + "\nFish processed (kg): " + fishProcessedKg;
		// System.out.printf("Value with 3 digits after decimal point %.3f %n", PI); To format a floating decimal number use : .3f
	}
}