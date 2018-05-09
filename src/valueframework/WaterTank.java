package valueframework;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import valueframework.common.Log;


//TODO: update it with Maarten sent code
public class WaterTank implements Comparable<WaterTank> {
	
	private double capacity;
	private double filledLevel;
	private double drainingAmount;
	private double increasingAmount;
	private double threshold;
	private String relatedAbstractValue;
		
	public WaterTank(double capacity, double filledLevel, double threshould, double drainingAmount, String relatedAbstractValue) {
		this.capacity = capacity;
	    this.filledLevel = filledLevel;
	    this.threshold = threshould;
	    this.drainingAmount = drainingAmount;
	    increasingAmount = capacity - threshould;
	    this.relatedAbstractValue = relatedAbstractValue;
	    
	    Log.printLog("Created watertank " + relatedAbstractValue + ", cap: " + capacity + ", lvl: " + filledLevel +
				   ", thr: " + threshold + ", inc:" + increasingAmount + ", dra:" + drainingAmount);
	}
	
	public WaterTank(String waterTankInfo, String relatedAbstractValue) {
	
		this.relatedAbstractValue = relatedAbstractValue;
		
		List<String> items = Arrays.asList(waterTankInfo.split("\\s*,\\s*"));
		int maxCapacity = Integer.valueOf(items.get(1));
		//int minCapacity = Integer.valueOf(items.get(2));
		int maxFilledLevel = Integer.valueOf(items.get(3));
		int minFilledLevel = Integer.valueOf(items.get(4));
		drainingAmount = Integer.valueOf(items.get(5));
		int minThresould = Integer.valueOf(items.get(6));
		int maxThresould = Integer.valueOf(items.get(7));
		Random rand = new Random();
	//	int cp = rand.nextInt((maxCapacity - minCapacity) + 1) + minCapacity;
		int cp = maxCapacity;
		int fl = rand.nextInt((maxFilledLevel - minFilledLevel) + 1) + minFilledLevel;
		int tr = rand.nextInt((maxThresould - minThresould) + 1) + minThresould;
	    
		capacity = cp;
	    filledLevel = fl;
	    threshold = tr;
	    increasingAmount =  capacity-threshold;
	    
	    Log.printLog("Created watertank " + relatedAbstractValue + ", cap:" + capacity + ", lvl:" + filledLevel +
	    				   ", thr:" + threshold + ", inc:" + increasingAmount + ", dra:" + drainingAmount);
	}

	@Override
	public int compareTo(WaterTank other) {
		return Double.compare(getPriorityPercentage(),other.getPriorityPercentage());
	}

	public void draining() {		
		filledLevel = Math.max(0, filledLevel - drainingAmount);
	}	
	
	public void increaseLevel() {//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount);
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: " + oldFilledLevel + " + " + increasingAmount + ", new lvl:" + filledLevel);
	}
	
	public void increasingLevel(double multiply) {//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount * multiply);
		Log.printLog("Incr wt [" + relatedAbstractValue + "]: old lvl: " + oldFilledLevel + " + " + increasingAmount * multiply + ", new lvl:" + filledLevel);
	}
	
	public void decreaseLevel() {//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - increasingAmount);
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: " + oldFilledLevel + " - " + increasingAmount + ", new lvl:" + filledLevel);
	}
	
	public void decreaseLevel(double multiply) {//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.max(0, filledLevel - increasingAmount * multiply);
		Log.printLog("Decr wt [" + relatedAbstractValue + "]: old lvl: " + oldFilledLevel + " - " + increasingAmount * multiply + ", new lvl:" + filledLevel);
	}
	
	public double getCapacity() {
		return capacity;
	}

	public double getFilledLevel() {
		return filledLevel;
	}
	
	public double getPriorityPercentage(){
		return -1 * (((double)(filledLevel - threshold)/threshold)*100.0);
	}
	
	public double getIncreasingAmount(){
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

	public void setRelatedAbstractValue(String valueName) {
		this.relatedAbstractValue = valueName;
	}
}
