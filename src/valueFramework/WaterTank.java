package valueFramework;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


//TODO: update it with Maarten sent code
public class WaterTank implements Comparable<WaterTank> {
	
	private double capacity;
	private double filledLevel;
	private double drainingAmount;// = 0.5;
	private double increasingAmount;//0.5;
	private double threshould;
	private String relatedAbstractValue;
	
	private int maxCapacity;// = 70;
	private int minCapacity;// = 30;
	private int maxFilledLevel;// = 70;
	private int minFilledLevel;// = 20;
	
	private int minThresould;// = 25;
	private int maxThresould;// = 65;
		
	public WaterTank(double drainingAmountIn) {
		maxCapacity = 70;
		minCapacity = 30;
		maxFilledLevel = 70;
		minFilledLevel = 20;
		
		minThresould = 25;
		maxThresould = 65;
		
		Random rand = new Random();
		int cp = rand.nextInt((maxCapacity - minCapacity) + 1) + minCapacity;
		int fl = rand.nextInt((maxFilledLevel - minFilledLevel) + 1) + minFilledLevel;
		int tr = rand.nextInt((maxThresould - minThresould) + 1) + minThresould;
	    capacity = cp; //TODO:check if the automatic cast from int to double works fine
	    filledLevel = fl;
	    threshould = tr;
	    increasingAmount = capacity-threshould;
	    drainingAmount = drainingAmountIn;
	}
	/*
	public WaterTank(String relatedAbstractValue) {
		maxCapacity = 70;
		minCapacity = 30;
		maxFilledLevel = 70;
		minFilledLevel = 20;
		drainingAmount = 10;
		minThresould = 25;
		maxThresould = 65;
		this.relatedAbstractValue = relatedAbstractValue;
		
		Random rand = new Random();
		int cp = rand.nextInt((maxCapacity - minCapacity) + 1) + minCapacity;
		int fl = rand.nextInt((maxFilledLevel - minFilledLevel) + 1) + minFilledLevel;
		int tr = rand.nextInt((maxThresould - minThresould) + 1) + minThresould;
	    setCapacity(cp); //TODO:check if the automatic cast from int to double works fine
	    setFilledLevel(fl);
	    setThresould(tr);
	   setDrainingAmount(drainingAmount);
	}*/

	public WaterTank(double capacity, double filledLevel, double threshould, double drainingAmount, String relatedAbstractValue) {
		this.capacity = capacity;
	    this.filledLevel = filledLevel;
	    this.threshould = threshould;
	    this.drainingAmount = drainingAmount;
	    increasingAmount = capacity - threshould;
	    this.relatedAbstractValue = relatedAbstractValue;
	}
	
public WaterTank(String waterTankInfo, String relatedAbstractValue) {
	
	
	List<String> items = Arrays.asList(waterTankInfo.split("\\s*,\\s*"));
	maxCapacity = Integer.valueOf(items.get(1));
	minCapacity = Integer.valueOf(items.get(2));
	maxFilledLevel = Integer.valueOf(items.get(3));
	minFilledLevel = Integer.valueOf(items.get(4));
	drainingAmount = Integer.valueOf(items.get(5));
	minThresould = Integer.valueOf(items.get(6));
	this.relatedAbstractValue = relatedAbstractValue;
	maxThresould = Integer.valueOf(items.get(7));
	Random rand = new Random();
//	int cp = rand.nextInt((maxCapacity - minCapacity) + 1) + minCapacity;
	int cp = maxCapacity;
	int fl = rand.nextInt((maxFilledLevel - minFilledLevel) + 1) + minFilledLevel;
	int tr = rand.nextInt((maxThresould - minThresould) + 1) + minThresould;
    
	
	capacity = cp; //TODO:check if the automatic cast from int to double works fine
    filledLevel = fl;
    threshould = tr;
    System.out.println("capacity : " + capacity + ", threshold : " + threshould);
    increasingAmount =  capacity-threshould;
   
	}

	@Override
	public int compareTo(WaterTank other) {
		return Double.compare(getPriorityPercentage(),other.getPriorityPercentage());
	}

	//	TODO: call it in a while loop
	public void step(){
		draining();
//		increasingLevel();
		System.out.println("water tank level in each step after draining and increasinglevel :"  );
		System.out.println("\t filled level = "+ this.getFilledLevel() + 
				", threshould = " + this.threshould + ", priorityPercentage : " + this.getPriorityPercentage());
		
	}
	
	public void draining() {		
		filledLevel = Math.max(0, filledLevel - drainingAmount);
	}
	
	
	public void increasingLevel(){//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount);
		System.out.println("Increase water tank, oldLevel: " + oldFilledLevel + ", increase: " + increasingAmount + ", newLevel:" + filledLevel);
	}
	
	public void increasingLevel(double multiply){//TODO: level of satisfaction?
		double oldFilledLevel = filledLevel;
		filledLevel = Math.min(capacity, filledLevel + increasingAmount * multiply);
		System.out.println("Increase water tank, oldLevel: " + oldFilledLevel + ", increase: " + increasingAmount * multiply + ", newLevel:" + filledLevel);
	}
	
	public double getCapacity() {
		return capacity;
	}

	public double getFilledLevel() {
		return filledLevel;
	}
	
	public double getPriorityPercentage(){
		return (((double)(filledLevel - threshould)/threshould)*100.0);
	}
	
	public double getIncreasingAmount(){
		return increasingAmount;
	}

	public double getDrainingAmount() {
		return drainingAmount;
	}

	public double getThreshould() {
		return threshould;
	}

	public String getRelatedAbstractValue() {
		return relatedAbstractValue;
	}

	public void setRelatedAbstractValue(String valueName) {
		this.relatedAbstractValue = valueName;
	}
}
