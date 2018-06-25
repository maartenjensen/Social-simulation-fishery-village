package fisheryvillage.population;

/**
* The status enum shows the status of a Human. This status
* is also represented by different icons.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public enum Status {
	
	CHILD("none",0.5),
	ELDER("none",0.5),
	ELDEST("none",0.5),
	UNEMPLOYED("Job unemployed",0.0),
	TEACHER("Job teacher",0.5),
	FACTORY_WORKER("Job factory worker",0.25),
	FACTORY_BOSS("Job factory boss",1.0),
	FISHER("Job fisher",0.5),
	MAYOR("Job mayor",1.0),
	CAPTAIN("Job captain",0.75), 
	ELDERLY_CARETAKER("Job elderly caretaker",0.25),
	WORK_OUT_OF_TOWN("Job work outside village",0.0),
	DEAD("none",0.0),
	NONE("none",0.0);
	
	private final String jobActionName;
	private final double socialStatusWork;
	
	Status(String jobActionName, Double socialStatusWork) {
		
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
	
	public String getJobActionName() {
		return jobActionName;
	}
	
	public double getSocialStatusWork() {
		return socialStatusWork;
	}
}