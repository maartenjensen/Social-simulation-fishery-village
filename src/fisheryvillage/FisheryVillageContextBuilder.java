package fisheryvillage;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.RepastParam;
import fisheryvillage.common.SimUtils;
import fisheryvillage.ecosystem.Ecosystem;
import fisheryvillage.population.Resident;
import fisheryvillage.property.Boat;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
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
		RepastParam.setRepastParameters();
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
		
		// Set Context for SimUtils
		SimUtils.setContext(context);
		SimUtils.getGrid();
		SimUtils.getValueLayer();
		
		// Create value framework
		FrameworkBuilder.initialize();
		
		// Create village
		VillageBuilder villageBuilder = new VillageBuilder();
		villageBuilder.buildVillage();
		
		// Create ecosystem
		new Ecosystem(Constants.ECOSYSTEM_STABLE_FISH, new GridPoint(Constants.GRID_SEA_START + 2, Constants.GRID_HEIGHT - 20));
		new DataCollector(new GridPoint(2,2));
		
		// Create population
		initializePopulation();

		return context;
	}

	/*=========================================
	 * Simulation schedule
	 *=========================================
	 */
	
	/*
	 * Runs a fullStep, apart from the scheduler
	 * Used to generate a starting population that has some properties/children
	 * @param tick the current tick
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void fullStep() {
		
		Logger.logMain("Run fullstep");
		int pauseRunTick = RepastParam.getSimulationPauseInt();
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

		Logger.logMain("------------------------------------------------------------------------------");
		Logger.logMain("0TICK: Starting tick: "+ tick +", " + SimUtils.getCouncil().getDate());
		
		if (tick % Constants.TICKS_PER_YEAR == 1) {
			step1Year();
		}
		step2Tick();
		if (tick % Constants.TICKS_PER_MONTH == 1) {
			step3Month();
		}
		step4Tick();
		if (tick % Constants.TICKS_PER_MONTH == 1) {
			step5Month();
		}
		step6Tick();
		if (tick == pauseRunTick && pauseRunTick >= 1) {
			RunEnvironment.getInstance().pauseRun();
			Logger.logMain("Pause simulation at : " + pauseRunTick);
			Logger.logMain("------------------------------------------------------------------------------");
		}
	}

	/**
	 * Step 1 Year: aging and family
	 */
	//@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_YEAR, priority = -1)
	public void step1Year() {
		
		Logger.logMain("1YEAR: aging, migration and family");
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		Logger.logMain("- Run Human.stepAging");
		for (final Resident resident: residents) {
			resident.stepAging();
		}
		
		Logger.logMain("- Check migration");
		if (Constants.NEW_RESIDENT_PROB > RandomHelper.nextDouble()) {
			
			Resident resident = new Resident(HumanUtils.getNewHumanId(), SimUtils.getRandomBoolean(), true,
									RandomHelper.nextIntFromTo(Constants.HUMAN_INIT_MIN_AGE, Constants.HUMAN_INIT_MAX_AGE),
									Constants.HUMAN_INIT_STARTING_MONEY);
			Logger.logMain("-- New human spawned : " + resident.getId());
		}
		
		Logger.logMain("- Run Human.stepFamily");
		for (final Resident resident: residents) {
			resident.stepFamily();
		}
	}

	/**
	 * Step 2 tick: drain tanks
	 */
	public void step2Tick() {
		
		Logger.logMain("2TICK: drain tanks");
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		Logger.logMain("- Run Human.stepDrainTanks");
		for (final Resident resident: residents) {
			resident.stepDrainTanks();
		}
	}
	
	/**
	 * Step 3 month: teacher removal
	 */
	//@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_MONTH, priority = -2)
	public void step3Month() {
		
		Logger.logMain("3MONTH: teaching, housing and relations");
		
		Logger.logMain("- Run School.removeExcessiveTeachers");
		SimUtils.getSchool().removeExcessiveTeachers(); //TODO put this somewhere that is more appropriate
		Logger.logMain("- Run ElderlyCare.removeExcessiveCaretakers");
		SimUtils.getElderlyCare().removeExcessiveCaretakers();
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		Logger.logMain("- Run Human.stepHousing");
		for (final Resident resident: residents) {
			resident.stepHousing();
		}
		
		Logger.logMain("- Run Human.stepChildrenSchooling");
		for (final Resident resident: residents) {
			resident.stepChildrenSchooling();
		}
		Logger.logMain("- Run Human.stepRelation");
		for (final Resident resident: residents) {
			resident.stepRelation();
		}
		
		Logger.logMain("- Run Human.stepSelectWork");
		for (final Resident resident: residents) {
			resident.stepSelectWork();
		}
	}
	
	/**
	 * Step 4 week: working and social events
	 */
	//@ScheduledMethod(start = 1, interval = 1, priority = -3)
	public void step4Tick() {
		
		Logger.logMain("4TICK: working and social events");
		
		Logger.logMain("- Run EventHall.resetEventHall");
		SimUtils.getEventHall().stepResetEventHall();
		SimUtils.getCouncil().resetCounts();
		
		Logger.logMain("- Ecosystem.stepEcosystem");
		SimUtils.getEcosystem().stepEcosystem();
		
		Logger.logMain("- Boat.stepFish");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.stepFish();
		}
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		Logger.logMain("- Run Human.stepDonate");
		for (final Resident resident: residents) {
			resident.stepDonate();
		}
		
		Logger.logMain("- Run Human.stepSocialEvent");
		for (final Resident resident: residents) {
			resident.stepSocialEvent();
		}

		Logger.logMain("- Run EventHall.stepPerformSocialEvent");
		SimUtils.getEventHall().stepPerformSocialEvent();
	}

	/**
	 * Step 5 month: monthly payments and death
	 */
	//@ScheduledMethod(start = 1, interval = Constants.TICKS_PER_MONTH, priority = -4)
	public void step5Month() {
		Logger.logMain("5MONTH: fishing/processing, montly payments, work selection, migration/death, council");
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		
		Logger.logMain("- Boat.stepSellFish and calculate payment");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.stepSellFish();
		}
	
		Logger.logMain("- Run Boat.stepProcessFish");
		SimUtils.getFactory().stepProcessFish();
		
		Logger.logMain("- Run Human.stepResetStandardCosts");
		for (final Resident resident: residents) {
			resident.stepResetStandardCosts();
		}

		Logger.logMain("- Run Human.stepWork");
		for (final Resident resident: residents) {
			resident.stepWork();
		}
		
		Logger.logMain("- Run Human.stepPayStandardCosts");
		for (final Resident resident: residents) {
			resident.stepPayStandardCosts();
		}
		
		Logger.logMain("- Run Council.stepDistributeMoney");
		SimUtils.getCouncil().stepDistributeMoney();
		
		// Human.stepDeath should be the last one before Human.stepLocation
		Logger.logMain("- Run Human.stepRemove");
		ArrayList<Integer> humanIds = new ArrayList<Integer>();
		for (final Resident resident: residents) {
			humanIds.add(resident.getId());
		}
		// Loop through humanIds
		for (Integer humanId: humanIds) {
			if (HumanUtils.getHumanByIdNoException(humanId) != null) {
				Resident resident = HumanUtils.getResidentById(humanId);
				Logger.logProb("Remove step for H" + resident.getId(), 0.01);
				resident.stepRemove();
			}
			else {
				Logger.logDebug("NO Death step for H" + humanId);
			}
		}
	}
	
	/**
	 * Step 6 tick: movement
	 */
	//@ScheduledMethod(start = 1, interval = 1, priority = -5)
	public void step6Tick() {
	
		Logger.logMain("6TICK: human location");
		
		final ArrayList<Resident> residents = SimUtils.getObjectsAllRandom(Resident.class);
		Logger.logMain("- Run Human.stepLocation");
		for (final Resident resident: residents) {
			resident.stepLocation();
		}

		Logger.logMain("------------------------------------------------------------------------------");
		Logger.logMain("End of this step");
		
		// checks whether to save the population to a file
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		savePopulation(tick);
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
	
	private void initializePopulation() {
		
		boolean initializePopulationFromFile = RepastParam.getPopInitFromFile();
		PopulationBuilder populationBuilder = new PopulationBuilder();
		
		Logger.enableLogger();
		
		if (initializePopulationFromFile) {
			String fileName = RepastParam.getPopInitFileName();
			Logger.logMain("Initialize " + Constants.INITIAL_POPULATION_SIZE + " humans from file : ./output/" + fileName + ".txt");
			populationBuilder.generatePopulation("./output", fileName);
		}
		else
		{
			Logger.logMain("Initialize " + Constants.INITIAL_POPULATION_SIZE + " humans from scratch");
			for (int i = 0; i < Constants.INITIAL_POPULATION_SIZE; ++i) {
	
				// Humans are automatically added to the context and placed in the grid
				Resident resident = new Resident(HumanUtils.getNewHumanId(), SimUtils.getRandomBoolean(), false,
									RandomHelper.nextIntFromTo(Constants.HUMAN_INIT_MIN_AGE, Constants.HUMAN_INIT_MAX_AGE),
						  			RandomHelper.nextIntFromTo(0, Constants.HUMAN_INIT_STARTING_MONEY) );
				Logger.logInfo("Create H" + resident.getId() + ", age: " + resident.getAge());
			}
		}
		
		HumanUtils.printAverageValues();
		// Do a location step
		step6Tick();
		Logger.enableLogger();
	}

	private void savePopulation(int tick) {

		int stopYear = RepastParam.getPopGenTickLimit();
		boolean saveToFile = RepastParam.getPopGenToFile();
		if (saveToFile && (tick == Constants.TICKS_PER_YEAR * stopYear)) {
			String fileName = RepastParam.getPopGenFileName();
			Logger.logMain("------------------------------------------------------------------------------");
			Logger.logMain(stopYear + " years have passed, save population in file: ./output/" + fileName + ".txt");
			PopulationBuilder populationBuilder = new PopulationBuilder();
			populationBuilder.savePopulation("./output", fileName);
			RunEnvironment.getInstance().pauseRun();
			Logger.logMain("------------------------------------------------------------------------------");
		}
	}
}