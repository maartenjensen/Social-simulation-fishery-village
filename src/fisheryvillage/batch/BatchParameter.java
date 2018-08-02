package fisheryvillage.batch;

public class BatchParameter {

	private String actionListPath = "";
	
	private RunningCondition runningCondition;
	
	private int v1p = 50;
	private int v1t = 50;
	private int v1u = 50;
	private int v1s = 50;
	
	private int v2p = 50;
	private int v2t = 50;
	private int v2u = 50;
	private int v2s = 50;
	
	public BatchParameter(String actionListPath, RunningCondition runningCondition, 
						  int v1p, int v1t, int v1u, int v1s) {
		
		this.actionListPath = actionListPath;
		this.runningCondition = runningCondition;
		
		this.v1p = v1p;
		this.v1t = v1t;
		this.v1u = v1u;
		this.v1s = v1s;
		
		this.v2p = v1p;
		this.v2t = v1t;
		this.v2u = v1u;
		this.v2s = v1s;
	}
	
	public BatchParameter(String actionListPath, RunningCondition runningCondition,
						  int v1p, int v1t, int v1u, int v1s,
						  int v2p, int v2t, int v2u, int v2s) {
		
		this.actionListPath = actionListPath;
		this.runningCondition = runningCondition;
		
		this.v1p = v1p;
		this.v1t = v1t;
		this.v1u = v1u;
		this.v1s = v1s;
		
		this.v2p = v2p;
		this.v2t = v2t;
		this.v2u = v2u;
		this.v2s = v2s;
	}
	
	public String getActionListPath() {
		return actionListPath;
	}
	
	public RunningCondition getRunningCondition() {
		return runningCondition;
	}
	
	public int getPower(int index) {
		if (index == 1) 
			return v1p;
		else 
			return v2p;
	}
	
	public int getTradition(int index) {
		if (index == 1) 
			return v1t;
		else 
			return v2t;
	}
	
	public int getUniversalism(int index) {
		if (index == 1) 
			return v1u;
		else 
			return v2u;
	}
	
	public int getSelfDir(int index) {
		if (index == 1) 
			return v1s;
		else 
			return v2s;
	}
	
	public String toString() {
		return actionListPath + "," + runningCondition.name() + ",p" + v1p + ",t" + v1t + ",u" + v1u + ",s" + v1s
															  + ",2p" + v2p + ",2t" + v2t + ",2u" + v2u + ",2s" + v2s;
	}
}
