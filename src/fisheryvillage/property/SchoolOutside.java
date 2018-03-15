package fisheryvillage.property;

import java.awt.Color;
import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;

public class SchoolOutside extends Property {

	public SchoolOutside(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 5, Status.NONE);
		colorId = 5;
		addToValueLayer();
	}
	
	public int getPupilCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int pupils = 0;
		for (final Human human: humans) {
			if (human.getSchoolType() == SchoolType.OUTSIDE_VILLAGE) {
				pupils ++;
			}
		}
		return pupils;
	}
	
	@Override
	public Color getColor() {
		return Constants.COLOR_HOMELESS_CARE;
	}

	@Override
	public String getLabel() {
		return "School P:" + getPupilCount();
	}
}
