package fisheryvillage;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.ecosystem.Ecosystem;
import fisheryvillage.municipality.Council;
import fisheryvillage.municipality.EventHall;
import fisheryvillage.population.Human;
import fisheryvillage.property.Boat;
import fisheryvillage.property.CompanyOutside;
import fisheryvillage.property.ElderlyCare;
import fisheryvillage.property.Factory;
import fisheryvillage.property.House;
import fisheryvillage.property.HouseType;
import fisheryvillage.property.School;
import fisheryvillage.property.SchoolOutside;
import fisheryvillage.property.SocialCare;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.valueLayer.GridValueLayer;
import valueframework.common.FrameworkBuilder;

/**
* The FisheryVillageContextBuilder builds the repast simulation
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class FisheryVillageContextBuilder implements ContextBuilder<Object> {
	
	/*=========================================
	 * Simulation building
	 *=========================================
	 */
	@Override
	public Context<Object> build(Context<Object> context) {

		// Reset human id
		HumanUtils.resetHumanId();
		SimUtils.resetPropertyId();
		Logger.enableLogger();
		
		// Add context to this ID
		Logger.logMain("Set context ID and add context to context");
		context.setId(Constants.ID_CONTEXT);
		context.add(this); //this can be removed if there is no ScheduledMethod in this contextbuilder
		
		// Create space and grid
		Logger.logMain("Create continuous space and grid");
		@SuppressWarnings("unused")
		final ContinuousSpace<Object> space = createContinuousSpace(context);
		@SuppressWarnings("unused")
		final Grid<Object> grid = createGrid(context);

		// Create value layer
		final GridValueLayer valueLayer = createValueLayer();
		context.addValueLayer(valueLayer);
		generateNature(valueLayer);
		
		// Create networks
		Logger.logMain("Create couple and children network");
		NetworkBuilder<Object> netBuilderCouple = new NetworkBuilder<Object> (Constants.ID_NETWORK_COUPLE, context, false);
		netBuilderCouple.buildNetwork();
		NetworkBuilder<Object> netBuilderChildren = new NetworkBuilder<Object> (Constants.ID_NETWORK_CHILDREN, context, true);
		netBuilderChildren.buildNetwork();
		NetworkBuilder<Object> netBuilderProperty = new NetworkBuilder<Object> (Constants.ID_NETWORK_PROPERTY, context, true);
		netBuilderProperty.buildNetwork();
		
		// Set Context for SimUtils
		SimUtils.setContext(context);
		SimUtils.getGrid();
		SimUtils.getValueLayer();
		
		// Create value framework
		FrameworkBuilder.initialize();
		
		// Create houses
		createHouses();

		// Create boats
		new Boat(SimUtils.getNewPropertyId(), Constants.BOAT_BASIC_PRICE, Constants.BOAT_BASIC_MAINTENANCE, 0, new GridPoint(Constants.GRID_SEA_START + 3, Constants.GRID_HEIGHT - 8));
		new Boat(SimUtils.getNewPropertyId(), Constants.BOAT_BASIC_PRICE, Constants.BOAT_BASIC_MAINTENANCE, 0, new GridPoint(Constants.GRID_SEA_START + 3, Constants.GRID_HEIGHT - 13));

		// Create buildings
		new SocialCare(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 2));
		new ElderlyCare(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 22));
		new School(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_VILLAGE_START + 25, 12));
		new Factory(SimUtils.getNewPropertyId(), 1000, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 24));
		new Council(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 18));
		new EventHall(SimUtils.getNewPropertyId(), 0, 0, Constants.BUILDING_INITIAL_MONEY, new GridPoint(Constants.GRID_SEA_START - 11, 10));

		// Create buildings outside village
		new SchoolOutside(SimUtils.getNewPropertyId(), 0, 0, 0, new GridPoint(1, 12));
		new CompanyOutside(SimUtils.getNewPropertyId(), 0, 0, 0, new GridPoint(1, 24));

		// Create ecosystem
		new Ecosystem(Constants.ECOSYSTEM_INITIAL_FISH, new GridPoint(Constants.GRID_SEA_START + 2, Constants.GRID_HEIGHT - 20));
		
		// Create population
		generatePopulation();

		return context;
	}
	
	/*=========================================
	 * Simulation schedule
	 *=========================================
	 */
	/**
	 * Step 0 Tick: starting tick, migration
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 0) //Highest priority, so this is activated first
	public void step0Tick() {
		
		Logger.logMain("------------------------------------------------------------------------------");
		double tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		Logger.logMain("0TICK: Starting tick: "+ tick +", " + SimUtils.getCouncil().getDate());
		if (Constants.MIGRATION_PROBABILITY > RandomHelper.nextDouble()) {
			
			Human human = new Human(SimUtils.getRandomBoolean(),
									RandomHelper.nextIntFromTo(Constants.HUMAN_INIT_MIN_AGE, Constants.HUMAN_INIT_MAX_AGE),
									HumanUtils.getNewHumanId(), Constants.HUMAN_INIT_STARTING_MONEY, true);
			Logger.logMain("-- New human spawned : " + human.getId());
		}
		/*
		if (tick == 2400) {
			Logger.logMain("-- 50 years have passed, save population");
			PopulationBuilder populationBuilder = new PopulationBuilder();
			populationBuilder.savePopulation("./output", "population");
		}*/
	}

	/**
	 * Step 1 Year: aging and family
	 */
	@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_YEAR, priority = -1)
	public void step1Year() {
		
		Logger.logMain("1YEAR: aging and family");
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		Logger.logMain("- Run Human.stepAging");
		for (final Human human: humans) {
			human.stepAging();
		}
		
		Logger.logMain("- Run Boat.hasCaptain");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.hasCaptain();
		}
		
		Logger.logMain("- Run Human.stepFamily");
		for (final Human human: humans) {
			human.stepFamily();
		}
	}
	
	/**
	 * Step 2 month: teacher removal
	 */
	@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_MONTH, priority = -2)
	public void step2Month() {
		
		Logger.logMain("2MONTH: aging and family");
		
		Logger.logMain("- Run School.removeExcessiveTeachers");
		SimUtils.getSchool().removeExcessiveTeachers(); //TODO put this somewhere that is more appropriate
		Logger.logMain("- Run ElderlyCare.removeExcessiveCaretakers");
		SimUtils.getElderlyCare().removeExcessiveCaretakers();
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		Logger.logMain("- Run Human.stepHousing");
		for (final Human human: humans) {
			human.stepHousing();
		}
		
		Logger.logMain("- Run Human.stepChildrenSchooling");
		for (final Human human: humans) {
			human.stepChildrenSchooling();
		}
		Logger.logMain("- Run Human.stepRelation");
		for (final Human human: humans) {
			human.stepRelation();
		}
	}
	
	/**
	 * Step 3 week: working and social events
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = -3)
	public void step3Tick() {
		
		Logger.logMain("3TICK: working and social events");
		
		Logger.logMain("- Run EventHall.resetEventHall");
		SimUtils.getEventHall().stepResetEventHall();
		SimUtils.getCouncil().resetCounts();
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		if (!SimUtils.getInitializationPhase()) {
			Logger.logMain("- Run Human.stepDrainTanks");
			for (final Human human: humans) {
				human.stepDrainTanks();
			}
		}
		
		Logger.logMain("- Run Human.stepSocialStatusDrain");
		for (final Human human: humans) {
			human.stepSocialStatusDrain();
		}
		
		Logger.logMain("- Run Human.stepSocialEvent");
		for (final Human human: humans) {
			if (!SimUtils.getInitializationPhase()) {
				human.stepSocialEvent();
			}
			else {
				human.stepSocialEventOld();
			}
		}

		Logger.logMain("- Run EventHall.stepPerformSocialEvent");
		SimUtils.getEventHall().stepPerformSocialEvent();
		
		Logger.logMain("- Ecosystem.stepEcosystem");
		SimUtils.getEcosystem().stepEcosystem();
		
		Logger.logMain("- Boat.stepFish");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.stepFish();
		}
		
		Logger.logMain("- Run Human.stepDonate");
		for (final Human human: humans) {
			human.stepDonate();
		}
	}
	
	/**
	 * Step 4 month: monthly payments and death
	 */
	@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_MONTH, priority = -4)
	public void step4Month() {
		Logger.logMain("4MONTH: fishing/processing, montly payments, work selection, migration/death, council");
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		
		Logger.logMain("- Boat.stepSellFish");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.stepSellFish();
		}
				
		Logger.logMain("- Run Boat.stepProcessFish");
		SimUtils.getFactory().stepProcessFish();
		
		Logger.logMain("- Run Human.stepResetStandardCosts");
		for (final Human human: humans) {
			human.stepResetStandardCosts();
		}
		
		Logger.logMain("- Run Human.stepWork");
		for (final Human human: humans) {
			human.stepWork();
		}
		
		Logger.logMain("- Run Human.stepPayStandardCosts");
		for (final Human human: humans) {
			human.stepPayStandardCosts();
		}
		Logger.logMain("- Run Human.stepSelectWork");
		for (final Human human: humans) {
			human.stepSelectWork();
		}
		
		Logger.logMain("- Run Council.stepDistributeMoney");
		SimUtils.getCouncil().stepDistributeMoney();
		
		// Human.stepDeath should be the last one before Human.stepLocation
		Logger.logMain("- Run Human.stepDeath");
		ArrayList<Integer> humanIds = new ArrayList<Integer>();
		for (final Human human: humans) {
			humanIds.add(human.getId());
		}
		// Loop through humanIds
		for (Integer humanId: humanIds) {
			if (HumanUtils.getHumanById(humanId) != null) {
				Human human = HumanUtils.getHumanById(humanId);
				Logger.logProb("Death step for H" + human.getId(), 0.01);
				human.stepDeath();
			}
			else {
				Logger.logDebug("NO Death step for H" + humanId);
			}
		}
	}
	
	/**
	 * Step 5 tick: movement
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = -5)
	public void step5Tick() {
	
		Logger.logMain("5TICK: human location");
		
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
		Logger.logMain("- Run Human.stepLocation");
		for (final Human human: humans) {
			human.stepLocation();
		}

		Logger.logMain("------------------------------------------------------------------------------");
		Logger.logMain("End of this step");
	}

	/*=========================================
	 * Extra functions
	 *=========================================
	 */
	/** 
	 * Creates the continuous space for the fishery village.
	 * @param context
	 * @return
	 */ 
	private ContinuousSpace<Object> createContinuousSpace(final Context<Object> context) {
		
		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).
				createContinuousSpace( 	Constants.ID_SPACE, context,
										new RandomCartesianAdder<Object>(),
										new repast.simphony.space.continuous.BouncyBorders(),
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
		return space;
	}
	
	private Grid<Object> createGrid(final Context<Object> context) {
		
		final Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid(
										Constants.ID_GRID, context,
										new GridBuilderParameters<Object>(
										new repast.simphony.space.grid.BouncyBorders(),
										new SimpleGridAdder<Object>(), true,
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT));
		return grid;
	}

	private GridValueLayer createValueLayer() {
		
		final GridValueLayer valueLayer = new GridValueLayer(
		        Constants.ID_VALUE_LAYER,
		        true,
		        new repast.simphony.space.grid.BouncyBorders(),
		        Constants.GRID_WIDTH,
		        Constants.GRID_HEIGHT);
		return valueLayer;
	}

	private void generateNature(GridValueLayer valueLayer) {
		
		for (int i = 0; i < Constants.GRID_WIDTH; i ++) {
			for (int j = 0; j < Constants.GRID_HEIGHT; j ++) {
				
				if (i < Constants.GRID_VILLAGE_START) { // Rubble
					valueLayer.set(RandomHelper.nextDoubleFromTo(2.9, 2.99), i, j);
				}
				else if (i < Constants.GRID_SEA_START) { // Grass
					valueLayer.set(RandomHelper.nextDoubleFromTo(0.95, 0.99), i, j);
				}
				else { // Water
					valueLayer.set(RandomHelper.nextDoubleFromTo(1.9, 1.99), i, j);
				}
			}
		}
	}
	
	private void createHouses() {
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_CHEAP + " cheap houses");
		int x = 0, y = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_CHEAP; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 1 + x * 5, 1 + y * 3);
			new House(SimUtils.getNewPropertyId(), HouseType.CHEAP, Constants.HOUSE_CHEAP_PRICE, Constants.HOUSE_CHEAP_MAINTENANCE, 0, location);
			if (y == 7) {
				y = 0;
				x ++;
			}
			else {
				y ++;
			}
		}
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_STANDARD + " standard houses");
		x = 0;
		y = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_STANDARD; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 13 + x * 6, 2 + y * 5);
			new House(SimUtils.getNewPropertyId(), HouseType.STANDARD, Constants.HOUSE_STANDARD_PRICE, Constants.HOUSE_STANDARD_MAINTENANCE, 0, location);
			if (y == 4) {
				y = 0;
				x ++;
			}
			else {
				y ++;
			}
		}
		
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES_EXPENSIVE + " expensive houses");
		x = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES_EXPENSIVE; ++i) {
			
			final GridPoint location = new GridPoint(Constants.GRID_VILLAGE_START + 1 + x * 7, Constants.GRID_HEIGHT - 6);
			new House(SimUtils.getNewPropertyId(), HouseType.EXPENSIVE, Constants.HOUSE_EXPENSIVE_PRICE, Constants.HOUSE_EXPENSIVE_MAINTENANCE, 0, location);
			x ++;
		}
	}
	
	private void generatePopulation() {
		
		boolean generatePopulationFromFile = true;
		PopulationBuilder populationBuilder = new PopulationBuilder();
		
		// Disable value based framework for initialization
		SimUtils.enableInitializationPhase();
		
		if (generatePopulationFromFile) {
			Logger.logMain("Generate " + Constants.INITIAL_POPULATION_SIZE + " humans");
			populationBuilder.generatePopulation("./output", "population");
		}
		else
		{
			Logger.logMain("Create " + Constants.INITIAL_POPULATION_SIZE + " humans");
			for (int i = 0; i < Constants.INITIAL_POPULATION_SIZE; ++i) {
	
				// Humans are automatically added to the context and placed in the grid
				Human human = new Human(SimUtils.getRandomBoolean(), RandomHelper.nextIntFromTo(Constants.HUMAN_INIT_MIN_AGE, Constants.HUMAN_INIT_MAX_AGE),
						  				HumanUtils.getNewHumanId(), Constants.HUMAN_INIT_STARTING_MONEY, false);
				Logger.logInfo("Create H" + human.getId() + ", age: " + human.getAge());
			}
			
			//Logger.setLoggerAll(true, true, false, false, false);
			Logger.enableLogger();
			// Humans
			int years = 0;
			for (int i = 1; i <= Constants.TICKS_PER_YEAR * years; i ++) { //It starts at 1 since a real scheduled run will also start at 1
				Logger.logMain("----- PRE-SCHEDULER STEP " + i + " -----");
				fullStep(i);
			}
			
			// Save population	
			//populationBuilder.savePopulation("./output", "population");
		}
		
		// Do a location step
		step5Tick();
		// To do a value based run
		SimUtils.disableInitializationPhase();
		Logger.enableLogger();
	}
	
	/**
	 * Runs a fullStep, apart from the scheduler.
	 * Used to generate a starting population that has some properties/children
	 * @param tick the current tick
	 */
	private void fullStep(int tick) {
		
		step0Tick();
		if (tick % Constants.TICKS_PER_YEAR == 1) {
			step1Year();
		}
		if (tick % Constants.TICKS_PER_MONTH == 1) {
			step2Month();
		}
		step3Tick();
		if (tick % Constants.TICKS_PER_MONTH == 1) {
			step4Month();
		}
		step5Tick();
	}
}
