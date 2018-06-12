package valueframework;

import java.util.Arrays;
import java.util.List;

import valueframework.common.Log;

public class WaterTank implements Comparable<WaterTank> {

	private double capacity;
	private double filledLevel;
	private double drainingAmount;
	private double increasingAmount;
	private double decreasingAmount;
	private double threshold;
	private String relatedAbstractValue;

	public WaterTank(double capacity, double filledLevel, double threshold, double drainingAmount, String relatedAbstractValue) {
		
		this.capacity = capacity;
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		this.drainingAmount = drainingAmount;
		increasingAmount = calculateIncreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		decreasingAmount = calculateDecreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		this.relatedAbstractValue = relatedAbstractValue;
	}

	public WaterTank(String waterTankInfo, String relatedAbstractValue) {

		this.relatedAbstractValue = relatedAbstractValue;

		List<String> items = Arrays.asList(waterTankInfo.split("\\s*,\\s*"));

		capacity = Integer.valueOf(items.get(1));
		filledLevel = Integer.valueOf(items.get(3));
		drainingAmount = Integer.valueOf(items.get(5));
		threshold = Integer.valueOf(items.get(6)); 
		increasingAmount = calculateIncreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		decreasingAmount = calculateDecreaseAmount(this.drainingAmount, this.capacity, this.threshold);
	}

	public void setLevelAndThreshold(double filledLevel, double threshold) {
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		increasingAmount = calculateIncreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		decreasingAmount = calculateDecreaseAmount(this.drainingAmount, this.capacity, this.threshold);
	}

	@Override
	public int compareTo(WaterTank other) {
		return Double.compare(getPriorityPercentage(),
				other.getPriorityPercentage());
	}

	public void draining() {
		filledLevel = Math.max(0, filledLevel - drainingAmount);
	}

	public double calculateIncreaseAmount(double drain, double capacity, double threshold) {
		
		double thresholdFactor = (capacity - threshold) / 100.0;
		return 0.5 * drain + drain * (thresholdFactor);
	}
	
	public double calculateDecreaseAmount(double drain, double capacity, double threshold) {
		
		double thresholdFactor = (threshold) / 100.0;
		return -0.5 * drain - drain * (thresholdFactor);
	}

	public void increaseLevel() {
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount);
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " + " + increasingAmount + ", new lvl:"
				+ filledLevel);
	}

	public void increaseLevel(double multiply) {
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount * multiply);
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " + " + drainingAmount * multiply
				+ ", new lvl:" + filledLevel);
	}

	public void decreaseLevel() {
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(capacity, filledLevel + decreasingAmount);
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " - " + decreasingAmount + ", new lvl:"
				+ filledLevel);
	}

	public void decreaseLevel(double multiply) {
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(capacity, filledLevel + decreasingAmount * multiply);
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: "
				+ oldFilledLevel + " - " + decreasingAmount * multiply
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
		increasingAmount = calculateIncreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		decreasingAmount = calculateDecreaseAmount(this.drainingAmount, this.capacity, this.threshold);
	}
	
	public void setFilledLevel(double level) {
		this.filledLevel = level;
	}

	public void adjustThreshold(double add, double min, double max) {
		threshold = Math.max(min, Math.min(max, threshold + add));
		increasingAmount = calculateIncreaseAmount(this.drainingAmount, this.capacity, this.threshold);
		decreasingAmount = calculateDecreaseAmount(this.drainingAmount, this.capacity, this.threshold);
	}

	public void setRelatedAbstractValue(String valueName) {
		this.relatedAbstractValue = valueName;
	}
}
