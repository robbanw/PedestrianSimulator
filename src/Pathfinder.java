import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Pathfinder is used to find a path between two tiles: a start and target tile.
 * It uses an implementation of A* to achieve this.
 * 
 * @author Robert Wideberg & Christoffer Wiss
 * @version 12-07-2013
 */
public class Pathfinder {
	int[][] collisionMatrix;
	private int moveCost = 5; 	   			//How much should it cost to move one tile (Horizontally/Vertically)
    										//WARNING: Do not set this to a cost that when summed with the smallest tile cost (see OSM_Reader) is negative.
	private final int MOVE_DIAG = Math.round(moveCost*1.4f);	//How much should it cost to move one tile (diagonally)
	private final int hWeight   = Math.round(0.4f*moveCost);	//How much influence the heuristic should have on the pathfinding (for really crappy results set to a high value).
	
	/**
	 * Initializes the Pathfinder with a collisionMatrix to use.
	 * @param collMatrix CollisionMatrix to use for the pathfinding
	 */
	public Pathfinder(int[][] collMatrix){
		collisionMatrix = collMatrix;
	}

    /**
     * Calculates the Heuristic (i.e unknown) movement cost 
     * from start to goal.
     * @param start Start tile
     * @param goal Goal tile
     * @return Estimated movement cost from start to goal
     */
    private int calculateH(int[] start, int[] goal){
        /*MANHATTAN METHOD:
         Sum of vertical and horizontal tiles to goal from current position*/
        return hWeight*(Math.abs(start[0] - goal[0]) + Math.abs(start[1] - goal[1]));
    }
	
    /**
	 * Checks if current path to tile is already on list, 
     * if so we check if the current path is better than the one stored on the list (check gCost value, i.e movementcost from start).
     * If the tile isn't on the list then it is added.
     * @param tile Tile to check     (indices)
     * @param parentTile Parent tile (indices)
     * @param fCost F-Cost (G+H) for current tile
     * @param gCost G-Cost (from start) for current tile
     * @param hCost H-Cost (to goal) for current tile
     * @param tiles Map of tiles to check against   (fast lookup)
     * @param queue Queue of tiles to check against (priority queue, lowest cost is at start)
	 */
    private void checkPathTile(int[] tile, int[] parentTile, int fCost, int gCost, int hCost, HashMap<String,int[]> tiles, PriorityQueue<int[]> queue){
        int[] currentTile = new int[] { tile[0], tile[1], parentTile[0], parentTile[1], fCost, gCost, hCost };
        String position = tile[0] + " " + tile[1];
        
        //Check if this tile already has been added to list
        if(tiles.containsKey(position))
        {
        	int[] oldTile = tiles.get(position);
            //Check if current path has a better gCost; if so update tile
            if (oldTile[5] > gCost)
            {
            	queue.remove(oldTile);
                tiles.put(position, currentTile);
                queue.add(currentTile);
            }
        }
        //Tile was not on list, lets add it!
        else
        {
            tiles.put(position,currentTile);
            queue.add(currentTile);
        }
    }
    
