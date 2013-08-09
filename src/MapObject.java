import java.awt.Color;
import java.util.LinkedList;

/**
 * This interface represents a map object.
 * 
 * @author Robert Wideberg
 * @version 12-07-2013
 */
public interface MapObject {

	/** Adds the nodes that represent the object */
	public void addNodes(LinkedList<Node> nodes);
	
	/** Calculates and inserts collision representation of the object into the collision matrix */
	public void calculateCollision(int[][] collisionMatrix);
	
	/** Sets a collision cost for the object */
	public void setCost(int cost);
	
	/** Retrieves the collision cost for the object */
	public int getCost();
	
	/** Sets a color for the object */
	public void setColor(Color color);
	
	/** Retrieves the color of the object */
	public Color getColor();
	
	/** Sets a tag for the object */
	public void setTag(String tag);
	
	/** Sets a tag for the object */
	public String getTag();
}
