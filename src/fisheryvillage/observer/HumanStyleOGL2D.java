package fisheryvillage.observer;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;

import fisheryvillage.common.Constants;
import fisheryvillage.common.Logger;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class HumanStyleOGL2D extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		
	    if (agent instanceof Human) {
    		Human human = (Human) agent;
    		VSpatial newSpatial = human.getSpatialImage();

    		if (newSpatial != null) {
    			return newSpatial;
    		}
    		else {
    			loadSpatialImages(human);
    			newSpatial = human.getSpatialImage();
    			if (newSpatial != null) {
        			return newSpatial;
        		}
    			else {
    				Logger.logErrorLn("Getting spatial image for human" + human.getId() + " went wrong.");
    				return shapeFactory.createRectangle(6, 6);
    			}
    		}
	    }
	    Logger.logErrorLn("Getting spatial went wrong since it is no human.");
	    return shapeFactory.createRectangle(6, 6);
	}
	
	public void loadSpatialImages(Human human) {
		
		String append_gender = "_male.png";
		if (!human.isMan()) {
			append_gender = "_female.png";
		}
		HashMap<Status, VSpatial> spatialImages = new HashMap<Status, VSpatial>();
		spatialImages.put(Status.CHILD, createImageFromPath(Constants.ICON_CHILD + append_gender));
		spatialImages.put(Status.ELDERLY_CARETAKER, createImageFromPath(Constants.ICON_CARETAKER + append_gender));
		spatialImages.put(Status.ELDER, createImageFromPath(Constants.ICON_ELDER + append_gender));
		spatialImages.put(Status.ELDEST, createImageFromPath(Constants.ICON_ELDEST + append_gender));
		spatialImages.put(Status.FISHER, createImageFromPath(Constants.ICON_FISHER + append_gender));
		spatialImages.put(Status.FACTORY_WORKER, createImageFromPath(Constants.ICON_PROCESSOR + append_gender));
		spatialImages.put(Status.TEACHER, createImageFromPath(Constants.ICON_TEACHER + append_gender));
		spatialImages.put(Status.UNEMPLOYED, createImageFromPath(Constants.ICON_UNEMPLOYED + append_gender));
		spatialImages.put(Status.WORK_OUT_OF_TOWN, createImageFromPath(Constants.ICON_WORKER_OUTSIDE + append_gender));
		human.setSpatialImages(spatialImages);
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
