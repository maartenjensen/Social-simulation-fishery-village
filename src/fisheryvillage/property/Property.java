package fisheryvillage.property;

import java.awt.Color;

import fisheryvillage.common.Constants;
import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridValueLayer;

public class Property {
	
	// Variable declaration (initialization in constructor)
	private double price;
	private double maintenanceCost;
	private double savings;
	private final GridPoint location;
	private final int width;
	private final int height;
	protected final Status jobStatus;
	
	// Variable initialization
	protected int colorId = 3;
	//private boolean active = false;

	public Property(double price, double maintenanceCost, double savings, GridPoint location, int width, int height, Status jobStatus) {
		this.price = price;
		this.maintenanceCost = maintenanceCost;
		this.savings = savings;
		this.location = location;
		this.width = width; // width as number of cells
		this.height = height; // width as number of cells
		this.jobStatus = jobStatus;
		
		SimUtils.getContext().add(this);
		if (!SimUtils.getGrid().moveTo(this, location.getX(), location.getY())) {
			Logger.logErrorLn("Property could not be placed, coordinate: " + location);
		}
	}

	public void addToValueLayer() {

		GridValueLayer valueLayer = SimUtils.getValueLayer();
		if (valueLayer == null) {
			Logger.logOutputLn("Error valueLayer is null");
			return ;
		}
		for (int i = 0; i < width; i ++) {
			for (int j = 0; j < height; j ++) {
				valueLayer.set(colorId, location.getX() + i, location.getY() + j);
			}
		}
	}

	public double getPrice() {
		return price;
	}
	
	public double getMaintenanceCost() {
		return maintenanceCost;
	}
	
	public double getSavings() {
		return savings;
	}
	
	public void addToSavings(double money) {
		if (money < 0) {
			Logger.logErrorLn("addToSavings() should be positive or 0");
			return ;
		}
		savings += money;
	}
	
	/**
	 * Insert negative amount of money which will be added to savings
	 * @param money
	 */
	public void removeFromSavings(double money) {
		if (money > 0) {
			Logger.logErrorLn("removeFromSavings() should be negative or 0");
			return ;
		}
		savings += money;
	}
	
	public GridPoint getLocation() {
		return location;
	}
	
	public boolean getVacancy() {
		return false;
	}
	
	public Status getJobStatus() {
		return jobStatus;
	}
	
	public Human getOwner() {
		
		Network<Object> propertyNetwork = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
		final Iterable<RepastEdge<Object>> propertyEdges = propertyNetwork.getInEdges(this);
		for (final RepastEdge<Object> propertyEdge : propertyEdges) {
			if (propertyEdge.getSource() instanceof Human) {
				return (Human) propertyEdge.getSource();
			}
		}
		return null;
	}
	
	public boolean getAvailable() {
		if (getOwner() == null) {
			return true;
		}
		return false;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public GridPoint getFreeLocation() {
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (HumanUtils.cellFreeOfHumans(new GridPoint(location.getX() + i, location.getY() + j))) {
					return new GridPoint(location.getX() + i, location.getY() + j);
				}
			}
		}
		return null;
	}
	
	public GridPoint getFreeLocationExcluded(Human humanExcluded) {
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (HumanUtils.cellFreeOfHumansExcluded(new GridPoint(location.getX() + i, location.getY() + j), humanExcluded)) {
					return new GridPoint(location.getX() + i, location.getY() + j);
				}
			}
		}
		return null;
	}
	
	public String getLabel() {
		return "Property";
	}
	
	public Color getColor() {
		return Constants.COLOR_PROPERTY;
	}
	
	public String toString() {
		return String.format("Property ($" + price + "), location %s", SimUtils.getGrid().getLocation(this));
	}
}