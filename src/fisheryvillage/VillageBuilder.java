package fisheryvillage;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.property.Boat;
import fisheryvillage.property.BoatType;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.municipality.Council;
import fisheryvillage.property.municipality.ElderlyCare;
import fisheryvillage.property.municipality.EventHall;
import fisheryvillage.property.municipality.Factory;
import fisheryvillage.property.municipality.School;
import fisheryvillage.property.municipality.SocialCare;
import fisheryvillage.property.other.CompanyOutside;
import fisheryvillage.property.other.SchoolOutside;
import repast.simphony.space.grid.GridPoint;

public class VillageBuilder {

	public void buildVillage() {
		
		createHouses();
		createBoats();
		
		// Create buildings
		new SocialCare(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 2));
		new ElderlyCare(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 22));
		new School(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 12));
		new Factory(SimUtils.getNewPropertyId(), Constants.FACTORY_BUY_PRICE, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 24));
		new Council(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 18));
		new EventHall(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 8));

		// Create buildings outside village
		new SchoolOutside(SimUtils.getNewPropertyId(), 0, 0, 0, new GridPoint(1, 12));
		new CompanyOutside(SimUtils.getNewPropertyId(), 0, 0, 0, new GridPoint(1, 24));
	}

	public void createBoats() {
		
		for (int i = 0; i < Constants.NUMBER_OF_BOATS; i ++) {
			new Boat(SimUtils.getNewPropertyId(), BoatType.NONE, 0, new GridPoint(Constants.GRID_SEA_START + 4, Constants.GRID_HEIGHT - 4 - i * 6));
		}
	}
	
	public void createHouses() {
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_CHEAP + " cheap houses");
		int x = 0, y = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_CHEAP; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 1 + x * 5, 1 + y * 3);
			new House(SimUtils.getNewPropertyId(), HouseType.CHEAP, Constants.HOUSE_CHEAP_PRICE, Constants.HOUSE_CHEAP_MAINTENANCE, 0, location);
			if (y == 7) {
				y = 0;
				x ++;
			}
			else {
				y ++;
			}
		}
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_STANDARD + " standard houses");
		x = 0;
		y = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_STANDARD; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 13 + x * 6, 2 + y * 5);
			new House(SimUtils.getNewPropertyId(), HouseType.STANDARD, Constants.HOUSE_STANDARD_PRICE, Constants.HOUSE_STANDARD_MAINTENANCE, 0, location);
			if (y == 4) {
				y = 0;
				x ++;
			}
			else {
				y ++;
			}
		}
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_EXPENSIVE + " expensive houses");
		x = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_EXPENSIVE; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 1 + x * 7, Constants.GRID_HEIGHT - 6);
			new House(SimUtils.getNewPropertyId(), HouseType.EXPENSIVE, Constants.HOUSE_EXPENSIVE_PRICE, Constants.HOUSE_EXPENSIVE_MAINTENANCE, 0, location);
			x ++;
		}
	}
}
