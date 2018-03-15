package fisheryvillage;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.municipality.Council;
import fisheryvillage.population.Human;
import fisheryvillage.property.CompanyOutside;
import fisheryvillage.property.ElderlyCare;
import fisheryvillage.property.Factory;
import fisheryvillage.property.HomelessCare;
import fisheryvillage.property.House;
import fisheryvillage.property.School;
import fisheryvillage.property.SchoolOutside;
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

public class FisheryVillageContextBuilder implements ContextBuilder<Object> {
	
	@Override
	public Context<Object> build(Context<Object> context) {

		// Reset human id
		HumanUtils.resetHumanId();
		Logger.enableLogger();
		
		// Add context to this ID
		Logger.logOutputLn("Set context ID and add context to context");
		context.setId(Constants.ID_CONTEXT);
		context.add(this); //this can be removed if there is no ScheduledMethod in this contextbuilder
		
		// Create space and grid
		Logger.logOutputLn("Create continuous space and grid");
		@SuppressWarnings("unused")
		final ContinuousSpace<Object> space = createContinuousSpace(context);
		@SuppressWarnings("unused")
		final Grid<Object> grid = createGrid(context);
		
		// Create value layer
		final GridValueLayer valueLayer = createValueLayer();
		context.addValueLayer(valueLayer);
		generateNature(valueLayer);

		// Create networks
		Logger.logOutputLn("Create couple and children network");
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
		
		// Create houses
		Logger.logOutputLn("Create " + Constants.NUMBER_OF_HOUSES + " houses");
		int x = 0, y = 0;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES; ++i) {
			final GridPoint location = new GridPoint(1 + Constants.GRID_VILLAGE_START + x * 5, 1 + y * 3);
			// Objects of class property are automatically added to the context and the value layer
			new House(1000, 500, 0, location); //TODO change house prices and stuff

			y++;
			if (y == 10) {
				y = 0;
				x ++;
			}
		}

		// Create buildings
		new HomelessCare(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 2));
		new ElderlyCare(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 22));
		//new Graveyard(0, 0, new GridPoint(Constants.GRID_VILLAGE_START + 14, 10));
		new School(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 12));
		new Factory(0, 0, 10000, new GridPoint(Constants.GRID_SEA_START - 11, 24));
		new Council(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 28, 2));
		
		// Create buildings outside village
		new SchoolOutside(0, 0, 0, new GridPoint(1, 12));
		new CompanyOutside(0, 0, 0, new GridPoint(1, 24));
		
		// Create population
		generatePopulation();
		
		return context;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1) // Done before council step
	public void step() {

		Logger.logOutputLn("Before tick");
		Logger.logOutputLn("----- Tick:"+ RunEnvironment.getInstance().getCurrentSchedule().getTickCount() +"-----");
		
		if (Constants.MIGRATION_PROBABILITY > RandomHelper.nextDouble()) {
			Logger.logOutputLn("-- Spawn random agent --");
			new Human(SimUtils.getRandomBoolean(), RandomHelper.nextIntFromTo(10, 40),
					  HumanUtils.getNewHumanId(), 5000, true, false);//TODO constants starting money and age
		}
		
		//final ArrayList<Human> humans = HumanUtils.getHumansAllRandom();	
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
				
		Logger.logOutputLn("-- Run Human.stepReset --");
		for (final Human human: humans) {
			human.stepReset();
		}
		
		Logger.logOutputLn("-- Run Human.stepHousing --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepHousing();
			}
		}
		Logger.logOutputLn("-- Run Human.stepFamily --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepFamily();
			}
		}
		
		SimUtils.getSchool().removeExcessiveTeachers(); //TODO put this somewhere that is more appropriate
		
		Logger.logOutputLn("-- Run Human.stepChildrenSchooling --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepChildrenSchooling();
			}
		}
		Logger.logOutputLn("-- Run Human.stepRelation --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepRelation();
			}
		}
		Logger.logOutputLn("-- Run Human.stepWork --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepWork();
			}
		}
		Logger.logOutputLn("-- Run Human.stepPayStandardCosts --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepPayStandardCosts();
			}
		}
		Logger.logOutputLn("-- Run Human.stepSelectWork --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepSelectWork();
			}
		}
		Logger.logOutputLn("-- Run Human.step --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepLocation();
			}
		}
		Logger.logOutputLn("-- Run Human.stepDeath --");
		for (final Human human: humans) {
			human.stepDeath();
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void stepCouncil() {
		
		Logger.logOutputLn("-- Run Council.stepDistributeMoney --");
		SimUtils.getCouncil().stepDistributeMoney();
	}

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
										new SimpleGridAdder<Object>(), true, //TODO check whether multi-occupancy should be allowed
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
				
				if (i < Constants.GRID_VILLAGE_START) {
					valueLayer.set(RandomHelper.nextDoubleFromTo(5.9, 5.99), i, j);
				}
				else if (i < Constants.GRID_SEA_START) {
					valueLayer.set(RandomHelper.nextDoubleFromTo(0.95, 0.99), i, j);
				}
				else {
					valueLayer.set(RandomHelper.nextDoubleFromTo(1.9, 1.99), i, j);
				}
			}
		}
	}
	
	private void generatePopulation() {
		
		Logger.logOutputLn("Create " + Constants.INITIAL_POPULATION_SIZE + " humans");
		for (int i = 0; i < Constants.INITIAL_POPULATION_SIZE; ++i) {

			// Humans are automatically added to the context and placed in the grid
			new Human(SimUtils.getRandomBoolean(), RandomHelper.nextIntFromTo(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE),
					  HumanUtils.getNewHumanId(), RandomHelper.nextDoubleFromTo(2000, 6000), false, false); //TODO constants starting money and age			
		}
		
		// Humans
		for (int i = 0; i < 5; i ++) {
			Logger.logOutputLn("Run generation step " + i);
			step();
			stepCouncil();
		}
	}
}
