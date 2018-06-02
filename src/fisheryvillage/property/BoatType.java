package fisheryvillage.property;

import fisheryvillage.common.Constants;
import repast.simphony.space.grid.GridPoint;

public enum BoatType {
	
	NONE(0,0,0,0),
	SMALL(Constants.BOAT_SMALL_PRICE, Constants.BOAT_SMALL_MAINTENANCE, Constants.BOAT_SMALL_EMPLOYEES,0.3),
	MEDIUM(Constants.BOAT_MEDIUM_PRICE, Constants.BOAT_MEDIUM_MAINTENANCE, Constants.BOAT_MEDIUM_EMPLOYEES,0.6),
	LARGE(Constants.BOAT_LARGE_PRICE, Constants.BOAT_LARGE_MAINTENANCE, Constants.BOAT_LARGE_EMPLOYEES,1);
	
	private final int price;
	private final int maintenanceCost;
	private final int employeeCapacity;
	private final GridPoint dimensions;
	private final double socialStatusBoat;
	
	BoatType(int price, int maintenanceCost, int employeeCapacity, double socialStatusBoat) {
		
		this.price = price;
		this.maintenanceCost = maintenanceCost;
		this.employeeCapacity = employeeCapacity;
		if (employeeCapacity == 0) {
			this.dimensions = new GridPoint(0,0);
		}
		else {
			this.dimensions = new GridPoint(employeeCapacity / 2, 2);
		}
		this.socialStatusBoat = socialStatusBoat;
	}
	
	public int getPrice() {
		return price;
	}
	
	public int getMaintenanceCost() {
		return maintenanceCost;
	}
	
	public int getEmployeeCapacity() {
		return employeeCapacity;
	}
	
	public GridPoint getDimensions() {
		return dimensions;
	}
	
	public double getSocialStatusBoat() {
		return socialStatusBoat;
	}
	
	public static BoatType getEnumByString(String name){
		for(BoatType e : BoatType.values()){
			if(e.name().equals(name))
				return e;
		}
		return null;
	}
}