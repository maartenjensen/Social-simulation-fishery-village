package fisheryvillage.property.other;

import java.util.ArrayList;

import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.property.PropertyColor;
import fisheryvillage.property.Workplace;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* An abstraction of schools outside of the village, the humans inside
* the village have to pay more to get their children to this outside school
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SchoolOutside extends Workplace {

	public SchoolOutside(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 8, 5, PropertyColor.SCHOOL);
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
	public String getName() {
		return "SchoolOutside [" + getId() + "]";
	}
	
	@Override
	public String getLabel() {
		return "School [" + getId() + "] P:" + getPupilCount();
	}
}
