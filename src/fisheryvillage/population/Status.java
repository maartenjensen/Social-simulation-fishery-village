package fisheryvillage.population;

/**
* The status enum shows the status of a Human. This status
* is also represented by different icons.
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public enum Status {
	
	CHILD(false,"none"),
	ELDER(false,"none"),
	ELDEST(false,"none"),
	UNEMPLOYED(false,"Job unemployed"),
	TEACHER(true,"Job teacher"),
	FACTORY_WORKER(false,"Job factory worker"),
	FACTORY_BOSS(true,"Job factory boss"),
	FISHER(false,"Job fisher"),
	MAYOR(true,"Job mayor"),
	CAPTAIN(true,"Job captain"), 
	ELDERLY_CARETAKER(false,"Job elderly caretaker"),
	STUDENT(false,"Job student"),
	WORK_OUT_OF_TOWN(false,"Job work outside village"),
	DEAD(false,"none"),
	NONE(false,"none");
	
	private final boolean needHigherEducated;
	private final String jobActionName;
	
	Status(boolean needHigherEducated, String jobActionName) {
		this.needHigherEducated = needHigherEducated;
		this.jobActionName = jobActionName;
	}
	
	public boolean getNeedHigherEducated() {
		return needHigherEducated;
	}
	
	public String getJobActionName() {
		return jobActionName;
	}
}
