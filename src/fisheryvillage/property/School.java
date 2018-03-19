package fisheryvillage.property;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.SchoolType;
import fisheryvillage.population.Status;
import repast.simphony.space.grid.GridPoint;
import saf.v3d.scene.VSpatial;

public class School extends Property {

	private int maxChildrenPerTeacher = 10; // TODO Put into constants
	private double paymentAmount = 0;
	private int paymentCount = 0;
	
	public School(double price, double maintenanceCost, double money, GridPoint location) {
		super(price, maintenanceCost, money, location, 8, 5, Status.TEACHER, PropertyColor.SCHOOL);
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
	
	public boolean getVacancy() {
		
		int teachers = getTeacherCount();
		if (teachers < Math.ceil(getChildrenCount()/ (float) maxChildrenPerTeacher)) {
			return true;
		}
		return false;
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
		Logger.logErrorLn("Error no children left to remove, need to still remove:" + childrenToRemove);
	}

	public void removeExcessiveTeachers() {
		
		
		int teachersToRemove = getTeacherCount() - (int) Math.ceil((float) getChildrenCount() / (float) maxChildrenPerTeacher);
		if (teachersToRemove <= 0)
			return ;
		
		Logger.logOutputLn("Remove so many teachers:" + teachersToRemove);
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		for (final Human human: humans) {
			if (human.getStatus() == Status.TEACHER) {
				teachersToRemove --;
				human.setStatus(Status.UNEMPLOYED);
			}
			if (teachersToRemove == 0)
				return ;
		}
		Logger.logErrorLn("Error no teachers left to remove, need to still remove:" + teachersToRemove);
	}
	
	/**
	 * Retrieves the payment of the teacher and subtracts it from the savings
	 * Should only be called in the work function of Human
	 * @return
	 */
	public double getTeacherPayment() {
		
		if (paymentCount == 0) {
			paymentCount = getTeacherCount();
			paymentAmount = Math.min(Constants.SALARY_TEACHER, getSavings() / paymentCount);
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			// Also remove cost of pupils
			removeFromSavings(-Constants.COST_SCHOOL_CHILD * getPupilCount());
			return paymentAmount;
		}
		else if (paymentCount < -1) {
			Logger.logErrorLn("Error in School, exceeded paymentCount : " + paymentCount);
			return 0;
		}
		else {
			paymentCount -= 1;
			removeFromSavings(-paymentAmount);
			return paymentAmount;
		}
	}

	@Override
	public VSpatial getSpatial() {
		
		if (getTeacherCount() >= 1) {
			return spatialImagesOwned.get(true);
		}
		return spatialImagesOwned.get(false);
	}
	
	@Override
	public String getLabel() {
		return "School T:"+ getTeacherCount() + "/" + Math.ceil(getChildrenCount()/(float) maxChildrenPerTeacher) + ", P:" + getPupilCount() + ", $:" + Math.round(getSavings());
	}
}