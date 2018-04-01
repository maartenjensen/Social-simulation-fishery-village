import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;

import javax.lang.model.element.VariableElement;

import common.FrameworkBuilder;
import mas.DecisionMaker;
import valueFramework.Action;
import valueFramework.RandomTree;
import valueFramework.WaterTank;


public class Test /*extends TimerTask*/{
	
	public static void something() throws IOException {//main(String [] args) throws IOException{
		FrameworkBuilder v = new FrameworkBuilder();

		//first create value files
		v.readValueTreeFile();
		ArrayList<WaterTank> waterTanks = new ArrayList<WaterTank>();
		//then read actions from file
		v.readActionsFromFile("inputFiles\\actionList3.txt");
		
		//	v.conncetActionsAndConcreteValues();
		v.assginRelatedActionsToConcreteValues();
		
		DecisionMaker h1 = new DecisionMaker(1);
		FrameworkBuilder.decisionMakerList.add(h1);
		
		for(DecisionMaker ha : FrameworkBuilder.decisionMakerList){	
			ha.assignPossibleActions(FrameworkBuilder.allPossibleActions);
		}
		
		h1.createValueTrees();
		
		for(DecisionMaker ha : FrameworkBuilder.decisionMakerList){		
			for (RandomTree value : ha.getValueTrees().values()) {				        
				waterTanks.add(value.getWaterTank());
			}
		}
		
		theoryTest(100, FrameworkBuilder.decisionMakerList, waterTanks);
		System.out.println("end :)");
	}
	
	public static void assginConcreteValuesToActions() {
		for(Action act: FrameworkBuilder.allPossibleActions){
			act.assignRandomConcreteValues();
		}
	}

	public static void theoryTest(int steps, ArrayList<DecisionMaker> agents, ArrayList<WaterTank> waterTanks) {
		
		int initialSteps = steps;
		
		while(steps != 0){
			System.out.println("--------step " + steps + "----------------");
			for (DecisionMaker ha : agents) {
				ha.step();
			}
			for(WaterTank wt : waterTanks){
				wt.step();
			}
			steps--;
		}
//		System.out.println("Result: #uni: " + numOfUniversalism + ", #power: " + numOfPower);
	}

	/*@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}*/
	
//	v.createRandomAction(20);
	
/*		HumanAgent h1 = new HumanAgent();
	//agents can do all the actions
	ArrayList<HumanAgent> agents = new ArrayList<HumanAgent>();
	agents.add(h1);
	
	//after creating human and therefore creating value trees, concrete values are determined
	assginConcreteValuesToActions();
	
	ArrayList<WaterTank> waterTanks = new ArrayList<WaterTank>();
	//Weird! after calling it.remove the entry removed from the hashmap!
	for(int ha = 0 ; ha < agents.size(); ha++){		
		for (RandomTree value : agents.get(ha).getValueTrees().values()) {				
	        waterTanks.add(value.getWaterTank());
		}
	}
	
	h1.assignPossibleActions(v.allPossibleActions);
*/
}
