package fisheryvillage.common;

import repast.simphony.engine.environment.RunEnvironment;
import valueframework.AbstractValue;

public final class RepastParam {

	private static boolean popGenToFile = false;
	private static String popGenFileName = "none";
	private static int popGenTickLimit = 1;
	
	private static boolean popInitFromFile = false;
	private static String popInitFileName = "none";
	
	private static int pauseSimulationAt = -1;
	
	private static int valuePower1 = 50;
	private static int valueTradition1 = 50;
	private static int valueUniversalism1 = 50;
	private static int valueSelfDirection1 = 50;
	
	private static int valuePower2 = 50;
	private static int valueTradition2 = 45;
	private static int valueUniversalism2 = 45;
	private static int valueSelfDirection2 = 45;

	public static void setRepastParameters() {
		
		popGenToFile = RunEnvironment.getInstance().getParameters().getBoolean("popGenToFile");
		popGenFileName = RunEnvironment.getInstance().getParameters().getString("popGenFileName");
		popGenTickLimit = RunEnvironment.getInstance().getParameters().getInteger("popGenTickLimit");
		
		popInitFromFile = RunEnvironment.getInstance().getParameters().getBoolean("popInitFromFile");
		popInitFileName = RunEnvironment.getInstance().getParameters().getString("popInitFileName");
		
		pauseSimulationAt = RunEnvironment.getInstance().getParameters().getInteger("simPauseTick");
		
		valuePower1 = RunEnvironment.getInstance().getParameters().getInteger("valuePower");
		valueSelfDirection1 = RunEnvironment.getInstance().getParameters().getInteger("valueSelfDirection");
		valueUniversalism1 = RunEnvironment.getInstance().getParameters().getInteger("valueUniversalism");
		valueTradition1 = RunEnvironment.getInstance().getParameters().getInteger("valueTradition");
		
		valuePower2 = valuePower1;
		valueTradition2 = valueTradition1;
		valueUniversalism2 = valueUniversalism1;
		valueSelfDirection2 = valueSelfDirection1;
	}
	
	public static void setRepastParameters(boolean pGenToFile, String pGenFileName, int pGenTickLimit,
										   boolean pInitFromFile, String pInitFileName, int pPauseAtTick,
										   int pPower, int pSelf, int pUni, int pTrad,
										   int pPower2, int pSelf2, int pUni2, int pTrad2) {
		popGenToFile = pGenToFile;
		popGenFileName = pGenFileName;
		popGenTickLimit = pGenTickLimit;
		
		popInitFromFile = pInitFromFile;
		popInitFileName = pInitFileName;
		
		pauseSimulationAt = pPauseAtTick;
		
		valuePower1 = pPower;
		valueSelfDirection1 = pSelf;
		valueUniversalism1 = pUni;
		valueTradition1 = pTrad;
		
		valuePower2 = pPower2;
		valueSelfDirection2 = pSelf2;
		valueUniversalism2 = pUni2;
		valueTradition2 = pTrad2;
	}
	
	public static boolean getPopGenToFile() {
		return popGenToFile;
	}
	
	public static String getPopGenFileName() {
		return popGenFileName;
	}
	
	public static int getPopGenTickLimit() {
		return popGenTickLimit;
	}
	
	public static boolean getPopInitFromFile() {
		return popInitFromFile;
	}
	
	public static String getPopInitFileName() {
		return popInitFileName;
	}
	
	public static int getSimulationPauseInt() {
		return pauseSimulationAt;
	}
	
	public static int getPower(int index) {
		if (index == 1) 
			return valuePower1;
		else 
			return valuePower2;
	}
	
	public static int getSelfDirection(int index) {
		if (index == 1) 
			return valueSelfDirection1;
		else 
			return valueSelfDirection2;
	}
	
	public static int getUniversalism(int index) {
		if (index == 1) 
			return valueUniversalism1;
		else 
			return valueUniversalism2;
	}
	
	public static int getTradition(int index) {
		if (index == 1) 
			return valueTradition1;
		else 
			return valueTradition2;
	}
	
	public static int getAbstractValue(String valueName, int index) {
		
		if (valueName.equals(AbstractValue.POWER.name())) {
			return getPower(index);
		}
		else if (valueName.equals(AbstractValue.UNIVERSALISM.name())) {
			return getUniversalism(index);
		}
		else if (valueName.equals(AbstractValue.SELFDIRECTION.name())) {
			return getSelfDirection(index);
		}
		else if (valueName.equals(AbstractValue.TRADITION.name())) {
			return getTradition(index);
		}
		return 0;
	}
}