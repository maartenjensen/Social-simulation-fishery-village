package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* An abstraction of schools outside of the village, the humans inside
* the village have to pay more to get their children to this outside school
*
* @author Maarten Jensen
*/
public class SchoolOutside extends Property {

	public SchoolOutside(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 5, Status.NONE, PropertyColor.SCHOOL);
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
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "School P:" + getPupilCount();
	}
}
