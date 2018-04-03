package fisheryvillage.common;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import fisheryvillage.population.Status;

/**
* This class holds many constants used throughout the code
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class Constants {

	// Initialize important IDs
	public static final String ID_CONTEXT = "fisheryvillage";
	public static final String ID_SPACE = "space";
	public static final String ID_GRID = "grid";
	public static final String ID_VALUE_LAYER = "village value layer";
	public static final String ID_NETWORK_COUPLE = "couple network";
	public static final String ID_NETWORK_CHILDREN = "children network";
	public static final String ID_NETWORK_PROPERTY = "property network";
	
	// Initialize world parameters
	public static final int GRID_WIDTH = 80;
	public static final int GRID_HEIGHT = 40;
	public static final int GRID_CELL_SIZE = 25;
	public static final int GRID_SEA_START = GRID_WIDTH - 12;
	public static final int GRID_VILLAGE_START = 18;
	public static final int INITIAL_POPULATION_SIZE = 40;
	public static final double MIGRATION_PROBABILITY = 0.001;
	public static final int TICKS_PER_MONTH = 4; // If you change this make sure you change the interval parameters of the repast charts
	public static final int TICKS_PER_YEAR = 12 * TICKS_PER_MONTH;
	
	// Initialize building parameters
	public static final int NUMBER_OF_HOUSES_CHEAP = 16;
	public static final int NUMBER_OF_HOUSES_STANDARD = 10;
	public static final int NUMBER_OF_HOUSES_EXPENSIVE = 5;
	
	public static final int HOUSE_CHEAP_PRICE = 1000;
	public static final int HOUSE_CHEAP_MAINTENANCE = 500;
	public static final int HOUSE_STANDARD_PRICE = 2000;
	public static final int HOUSE_STANDARD_MAINTENANCE = 1000;
	public static final int HOUSE_EXPENSIVE_PRICE = 3000;
	public static final int HOUSE_EXPENSIVE_MAINTENANCE = 1500;
	public static final double PRICE_PER_KG_FISH_UNPROCESSED = 5;
	public static final double PRICE_PER_KG_FISH_PROCESSED = 10;
	public static final int FISH_PROCESSING_AMOUNT_PP = 750;
	public static final int FISH_CATCH_AMOUNT_MIN_PP = 50;
	public static final int FISH_CATCH_AMOUNT_MAX_PP = 200;
	public static final int BOAT_BASIC_PRICE = 1000;
	public static final int BOAT_BASIC_MAINTENANCE = 100;
	public static final int BUILDING_MONEY_DANGER_LEVEL = 15000;
	
	// Initialize population generate parameters
	public static final int HUMAN_INIT_STARTING_MONEY = 5000;
	public static final int HUMAN_INIT_MIN_AGE = 18;
	public static final int HUMAN_INIT_MAX_AGE = 65;
	
	// Initialize population parameters
	public static final int HUMAN_MIN_CHILDREN_WANTED = 0;
	public static final int HUMAN_MAX_CHILDREN_WANTED = 6;
	public static final int HUMAN_RELATIVE_WIFE_MIN_AGE = -20;
	public static final int HUMAN_RELATIVE_WIFE_MAX_AGE = -10;
	public static final int HUMAN_ANCESTORS_LAYERS = 1; //0:none, 1:parents, 2:grandparents, 3:great-grandparents, 4:great-great-grandparents, 5:etc
	public static final int HUMAN_MAX_LIVING_AGE = 90; //TODO should be changed in a distribution
	public static final int HUMAN_ELDERLY_CARE_AGE = 75;
	public static final int HUMAN_MAX_CHILD_GET_AGE = 45;
	public static final int HUMAN_ADULT_AGE = 18;
	public static final int HUMAN_ELDERLY_AGE = 65;
	public static final int HUMAN_MONEY_DANGER_LEVEL = 3000;
	public static final double HUMAN_PROB_GET_RELATION = 0.5;
	public static final double HUMAN_PROB_GET_CHILD = 0.5;
	public static final double HUMAN_PROB_GET_HOUSE = 0.5;
	public static final int HOMELESS_TICK = 3 * TICKS_PER_YEAR;
	
	// Initialize ecosystem parameters
	public static final int ECOSYSTEM_INITIAL_FISH = 100000;
	
	// Initialize money parameters
	public static final double LIVING_COST_ADULT = 300;
	public static final double LIVING_COST_CHILD = 150;
	public static final double COST_SCHOOL_INSIDE = 25;
	public static final double COST_SCHOOL_OUTSIDE = 100;
	public static final double COST_SCHOOL_CHILD = 0;
	public static final double NETTO_INCOME_PERCENTAGE = 50;
	public static final double TAX_TO_COUNCIL = 10;
	
	// Initialize employee numbers
	public static final int MAX_FISHERS_PER_BOAT = 5;
	public static final int FACTORY_INITIAL_MAX_EMPLOYEES = 10;
	public static final int FACTORY_MAX_EMPLOYEES = 50;
	public static final int FACTORY_MIN_EMPLOYEES = 3;
	
	// Initialize salary and benefits parameters
	public static final double SALARY_TEACHER = 3000;
	public static final double SALARY_FACTORY_WORKER = 2000;
	public static final double SALARY_FACTORY_BOSS = 6000; 
	public static final double SALARY_MAYOR = 4000;
	public static final double SALARY_OUTSIDE_WORK = 2500;
	public static final double SALARY_ELDERLY_CARETAKER = 2000;
	public static final double BENEFIT_UNEMPLOYED = 800;
	public static final double BENEFIT_ELDERLY = 1000;
	
	// Initialize event parameters
	public static final double EVENT_PERCENT_TOO_ABSENT = 25;
	public static final double EVENT_PERCENT_TOO_CROWDED = 75;
	
	// Setup parameter IDs
	public static final String PARAMETER_PERCENTAGE_TAX = "percentageTax";
	public static final String PARAMETER_TAX_TO_COUNCIL = "percentageTaxToCouncil";

	// Initialize social status
	// Look at SocialStatus class
	public static final Map<Status, Integer> SOCIAL_STATUS_WORK = new HashMap<Status, Integer>() 
		{
			private static final long serialVersionUID = 1L;
			{
				put(Status.CAPTAIN, 75);
				put(Status.CHILD, 0);
				put(Status.DEAD, 0);
				put(Status.ELDER, 0);
				put(Status.ELDERLY_CARETAKER, 25);
				put(Status.ELDEST, 0);
				put(Status.FACTORY_BOSS, 75);
				put(Status.FACTORY_WORKER, 25);
				put(Status.FISHER, 50);
				put(Status.MAYOR, 100);
				put(Status.NONE, 0);
				put(Status.TEACHER, 50);
				put(Status.UNEMPLOYED, 0);
				put(Status.WORK_OUT_OF_TOWN, 0);
			}};
			
	public static final int SOCIAL_STATUS_EVENT_NONE = 0;
	public static final int SOCIAL_STATUS_EVENT_ORGANIZE = 75;
	public static final int SOCIAL_STATUS_EVENT_ATTEND = 25;
	
	// Initialize icon paths
	public static final String ICON_CHILD = "./icons/child";
	public static final String ICON_TEACHER = "./icons/teacher";
	public static final String ICON_PROCESSOR = "./icons/processor";
	public static final String ICON_FACTORY_BOSS = "./icons/factory_boss";
	public static final String ICON_CARETAKER = "./icons/caretaker";
	public static final String ICON_WORKER_OUTSIDE = "./icons/business";
	public static final String ICON_FISHER = "./icons/fisher";
	public static final String ICON_CAPTAIN = "./icons/captain";
	public static final String ICON_MAYOR = "./icons/mayor";
	public static final String ICON_UNEMPLOYED = "./icons/unemployed";
	public static final String ICON_ELDER = "./icons/elder";
	public static final String ICON_ELDEST = "./icons/eldest";
	
	public static final String ICON_OWNED = "./icons/owned.png";
	public static final String ICON_NOT_OWNED = "./icons/not_owned.png";
	
	// Initialize graphics
	public static final Font FONT_SMALL = new Font("Tahoma", Font.PLAIN , 10);
}