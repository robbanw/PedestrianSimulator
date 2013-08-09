import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * PedestrianSimulator contains the necessary methods to display, update and simulate an urban environment and its pedestrians.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 09-08-2013
 */
@SuppressWarnings("serial")
public class PedestriansSimulator extends JPanel implements ActionListener {
	public static final int PEDESTRIAN_RADIUS = 4; //Radius of the pedestrian in pixels
	private final int TARGET_SIZE = 8;
	
	public static enum PEDESTRIAN_TYPE{
		STUDENT(Color.BLUE),
		PROFESSOR(Color.CYAN),
		CIVILIAN(Color.MAGENTA);
		private final Color color;
		PEDESTRIAN_TYPE(Color color){
			this.color = color;
		}
		public Color color(){return color;}
	}
	
	//Distribution of pedestrian types, this are not final since education targets might be missing
	private float STUDENTS_AMOUNT   = 0.65f;
	private float PROFESSORS_AMOUNT = 0.20f;
	private float CIVILIANS_AMOUNT  = 0.15f;
	
	private final int NR_OF_PEDESTRIANS = 100;
	public static int PEDESTRIAN_SPEED  = Math.round(10000f/Clock.minuteLength); //Initialize to a speed that maintain this ratio
	
	public static final int CENTRAL_START_HOUR = 8;
	public static final int WIDTH_START_HOUR = 1;
	
	public static final int CENTRAL_END_HOUR = 17;
	public static final int WIDTH_END_HOUR = 1;
	
	public static boolean renderCollision;
	public static boolean renderTargets;
	public static boolean renderCells;
	private boolean collisionDetection = false; // A bit buggy, use at your own risk!

	private final Color SELECTION_COLOR   = Color.GREEN;
	private final Color TARGET_COLOR      = new Color(144,200,130);
	private final Color TARGET_LINE_COLOR = Color.MAGENTA;
	public static Color lowestCollisionColor    = Color.GREEN;
	public static Color highestCollisionColor   = Color.RED;
	
	private int currentMousePosX = 0;
	private int currentMousePosY = 0;
	private boolean mouseLeftClicked = false;
	public static int currentSelectedID = 0;
	public static int currentCursorOverID = 0;
	
	public static final int CELL_SIZE = 25; 			//Size of each cell that map should be divided into
	public static int NR_CELL_ROW;                      //Nr of cells per row (used so that this only needs to be calculated once)
	public static ArrayList<HashSet<Integer>> mapCells;	//Used to keep track of where collisions appear and with which pedestrians
	public static HashMap<Integer,Pedestrian> pedestrians;
	
	private Timer updateTimer; 			     //Used to schedule updates
	private final int updateInterval = 50;   //How many ms that should pass between each update (should depend on minute length)
	private static boolean timeStopped;
	private LinkedList<Way> ways;
	private LinkedList<Building> buildings;
	private LinkedList<Area> areas;
	private ArrayList<LinkedList<Node>> targets;
	private LinkedList<Node> nonTargets;
	private HashMap<String,Image> icons;
	private int[][] collisionMatrix;
	
	//Used for toggling anti-aliasing
	public static RenderingHints renderSpecs;
	public static RenderingHints useAntiAliasing;
	public static RenderingHints noAntiAliasing;
	
	/**
	 * Creates a new simulator, loads the .osm file (path to it given in input) and starts the simulation.
	 * @param path Path to the .osm file containing the map
	 */
    public PedestriansSimulator() {
    	targets = Main.osm_reader.getTargets();
    	if(!mapValid()){
    		JFrame frame = new JFrame();
    		JOptionPane.showMessageDialog(frame,
    			    "The .osm file that you have selected does not contain enough targets!\nSome examples of targets are: restaurants, shops, cafes etc.\n\nPlease select another file or edit the file to add targets.",
    			    "Invalid map",
    			    JOptionPane.ERROR_MESSAGE);
    		this.setVisible(false);
    		Frame.loadNewMapButton.doClick();
    		return;
    	}
    	
    	addKeyListener(new TAdapter());
    	setFocusable(true);
    	setDoubleBuffered(true);

    	NR_CELL_ROW = Frame.SIM_WINDOW_LENGTH/CELL_SIZE;
    	mapCells = new ArrayList<HashSet<Integer>>();
    	pedestrians = new HashMap<Integer,Pedestrian>();
    	collisionMatrix = Main.osm_reader.getCollisionMatrix();   	
    	
    	icons = Main.osm_reader.getIcons();
    	nonTargets = Main.osm_reader.getNonTargets();
    	ways = Main.osm_reader.getWays();
    	buildings = Main.osm_reader.getBuildings();
    	areas = Main.osm_reader.getAreas();

    	//Setup rendering specifications (hints)
    	useAntiAliasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	useAntiAliasing.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    	noAntiAliasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    	noAntiAliasing.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    	
    	renderSpecs = useAntiAliasing; //Anti-aliasing should be on by default
    	renderTargets = false;
    	renderCollision = false;
    	renderCells = false;
    	timeStopped = false;
		
    	addMapCells();
    	initiatePedestrians();
    	addMouseMotionListener(new MAdapter());
    	addMouseListener(new MAdapter());
    	
		updateTimer = new Timer(updateInterval,this);
    	updateTimer.start();
    }

