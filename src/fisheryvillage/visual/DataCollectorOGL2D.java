package fisheryvillage.visual;

import java.awt.Font;

import fisheryvillage.DataCollector;
import fisheryvillage.common.Constants;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;

/**
* Changes 
*
* @author Maarten Jensen
* @since 2018-03-24
*/
public class DataCollectorOGL2D extends DefaultStyleOGL2D {

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_SMALL;
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof DataCollector) {
			return ((DataCollector) object).getLabel();
		}
		
		return "Warning label not found for object";
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.SOUTH;
	}
}
