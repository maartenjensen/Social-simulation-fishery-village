package fisheryvillage.property.other;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.population.Status;
import fisheryvillage.property.PropertyColor;
import fisheryvillage.property.Workplace;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* An abstraction of all types of work outside the village
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class CompanyOutside extends Workplace {

	private int maxEmployees = 100;
	
	public CompanyOutside(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 10, 10, PropertyColor.COMPANY);
		allJobs.add(Status.WORK_OUT_OF_TOWN);
		addToValueLayer();
	}
	
	@Override
	public ArrayList<Status> getVacancy(boolean higherEducated, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		if (getEmployeeCount(Status.WORK_OUT_OF_TOWN) < maxEmployees && RandomHelper.nextDouble() <= Constants.WORK_OUTSIDE_PROBABILITY) {
			possibleJobs.add(Status.WORK_OUT_OF_TOWN);
		}
		return possibleJobs;
	}

	@Override
	public String getName() {
		return "Company outside [" + getId() + "]";
	}
	
	@Override
	public VSpatial getSpatial() {
		
		return spatialImagesOwned.get(true);
	}

	@Override
	public String getLabel() {
		return "Company [" + getId() + "]: W:" + getEmployeeCount(Status.WORK_OUT_OF_TOWN) + "/" + maxEmployees;
	}	
}