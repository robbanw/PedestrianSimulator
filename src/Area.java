import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.LinkedList;

/**
 * An area is built by using a List of Nodes.
 * It is similar to ways (except it covers an area).
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 12-07-2013
 */
public class Area implements MapObject{
	
	private Color color = Color.black;  // Color of the area
	private int cost;					 // Move cost of the area 
	private Polygon p;                  // Polygon object
	private String tag = "";            // Used to identify the type of area (e.g. park) 
	
	/**
	 * Initializes a new Way object.
	 */
	public Area(){
		p = new Polygon();
	}
	
	/**
	 * Adds an array of nodes to the area.
	 * @param nodes Nodes to be added
	 */
	public void addNodes(LinkedList<Node> nodes){
		for(Node node : nodes){
			p.addPoint(node.getXPos(),node.getYPos());
		}
	}
	
	/**
	 * Calculates an approximation of the tiles that are making up the area.
	 * Sets these tiles values to the corresponding collision values.
	 * @param collsionMatrix The collision matrix that is modified
	 */
	public void calculateCollision(int[][] collisionMatrix){
		Rectangle rect = p.getBounds();
		int maxX,maxY,minX,minY;
		maxX = (int)rect.getMaxX();
		maxY = (int)rect.getMaxY();
		minX = (int)rect.getMinX();
		minY = (int)rect.getMinY();
		
		//Go through each line of the area
		for(int y = minY; y <= maxY; y++){
			//Are we inside of bounds (y)
			if(Math.round(OSM_Reader.scaleCollision * y) >= 0 && Math.round(OSM_Reader.scaleCollision * y) < collisionMatrix.length){
				for(int x = minX; x <= maxX; x++){
					//Are we inside of bounds (x)
					if(Math.round(OSM_Reader.scaleCollision * x) >= 0 && Math.round(OSM_Reader.scaleCollision * x) < collisionMatrix.length){
						//Is this point inside of the area?
						if(p.contains(x, y)){
							collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] = cost;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the polygon that represents the area.
	 * @return The area polygon
	 */
	public Polygon getArea(){
		return p;
	}
	
	/**
	 * Returns the color of the area.
	 */
	public Color getColor(){
		return color;
	}	
	
	/**
	 * Returns the cost of the area. 
	 */
	public int getCost(){
		return cost;
	}
	
	/**
	 * Sets the movement cost of the area.
	 * @param newCost New movement cost to use.
	 */
	public void setCost(int newCost){
		cost = newCost;
	}
	
	/**
	 * Returns the tag for this Area.
	 */
	public String getTag(){
		return tag;
	}	
		
	/**
	 * Sets the color of the area to input.
	 * @param newColor New color to use
	 */
	public void setColor(Color newColor){
		color = newColor;
	}
	
	/**
	 * Set a tag to identify this Area with (e.g park).
	 */
	public void setTag(String newTag){
		tag = newTag;
	}
}