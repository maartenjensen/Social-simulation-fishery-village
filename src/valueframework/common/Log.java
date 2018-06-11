package valueframework.common;

import java.util.ArrayList;

import valueframework.Action;
import valueframework.Node;
import valueframework.RandomTree;
import valueframework.ValuedAction;

public final class Log {
	
	private static boolean debug = true;
	
	public static void printActions(ArrayList<Action> actions){
	
		for(Action act: actions)
			System.out.print( "act " + act.getTitle() + ", ");
		System.out.println("");
	}

	public static void printValuedActions(String title, ArrayList<ValuedAction> valuedActions) {
		
		String print = title;
		for(ValuedAction act: valuedActions)
			print += act.toString() + " | ";
		System.out.println("VF-vac: " + print);
	}
	
	public static void printAbstractValues(ArrayList<RandomTree> relatedAbstractValue) {		

		Log.printLog("PrintAbstractValues size:" + relatedAbstractValue.size() );
		Log.printLog(", root:" + relatedAbstractValue.get(0).getRoot());
		for(RandomTree rt: relatedAbstractValue)
			System.out.print( "abstractValue " + rt.getRoot().getValueName() + ", ");
		System.out.println("");
	}

	public static void printConcreteValues(String title,
			ArrayList<Node> relatedConcreteValues) {
		System.out.println("Action " + title );		
		for(Node obj: relatedConcreteValues){
			System.out.print( "abstractValue " + (obj.getValueName()) + ", ");
		}
		System.out.println("");
	}
	
	public static void printLog(String output) {
		System.out.println("VF-log: " + output);
	}
	
	public static void printDebug(String debug) {
		if (Log.debug)
			System.out.println("VF-debug: " + debug);
	}
	
	public static void printError(String error) {
		System.err.println("VF-err: " + error);
		new Exception().printStackTrace();
	}
	
	public static void printStars() {
		System.out.println("VF-log:*********************************************************");
	}
}
