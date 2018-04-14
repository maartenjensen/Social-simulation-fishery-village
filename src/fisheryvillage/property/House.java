package fisheryvillage.property;

import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Status;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridValueLayer;

/**
* A house with space for 8 persons, comes
* in three price ranges.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class House extends Property {

	HouseType houseType;
	
	public House(int id, HouseType houseType, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 4, 2, Status.NONE, PropertyColor.HOUSE);
		this.houseType = houseType;
		addGardenToValueLayer();
		addToValueLayer();
	}

	public HouseType getHouseType() {
		return houseType;
	}
	
	public void addGardenToValueLayer() {

		GridValueLayer valueLayer = SimUtils.getValueLayer();
		if (valueLayer == null) {
			Logger.logError("Error valueLayer is null");
			return ;
		}
		
		int width, height;
		switch(houseType) {
			case CHEAP:
				return;
			case STANDARD:
				width = 4;
				height = 3;
				break;
			case EXPENSIVE:
				width = 6;
				height = 5;
				break;
			default:
				width = 0;
				height = 0;
		}
		Logger.logDebug(houseType + " width:" + width + ", height:" + height);
		for (int i = 0; i < width; i ++) {
			for (int j = 0; j < height; j ++) {
				valueLayer.set(RandomHelper.nextDoubleFromTo(3.90, 3.93), getX() + i, getY() + j);
			}
		}
	}
	
	@Override
	public String getName() {
		return "House " + houseType + ": " + getX() + ", " + getY();
	}
	
	@Override
	public String getLabel() {
		return houseType + " $:" + getPrice() + "\nCost $:" + getMaintenanceCost();
	}
}