    /**
     * Used as update method (will run each time timer has passed its interval)
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		//Avoid updating pedestrian position if time is stopped
		if(!timeStopped){
			checkNClearMapCells();
			
			Iterator<Pedestrian> itr = pedestrians.values().iterator();
			Pedestrian current = null;
			while(itr.hasNext()){
				current = itr.next();
				current.updatePosition();
				current.updateOccupiedCells();
			}
		}
		repaint();
		if(currentSelectedID != 0)updateNeedsBars();
	}
	
	/**
	 * Initializes the cells that the map should be divided into.
	 * These are then used in order to know where and with what a collision occurred with.
	 */
	private void addMapCells(){
		int nrCells = Frame.SIM_WINDOW_LENGTH/CELL_SIZE * Frame.SIM_WINDOW_LENGTH/CELL_SIZE;
		
		for(int i = 0; i < nrCells; i++){
			mapCells.add(new HashSet<Integer>());
		}
	}
	
    /**
     * Checks if mouse is over a pedestrian, if mouse was clicked or both. Action is taken accordingly.
     * If cursor is over no pedestrian and mouse is clicked, the previously selected pedestrian is deselected.
     * @param checkCursorOver Sets if we should check for if the cursor is over a pedestrian or not.
     * @param p Pedestrian to check against
     * @return True if mouse was over a Pedestrian, i.e. cursor has selection icon, false otherwise.
     */
	private boolean checkCursorPedestrian(Pedestrian p, boolean checkCursorOver) {
		boolean deselect = true;
		
		Rectangle boundingBox = new Rectangle();
		boolean mouseOver = false;
		boolean previousMouseOver = false;
		
		//Check if cursor previously was over a pedestrian
		if(currentCursorOverID != 0){
			boundingBox.x = Math.round(pedestrians.get(currentCursorOverID).getPosX()-PEDESTRIAN_RADIUS);
			boundingBox.y = Math.round(pedestrians.get(currentCursorOverID).getPosY()-PEDESTRIAN_RADIUS);
			boundingBox.height = 2*PEDESTRIAN_RADIUS;
			boundingBox.width  = 2*PEDESTRIAN_RADIUS;
			previousMouseOver  = boundingBox.contains(currentMousePosX,currentMousePosY);
		}
		
		//Check for if mouse is over a Pedestrian
		boundingBox.x = Math.round(p.getPosX()-PEDESTRIAN_RADIUS);
		boundingBox.y = Math.round(p.getPosY()-PEDESTRIAN_RADIUS);
		boundingBox.height = 2*PEDESTRIAN_RADIUS;
		boundingBox.width  = 2*PEDESTRIAN_RADIUS;
		
		//Should we check if the cursor is over anything new?
		if(checkCursorOver){
			//Is cursor over object?
	    	if(boundingBox.contains(currentMousePosX, currentMousePosY)){
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				currentCursorOverID = p.getID();
				mouseOver = true;
	    	}
	    	//Restore cursor if not over an object
	    	else if(!previousMouseOver){
				this.setCursor(Cursor.getDefaultCursor());
				currentCursorOverID = 0;
			}
	    	else{
	    		mouseOver = true;
	    	}
		}
    	
		//Check if over previously cursor over pedestrian (we might have mouse over a new pedestrian)
		if(currentCursorOverID != 0){
			boundingBox.x = Math.round(pedestrians.get(currentCursorOverID).getPosX()-PEDESTRIAN_RADIUS);
			boundingBox.y = Math.round(pedestrians.get(currentCursorOverID).getPosY()-PEDESTRIAN_RADIUS);
			boundingBox.height = 2*PEDESTRIAN_RADIUS;
			boundingBox.width  = 2*PEDESTRIAN_RADIUS;
			previousMouseOver  = boundingBox.contains(currentMousePosX,currentMousePosY);
		}
		
		//Deselect pedestrian if it was previously selected
		if(mouseLeftClicked){
			//Select target (mouse is over the target and target exists)
			if(previousMouseOver){
				currentSelectedID = currentCursorOverID;
				deselect = false;
			}
			//Deselect an old target
			if(deselect){
				deselectPedestrian();
			}
			mouseLeftClicked = false; //Reset so that we do not detect multiple clicks.
		}
		
		return (mouseOver||!checkCursorOver);
	}
	
