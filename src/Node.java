/**
 * A node contains a x and y value (converted from the .OSM file latitude and longitude).
 * It is used as a point on the screen and can be a component of various things such as roads and areas.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 12-07-2013
 */
public class Node {
	private int x;
	private int y;
	private String tag = "";	//Used to indicate the node type (e.g. shop, crossing)
	
	/**
	 * Create a node with inputs.
	 * @param x X-pos
	 * @param y Y-pos
	 */
	public Node(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the x-position of the node in the collisionMatrix.
	 * @return x-position (in the collision matrix)
	 */
	public int getCollisionXPos(float scaleCollision){
		return Math.round(scaleCollision*x);
	}	
	
	/**
	 * Returns the y-position of the node in the collisionMatrix.
	 * @return y-position (in the collision matrix)
	 */
	public int getCollisionYPos(float scaleCollision){
		return Math.round(scaleCollision*y);
	}
	
	/**
	 * Returns the tag string of the object.
	 */
	public String getTag(){
		return tag;
	}
	
	/**
	 * Returns the x-position of the node (on the screen).
	 * @return x-position (in pixels)
	 */
	public int getXPos(){
		return x;
	}
	
	/**
	 * Returns the y-position of the node (on the screen)
	 * @return y-position (in pixels)
	 */
	public int getYPos(){
		return y;
	}	
	
	/**
	 * Sets the tag value, defines node as special in some way.
	 * @param newTag New tag to set (e.g. crossing)
	 */
	public void setTag(String newTag){
		tag = newTag;
	}
	
	/**
	 * Returns a string representation of the x, y - CollisionCoordinates of the node.
	 */
	public String toStringCollision(float scaleCollision){
		return Math.round(scaleCollision*x) + "," + Math.round(scaleCollision*y);
	}
}
