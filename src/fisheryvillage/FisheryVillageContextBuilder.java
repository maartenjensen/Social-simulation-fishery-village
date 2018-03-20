package fisheryvillage;

import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.municipality.Council;
import fisheryvillage.population.Human;
import fisheryvillage.property.Boat;
import fisheryvillage.property.CompanyOutside;
import fisheryvillage.property.ElderlyCare;
import fisheryvillage.property.Factory;
import fisheryvillage.property.SocialCare;
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

/**
* The FisheryVillageContextBuilder builds the repast simulation
*
* @author Maarten Jensen
*/
public class FisheryVillageContextBuilder implements ContextBuilder<Object> {
	
	@Override
	public Context<Object> build(Context<Object> context) {

		// Reset human id
		HumanUtils.resetHumanId();
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
		
		// Create houses
		Logger.logMain("Create " + Constants.NUMBER_OF_HOUSES + " houses");
		int x = 0, y = 0;
		double housePrice = Constants.HOUSE_BASIC_PRICE;
		double houseMaintenance = Constants.HOUSE_BASIC_MAINTENANCE;
		for (int i = 0; i < Constants.NUMBER_OF_HOUSES; ++i) {
			final GridPoint location = new GridPoint(1 + Constants.GRID_VILLAGE_START + x * 5, 1 + y * 3);
			// Objects of class property are automatically added to the context and the value layer
			new House(housePrice, houseMaintenance, 0, location); //TODO change house prices and stuff

			y++;
			if (y == 10) {
				y = 0;
				x ++;
				if (x == 1) {
					housePrice = Math.round(Constants.HOUSE_BASIC_PRICE * Constants.HOUSE_UPGRADE_1_MULT);
					houseMaintenance = Math.round(Constants.HOUSE_BASIC_MAINTENANCE * Constants.HOUSE_UPGRADE_1_MULT);
				}
				else if (x == 2) {
					housePrice = Math.round(Constants.HOUSE_BASIC_PRICE * Constants.HOUSE_UPGRADE_2_MULT);
					houseMaintenance = Math.round(Constants.HOUSE_BASIC_MAINTENANCE * Constants.HOUSE_UPGRADE_2_MULT);
				}
			}
		}
		
		// Create boats
		new Boat(Constants.BOAT_BASIC_PRICE, Constants.BOAT_BASIC_MAINTENANCE, 0, new GridPoint(Constants.GRID_SEA_START + 3, Constants.GRID_HEIGHT - 8));
		new Boat(Constants.BOAT_BASIC_PRICE, Constants.BOAT_BASIC_MAINTENANCE, 0, new GridPoint(Constants.GRID_SEA_START + 3, Constants.GRID_HEIGHT - 13));

		// Create buildings
		new SocialCare(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 2));
		new ElderlyCare(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 22));
		//new Graveyard(0, 0, new GridPoint(Constants.GRID_VILLAGE_START + 14, 10));
		new School(0, 0, 10000, new GridPoint(Constants.GRID_VILLAGE_START + 18, 12));
		new Factory(0, 0, 10000, new GridPoint(Constants.GRID_SEA_START - 11, 24));
		new Council(0, 0, 10000, new GridPoint(Constants.GRID_SEA_START - 11, 12));
		
		// Create buildings outside village
		new SchoolOutside(0, 0, 0, new GridPoint(1, 12));
		new CompanyOutside(0, 0, 0, new GridPoint(1, 24));
		
		// Create population
		generatePopulation();
		
		return context;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1) // Done before council step
	public void step() {

		Logger.logMain("Before tick");
		Logger.logMain("----- Tick:"+ RunEnvironment.getInstance().getCurrentSchedule().getTickCount() +"-----");
		
		if (Constants.MIGRATION_PROBABILITY > RandomHelper.nextDouble()) {
			Logger.logMain("-- Spawn random agent --");
			new Human(SimUtils.getRandomBoolean(), RandomHelper.nextIntFromTo(10, 40),
					  HumanUtils.getNewHumanId(), 5000, true, false);//TODO constants starting money and age
		}
		
		//final ArrayList<Human> humans = HumanUtils.getHumansAllRandom();	
		final ArrayList<Human> humans = SimUtils.getObjectsAllRandom(Human.class);
				
		Logger.logMain("-- Run Human.stepReset --");
		for (final Human human: humans) {
			human.stepReset();
		}
		
		Logger.logMain("-- Run Human.stepHousing --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepHousing();
			}
		}
		Logger.logMain("-- Run Human.stepFamily --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepFamily();
			}
		}
		
		Logger.logMain("-- Run School.removeExcessiveTeachers --");
		SimUtils.getSchool().removeExcessiveTeachers(); //TODO put this somewhere that is more appropriate
		
		Logger.logMain("-- Run Human.stepChildrenSchooling --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepChildrenSchooling();
			}
		}
		Logger.logMain("-- Run Human.stepRelation --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepRelation();
			}
		}
		
		Logger.logMain("-- Run Boat.stepFish & Boat.stepSellFish --");
		ArrayList<Boat> boats = SimUtils.getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			boat.stepFish();
			boat.stepSellFish();
		}
		
		Logger.logMain("-- Run Boat.stepProcessFish --");
		SimUtils.getFactory().stepProcessFish();
		
		Logger.logMain("-- Run Human.stepWork --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepWork();
			}
		}
		Logger.logMain("-- Run Human.stepPayStandardCosts --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepPayStandardCosts();
			}
		}
		Logger.logMain("-- Run Human.stepSelectWork --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepSelectWork();
			}
		}
		Logger.logMain("-- Run Human.step --");
		for (final Human human: humans) {
			if (!human.isLivingOutOfTown()) {
				human.stepLocation();
			}
		}
		Logger.logMain("-- Run Human.stepDeath --");
		for (final Human human: humans) {
			human.stepDeath();
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void stepCouncil() {
		
		Logger.logMain("-- Run Council.stepDistributeMoney --");
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
					valueLayer.set(RandomHelper.nextDoubleFromTo(2.9, 2.99), i, j);
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
		
		Logger.logMain("Create " + Constants.INITIAL_POPULATION_SIZE + " humans");
		for (int i = 0; i < Constants.INITIAL_POPULATION_SIZE; ++i) {

			// Humans are automatically added to the context and placed in the grid
			new Human(SimUtils.getRandomBoolean(), RandomHelper.nextIntFromTo(Constants.HUMAN_ADULT_AGE, Constants.HUMAN_ELDERLY_AGE),
					  HumanUtils.getNewHumanId(), RandomHelper.nextDoubleFromTo(2000, 6000), false, false); //TODO constants starting money and age			
		}
		
		// Humans
		for (int i = 0; i < 5; i ++) {
			Logger.logMain("--- Run generation step " + i + " ---");
			step();
			stepCouncil();
		}
	}
}
