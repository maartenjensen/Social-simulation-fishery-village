package fisheryvillage.property;

import java.awt.Color;

/**
* An enum for the property color
*
* @author Maarten Jensen
*/
public enum PropertyColor {
	
	COUNCIL(0, new Color(150, 150, 150)),
	SCHOOL(1, new Color(240, 230, 185)),
	HOUSE(2, new Color(230, 210, 175)),
	HOMELESS_CARE(3, new Color(200, 200, 200)),
	ELDERLY_CARE(4, new Color(230, 230, 230)),
	COMPANY(5, new Color(150, 150, 150)),
	FACTORY(6, new Color(150, 150, 150)),
	BOAT(7, new Color(255, 130, 130));
	
	private final double valueLayerIndex;
	private final Color color;
	
	PropertyColor(double index, Color color) {
		this.valueLayerIndex = index + 5; // + 5, since everything below 5 are value layer colors for non-buildings
		this.color = color;
	}
	
	public double getValueLayerIndex() {
		return valueLayerIndex;
	}
	
	public Color getColor() {
		return color;
	}
}