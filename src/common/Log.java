package common;

import java.util.ArrayList;

import valueFramework.Action;
import valueFramework.Node;
import valueFramework.RandomTree;

public class Log {
	public static void printActions(int humanId, ArrayList<Action> actions){
		System.out.println("Human " + humanId );
		for(Action act: actions)
			System.out.print( "act " + act.getTitle() + ",\t");
		System.out.println("");
	}

	public static void printAbstractValues(int id,
			ArrayList<RandomTree> relatedAbstractValue) {		
		System.out.println("Human " + id );
		for(RandomTree rt: relatedAbstractValue)
			System.out.print( "abstractValue " + rt.getRoot().getValueName() + ",\t");
		System.out.println("");
	}

	/*public static void printConcreteValues(String title,
			ArrayList<Object[]> relatedConcreteValues) {
		System.out.println("Action " + title );		
		for(Object[] obj: relatedConcreteValues){
			System.out.print( "abstractValue " + ((Node)(obj[0])).getValueName() + ",\t");
		}
		System.out.println("");
	}*/

	public static void printConcreteValues(String title,
			ArrayList<Node> relatedConcreteValues) {
		System.out.println("Action " + title );		
		for(Node obj: relatedConcreteValues){
			System.out.print( "abstractValue " + (obj.getValueName()) + ",\t");
		}
		System.out.println("");
	}
}
