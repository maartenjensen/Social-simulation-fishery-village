package fisheryvillage.observer;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

/**
* Changes Edge layout
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class EdgeStyleOGL2D extends DefaultEdgeStyleOGL2D {

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
	    return 0;
	}
}
