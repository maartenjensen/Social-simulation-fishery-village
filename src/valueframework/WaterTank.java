package valueframework;

import java.util.Arrays;
import java.util.List;

import valueframework.common.Log;

public class WaterTank implements Comparable<WaterTank> {

	private double capacity;
	private double filledLevel;
	private double drainingAmount;
	private double threshold;
	private String relatedAbstractValue;
	private double increaseAmount;
	private double decreaseAmount;
	private int satisfactionCountPos = 0;
	private int satisfactionCountNeg = 0;

	public WaterTank(double capacity, double filledLevel, double threshold, double drainingAmount, String relatedAbstractValue) {
		
		this.capacity = capacity;
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		this.drainingAmount = drainingAmount;
		this.relatedAbstractValue = relatedAbstractValue;
		this.increaseAmount = calculateBaseIncr();
		this.decreaseAmount = calculateBaseDecr();
	}

	public double calculateBaseIncr() {
		double value = (1 / Math.sqrt(capacity)) * (capacity - threshold);
		return Math.pow(value, 2);
	}
	
	public double calculateBaseDecr() {
		double value = (1 / Math.sqrt(capacity)) * threshold;
		return Math.pow(value, 2) * 0.5;
	}
	
	public WaterTank(String waterTankInfo, String relatedAbstractValue) {

		this.relatedAbstractValue = relatedAbstractValue;

		List<String> items = Arrays.asList(waterTankInfo.split("\\s*,\\s*"));

		capacity = Integer.valueOf(items.get(1));
		filledLevel = Integer.valueOf(items.get(3));
		drainingAmount = Integer.valueOf(items.get(5));
		threshold = Integer.valueOf(items.get(6));
		this.increaseAmount = calculateBaseIncr();
		this.decreaseAmount = calculateBaseDecr();
	}

	public void setLevelAndThreshold(double filledLevel, double threshold) {
		this.filledLevel = filledLevel;
		this.threshold = threshold;
		this.increaseAmount = calculateBaseIncr();
		this.decreaseAmount = calculateBaseDecr();
	}

	@Override
	public int compareTo(WaterTank other) {
		return Double.compare(getPriorityPercentage(),
				other.getPriorityPercentage());
	}

	public void draining() {
		filledLevel = Math.max(0, filledLevel - drainingAmount);
		satisfactionCountPos = 0;
		satisfactionCountNeg = 0;
	}

	public double calculateIncr(double multiplier) {
		
		if (satisfactionCountPos == 1)
			return drainingAmount * multiplier + increaseAmount;
		else
			return (1.0 / satisfactionCountPos) * increaseAmount;
	}
	
	public double calculateDecr() {
		
		return (1.0 / satisfactionCountNeg) * decreaseAmount;
	}

	public void increaseLevel() {
		
		satisfactionCountPos ++;
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + calculateIncr(1));
		Log.printLog("Incr wt [" + relatedAbstractValue + "] count " + satisfactionCountPos + ": old lvl: " + oldFilledLevel + " + " + calculateIncr(1) + ", new lvl:" + filledLevel);
	}

	public void increaseLevel(double multiply) {
		
		satisfactionCountPos ++;
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + calculateIncr(multiply));
		Log.printLog("Incr wt [" + relatedAbstractValue + "] count " + satisfactionCountPos + ": old lvl: " + oldFilledLevel + " + " + calculateIncr(multiply) + ", new lvl:" + filledLevel);
	}

	public void decreaseLevel() {
		
		satisfactionCountNeg ++;
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - calculateDecr());
		Log.printLog("Decr wt [" + relatedAbstractValue + "] count " + satisfactionCountNeg + ": old lvl: " + oldFilledLevel + " - " + calculateDecr() + ", new lvl:" + filledLevel);
	}

	public void decreaseLevel(double multiply) {
		
		satisfactionCountNeg ++;
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - calculateDecr());
		Log.printLog("Decr wt [" + relatedAbstractValue + "] count " + satisfactionCountNeg + ": old lvl: " + oldFilledLevel + " - " + calculateDecr()  + ", new lvl:" + filledLevel);
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
		this.increaseAmount = calculateBaseIncr();
		this.decreaseAmount = calculateBaseDecr();
	}
	
	public void setFilledLevel(double level) {
		this.filledLevel = level;
	}

	public void adjustThreshold(double add, double min, double max) {
		threshold = Math.max(min, Math.min(max, threshold + add));
		this.increaseAmount = calculateBaseIncr();
		this.decreaseAmount = calculateBaseDecr();
	}

	public void setRelatedAbstractValue(String valueName) {
		this.relatedAbstractValue = valueName;
	}
}
