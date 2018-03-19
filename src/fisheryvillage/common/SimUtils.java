package fisheryvillage.common;

import java.util.ArrayList;
import java.util.Random;

import fisheryvillage.municipality.Council;
import fisheryvillage.property.Boat;
import fisheryvillage.property.Factory;
import fisheryvillage.property.Property;
import fisheryvillage.property.School;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;

public final strictfp class SimUtils {

	// Initialize variables
	private static final Random random = new Random();
	private static Context<Object> masterContext = null;
	
	public static boolean getRandomBoolean() {
		return random.nextBoolean();
	}

	/**O
	 * Set masterContext
	 */
	public static void setContext(Context<Object> masterContext) {
		SimUtils.masterContext = masterContext;
	}
	
	/**
	 * Gets master context since I don't use the sub-contexts
	 * @return Context the master context
	 */
	public static Context<Object> getContext() {

		if (masterContext == null)  {
			Logger.logErrorLn("SimUtils.getContext(): context returned null");
		}
		return masterContext;
	}
	
	@SuppressWarnings("unchecked")
	public static Grid<Object> getGrid() {
		Grid<Object> grid = (Grid<Object>) getContext().getProjection(Constants.ID_GRID);
		if (grid == null)  {
			Logger.logErrorLn("SimUtils.getGrid(): grid returned null");
		}
		return grid;
	}
	
	@SuppressWarnings("unchecked")
	public static ContinuousSpace<Object> getSpace() {
		ContinuousSpace<Object> space = (ContinuousSpace<Object>) getContext().getProjection(Constants.ID_SPACE);
		if (space == null)  {
			Logger.logErrorLn("SimUtils.getSpace(): space returned null");
		}
		return space;
	}

	@SuppressWarnings("unchecked")
	public static Network<Object> getNetwork(String networkId) {
		
		Network<Object> network = (Network<Object>) getContext().getProjection(networkId);
		if (network == null)  {
			Logger.logErrorLn("SimUtils.getNetwork(): network returned null, ID:" + networkId);
		}
		return network;
	}
	
	public static GridValueLayer getValueLayer() {
		
		GridValueLayer valueLayer = (GridValueLayer) getContext().getValueLayer(Constants.ID_VALUE_LAYER);
		if (valueLayer == null)  {
			Logger.logErrorLn("SimUtils.getValueLayer(): valueLayer returned null");
		}
		return valueLayer;
	}
	
	public static Boat getBoat(int fisherId) {
		ArrayList<Boat> boats = getObjectsAllRandom(Boat.class);
		for (Boat boat : boats) {
			if (boat.employeeOnBoat(fisherId)) {
				return boat;
			}
		}
		return null;
	}
	
	public static School getSchool() {
		return getObjectsAll(School.class).get(0);
	}
	
	public static Council getCouncil() {
		return getObjectsAll(Council.class).get(0);
	}
	
	public static Factory getFactory() {
		return getObjectsAll(Factory.class).get(0);
	}
	
	/**
	 * Retrieves all the objects within the master context based on the given class.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAll(Class<T> clazz) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> objects = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> objectList = new ArrayList<T>();
		for (final T object : objects) {
			objectList.add(object);
		}
		return objectList;
	}
	
	/**
	 * Same as getObjectsAll but uses SimUtilities.shuffle to randomize
	 * the ArrayList of objects
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllRandom(Class<T> clazz) {
		
		ArrayList<T> objectList = getObjectsAll(clazz);
		SimUtilities.shuffle(objectList, RandomHelper.getUniform());
		return objectList;
	}
	
	/**
	 * Retrieves all the objects, excluding the given object, within the master context based
	 * on the given class.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllExcluded(Class<T> clazz, Object excludedObject) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> objects = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> objectList = new ArrayList<T>();
		for (final T object : objects) {
			if (object != excludedObject) {
				objectList.add(object);
			}
		}
		return objectList;
	}
	
	/**
	 * Same as getObjectsAllExcluded but uses SimUtilities.shuffle to randomize
	 * the ArrayList of objects
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllRandomExcluded(Class<T> clazz, Object excludedObject) {
		
		ArrayList<T> objectList = getObjectsAllExcluded(clazz, excludedObject);
		SimUtilities.shuffle(objectList, RandomHelper.getUniform());
		return objectList;
	}
	
	/**
	 * Get all available property
	 * @param <T> class
	 * @return
	 */
	public static <T> ArrayList<T> getPropertyAvailableAll(Class<T> clazz) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> properties = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> propertyList = new ArrayList<T>();
		for (final T property : properties) {
			if (((Property) property).getAvailable()) {
				propertyList.add(property);
			}
		}
		return propertyList;
	}
	
	public static <T> ArrayList<T> getPropertyAvailableAllRandom(Class<T> clazz) {
		
		ArrayList<T> propertyList = getPropertyAvailableAll(clazz);
		SimUtilities.shuffle(propertyList, RandomHelper.getUniform());
		return propertyList;
	}
	
}