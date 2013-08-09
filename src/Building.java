import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * A building is built by using a List of Nodes.
 * It has an unwalkable cost and typically target(s) inside of it.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 12-07-2013
 */
public class Building implements MapObject{
	
	private Color color = Color.black;  	 // Color of the building
	private Polygon p;						 // Polygon which encloses the building
	private int cost;						 // Movement cost for 
	private String tag = "";				 // Tag to identify the building with (e.g. education)
	private LinkedList<Node> targetsInside; // Targets that are inside of the building
	
	/**
	 * Initializes a new Building object.
	 */
	public Building(){
		p = new Polygon();
		targetsInside = new LinkedList<Node>();
	}
	
	/**
	 * Mark a node as inside the building.
	 * @param target A target node that is located in the building
	 */
	public void addInsideTarget(Node target){
		targetsInside.add(target);
	}
	
	/**
	 * Adds nodes to the building polygon.
	 * @param nodes Nodes to be added
	 */
	public void addNodes(LinkedList<Node> nodes){
		for(Node node : nodes){
			p.addPoint(node.getXPos(),node.getYPos());
		}
	}
	
	/**
	 * Calculates an approximation of the tiles that are making up the building.
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
		
		//If building is a building used for education - find the center of it and add an EDUCATION node
		if(tag.equals(OSM_Reader.EDUCATION)){
			createEducationCenter(collisionMatrix, (maxX-minX)/2 + minX, (maxY-minY)/2 + minY);
		}
		
		double distance;
		double[] minDistances = new double[targetsInside.size()]; 
		Arrays.fill(minDistances, Double.MAX_VALUE);
		LinkedList<int[]> closestNodes = new LinkedList<int[]>();
		//Initiate vector with edge points (closest to respective target point)
		for(int i = 0; i < targetsInside.size(); i++){
			int[] temp = new int[2];
			closestNodes.add(temp);
		}
		
		//Go through each line of the building
		for(int y = minY; y <= maxY; y++){
			//Are we inside of map bounds (y)
			if(Math.round(OSM_Reader.scaleCollision * y) >= 0 && Math.round(OSM_Reader.scaleCollision * y) < collisionMatrix.length){
				for(int x = minX; x <= maxX; x++){
					//Are we inside of map bounds (x)
					if(Math.round(OSM_Reader.scaleCollision * x) >= 0 && Math.round(OSM_Reader.scaleCollision * x) < collisionMatrix.length){
						//Is this point inside of the building? (and has no previous cost value)
						if(p.contains(x, y) && collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] == 0){
							collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] = cost;
							
							//On the edge of the building
							if(!p.contains(x-1,y) || !p.contains(x+1,y)){
								Node node;
								//Go through all targets that are inside of the building
								for(int i = 0; i < targetsInside.size(); i++){
									node = targetsInside.get(i);
									
									//Calculate the Euclidean distance from target to edge
									distance = Math.sqrt(Math.pow(node.getXPos()-x, 2)+Math.pow(node.getYPos()-y, 2));
									
									//Check if target is closer to this edge point
									if(minDistances[i] > distance){
										minDistances[i] = distance;
										closestNodes.get(i)[0] = x;
										closestNodes.get(i)[1] = y;
									}
								}
							}
						}
					}
				}
			}
		}
		createEntranceWays(collisionMatrix, closestNodes);		
	}
	
	/**
	 * Checks if any target is inside of the building.
	 * @param targets Targets to check.
	 */
	public void checkTargetsInside(ArrayList<LinkedList<Node>> targets){
		//Check which targets that are inside of this building
		for(LinkedList<Node> list : targets){
			for(Node node : list){
				if(p.contains(node.getXPos(), node.getYPos())){
					addInsideTarget(node);
				}
			}
		}
	}
	
