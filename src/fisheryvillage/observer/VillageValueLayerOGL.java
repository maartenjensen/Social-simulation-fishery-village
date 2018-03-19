package fisheryvillage.observer;

import java.awt.Color;

import fisheryvillage.common.Constants;
import fisheryvillage.property.PropertyColor;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class VillageValueLayerOGL implements ValueLayerStyleOGL {

	private ValueLayer layer = null;

	@Override
	public void init(ValueLayer layer) {
		this.layer = layer;
	}

	@Override
	public float getCellSize() {
		return Constants.GRID_CELL_SIZE;
	}

	@Override
	public Color getColor(double... coordinates) {
		
		final double value = layer.get(coordinates);
		if (value <= 1) { // Grass land
			return new Color(215, (int) Math.min(254 * value, 255), 185);
		}
		else if (value <= 2) { // Sea
			return new Color(180, (int) Math.min(254 * (value - 1), 255), (int) Math.min(254 * (value - 1), 255));
		}
		else if (value <= 3) { // Outside world
			int strength = (int) Math.min(254 * (value - 2), 255);
			return new Color(strength, strength, strength);
		}
		else {
			for (PropertyColor propertyColor : PropertyColor.values()) {
				if (propertyColor.getValueLayerIndex() > value - 0.5 && propertyColor.getValueLayerIndex() < value + 0.5) {
					return propertyColor.getColor();
				}
			}
		}
		return new Color(0, 0, 0);
	}
}