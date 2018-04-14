package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The elderly care is the living place for humans above a certain age threshold.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class ElderlyCare extends Property {
	
	private int maxElderlyPerCaretaker = 10; // TODO Put into constants
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	public ElderlyCare(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 11, 8, Status.ELDERLY_CARETAKER, PropertyColor.ELDERLY_CARE);
		addToValueLayer();
		actionName = "Job elderly caretaker";
	}
	
	public boolean getVacancy() {
		
		int caretakers = getCaretakerCount();
		if (caretakers < Math.ceil(getElderlyCount()/ (float) maxElderlyPerCaretaker)) {
			return true;
		}
		return false;
	}
	
	public int getCaretakerCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int caretakers = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.ELDERLY_CARETAKER) {
				caretakers ++;
			}
		}
		return caretakers;
	}
	
	public int getElderlyCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int elderly = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.ELDEST) {
				elderly ++;
			}
		}
		return elderly;
	}
	
	/**
	 * Retrieves the payment of the elderly care workers and subtracts it from the savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getCaretakerPayment() {
		
		if (paymentCount == 0) {
			paymentCount = getCaretakerCount();
			paymentAmount = Math.min(Constants.SALARY_ELDERLY_CARETAKER, getSavings() / paymentCount);
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
		else if (paymentCount < -1) {
			Logger.logError("Error in ElderlyCare, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else {
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
	}

	public void removeExcessiveCaretakers() {
			
		int caretakersToRemove = getCaretakerCount() - (int) Math.ceil((float) getElderlyCount() / (float) maxElderlyPerCaretaker);
		if (caretakersToRemove <= 0)
			return ;
		
		Logger.logInfo("Remove so many caretakers:" + caretakersToRemove);
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.ELDERLY_CARETAKER) {
				caretakersToRemove --;
				human.setStatus(Status.UNEMPLOYED);
			}
			if (caretakersToRemove == 0)
				return ;
		}
		Logger.logError("Error no caretakers left to remove, need to still remove:" + caretakersToRemove);
	}
	
	/**
	 * Retrieves pension for the elderly
	 * @return
	 */
	public double getPension() {
		
		if (paymentCount == 0) {
			paymentCount = SimUtils.getCouncil().getNumberOfElderlyOld() + SimUtils.getCouncil().getNumberOfElderlyYoung();
			paymentAmount = Math.min(Constants.BENEFIT_ELDERLY, getSavings() / paymentCount);
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
		else if (paymentCount < -1) {
			Logger.logError("Error in Unemployed, exceeded paymentCount : " + paymentCount);
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
		
		return spatialImagesOwned.get(true);
	}
	
	@Override
	public String getName() {
		return "ElderlyCare";
	}
	
	@Override
	public String getLabel() {
		return "Elderly care" + ", $:" + Math.round(getSavings());
	}
}