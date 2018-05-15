package fisheryvillage.visual;

import java.awt.Color;
import java.awt.Font;

import fisheryvillage.common.Constants;
import fisheryvillage.ecosystem.Ecosystem;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;

/**
* Changes ecosystem visual layout, mainly important for label
*
* @author Maarten Jensen
* @since 2018-03-24
*/
public class EcosystemStyleOGL2D extends DefaultStyleOGL2D {

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_SMALL;
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof Ecosystem) {
			final Ecosystem ecosystem = (Ecosystem) object;
			return ecosystem.getLabel();
		}
		
		return "Warning label not found for object";
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.SOUTH;
	}
	
	@Override
	public Color getColor(final Object agent) {
		
		if (agent instanceof Ecosystem) {
			final Ecosystem ecosystem = (Ecosystem) agent;
			if (ecosystem.fishInDanger()) {
				return new Color(0xFF, 0x00, 0x00);
			}
			else {
				return new Color(0x00, 0xFF, 0x00);
			}
		}
		return super.getColor(agent);
	}
}
