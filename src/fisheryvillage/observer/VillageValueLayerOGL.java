package fisheryvillage.observer;

import java.awt.Color;

import fisheryvillage.common.Constants;
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
		Color newColor = new Color(0, 0, 0);
		if (value <= 1) { // Grass land
			newColor = new Color(215, (int) Math.min(254 * value, 255), 185);
		}
		else if (value <= 2) { // Sea
			newColor = new Color(180, (int) Math.min(254 * (value - 1), 255), (int) Math.min(254 * (value - 1), 255));
		}
		else if (value <= 3) { // Property
			newColor = Constants.COLOR_PROPERTY;
		}
		else if (value <= 4) { // House
			newColor = Constants.COLOR_HOUSE;
		}
		else if (value <= 5) { // Homeless care
			newColor = Constants.COLOR_HOMELESS_CARE;
		}
		else if (value <= 6) { // Outside world
			int strength = (int) Math.min(254 * (value - 5), 255);
			newColor = new Color(strength, strength, strength);
		}
		
		return newColor;
	}
}