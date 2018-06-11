package valueframework;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import fisheryvillage.common.RepastParam;
import valueframework.common.Log;

//TODO: update it with Maarten sent code
public class WaterTank implements Comparable<WaterTank> {

	private double capacity;
	private double filledLevel;
	private double drainingAmount;
	private double increasingAmount;
	private double threshold;
	private String relatedAbstractValue;

	public WaterTank(double capacity, double filledLevel, double threshold,
			double drainingAmount, String relatedAbstractValue) {
		this.capacity = capacity;
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		this.drainingAmount = drainingAmount;
		increasingAmount = capacity - threshold;
		this.relatedAbstractValue = relatedAbstractValue;
	}

	public WaterTank(String waterTankInfo, String relatedAbstractValue) {

		this.relatedAbstractValue = relatedAbstractValue;

		List<String> items = Arrays.asList(waterTankInfo.split("\\s*,\\s*"));
		int maxCapacity = Integer.valueOf(items.get(1));
		// int minCapacity = Integer.valueOf(items.get(2));
		int maxFilledLevel = Integer.valueOf(items.get(3));
		int minFilledLevel = Integer.valueOf(items.get(4));
		drainingAmount = Integer.valueOf(items.get(5));
		// int minThresould = Integer.valueOf(items.get(6));
		// int maxThresould = Integer.valueOf(items.get(7));
		Random rand = new Random();
		// int cp = rand.nextInt((maxCapacity - minCapacity) + 1) + minCapacity;
		int cp = maxCapacity;
		int fl = rand.nextInt((maxFilledLevel - minFilledLevel) + 1)
				+ minFilledLevel;
		int tr = RepastParam.getAbstractValue(relatedAbstractValue); // TODO
																		// don't
																		// get
																		// this
																		// directly
																		// from
																		// the
																		// class
																		// RepastParam
																		// but
																		// give
																		// the
																		// values
																		// in
																		// the
																		// constructor

		capacity = cp;
		filledLevel = fl;
		threshold = tr;
		increasingAmount = capacity - threshold;
	}

	public void setLevelAndThreshold(double filledLevel, double threshold) {
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		increasingAmount = capacity - threshold;
	}

	@Override
	public int compareTo(WaterTank other) {
		return Double.compare(getPriorityPercentage(),
				other.getPriorityPercentage());
	}

	public void draining() {
		filledLevel = Math.max(0, filledLevel - drainingAmount);
	}

	public void increaseLevel() {// TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity,
				filledLevel + Math.max(drainingAmount * 2, increasingAmount));
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " + "
				+ Math.max(drainingAmount * 2, increasingAmount) + ", new lvl:"
				+ filledLevel);
	}

	public void increaseLevel(double multiply) {// TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(
				capacity,
				filledLevel
						+ Math.max(drainingAmount * 2, increasingAmount
								* multiply));
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " + "
				+ Math.max(drainingAmount * 2, increasingAmount * multiply)
				+ ", new lvl:" + filledLevel);
	}

	public void decreaseLevel() {// TODO: check if it is correct to use the
									// drainingAmount for decreasing
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - drainingAmount); // -
																	// increasingAmount);
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " - " + drainingAmount + ", new lvl:"
				+ filledLevel);
	}

	public void decreaseLevel(double multiply) {// TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - drainingAmount * multiply); // was
																			// originally
																			// increasing
																			// amount
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " - " + drainingAmount * multiply
				+ ", new lvl:" + filledLevel);
	}

	public double getCapacity() {
		return capacity;
	}

	public double getFilledLevel() {
		return filledLevel;
	}

	public double getPriorityPercentage() {
		return -1 * (((double) (filledLevel - threshold) / threshold) * 100.0);
	}

	public double getIncreasingAmount() {
		return increasingAmount;
	}

	public double getDrainingAmount() {
		return drainingAmount;
	}

	public double getThreshold() {
		return threshold;
	}

	public String getRelatedAbstractValue() {
		return relatedAbstractValue;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
		increasingAmount = capacity - threshold;
	}

	public void adjustThreshold(double add, double min, double max) {
		threshold = Math.max(min, Math.min(max, threshold + add));
		increasingAmount = capacity - threshold;
	}

	public void setRelatedAbstractValue(String valueName) {
		this.relatedAbstractValue = valueName;
	}
}