	/**
	 * Checks if a collision has occurred between pedestrians inside of cell, 
	 * if so then a responsive action is issued.
	 */
	private void checkCollisionInCell(int cell){
		Integer p1,p2;
		int p1X,p1Y,p2X,p2Y,interLength;
		Integer cellPedestrians[] = mapCells.get(cell).toArray(new Integer[0]); //I have my suspicion that this line is taking its toll on the performance
		//Select a pedestrian in cell
		for(int i = 0; i < cellPedestrians.length; i++){
			p1  = cellPedestrians[i];
			p1X = pedestrians.get(p1).getPosX();
			p1Y = pedestrians.get(p1).getPosY();
			//and check it against another in cell
			for(int j = (i+1); j < cellPedestrians.length; j++){
				p2  = cellPedestrians[j];
				p2X = pedestrians.get(p2).getPosX();
				p2Y = pedestrians.get(p2).getPosY();
				interLength = circleCollisionLength(p1X,p1Y,p2X,p2Y,PEDESTRIAN_RADIUS,PEDESTRIAN_RADIUS);
				
				if(interLength > 0){
					pedestrians.get(p1).avoidCollision(p2X, p2Y, 1, interLength);
				}
			}
		}
	}
	
	/**
	 * Checks if two circles are colliding with each other.
	 * @param p1X X-Center of circle 1 (pixels)
	 * @param p1Y Y-Center of circle 1 (pixels)
	 * @param p2X X-Center of circle 1 (pixels)
	 * @param p2Y Y-Center of circle 2 (pixels)
	 * @param p1R Radius of circle 1 (pixels)
	 * @param p2R Radius of circle 2 (pixels)
	 * @return Intersection length if intersection occurred, otherwise 0.
	 */
	private int circleCollisionLength(int p1X, int p1Y, int p2X, int p2Y, int p1R, int p2R){
		int distX = p2X - p1X;
		int distY = p2Y - p1Y;
		//Use squared Pythogaras (that way we avoid those pesky square-roots)
		int length = distX*distX + distY*distY;
		if(length > (p1R+p2R)*(p1R+p2R))
		{
			length = 0;
		}
		return (int) Math.round(Math.sqrt(length));
	}
	
	/**
	 * Checks for collisions in each cell and then clears it from previous objects.
	 */
	private void checkNClearMapCells()
	{
		for(int i = 0; i < mapCells.size(); i++)
		{
			if(collisionDetection && mapCells.get(i).size() > 1){
				checkCollisionInCell(i);
			}
			mapCells.get(i).clear();
		}
	}
	
	/**
     * Decreases the clock and simulation speed twofold.
     */
    public static void decreaseSpeed(){
    	if(!timeStopped){
    		Frame.clock.decrementSpeed();
    		PEDESTRIAN_SPEED = Math.round(10000f/Clock.minuteLength);
    	}
    }
	
	/**
     * Deselects the currently selected pedestrian and resets the needs progress bars.
     */
    public void deselectPedestrian(){
    	currentSelectedID = 0;
    	currentCursorOverID = 0;
    	Frame.hungerNeedBar.updateNeedLevel(0);
    	Frame.cafeNeedBar.updateNeedLevel(0);
    	Frame.toiletNeedBar.updateNeedLevel(0);
    	Frame.shopNeedBar.updateNeedLevel(0);
    	Frame.bankNeedBar.updateNeedLevel(0);
    	Frame.wasteNeedBar.updateNeedLevel(0);
    }
    
