package fisheryvillage.common;

import repast.simphony.engine.environment.RunEnvironment;
import valueframework.AbstractValue;

public final class RepastParam {
		
	private static boolean popGenToFile = false;
	private static String popGenFileName = "none";
	private static int popGenTickLimit = 1;
	
	private static boolean popInitFromFile = false;
	private static String popInitFileName = "none";
	
	private static int valuePower = 50;
	private static int valueTradition = 50;
	private static int valueUniversalism = 50;
	private static int valueSelfDirection = 50;
	
	public static void setRepastParameters() {
		
		popGenToFile = RunEnvironment.getInstance().getParameters().getBoolean("popGenToFile");
		popGenFileName = RunEnvironment.getInstance().getParameters().getString("popGenFileName");
		popGenTickLimit = RunEnvironment.getInstance().getParameters().getInteger("popGenTickLimit");
		
		popInitFromFile = RunEnvironment.getInstance().getParameters().getBoolean("popInitFromFile");
		popInitFileName = RunEnvironment.getInstance().getParameters().getString("popInitFileName");
		
		valuePower = RunEnvironment.getInstance().getParameters().getInteger("valuePower");
		valueSelfDirection = RunEnvironment.getInstance().getParameters().getInteger("valueSelfDirection");
		valueUniversalism = RunEnvironment.getInstance().getParameters().getInteger("valueUniversalism");
		valueTradition = RunEnvironment.getInstance().getParameters().getInteger("valueTradition");
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
	
	public static int getPower() {
		return valuePower;
	}
	
	public static int getSelfDirection() {
		return valueSelfDirection;
	}
	
	public static int getUniversalism() {
		return valueUniversalism;
	}
	
	public static int getTradition() {
		return valueTradition;
	}
	
	public static int getAbstractValue(String valueName) {
		
		Logger.logDebug("GETABSTRACTVALUE : " + valueName);
		if (valueName.equals(AbstractValue.POWER.name())) {
			return getPower();
		}
		else if (valueName.equals(AbstractValue.UNIVERSALISM.name())) {
			return getUniversalism();
		}
		else if (valueName.equals(AbstractValue.SELFDIRECTION.name())) {
			return getSelfDirection();
		}
		else if (valueName.equals(AbstractValue.TRADITION.name())) {
			return getTradition();
		}
		return 0;
	}
}