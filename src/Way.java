import java.awt.Color;
import java.util.LinkedList;

/**
 * A way is built by using a List of Nodes.
 * It has a color, width and a cost. 
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 12-07-2013
 */
public class Way implements MapObject{
	
	private final int CROSSING_COST = 1;
	
	private LinkedList<Node> nodes;  							//Contains all the nodes that makes up the way
	private int cost  = OSM_Reader.ROAD_DEFAULT_COST;			//Cost that it takes to go over this way
	private int wayWidth = OSM_Reader.ROAD_DEFAULT_WIDTH;		//Width of this way
	private Color color = Color.black;  						//Color of the way
	private String tag = "";
	
	/**
	 * Initializes a new Way object.
	 */
	public Way(){
		nodes = new LinkedList<Node>();
	}
	
	/**
	 * Adds new nodes to the way.
	 * @param nodes Nodes to be added
	 */
	public void addNodes(LinkedList<Node> newNodes){
		nodes.addAll(newNodes);
	}
	
	/**
	 * Calculates an approximation of the tiles that are on the road.
	 * Sets these tiles values to the corresponding collision values.
	 * @param collsionMatrix The collision matrix that is modified
	 */
	public void calculateCollision(int[][] collisionMatrix) {		
		Node startNode;
		Node endNode;
		int startX,startY,temp,error,y, stepX, stepY;
		int endX, endY, deltaX, deltaY;
		boolean steep;
		
		//Go through all nodes and approximate which tile that are between them (on a line)
		for(int i = 0; i < nodes.size()-1 ; i++){

			startNode = nodes.get(i);
			endNode = nodes.get(i+1);
			
			//Get start pos of line between the two nodes
			startX = startNode.getXPos();
			startY = startNode.getYPos();
			
			//Get end pos of line between the two nodes
			endX = endNode.getXPos();
			endY = endNode.getYPos();
			
			//See if current line between nodes is steep
			steep = Math.abs(startY-endY) > Math.abs(startX-endX);
			
			//Is line steep?
			if(steep){
				//Swap start x,y coordinates
				temp = startX;
				startX = startY;
				startY = temp;
				
				//Swap end x,y coordinates
				temp = endX;
				endX = endY;
				endY = temp;
			}
			deltaX = Math.abs(startX-endX); //Calculate line's horizontal distance
			deltaY = Math.abs(startY-endY); //Calculate line's vertical distance
			error = deltaX / 2;				//Initialize error
			
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
					//Out-of-bounds check X (swapped coordinates)
					if(Math.round(OSM_Reader.scaleCollision * x) < 0 || Math.round(OSM_Reader.scaleCollision * x) >= collisionMatrix.length){
						error -= deltaY;
						//Should we go to the next row?
		                if (error < 0)
		                {
		                    y += stepY;
		                    error += deltaX;
		                }
						continue; //Continue to next position (this might be inside of our bounds)
					}
					//Out-of-bounds check Y (swapped coordinates)
					if(Math.round(OSM_Reader.scaleCollision * y) < 0 || Math.round(OSM_Reader.scaleCollision * y) >= collisionMatrix.length){
						error -= deltaY;
						//Should we go to the next row?
		                if (error < 0)
		                {
		                    y += stepY;
		                    error += deltaX;
		                }
						continue;
					}
					//Add collision to matrix
					else{
						//Set tiles inside of the way's width
						for(int width = 1; width <= wayWidth/2; width++){
							//Out-of-bounds check Y (swapped coordinates)
							if((Math.round(OSM_Reader.scaleCollision * y)-width) < 0 || (Math.round(OSM_Reader.scaleCollision * y)+width) >= collisionMatrix.length){
								break;
							}
							else{
								//If near a crossing, use different cost
								if((startNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-startX), 2) + Math.pow(Math.abs(y-startY),2))) <= wayWidth)
								   || (endNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-endX), 2) + Math.pow(Math.abs(y-endY),2))) <= wayWidth))
								{
									collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] = CROSSING_COST;
									collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] = CROSSING_COST;
									if(Math.round(OSM_Reader.scaleCollision * (x+width)) < collisionMatrix.length){
										collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x+width))] = CROSSING_COST;
									}
									if(Math.round(OSM_Reader.scaleCollision * (x-width)) >= 0){
										collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x-width))] = CROSSING_COST;
									}
								}
								//Not a crossing
								else{
									//Make sure that ways only replace cost values for entries that are building costs or lower than this way (so that we avoid writing over e.g. motorway cost with cycle way cost)
									if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y+width))][Math.round(OSM_Reader.scaleCollision * x)] = cost;
									if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * (y-width))][Math.round(OSM_Reader.scaleCollision * x)] = cost;
									
									//Try to round of the bends of the way if possible)
									if(Math.round(OSM_Reader.scaleCollision * (x+width)) < collisionMatrix.length){
										if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x+width))] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x+width))] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x+width))] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x+width))] = cost;
									}
									if(Math.round(OSM_Reader.scaleCollision * (x-width)) >= 0){
										if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x-width))] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x-width))] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x-width))] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * (x-width))] = cost;
									}
								}
							}
						}
						
						//If near a crossing, use different cost
						if((startNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-startX), 2) + Math.pow(Math.abs(y-startY),2))) <= wayWidth)
								   || (endNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-endX), 2) + Math.pow(Math.abs(y-endY),2))) <= wayWidth)){
							collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] = CROSSING_COST;
						}
						else{
							if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] == 0)collisionMatrix[Math.round(OSM_Reader.scaleCollision * y)][Math.round(OSM_Reader.scaleCollision * x)] = cost;
						}
					}
				}
				//Not steep
				else{
					//Out-of-bounds check X 
					if(Math.round(OSM_Reader.scaleCollision * x) < 0 || Math.round(OSM_Reader.scaleCollision * x) >= collisionMatrix.length){
						error -= deltaY;
						//Should we go to the next row?
		                if (error < 0)
		                {
		                    y += stepY;
		                    error += deltaX;
		                }
						continue; //Continue to next position (this might be inside of our bounds)
					}
					//Out-of-bounds check Y
					if(Math.round(OSM_Reader.scaleCollision * y) < 0 || Math.round(OSM_Reader.scaleCollision * y) >= collisionMatrix.length){
						error -= deltaY;
						//Should we go to the next row?
		                if (error < 0)
		                {
		                    y += stepY;
		                    error += deltaX;
		                }
						continue;
					}
					//Add collision to matrix
					else{
						//Set tiles inside of the way's width
						for(int width = 1; width <= wayWidth/2; width++){
							//Out-of-bounds check X 
							if(Math.round(OSM_Reader.scaleCollision * (y-width)) < 0 || Math.round(OSM_Reader.scaleCollision * (y+width)) >= collisionMatrix.length){
								break;
							}
							else{
								//If near a crossing, use different cost
								if((startNode.getTag().equals(OSM_Reader.CROSSING) && 
										   Math.sqrt((Math.pow(Math.abs(x-startX), 2) + Math.pow(Math.abs(y-startY),2))) <= wayWidth)
										   || (endNode.getTag().equals(OSM_Reader.CROSSING) && 
										   Math.sqrt((Math.pow(Math.abs(x-endX), 2) + Math.pow(Math.abs(y-endY),2))) <= wayWidth))
								{
									collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] = CROSSING_COST;
									collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] = CROSSING_COST;
									if(Math.round(OSM_Reader.scaleCollision * (x+width)) < collisionMatrix.length){
										collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x+width))][Math.round(OSM_Reader.scaleCollision * y)] = CROSSING_COST;
									}
									if(Math.round(OSM_Reader.scaleCollision * (x-width)) >= 0){
										collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x-width))][Math.round(OSM_Reader.scaleCollision * y)] = CROSSING_COST;
									}
								}
								//Not a crossing
								else{
									//Make sure that ways only replace cost values for entries that are building costs or lower than this way (so that we avoid writing over e.g. motorway cost with cycle way cost)
									if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y+width))] = cost;
									if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * (y-width))] = cost;
									
									//Try to round of the bends of the way if possible)
									if(Math.round(OSM_Reader.scaleCollision * (x+width)) < collisionMatrix.length){
										if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x+width))][Math.round(OSM_Reader.scaleCollision * y)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x+width))][Math.round(OSM_Reader.scaleCollision * y)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x+width))][Math.round(OSM_Reader.scaleCollision * y)] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x+width))][Math.round(OSM_Reader.scaleCollision * y)] = cost;
									}
									if(Math.round(OSM_Reader.scaleCollision * (x-width)) >= 0){
										if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x-width))][Math.round(OSM_Reader.scaleCollision * y)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x-width))][Math.round(OSM_Reader.scaleCollision * y)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x-width))][Math.round(OSM_Reader.scaleCollision * y)] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * (x-width))][Math.round(OSM_Reader.scaleCollision * y)] = cost;
									}
								}
							}
						}
						
						//If near a crossing, use different cost
						if((startNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-startX), 2) + Math.pow(Math.abs(y-startY),2))) <= wayWidth)
								   || (endNode.getTag().equals(OSM_Reader.CROSSING) && 
								   Math.sqrt((Math.pow(Math.abs(x-endX), 2) + Math.pow(Math.abs(y-endY),2))) <= wayWidth)){
							collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] = CROSSING_COST;
						}
						else{
							if(collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] == OSM_Reader.BUILDING_COST || cost > collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] || collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] == 0) collisionMatrix[Math.round(OSM_Reader.scaleCollision * x)][Math.round(OSM_Reader.scaleCollision * y)] = cost;
						}
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
	 * Returns the color of the way (road).
	 * @return A color
	 */
	public Color getColor(){
		return color;
	}
	
	/**
	 * Returns the cost of the way (road). 
	 */
	public int getCost(){
		return cost;
	}
	
	/**
	 * Returns the tag for this Way
	 */
	public String getTag(){
		return tag;
	}
	
	/**
	 * Gets the collection of Nodes that builds up the way (road).
	 * @return LinkedList<Node> object 
	 */
	public LinkedList<Node> getWayNodes(){
		return nodes;
	}
	
	/**
	 * Returns the width of the way (road).
	 * @return The width of the road
	 */
	public int getWidth(){
		return wayWidth;
	}
	
	/**
	 * Sets the color of the road to input.
	 * @param newColor New color to use
	 */
	public void setColor(Color newColor){
		color = newColor;
	}
	
	/**
	 * Sets the movement cost of the road to input.
	 * @param newCost New movement cost to use.
	 */
	public void setCost(int newCost){
		cost = newCost;
	}
	
	/**
	 * Set a tag for this Way
	 */
	public void setTag(String newTag){
		tag = newTag;
	}
	
	/**
	 * Sets the width of the road to input.
	 * @param newWidth New width to use
	 */
	public void setWidth(int newWidth){
		wayWidth = newWidth;
	}
}