	/**
	 * Pathfinding algorithm for finding the path to the target position.
	 * @param startX X-pos of start tile
	 * @param startY Y-pos of start tile
	 * @param targetX X-pos of target tile
	 * @param targetY Y-pos of target tile
	 * @param shortPathUpdate Is this a short path update or a normal? (short is used while traversing an already found path and checking for better subpaths)
	 * @param currentPath Reference to the list where the path (if found) will appear
	 */
	public boolean findPath(int startX, int startY, int targetX, int targetY, LinkedList<Node> currentPath){
		//Check that we aren't already there (at target)
		if(!(startX == targetX && startY == targetY)){
			//Set boundaries
	        int maxTileX = collisionMatrix.length-1;
	        int maxTileY = collisionMatrix[1].length-1;
	        
	        //Local variables
	        int[] goal = new int[]{targetX, targetY};
	        int[] currentTile;  //Stores x, y, parent tile x, parent tile y, F, G and H cost 
	             				//(F) Movment cost from parent to current tile + Movement cost to goal from current tile
	        int parentCost = 0; //(G) Movment cost from parent to current tile
	              				//(H) Estimated cost from current tile to goal
	        int tileCost = 0;   //    Movement cost for current tile
	        int currentH = 0;   // Calculated H-Value for current tile  
	        int currentG = 0;   // Calculated G-Value for current tile
	        int[] nextTile = new int[2];
	        
	        Comparator<int[]> comparator = new ArrayComparator();										//Java uses this to sort the Priority queue
	        PriorityQueue<int[]> checkTilesQ   = new PriorityQueue<int[]>(maxTileX,comparator); 		//Available tiles to check (used for tiles to check)
	        HashMap<String,int[]> checkTiles   = new HashMap<String,int[]>();                   		//Available tiles to check (used for tiles to re-check)
	        boolean[][] visited = new boolean[collisionMatrix.length][collisionMatrix[0].length];		//Mark where we have been
	        String position = collisionMatrix + " " + startY;
	        
	        //Start at PlayerCharacter tile
	        checkTiles.put(position,new int[]{ startX, startY, startX, startY, calculateH(new int[]{startX, startY}, new int[]{targetX, targetY}), 0, calculateH(new int[]{startX, startY}, new int[]{targetX, targetY})});  
	        checkTilesQ.add(checkTiles.get(position));
	        
	        /*************/
	        /**FIND PATH**/
	        /*************/
	        //Stop when list is empty or goal is reached
	        while (checkTilesQ.size() != 0 && !(visited(visited,goal)))
	        {
	            currentTile = checkTilesQ.poll();  //Get next tile (with lowest totalcost)
	            visited[currentTile[0]][currentTile[1]] = true;
	            parentCost  = currentTile[5];      //Get G cost for current tile (Cost to parent)
			
	            //Check WEST
	            nextTile[0] = (currentTile[0] - 1);
	            nextTile[1] = (currentTile[1]);
	            if(nextTile[0] >= 0){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map and has not been visited yet. Decide next tile
		            if (tileCost != Integer.MAX_VALUE && !(visited(visited,nextTile)))
		            {
		                currentG = (parentCost + moveCost + tileCost);

		                currentH = calculateH(nextTile,goal);
		                checkPathTile(nextTile, new int[] {currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check SOUTH-WEST
	            nextTile[0] = (currentTile[0] - 1);
	            nextTile[1] = (currentTile[1] + 1);
	            if(nextTile[0] >= 0 && nextTile[1] < maxTileY){
	            	tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map, and has not been visited yet and that it is not 
		            //adjacent to walls (so that Pedestrian does not cut through walls). 
		            //Decide next tile
		            if (tileCost != Integer.MAX_VALUE  && !(visited(visited,nextTile)) && 
		            		collisionMatrix[nextTile[0]][nextTile[1] - 1] != Integer.MAX_VALUE &&
		            		collisionMatrix[nextTile[0]+1][nextTile[1]]   != Integer.MAX_VALUE )
		            {
		                currentG = (int)(parentCost + MOVE_DIAG + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] {currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	        	}
	            
	            //Check SOUTH
	            nextTile[0] = currentTile[0];
	            nextTile[1] = (currentTile[1] + 1);
	            if(nextTile[1] < maxTileY){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map and has not been visited yet. Decide next tile
		            if (tileCost != Integer.MAX_VALUE  && !(visited(visited,nextTile)))
		            {
		                currentG = (parentCost + moveCost + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] { currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check SOUTH-EAST
	            nextTile[0] = (currentTile[0] + 1);
	            nextTile[1] = (currentTile[1] + 1);
	            if(nextTile[0] < maxTileX && nextTile[1] < maxTileY){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map, and has not been visited yet and that it is not 
		            //adjacent to walls (so that Pedestrian does not cut through walls). 
		            //Decide next tile
		            if (tileCost != Integer.MAX_VALUE && !(visited(visited,nextTile)) && 
		            		collisionMatrix[nextTile[0]][nextTile[1] - 1] != Integer.MAX_VALUE  &&
		            		collisionMatrix[nextTile[0]-1][nextTile[1]]   != Integer.MAX_VALUE )
		            {
		                currentG = (int)(parentCost + MOVE_DIAG + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] { currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check EAST
	            nextTile[0] = (currentTile[0] + 1);
	            nextTile[1] = (currentTile[1]);
	            if(nextTile[0] < maxTileX){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map and has not been visited yet. Decide next tile
		            if (tileCost != Integer.MAX_VALUE && !(visited(visited,nextTile)))
		            {
		                currentG = (parentCost + moveCost + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] { currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check NORTH-EAST
	            nextTile[0] = (currentTile[0] + 1);
	            nextTile[1] = (currentTile[1] - 1);
	            if(nextTile[0] < maxTileX && nextTile[1] >= 0){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map, and has not been visited yet and that it is not 
		            //adjacent to walls (so that PlayerCharacter does not cut through walls). 
		            //Decide next tile
		            if (tileCost != Integer.MAX_VALUE  && !(visited(visited,nextTile)) && 
		            	collisionMatrix[nextTile[0]][ nextTile[1] + 1] != Integer.MAX_VALUE  &&
		                collisionMatrix[nextTile[0] - 1][nextTile[1]] != Integer.MAX_VALUE )
		            {
		                currentG = (int)(parentCost + MOVE_DIAG + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] { currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check NORTH
	            nextTile[0] = (currentTile[0]);
	            nextTile[1] = (currentTile[1] - 1);
	            if(nextTile[1] >= 0){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map and has not been visited yet. Decide next tile
		            if (tileCost != Integer.MAX_VALUE && !(visited(visited,nextTile)))
		            {
		                currentG = (parentCost + moveCost + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] {currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	            
	            //Check NORTH-WEST
	            nextTile[0] = (currentTile[0] - 1);
	            nextTile[1] = (currentTile[1] - 1);
	            if(nextTile[0] >= 0 && nextTile[1] >= 0){
		            tileCost = collisionMatrix[nextTile[0]][nextTile[1]];
		            //Check that tile is walkable, inside map, and has not been visited yet and that it is not 
		            //adjacent to walls (so that PlayerCharacter does not cut through walls). 
		            //Decide next tile
		            if (tileCost != Integer.MAX_VALUE && !(visited(visited,nextTile)) &&
		            		collisionMatrix[nextTile[0]][nextTile[1] + 1] != Integer.MAX_VALUE &&
		            		collisionMatrix[nextTile[0]+1][nextTile[1]]   != Integer.MAX_VALUE)
		            {
		                currentG = (int)(parentCost + MOVE_DIAG + tileCost);

		                currentH = calculateH(nextTile, goal);
		                checkPathTile(nextTile, new int[] { currentTile[0], currentTile[1] },
		                    (currentG + currentH), currentG, currentH, checkTiles, checkTilesQ);
		            }
	            }
	        }

	        /**********************/
	        /**    STORE PATH    **/
	        /**********************/
	        //ONLY STORE PATH IF ONE COULD BE FOUND
	        if (visited(visited, goal))
	        {
	            nextTile[0] = goal[0];
	            nextTile[1] = goal[1];
	            
	            currentPath.clear();  //Clear queue from previous (might be) unused tiles
	           
	            //Adds path to a queue 
	            while (!(nextTile[0] == startX && nextTile[1] == startY))
	            {
	                currentTile = checkTiles.get(nextTile[0] + " " + nextTile[1]);  //Get next tile
	                currentPath.addFirst(new Node(Math.round(currentTile[0]/OSM_Reader.scaleCollision), Math.round(currentTile[1]/OSM_Reader.scaleCollision))); //Add tile to currentPath
	                
	                //Parent to current tile is the next tile
	                nextTile[0] = currentTile[2];
	                nextTile[1] = currentTile[3];
	            }
	        }
	        else{
	        	return false;
	        }
		}
		return true;
	}

    /**
     * Check if this tile has been visited
     * 
     */
    private boolean visited(boolean[][] visited, int[] searched){
    	boolean found = false;
    	if(visited[searched[0]][searched[1]]){
    		found = true;
    	}
    	return found;
    }
}