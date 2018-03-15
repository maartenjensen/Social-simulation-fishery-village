package fisheryvillage.observer;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import fisheryvillage.common.Constants;
import fisheryvillage.population.Human;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class HumanStyleOGL2D extends DefaultStyleOGL2D {

	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	    	if (agent instanceof Human) {
	    		Human human = (Human) agent;
	    		ArrayList<VSpatial> spatialImages = new ArrayList<>();
	    		if (!human.getMigrated()) {
	    			String append_gender = "_female.png";
	    			if (human.isMan()) {
	    				append_gender = "_male.png";
	    			}
	    			spatialImages.add(createImageFromPath(Constants.ICON_CHILD + append_gender));
	    			spatialImages.add(shapeFactory.createCircle(5, 16));
	    			spatialImages.add(createImageFromPath(Constants.ICON_TEACHER + append_gender));
	    			spatialImages.add(createImageFromPath(Constants.ICON_PROCESSOR + append_gender));
	    		}
	    		else {
	    			String append_gender = "_female.png";
	    			if (human.isMan()) {
	    				append_gender = "_male.png";
	    			}
	    			spatialImages.add(createImageFromPath(Constants.ICON_CHILD + append_gender));
	    			spatialImages.add(shapeFactory.createCircle(5, 16));
	    			spatialImages.add(createImageFromPath(Constants.ICON_TEACHER + append_gender));
	    			spatialImages.add(createImageFromPath(Constants.ICON_PROCESSOR + append_gender));
	    		}
	    		human.setSpatials(spatialImages);
	    		VSpatial spatialImage = human.getSpatial();
	    		if (spatialImage != null) {
	    			return spatialImage;
	    		}
	    	}
	    	return shapeFactory.createRectangle(6, 6);
	    }
	    if (agent instanceof Human) {
	    	VSpatial spatialImage = ((Human) agent).getSpatial();
    		if (spatialImage != null) {
    			return spatialImage;
    		}
	    }
	    return spatial;
	}
	
	@Override
	public Color getColor(final Object agent) {
		
		if (agent instanceof Human) {
			final Human human = (Human) agent;
			if (human.isMan()) {
				return new Color(0x00, 0x00, 0xFF);
			}
			else {
				return new Color(0xFF, 0x00, 0x00);
			}
		}
		return super.getColor(agent);
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof Human) {
			final Human human = (Human) object;
			return human.getLabel();
		}
		
		return "Warning label not found for object";
	}
	
	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_SMALL;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.NORTH;
	}
	
	private VSpatial createImageFromPath(String path) {
		try {
			return shapeFactory.createImage(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
