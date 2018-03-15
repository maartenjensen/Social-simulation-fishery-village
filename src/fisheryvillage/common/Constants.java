package fisheryvillage.common;

import java.awt.Color;
import java.awt.Font;

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
	public static final int INITIAL_POPULATION_SIZE = 30;
	public static final double MIGRATION_PROBABILITY = 0.01;
	public static final int TICKS_PER_YEAR = 1;
	
	// Initialize building parameters
	public static final int NUMBER_OF_HOUSES = 30;
	public static final Color COLOR_PROPERTY = new Color(255, 255, 255);
	public static final Color COLOR_HOUSE = new Color(230, 210, 175);
	public static final Color COLOR_HOMELESS_CARE = new Color(200, 200, 200);
	
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
	public static final double HUMAN_PROB_GET_RELATION = 0.5;
	public static final double HUMAN_PROB_GET_CHILD = 0.5;
	public static final double HUMAN_PROB_GET_HOUSE = 0.5;
	
	// Initialize money parameters
	public static final double LIVING_COST_ADULT = 300;
	public static final double LIVING_COST_CHILD = 150;
	public static final double COST_SCHOOL_INSIDE = 25;
	public static final double COST_SCHOOL_OUTSIDE = 100;
	public static final double COST_SCHOOL_CHILD = 0;
	public static final double NETTO_INCOME_PERCENTAGE = 50;
	public static final double TAX_TO_COUNCIL = 20;
	
	// Initialize salary parameters
	public static final double SALARY_TEACHER = 3000;
	public static final double SALARY_FACTORY_WORKER = 2500;
	public static final double SALARY_OUTSIDE_WORK = 3000;

	// Setup parameter IDs
	public static final String PARAMETER_PERCENTAGE_TAX = "percentageTax";
	public static final String PARAMETER_TAX_TO_COUNCIL = "percentageTaxToCouncil";

	// Initialize icon paths
	public static final String ICON_HOUSE = "./icons/house.png";
	public static final String ICON_HOMELESS_CARE = "./icons/homelessCare.png";
	public static final String ICON_CHILD = "./icons/child";
	public static final String ICON_TEACHER = "./icons/teacher";
	public static final String ICON_PROCESSOR = "./icons/processor";
	
	// Initialize graphics
	public static final Font FONT_SMALL = new Font("Tahoma", Font.PLAIN , 10);
}