	/**
	 * Creates a target node at the center of this building and mark it as a place of education.
	 * @param collisionMatrix The collisionMatrix to check against
	 * @param middleX	Center x value of polygon bounds
	 * @param middleY	Center y value of polygon bounds
	 */
	private void createEducationCenter(int[][] collisionMatrix, int middleX, int middleY){
		Node educationCenter;
		//Check that middle (y) is inside of map bounds
		if(Math.round(OSM_Reader.scaleCollision * middleY) >= 0 && Math.round(OSM_Reader.scaleCollision * middleY) < collisionMatrix.length){
			for(int x = 0; x <= middleX; x++){
				//Are we inside of map bounds (x)
				if(Math.round(OSM_Reader.scaleCollision * middleX+x) >= 0 && Math.round(OSM_Reader.scaleCollision * middleX+x) < collisionMatrix.length){
					//Is this point inside of the building? (and has no previous cost value)
					if(p.contains(middleX+x, middleY) && collisionMatrix[Math.round(OSM_Reader.scaleCollision * middleX+x)][Math.round(OSM_Reader.scaleCollision * middleY)] == 0){
						educationCenter = new Node(middleX+x,middleY);
						educationCenter.setTag(OSM_Reader.EDUCATION);
						OSM_Reader.targets.get(OSM_Reader.TargetEnums.STUDY.ordinal()).add(educationCenter);
						break;
					}
				}
				//Are we inside of bounds (x)
				if(Math.round(OSM_Reader.scaleCollision * middleX-x) >= 0 && Math.round(OSM_Reader.scaleCollision * middleX-x) < collisionMatrix.length){
					//Is this point inside of the building? (and has no previous cost value)
					if(p.contains(middleX-x, middleY) && collisionMatrix[Math.round(OSM_Reader.scaleCollision * middleX-x)][Math.round(OSM_Reader.scaleCollision * middleY)] == 0){
						educationCenter = new Node(middleX-x,middleY);
						educationCenter.setTag(OSM_Reader.EDUCATION);
						OSM_Reader.targets.get(OSM_Reader.TargetEnums.STUDY.ordinal()).add(educationCenter);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Creates an entrance way to targets inside the building.
	 * @param collisionMatrix	The collision matrix to modify
	 * @param closestNodes		List containing the the best entry points for each entrance way
	 */
	private void createEntranceWays(int[][] collisionMatrix, LinkedList<int[]> closestNodes){
		Node startNode;
		int[] target;
		int startX,startY,temp,error,y, stepX, stepY;
		int endX, endY, deltaX, deltaY;
		boolean steep;
		
		//Go through all nodes and approximate which tile that are between them (on a line)
		for(int i = 0; i < targetsInside.size(); i++){
			startNode = targetsInside.get(i);
			target    = closestNodes.get(i);
			
			//Get start pos of line between the two nodes
			startX = startNode.getXPos();
			startY = startNode.getYPos();
			
			//Get end pos of line between the two nodes
			endX = target[0];
			endY = target[1];

			//See if current line between nodes is steep
			steep = Math.abs(startY-endY) > Math.abs(startX-endX);
			
			//Is line steep?
			if(steep){
				//Swap start x,y coordinates
				temp   = startX;
				startX = startY;
				startY = temp;
				
				//Swap end x,y coordinates
				temp = endX;
				endX = endY;
				endY = temp;
			}
			deltaX = Math.abs(startX-endX); //Calculate line's horizontal distance
			deltaY = Math.abs(startY-endY); //Calculate line's vertical distance
			error  = deltaX / 2;			//Initialize error
			
			y = startY;						//Initialize y-tile (row tile) to startY
			
			//Is the endpoint to the right or the left?
			if(startX < endX){
				stepX = 1;
			}
			else{
				stepX = -1;
			}
			
			//Is the endpoint over or under the startpoint?
			if(startY < endY){
				stepY = 1;
			}
			else{
				stepY = -1;
			}
			
			//Calculate tiles in current line
			for(int x = startX; x != (endX+stepX); x += stepX){
				//If steep then we need to remember that we swapped coordinates so that algorithm still can match
				if(steep){						
					//Set tiles inside of the way's width
					for(int width = 1; width < OSM_Reader.FOOTWAY_WIDTH/2; width++){
						if(Math.round(OSM_Reader.scaleCollision * (y-width)) < 0 || Math.round(OSM_Reader.scaleCollision * (y+width)) >= collisionMatrix.length){
							break;
						}
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] = OSM_Reader.FOOTWAY_COST;
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] = OSM_Reader.FOOTWAY_COST;
					}
					if(Math.round(OSM_Reader.scaleCollision * y) >= 0 && Math.round(OSM_Reader.scaleCollision * y) < collisionMatrix.length){
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] = OSM_Reader.FOOTWAY_COST;
					}
				}
				//Not steep
				else{
					//Set tiles inside of the way's width
					for(int width = 1; width < OSM_Reader.FOOTWAY_WIDTH/2; width++){
						if(Math.round(OSM_Reader.scaleCollision * (y-width)) < 0 || Math.round(OSM_Reader.scaleCollision * (y+width)) >= collisionMatrix[0].length){
							break;
						}
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] = OSM_Reader.FOOTWAY_COST;
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] = OSM_Reader.FOOTWAY_COST;
					}
					if(Math.round(OSM_Reader.scaleCollision * x) >= 0 && Math.round(OSM_Reader.scaleCollision * x) < collisionMatrix.length){
						collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] = OSM_Reader.FOOTWAY_COST;
					}
				}
				
				error -= deltaY;
				//Should we go to the next row?
                if (error < 0)
                {
                    y += stepY;
                    error += deltaX;
                }
			}
		}
	}
	
	/**
	 * Returns the color of the building.
	 */
	public Color getColor(){
		return color;
	}
	
	/**
	 * Returns the cost of the building. 
	 */
	public int getCost(){
		return cost;
	}
	
	/**
	 * Returns the polygon that represents the building.
	 * @return The building area polygon
	 */
	public Polygon getPolygon(){
		return p;
	}
	
	/**
	 * Returns the tag for this Building.
	 */
	public String getTag(){
		return tag;
	}
	
	/**
	 * Sets the color of the building to input.
	 * @param newColor New color to use
	 */
	public void setColor(Color newColor){
		color = newColor;
	}
	
	/**
	 * Sets the movement cost of the building to input.
	 * @param newCost New movement cost to use.
	 */
	public void setCost(int newCost){
		cost = newCost;
	}	
	
	/**
	 * Set a tag used to identify this Building with.
	 */
	public void setTag(String newTag){
		tag = newTag;
	}
}