    /**
     * Increases the clock and simulation speed twofold.
     */
    public static void increaseSpeed(){
    	if(!timeStopped){
			Frame.clock.incrementSpeed();
			PEDESTRIAN_SPEED = Math.round(10000f/Clock.minuteLength);
    	}
    }
	
    /**
     * Initiates all Pedestrians and assigns them with a waking time (i.e time when they should start to move) 
     * that correlates to the Gaussian Distribution. 
     */
    private void initiatePedestrians(){
    	long before = 0,after = 0;
    	int currentTypeIndex = 0;
    	boolean startAtPublicTrans = true;
    	PEDESTRIAN_TYPE currentPedestrianType = null;
    	Node start = null;
		Pedestrian pedestrian = null;
    	Random rand = new Random();
    	rand.nextGaussian();
    	int[] nrOfType = new int[PEDESTRIAN_TYPE.values().length];
    	if(targets.get(OSM_Reader.TargetEnums.STUDY.ordinal()).isEmpty()){
    		STUDENTS_AMOUNT = 0;
    		PROFESSORS_AMOUNT = 0;
    		CIVILIANS_AMOUNT = 1;
    	}
    	nrOfType[PEDESTRIAN_TYPE.STUDENT.ordinal()]   = Math.round(NR_OF_PEDESTRIANS*STUDENTS_AMOUNT);
    	nrOfType[PEDESTRIAN_TYPE.PROFESSOR.ordinal()] = Math.round(NR_OF_PEDESTRIANS*PROFESSORS_AMOUNT);
    	nrOfType[PEDESTRIAN_TYPE.CIVILIAN.ordinal()]  = Math.round(NR_OF_PEDESTRIANS*CIVILIANS_AMOUNT);
    	
    	if(OSM_Reader.targets.get((OSM_Reader.TargetEnums.PUBLIC_TRANSPORT.ordinal())).isEmpty()){
    		startAtPublicTrans = false;
    	}
		//Place the pedestrians at a normally distributed time
		for(int i = 1; i <= NR_OF_PEDESTRIANS; i++){
			//Check if we still have pedestrians left for this type
			if(nrOfType[currentTypeIndex] > 0){
				currentPedestrianType = PEDESTRIAN_TYPE.values()[currentTypeIndex];
				nrOfType[currentTypeIndex]--;
			}
			//Go to the next type
			else if (currentTypeIndex < nrOfType.length){
				currentTypeIndex++;
				i--;
				continue;
			}					
			
			if(Frame.DEBUG)before = System.currentTimeMillis();
			
			//Primarily try to get starting position from some sort of public transport such as subway etc.
			if(startAtPublicTrans) {
				start = OSM_Reader.targets.get((OSM_Reader.TargetEnums.PUBLIC_TRANSPORT.ordinal())).get(rand.nextInt(OSM_Reader.targets.get(OSM_Reader.TargetEnums.PUBLIC_TRANSPORT.ordinal()).size()));
			}
			//We didn't have any public transport targets let us start somewhere else
			else{
				for(LinkedList<Node> subTargetList : targets){
					if(!subTargetList.isEmpty()){
						start = subTargetList.get(rand.nextInt(subTargetList.size()));
					}
				}
			}
			pedestrian = new Pedestrian(start.getXPos(), start.getYPos(), collisionMatrix, currentPedestrianType, i);
			if(Frame.DEBUG)after = System.currentTimeMillis();
    		if(Frame.DEBUG)System.out.println("PEDESTRIAN PATH CALC TOOK:" + (after-before));
    		pedestrians.put(i,pedestrian);
    	}
    }
    
    /**
     * Checks if the given map has enough targets
     * @return true if this map is useable by the simulator.
     */
    private boolean mapValid(){
    	int nrOfTargets = 0;
    	for(LinkedList<Node> subTargetList : targets){
    		nrOfTargets += subTargetList.size();
    	}
    	if(nrOfTargets < 2){
    		return false;
    	}
    	return true;
    }
    
    /**
     * Draws all components that the simulator uses.
     */
    @Override
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
    	Graphics2D graphics = (Graphics2D) g;
    	
        graphics.setRenderingHints(renderSpecs);  	
    	
    	renderAreas(graphics);
    	renderWays(graphics);
    	renderCrossings(graphics);
    	if(renderCells)renderCells(graphics);
    	renderPedestrians(graphics);
    	renderBuildings(graphics);
    	renderIcons(graphics);
    	if(renderCollision)renderCollision(graphics);
    	if(renderTargets)renderTargets(graphics);
    	if(currentSelectedID != 0)renderSelectionBox(graphics);
    	
