package fisheryvillage.population;

/**
* The status enum shows the status of a Human. This status
* is also represented by different icons.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public enum Status {
	
	CHILD(false,"none",0.5),
	ELDER(false,"none",0.5),
	ELDEST(false,"none",0.5),
	UNEMPLOYED(false,"Job unemployed",0.0),
	TEACHER(true,"Job teacher",0.5),
	FACTORY_WORKER(false,"Job factory worker",0.25),
	FACTORY_BOSS(true,"Job factory boss",1.0),
	FISHER(false,"Job fisher",0.5),
	MAYOR(true,"Job mayor",1.0),
	CAPTAIN(true,"Job captain",0.75), 
	ELDERLY_CARETAKER(false,"Job elderly caretaker",0.25),
	STUDENT(false,"Job student",0.0),
	WORK_OUT_OF_TOWN(false,"Job work outside village",0.0),
	DEAD(false,"none",0.0),
	NONE(false,"none",0.0);
	
	private final boolean needHigherEducated;
	private final String jobActionName;
	private final double socialStatusWork;
	
	Status(boolean needHigherEducated, String jobActionName, Double socialStatusWork) {
		this.needHigherEducated = needHigherEducated;
		this.jobActionName = jobActionName;
		this.socialStatusWork = socialStatusWork;
	}
	
	public static Status getEnumByString(String actionName){
		for(Status e : Status.values()){
			if(e.getJobActionName().equals(actionName))
				return e;
		}
		return null;
	}
	
	public boolean getNeedHigherEducated() {
		return needHigherEducated;
	}
	
	public String getJobActionName() {
		return jobActionName;
	}
	
	public double getSocialStatusWork() {
		return socialStatusWork;
	}
}