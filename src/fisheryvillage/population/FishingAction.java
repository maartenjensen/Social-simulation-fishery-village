package fisheryvillage.population;

public enum FishingAction {

	A_LOT("Fish a lot", 1.0, 0),
	MEDIUM("Fish medium", 0.5, 0.5),
	LESS("Fish less", 0.0, 1.0),
	A_LOT_DANGER("Fish a lot danger", 1.0, 0.0),
	MEDIUM_DANGER("Fish medium danger", 0.5, 0.0),
	LESS_DANGER("Fish less danger", 0.0, 1.0);
	
	private final String actionName;
	private final double fisherEcon;
	private final double fisherEcol;
	
	FishingAction(String actionName, double fisherEcon, double fisherEcol) {
		
		this.actionName = actionName;
		this.fisherEcon = fisherEcon;
		this.fisherEcol = fisherEcol;
	}
	
	public static FishingAction getEnumByString(String actionName){
		for(FishingAction e : FishingAction.values()){
			if(e.getActionName().equals(actionName))
				return e;
		}
		return null;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public double getFisherEcon() {
		return fisherEcon;
	}
	
	public double getFisherEcol() {
		return fisherEcol;
	}
}
