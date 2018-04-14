package valueframework.example;

import java.util.ArrayList;

import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class Test {
	
	public static void main(String [] args) {

		//first create value files
		FrameworkBuilder.initialize();
		Log.printStars();
		ArrayList<Agent> agents = new ArrayList<Agent>();
		Log.printLog("Create agents");
		agents.add(new Agent(0));
		agents.add(new Agent(1));
		
		theoryTest(100, agents);
	}

	private static void theoryTest(int steps, ArrayList<Agent> agents) {
		
		for (int i = 1; i <= steps; i++) {
			
			Log.printLog("********************** Step " + i + " ************************");
			Log.printLog("Drain step");
			for (Agent agent : agents) {
				agent.stepDrainTanks();
			}
			
			Log.printStars();
			Log.printLog("Take action");
			for (Agent agent : agents) {
				agent.stepAction();
			}
		}
	}
}