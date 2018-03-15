package fisheryvillage.observer;

import java.awt.Color;
import java.awt.Font;
import fisheryvillage.common.Constants;
import fisheryvillage.property.HomelessCare;
import fisheryvillage.property.House;
import fisheryvillage.property.Property;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class PropertyStyleOGL2D extends DefaultStyleOGL2D {

	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	    	
	    	VSpatial spatialImage = null;
	    	if (agent instanceof House) {
	    		spatialImage = shapeFactory.createRectangle(Constants.GRID_CELL_SIZE, Constants.GRID_CELL_SIZE);//createImageFromPath(Constants.ICON_HOUSE);
	    	}
	    	else if (agent instanceof HomelessCare)	{
	    		spatialImage = shapeFactory.createRectangle(Constants.GRID_CELL_SIZE, Constants.GRID_CELL_SIZE);//createImageFromPath(Constants.ICON_HOMELESS_CARE);
	    	}
	    	if (spatialImage != null) {
	    		return spatialImage;
	    	}
	    	return shapeFactory.createRectangle(Constants.GRID_CELL_SIZE, Constants.GRID_CELL_SIZE);
	    }
	    return spatial;
	}

	@Override
	public Color getColor(final Object agent) {
		
		if (agent instanceof Property) {
			return ((Property) agent).getColor();
		}
		return super.getColor(agent);
	}
	
	/*@Override
	public float getScale(Object object) {
	    return (float) (Constants.GRID_CELL_SIZE / 5.0);
	}*/
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof Property) {
			final Property property = (Property) object;
			return property.getLabel();
		}
		
		return "Warning label not found for object";
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.SOUTH_EAST;
	}
	
	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_SMALL;
	}
	
	/*private VSpatial createImageFromPath(String path) {
		try {
			return shapeFactory.createImage(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}*/
}
