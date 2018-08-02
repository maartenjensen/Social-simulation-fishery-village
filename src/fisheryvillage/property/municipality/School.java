package fisheryvillage.property.municipality;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import fisheryvillage.property.PropertyColor;
import fisheryvillage.property.Workplace;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

/**
* The village school which has teachers and children
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class School extends Workplace {

	private int maxChildrenPerTeacher = Constants.TEACHER_MAX_CHILDREN;
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	public School(int id, int price, int maintenanceCost, double money, GridPoint location) {
		super(id, price, maintenanceCost, money, location, 11, 8, PropertyColor.SCHOOL);
		allJobs.add(Status.TEACHER);
		addToValueLayer();
	}

	public int getTeacherCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int teachers = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.TEACHER) {
				teachers ++;
			}
		}
		return teachers;
	}
	
	@Override
	public ArrayList<Status> getVacancy(boolean hasBeenFisher, double money) {
		
		ArrayList<Status> possibleJobs = new ArrayList<Status>();
		
		if (!allJobs.contains(Status.TEACHER))
			return possibleJobs;
		
		int teachers = getTeacherCount();
		if (teachers < Math.ceil(getChildrenCount() / (float) maxChildrenPerTeacher)) {
			possibleJobs.add(Status.TEACHER);
		}
		return possibleJobs;
	}
	
	public int getChildrenCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int children = 0;
		for (final Human human: humans) {
			if (human.getStatus() == Status.CHILD) {
				children ++;
			}
		}
		return children;
	}
	
	public int getPupilCount() {
		
		final ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		int pupils = 0;
		for (final Human human: humans) {
			if (human.getSchoolType() == SchoolType.INSIDE_VILLAGE) {
				pupils ++;
			}
		}
		return pupils;
	}
	
	public boolean getPupilVacancy() {
		
		if (getPupilCount() <= getTeacherCount() * maxChildrenPerTeacher && getTeacherCount() > 0) {
			return true;
		}
		return false;
	}

	public void removeExcessiveChildren() {
		
		int childrenToRemove = Math.max(0, getPupilCount() - (getTeacherCount() * maxChildrenPerTeacher));
		if (childrenToRemove == 0)
			return ;
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getSchoolType() == SchoolType.INSIDE_VILLAGE) {
				childrenToRemove --;
				human.setSchoolType(SchoolType.NO_SCHOOL);
			}
			if (childrenToRemove == 0)
				return ;
		}
		Logger.logError("Error no children left to remove, need to still remove:" + childrenToRemove);
	}

	public void removeExcessiveTeachers() {
		
		int teachersToRemove = getTeacherCount() - (int) Math.ceil((float) getChildrenCount() / (float) maxChildrenPerTeacher);
		if (teachersToRemove <= 0)
			return ;
		
		Logger.logInfo("Remove so many teachers:" + teachersToRemove);
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.TEACHER) {
				teachersToRemove --;
				human.stopWorkingAtWorkplace();
			}
			if (teachersToRemove == 0)
				return ;
		}
		Logger.logError("Error no teachers left to remove, need to still remove:" + teachersToRemove);
	}
	
	public void disableSchool() {
		
		// Remove teacher job
		allJobs.remove(0);
		// Remove all humans that are teacher
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.TEACHER) {
				human.stopWorkingAtWorkplace();
			}
		}
	}
	
	/**
	 * Retrieves the payment of the teacher and subtracts it from the savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getTeacherPayment() {
		
		if (paymentCount < 0) {
			Logger.logError("Error in School, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else if (paymentCount == 0) {

			paymentCount = getTeacherCount();
			paymentAmount = Math.max(0, Math.min(Constants.SALARY_TEACHER, getSavings() / paymentCount));
		}
		paymentCount -= 1;
		addSavings(-1 * paymentAmount);
		return paymentAmount;
	}
	
	@Override
	public VSpatial getSpatial() {
		
		if (getTeacherCount() >= 1) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getName() {
		return "School [" + getId() + "]";
	}
	
	@Override
	public String getLabel() {
		return "School [" + getId() + "] T:"+ getTeacherCount() + "/" + Math.ceil(getChildrenCount()/(float) maxChildrenPerTeacher) + ", P:" + getPupilCount() + ", $:" + Math.round(getSavings());
	}
}