    	g.dispose(); //Release resources allocated to this graphics object
    }
    
    /**
     * Draws all the areas.
     * @param graphics Graphics2D object to draw to
     */
    private void renderAreas(Graphics2D graphics){
    	for(Area area : areas){
    		graphics.setColor(area.getColor());
    		graphics.fill(area.getArea());
    	}
    }
    
    /**
     * Draws all the buildings.
     * @param graphics Graphics2D object to draw to
     */
    private void renderBuildings(Graphics2D graphics){
    	for(Building b : buildings){
        	graphics.setColor(b.getColor());
    		graphics.fill(b.getPolygon());
    	}
    }
    
    /**
     * Renders the cells of the map with a color representing amount of pedestrians in cell.
     * @param graphics Graphics2D object to draw to
     */
    private void renderCells(Graphics2D graphics){
    	//Render background (grid)
    	graphics.setColor(Color.WHITE);
    	graphics.fillRect(0, 0, Frame.SIM_WINDOW_LENGTH, Frame.SIM_WINDOW_LENGTH);
    	for(int i = 0; i < mapCells.size(); i++){
    		float dist = mapCells.get(i).size()/10.f;
    		
    		//Render cell
    		if(dist > 1) dist = 1.f;
    		graphics.setColor(Frame.colorLinearInterpolation(lowestCollisionColor, highestCollisionColor, dist));
    		graphics.fillRect((i%NR_CELL_ROW)*CELL_SIZE,(i/NR_CELL_ROW)*CELL_SIZE, CELL_SIZE-2, CELL_SIZE-2);
    	}
    }

    /**
     * Renders the collision (currently specified collision).
     * @param graphics Graphics2D object to draw to
     */
    private void renderCollision(Graphics2D graphics){
    	float colorDistance;
    	Color renderColor;
    	graphics.setStroke(new BasicStroke(1));
    	for(int i = 0; i < OSM_Reader.COLLISION_ROWS; i++){
    		for(int j = 0; j < OSM_Reader.COLLISION_COLUMNS; j++){
    			//Color unwalkable areas
				if(collisionMatrix[i][j] == Integer.MAX_VALUE){
					renderColor = Color.BLACK;
				}
				//Let all pavement and such use a special color
				else if(collisionMatrix[i][j] == 0)
				{
					renderColor = Color.WHITE;
				}
				else if(collisionMatrix[i][j] <= -5){
					renderColor = Color.YELLOW;
					System.out.println("ERROR AT " + i + " " + j);
				}
				//Color interpolate a suitable color
				else{
					colorDistance = (collisionMatrix[i][j] + Math.abs(OSM_Reader.COLLISION_COST_MIN))/(float)OSM_Reader.COLLISION_COST_MAX;
					
					//Do not allow distance values over 1
					if(colorDistance > 1){
						colorDistance = 1;
					}
					renderColor = Frame.colorLinearInterpolation(lowestCollisionColor, highestCollisionColor, colorDistance);
				}
				graphics.setColor(renderColor);
				graphics.fillRect(Math.round(i/OSM_Reader.scaleCollision), Math.round(j/OSM_Reader.scaleCollision), Math.round(1/OSM_Reader.scaleCollision), Math.round(1/OSM_Reader.scaleCollision));
    		}
    	}
    }
    
    /**
     * Renders icons for all available targets on the map.
     * @param graphics
     */
    private void renderCrossings(Graphics2D graphics){
    	Image currentIcon;       
    	for(Node node : nonTargets){
    		currentIcon = icons.get(node.getTag());
    		graphics.drawImage(currentIcon,Math.round(node.getXPos()-currentIcon.getWidth(null)/2),Math.round(node.getYPos() - currentIcon.getHeight(null)/2),this);
    	}
    }
    
    /**
     * Renders icons for all available targets on the map.
     * @param graphics
     */
    private void renderIcons(Graphics2D graphics){
    	Image currentIcon;
    	for(LinkedList<Node> list : targets){
	    	for(Node node : list){
	    		currentIcon = icons.get(node.getTag());
	    		graphics.drawImage(currentIcon,Math.round(node.getXPos()-currentIcon.getWidth(null)/2),Math.round(node.getYPos() - currentIcon.getHeight(null)/2),this);
	    	}
    	}
    }
    
    /**
     * Renders the Pedestrians.
     * @param graphics Graphics2D object to draw to
     */
    private void renderPedestrians(Graphics2D graphics){
		Iterator<Pedestrian> itr = pedestrians.values().iterator();
		Pedestrian p;
		boolean mouseOverPedestrian = false;
		while(itr.hasNext()){
			p = itr.next();
	    	graphics.setColor(p.getColor());
	    	graphics.fillOval(Math.round(p.getPosX()-PEDESTRIAN_RADIUS), Math.round(p.getPosY()-PEDESTRIAN_RADIUS), 2*PEDESTRIAN_RADIUS, 2*PEDESTRIAN_RADIUS);
	    	mouseOverPedestrian = checkCursorPedestrian(p, !mouseOverPedestrian);
    	}
    }
    
    /**
     * Draws a selection rectangle around the currently selected pedestrian.
     * @param graphics
     */
    private void renderSelectionBox(Graphics2D graphics){
    	graphics.setColor(SELECTION_COLOR);
    	graphics.setStroke(new BasicStroke(2));
    	graphics.drawRoundRect(Math.round(pedestrians.get(currentSelectedID).getPosX()-2*PEDESTRIAN_RADIUS), Math.round(pedestrians.get(currentSelectedID).getPosY()-2*PEDESTRIAN_RADIUS), 4*PEDESTRIAN_RADIUS, 4*PEDESTRIAN_RADIUS, 2, 2);
    }
    
    /**
     * Renders the target and target lines (i.e. lines from Pedestrian to target) 
     * for each Pedestrian.
     * @param graphics Graphics2D object to draw to
     */
    private void renderTargets(Graphics2D graphics){
    	Iterator<Pedestrian> itr = pedestrians.values().iterator();
		Pedestrian p;
		while(itr.hasNext()){
			p = itr.next();
			graphics.setColor(TARGET_COLOR);
			graphics.fillRect(Math.round(p.getTargetPosX()-TARGET_SIZE/2), Math.round(p.getTargetPosY()-TARGET_SIZE/2), TARGET_SIZE, TARGET_SIZE);
	    	graphics.setStroke(new BasicStroke(2));
			graphics.setColor(TARGET_LINE_COLOR);
			graphics.drawLine(p.getPosX(), p.getPosY(), p.getTargetPosX(), p.getTargetPosY());
		}
    }
    
    /**
     * Draws all the ways (roads).
     * @param graphics Graphics2D object to draw to
     */
    private void renderWays(Graphics2D graphics){
    	LinkedList<Node> wayNodes;
    	Line2D currentLine; 
    	
    	//Go through all ways and draw them
    	for(Way way : ways){
			wayNodes = way.getWayNodes();
			currentLine = new Line2D.Float(0,0,0,0);
			graphics.setColor(way.getColor());
			graphics.setStroke(new BasicStroke(way.getWidth(),BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			
			// Draw the way
			for (int i = 0; i < (wayNodes.size()-1); i++){
				currentLine.setLine(wayNodes.get(i).getXPos(), wayNodes.get(i).getYPos(), wayNodes.get(i+1).getXPos(), wayNodes.get(i+1).getYPos());
				graphics.draw(currentLine);
			}
    	}
    }
    
    /**
     * Sets if anti-aliasing should be used or not (i.e. smoother edges of rendered objects).
     * @param value If true then anti-aliasing is used, otherwise it is not.
     */
    public static void setAntialiasing(boolean value){
    	if(value)
    	{
    		renderSpecs = useAntiAliasing;
    	}
    	else
    	{
    		renderSpecs = noAntiAliasing;
    	}
    }
    
    /**
     * Stops the draw update timer
     */
    public void stopTimer(){
    	updateTimer.stop();
    }
    
    /**
     * Toggles the run status of the simulation
     */
    public static void toggleRunStatus(){
		if(timeStopped){
			Frame.clock.startTime();
			Frame.playButton.setText(Frame.pauseSymbol);
			timeStopped = false;
		}
		else{
			Frame.clock.stopTime();
			Frame.playButton.setText(Frame.forwardArrow);
			timeStopped = true;
		}
    }
    
    /**
     * Updates the progress bars for the different needs of the currently selected pedestrian.
     */
    private void updateNeedsBars(){
    	Frame.hungerNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getHungerNeed());
    	Frame.cafeNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getCafeNeed());
    	Frame.toiletNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getToiletNeed());
    	Frame.shopNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getShopNeed());
    	Frame.bankNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getBankNeed());
    	Frame.wasteNeedBar.updateNeedLevel(pedestrians.get(currentSelectedID).getWasteNeed());
    }
    
    /**
     * Mouse adapter class for the simulation
     */
    private class MAdapter extends MouseAdapter{
    	
    	/**
    	 * If mouse was moved, update our internal mouse value.
    	 */
    	public void mouseMoved(MouseEvent event) {
    		currentMousePosX = event.getX();
    		currentMousePosY = event.getY();
        }
    	
    	/**
    	 * Signal that a mouse button was released.
    	 */
    	public void mouseReleased(MouseEvent event){
    		currentMousePosX = event.getX();
    		currentMousePosY = event.getY();
    		if(event.getButton() == MouseEvent.BUTTON1){
    			mouseLeftClicked = true;
    		}
    	}
    }
    
    /**
     * Keyboard adapter for the simulation
     */
    private class TAdapter extends KeyAdapter {
    	private boolean prevKeyPressed  = false;
    	private boolean renderAntialiasing = false;
    	

    	public void keyPressed(KeyEvent e){
    		//Only do one action per key press
    		if(!prevKeyPressed){
    			//Only allow speed changes when time is running 
    			if(!timeStopped){
	    			//Increases clock speed twofold
	    			if(e.getKeyCode() == KeyEvent.VK_RIGHT){
	    				increaseSpeed();
	    			}
	    			
	    			//Decreases clock speed twofold
	    			if(e.getKeyCode() == KeyEvent.VK_LEFT){
	    				decreaseSpeed();
	    			}
	    			
	    			//Set clock speed to the lowest speed (1x)
	    			if(e.getKeyCode() == KeyEvent.VK_DOWN){
	    				Frame.clock.setToMinSpeed();
	    				PEDESTRIAN_SPEED = Math.round(10000f/Clock.minuteLength);
	    			}
	    			
	    			//Set clock speed to the highest speed
	    			if(e.getKeyCode() == KeyEvent.VK_UP){
	    				Frame.clock.setToMaxSpeed();
	    				PEDESTRIAN_SPEED = Math.round(10000f/Clock.minuteLength);
	    			}
    			}
    			
    			//Stops/starts the time for the simulation
    			if(e.getKeyCode() == KeyEvent.VK_SPACE){
    				toggleRunStatus();
    			}
    			
	        	//If C is pressed, render collisions
	        	if(e.getKeyCode() == KeyEvent.VK_C){
	        		if(renderCollision){
	        			renderCollision = false;
	        			Frame.filterList.setSelectedItem(Frame.FILTER_NORMAL);
	        		}
	        		else{
	        			renderCollision = true;
	        			Frame.filterList.setSelectedItem(Frame.FILTER_COST);
	        		}
	        	}
	        	
	        	//If D is pressed, render cells
	        	if(e.getKeyCode() == KeyEvent.VK_D){
	        		if(renderCells){
	        			renderCells = false;
	        			Frame.filterList.setSelectedItem(Frame.FILTER_NORMAL);
	        		}
	        		else{
	        			renderCells = true;
	        			Frame.filterList.setSelectedItem(Frame.FILTER_CELL);
	        		}
	        	}
	        	
	        	//If T is pressed, render targets
	        	if(e.getKeyCode() == KeyEvent.VK_T){
	        		if(renderTargets){
	        			renderTargets = false;
	        			Frame.renderTargetNoBtn.setSelected(true);
	        		}
	        		else{
	        			renderTargets = true;
	        			Frame.renderTargetYesBtn.setSelected(true);
	        		}
	        	}
	        	//If Q is pressed, toggle anti-aliasing
	        	if(e.getKeyCode() == KeyEvent.VK_Q){
	        		if(renderAntialiasing){
	        			renderAntialiasing = false;
	        			renderSpecs = useAntiAliasing; //Turn on anti-aliasing again
	        			Frame.renderAntialiasingYesBtn.setSelected(true);
	        		}
	        		else{
	        			renderAntialiasing = true;
	        			renderSpecs = noAntiAliasing; //Turn off anti-aliasing to improve speed when rendering collision
	        			Frame.renderAntialiasingNoBtn.setSelected(true);
	        		}
	        	}
	        	prevKeyPressed = true;
    		}
    	}
        public void keyReleased(KeyEvent e) {
        	prevKeyPressed = false;
        }
    }
}