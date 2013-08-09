import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * The OSM_Reader reads an input .osm file (XML format) and creates appropriate 
 * data structures and objects for the different items described in the file.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 09-08-2013
 */
public class OSM_Reader extends SwingWorker<Void, Void> {
	private HashMap<String,Node> nodeMap;
	private HashMap<String,Image> iconMap;
	private LinkedList<Way> ways;
	private LinkedList<Building> buildings;
	private LinkedList<Area> areas;
	
	public enum TargetEnums {RESTAURANT_I,CAFE_I,SHOP_I,TOILET_I,FASTFOOD_I,WASTE_I,BANK_I,MISC,PUBLIC_TRANSPORT,HEALTH,STUDY};
	
	public static ArrayList<LinkedList<Node>> targets;
	private LinkedList<Node> nonTargetNodes;
	
	public static final String ICON_FILE_PATH = "data/icons/";
	
	private final String BOUNDS = "bounds";
	private final String MINLAT = "minlat";
	private final String MINLON = "minlon";
	private final String MAXLAT = "maxlat";
	private final String MAXLON = "maxlon";
	private final String NODE = "node";
	private final String LON = "lon";
	private final String LAT = "lat";
	private final String ID = "id";
	private final String ND = "nd";
	private final String REF = "ref";
	
	private final String ATM = "atm";
	private final String BANK = "bank";
	private final String LIBRARY = "library";
	private final String DOCTORS = "doctors";
	private final String HOSPITAL = "hospital";
	private final String PHARMACY = "pharmacy";
	private final String RESTAURANT = "restaurant";
	private final String CAFE = "cafe";
	private final String WASTEBIN = "waste_basket";
	private final String TOILETS = "toilets";
	private final String FAST_FOOD = "fast_food";
	private final String STATION = "station";
	private final String BUS_STATION = "bus_station";
	public static final String BUS_STOP = "bus_stop";
	private final String SUBWAY_ENTRANCE = "subway_entrance";
	private final String SHOP = "shop";
	private final String SUBWAY = "subway";
	public static final String EDUCATION = "education";
	
	private final String FOOTWAY = "footway";
	private final String PEDESTRIAN = "pedestrian";
	public static final String CROSSING = "crossing";
	private final String CYCLEWAY = "cycleway";
	private final String STEPS = "steps";
	private final String BARRIER = "barrier";
	private final String PARK = "park";
	private final String WATER = "water";
	private final String WOOD = "wood";
	
	private final String TAG = "tag";
	private final String V = "v";
	private final String K = "k";
	private final String AMENITY = "amenity";

	private final String BUILDING = "building";
	private final String AREA = "area";
	private final String WAY = "way";
	private final String LANDUSE = "landuse";
	private final String WATERWAY = "waterway";
	
	private final String SERVICE = "service";
	private final String PLATFORM = "platform";
	private final String NARROW_GAUGE = "narrow_gauge";
	
	private float maxlat;
	private float minlon;
	private float scaleLatY;
	private float scaleLonX;
	
	public static float scaleCollision;

	public static final int COLLISION_COLUMNS  = 200;
	public static final int COLLISION_ROWS     = 200;
	public static final int COLLISION_COST_MIN = -4;
	public static final int COLLISION_COST_MAX = 20;
	
	public static final int ROAD_DEFAULT_COST  = 20;
	public static final int ROAD_DEFAULT_WIDTH = 20;
	
	public static final Color STEPS_COLOR = new Color(159,139,112);  //Set to beaver
	
	public static final Color FOOTWAY_COLOR      = new Color(150,75,0);     //Set to brown
	public static final int FOOTWAY_COST  = -4;
	public static final int FOOTWAY_WIDTH = 12;
	
	private final Color PEDESTRIAN_COLOR = new Color(194, 178, 128);
	private final int PEDESTRIAN_COST	   = -4;
	private final int PEDESTRIAN_WIDTH   = FOOTWAY_WIDTH + 2;
	
	public static final Color PARK_COLOR = new Color(144, 238, 144);
	private final int PARK_COST = FOOTWAY_COST + 2;
	
	public static final Color WOOD_COLOR = new Color(50, 177, 65);
	private final int WOOD_COST = 1;
	
	public static final Color WATER_COLOR = new Color(137, 207, 240);
	private final int WATER_COST = Integer.MAX_VALUE;

	private final int WATERWAY_WIDTH	= 5;
	
	public static final Color CYCLEWAY_COLOR	 = Color.yellow;     
	private final int CYCLEWAY_COST    = 2;
	private final int CYCLEWAY_WIDTH 	 = 5;
	
	public static final Color SERVICE_COLOR 	 = new Color(147,61,65); //Set to smokey topaz
	private final int SERVICE_COST	 = -4;
	private final int SERVICE_WIDTH 	 = 20;

	public static final Color BUILDING_COLOR     = Color.GRAY; 
	public static final int BUILDING_COST = Integer.MAX_VALUE;
	
	public static final Color RAILWAY_COLOR = Color.DARK_GRAY; 
	private final int RAILWAY_COST 	= Integer.MAX_VALUE;
	private final int RAILWAY_WIDTH	= 5;
	
	private final int BARRIER_COST 	= Integer.MAX_VALUE;
	private final int BARRIER_WIDTH	= 1;
	
	public static final Color RAILWAY_PLATFORM_COLOR = Color.LIGHT_GRAY;
	private final int RAILWAY_PLATFORM_COST    = 1;
	private final int RAILWAY_PLATFORM_WIDTH   = 15;
	
	public int[][] collisionMatrix;
	public static HashMap<String,LinkedList<Node>> preCalculatedPaths;
	
	private String filename;
	
	private int progress = 0;
	
	/**
	 * Initializes the OSM_Reader
	 * @param filename Filename to .osm file containing the data
	 */

	public OSM_Reader(){
		nodeMap = new HashMap<String,Node>();
		ways = new LinkedList<Way>();
		buildings = new LinkedList<Building>();
		areas = new LinkedList<Area>();
		targets = new ArrayList<LinkedList<Node>>();
		preCalculatedPaths = new HashMap<String,LinkedList<Node>>(); 
		//Initiate all enums
		for(@SuppressWarnings("unused") TargetEnums e : TargetEnums.values()){
			targets.add(new LinkedList<Node>());
		}
		nonTargetNodes = new LinkedList<Node>();
		iconMap = new HashMap<String,Image>();
		collisionMatrix = new int[COLLISION_ROWS][COLLISION_COLUMNS];
		scaleCollision = (float)collisionMatrix.length / Frame.SIM_WINDOW_LENGTH;	//Scale factor between resolution and collision matrix size
	}
	
	/**
	 * Check that all targets are reachable, also while we're at it - pre-calculate paths (super fast lookup).
	 */
	private void checkTargetsReachable(){
		Pathfinder pathfinder  = new Pathfinder(collisionMatrix);
		LinkedList<Node> removeList = new LinkedList<Node>();
		boolean pathWasFound = false;
		LinkedList<Node> currentPath;
		LinkedList<Node> reversePath;
		
		//Go through all starting positions
		for(LinkedList<Node> startList : targets){
			progress += 8;
			setProgress(progress);
			for(Node startNode : startList){
				
				boolean outsideMap = (startNode.getCollisionXPos(scaleCollision) < 0 || startNode.getCollisionXPos(scaleCollision) >= (collisionMatrix.length-1) || startNode.getCollisionYPos(scaleCollision) < 0 || startNode.getCollisionYPos(scaleCollision) >= (collisionMatrix.length-1));
				
				pathWasFound = false;
				//Make sure that target is inside of map
				if(!outsideMap){
					//Check against all target positions
					for(LinkedList<Node> targetList : targets){
						for(Node targetNode : targetList){
							//Only check against targets that have not already been marked as unreachable
							boolean selfCheck = (targetNode.getXPos() != startNode.getXPos() || targetNode.getYPos() != startNode.getYPos());
							if(!removeList.contains(targetNode) && selfCheck){
								//Check if this path has already been checked
								if(!preCalculatedPaths.containsKey(startNode.toStringCollision(scaleCollision) + "-" + targetNode.toStringCollision(scaleCollision))){
									
									currentPath = new LinkedList<Node>();
									//Check if a path can be found for this start and target node
									if(pathfinder.findPath(startNode.getCollisionXPos(scaleCollision), startNode.getCollisionYPos(scaleCollision), targetNode.getCollisionXPos(scaleCollision), targetNode.getCollisionYPos(scaleCollision), currentPath)){
										
										if(Frame.USE_PRECALCULATED_PATHS)
										{
											preCalculatedPaths.put(startNode.toStringCollision(scaleCollision) + "-" + targetNode.toStringCollision(scaleCollision), currentPath);
											reversePath = new LinkedList<Node>();
											reversePath.addAll(currentPath);
											Collections.reverse(reversePath);
											reversePath.removeFirst();
											reversePath.add(startNode);
											preCalculatedPaths.put(targetNode.toStringCollision(scaleCollision) + "-" + startNode.toStringCollision(scaleCollision) ,reversePath);
										}
										
										pathWasFound = true;
										
									}
								}
								else{
									pathWasFound = true;
								}
							}
						}
					}
				}
				//Remove nodes which we cannot find a path from
				if(!pathWasFound){
					removeList.add(startNode);
				}
			}
		}
		
		//Go through the remove list and remove unreachable nodes
		for(Node node : removeList){
			for(LinkedList<Node> startList : targets){
				startList.remove(node);
			}
		}
	}
	
	/**
	 * This is done on a separate thread so that we can draw a progress bar.
	 */
	@Override
    public Void doInBackground() {
    	setProgress(0);
    	parseMapData(filename);
    	setProgress(100);
        return null;
    }
	
	/**
	 * Returns a collection of areas.
	 * @return All the areas that could be found in the .osm file
	 */
	public LinkedList<Area> getAreas(){
		return areas;
	}
	
	/**
	 * Returns a collection of buildings.
	 * @return All the buildings that could be found in the .osm file
	 */
	public LinkedList<Building> getBuildings(){
		return buildings;
	}
	
	/**
	 * Returns the collision matrix.
	 */
	public int[][] getCollisionMatrix(){
		return collisionMatrix;
	}
	
	/**
	 * Returns the icon map.
	 */
	public HashMap<String,Image> getIcons(){
		return iconMap;
	}
	
	/**
	 * Returns a collection of nodes (non-targets).
	 * @return All the non-targets (crossing) that could be found in the .osm file
	 */
	public LinkedList<Node> getNonTargets(){
		return nonTargetNodes;
	}
	
	/**
	 * Returns the HashMap containing precalculated paths.
	 */
	public HashMap<String,LinkedList<Node>> getPreCalculatedPaths(){
		return preCalculatedPaths;
	}
	
	/**
	 * Returns a collection of nodes (targets).
	 * @return All the targets (amenity,shop) that could be found in the .osm file
	 */
	public ArrayList<LinkedList<Node>> getTargets(){
		return targets;
	}
	
	/**
	 * Returns a collection of ways (i.e roads).
	 * @return All the ways (roads) that could be found in the .osm file
	 */
	public LinkedList<Way> getWays(){
		return ways;
	}
	
	/**
	 * Parses an .osm file and creates simulation map objects
	 * @param filename The file to be parsed
	 */
	@SuppressWarnings("unchecked")
	private void parseMapData(String filename){
		try{
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			InputStream in = new FileInputStream(filename);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			
			//Read through the .osm file
			while(eventReader.hasNext()){

				XMLEvent event = eventReader.nextEvent();
				
				//At start of a new tag
				if(event.isStartElement()){
					StartElement startElement = event.asStartElement();
					
					//If we have bounds tag
					if(startElement.getName().getLocalPart() == (BOUNDS)){
						if(Frame.DEBUG)System.out.println("Found bounds tag");
						float maxlon = 0,minlat = 0;
						Iterator<Attribute> attributes = startElement.getAttributes();
						
						//Go through all bounds attributes
						while(attributes.hasNext()){
							Attribute attribute = attributes.next();
							if(attribute.getName().toString().equals(MINLAT)) minlat = Float.parseFloat(attribute.getValue());
							else if(attribute.getName().toString().equals(MINLON)) minlon = Float.parseFloat(attribute.getValue());
							else if(attribute.getName().toString().equals(MAXLAT)) maxlat = Float.parseFloat(attribute.getValue());
							else if(attribute.getName().toString().equals(MAXLON)) maxlon = Float.parseFloat(attribute.getValue());
						}
						
						//Calculate scaling values for lon and lat to screen coordinates
						float latDiff = maxlat-minlat;
						float lonDiff = maxlon-minlon;
						scaleLonX =  Frame.SIM_WINDOW_LENGTH / lonDiff;
						scaleLatY = -Frame.SIM_WINDOW_LENGTH / latDiff;
					}
					
					//If we have a node tag
					else if(startElement.getName().getLocalPart() == (NODE)){
						String id = "";
						float lon = 0, lat = 0;
						
						Iterator<Attribute> attributes = startElement.getAttributes();
						
						//Go through all node attributes
						while(attributes.hasNext()){
							Attribute attribute = attributes.next();
							if(attribute.getName().toString().equals(ID)) id = attribute.getValue();
							else if(attribute.getName().toString().equals(LON)) lon = Float.parseFloat(attribute.getValue());
							else if(attribute.getName().toString().equals(LAT)) lat = Float.parseFloat(attribute.getValue());
						}
						
						Node node = new Node(Math.round(scaleLonX*(lon-minlon)),Math.round(scaleLatY*(lat-maxlat)));
						
						event = eventReader.nextEvent();
						
						//Check if node has any tags
						while(true){

							if(event.isStartElement()){
								startElement = event.asStartElement();
								//Does this node have any tags?
								if(startElement.getName().getLocalPart() == (TAG)){
									attributes = startElement.getAttributes();
									//Go through all tag attributes
									while(attributes.hasNext()){
										Attribute attribute = attributes.next();
										
										//K
										if(attribute.getName().toString().equals(K)){
											//SHOP
											if(attribute.getValue().toString().equals(SHOP)){
												if(Frame.DEBUG)System.out.println("Added shop");
												node.setTag(SHOP);
												targets.get(TargetEnums.SHOP_I.ordinal()).add(node);
											}
										}

										//V
										else if(attribute.getName().toString().equals(V)){
											//CROSSING
											if(attribute.getValue().toString().equals(CROSSING)){
												if(Frame.DEBUG)System.out.println("Added CROSSING");
												node.setTag(CROSSING);
												nonTargetNodes.add(node);
											}
											//SUBWAY ENTRANCE
											else if(attribute.getValue().toString().equals(SUBWAY_ENTRANCE)){
												if(Frame.DEBUG)System.out.println("Added SUBWAY_ENTRANCE");
												node.setTag(SUBWAY_ENTRANCE);
												targets.get(TargetEnums.PUBLIC_TRANSPORT.ordinal()).add(node);
											}
											//BUS STATION
											else if(attribute.getValue().toString().equals(BUS_STATION)){
												if(Frame.DEBUG)System.out.println("Added BUS_STATION");
												node.setTag(BUS_STATION);
												targets.get(TargetEnums.PUBLIC_TRANSPORT.ordinal()).add(node);
											}
											//BUS STOP
											else if(attribute.getValue().equals(BUS_STOP)){
												if(Frame.DEBUG)System.out.println("Added BUS_STOP");
												node.setTag(BUS_STOP);
												targets.get(TargetEnums.PUBLIC_TRANSPORT.ordinal()).add(node);
											}
											//RESTAURANT
											else if(attribute.getValue().toString().equals(RESTAURANT)){
												if(Frame.DEBUG)System.out.println("Added RESTAURANT");
												node.setTag(RESTAURANT);
												targets.get(TargetEnums.RESTAURANT_I.ordinal()).add(node);
											}
											//CAFE
											else if(attribute.getValue().toString().equals(CAFE)){
												if(Frame.DEBUG)System.out.println("Added CAFE");
												node.setTag(CAFE);
												targets.get(TargetEnums.CAFE_I.ordinal()).add(node);
											}
											//TOILETS
											else if(attribute.getValue().toString().equals(TOILETS)){
												if(Frame.DEBUG)System.out.println("Added Toilet");
												node.setTag(TOILETS);
												targets.get(TargetEnums.TOILET_I.ordinal()).add(node);
											}
											//WASTE BIN
											else if(attribute.getValue().toString().equals(WASTEBIN)){
												if(Frame.DEBUG)System.out.println("Added WASTE BIN");
												node.setTag(WASTEBIN);
												targets.get(TargetEnums.WASTE_I.ordinal()).add(node);
											}
											//FAST FOOD
											else if(attribute.getValue().toString().equals(FAST_FOOD)){
												if(Frame.DEBUG)System.out.println("Added FAST_FOOD");
												node.setTag(FAST_FOOD);
												targets.get(TargetEnums.FASTFOOD_I.ordinal()).add(node);
											}
											//DOCTORS
											else if(attribute.getValue().toString().equals(DOCTORS)){
												if(Frame.DEBUG)System.out.println("Added DOCTORS");
												node.setTag(DOCTORS);
												targets.get(TargetEnums.HEALTH.ordinal()).add(node);
											}
											//HOSPITAL
											else if(attribute.getValue().toString().equals(HOSPITAL)){
												if(Frame.DEBUG)System.out.println("Added HOSPITAL");
												node.setTag(HOSPITAL);
												targets.get(TargetEnums.HEALTH.ordinal()).add(node);
											}
											//PHARMACY
											else if(attribute.getValue().toString().equals(PHARMACY)){
												if(Frame.DEBUG)System.out.println("Added PHARMACY");
												node.setTag(PHARMACY);
												targets.get(TargetEnums.HEALTH.ordinal()).add(node);
											}
											//LIBRARY
											else if(attribute.getValue().toString().equals(LIBRARY)){
												if(Frame.DEBUG)System.out.println("Added LIBRARY");
												node.setTag(LIBRARY);
												targets.get(TargetEnums.STUDY.ordinal()).add(node);
											}
											//BANK
											else if(attribute.getValue().toString().equals(BANK)){
												if(Frame.DEBUG)System.out.println("Added BANK");
												node.setTag(BANK);
												targets.get(TargetEnums.BANK_I.ordinal()).add(node);
											}
											//ATM
											else if(attribute.getValue().toString().equals(ATM)){
												if(Frame.DEBUG)System.out.println("Added BANK");
												node.setTag(BANK);
												targets.get(TargetEnums.BANK_I.ordinal()).add(node);
											}
											//STATION
											else if(attribute.getValue().toString().equals(STATION)){
												if(node.getXPos() >= 0 && node.getXPos() < Frame.SIM_WINDOW_LENGTH && 
												   node.getYPos() >= 0 && node.getYPos() < Frame.SIM_WINDOW_LENGTH)
												{
													if(Frame.DEBUG)System.out.println("Added STATION");
													node.setTag(STATION);
													targets.get(TargetEnums.PUBLIC_TRANSPORT.ordinal()).add(node);
												}
											}
										}
									}
								}
							}
							else if(event.isEndElement()){
								EndElement endElement = event.asEndElement();
								//END NODE
								if(endElement.getName().getLocalPart() == (NODE)){
									break;
								}
							}
							event = eventReader.nextEvent();
						}
						
						nodeMap.put(id, node); //Put our node into the node map
					}
					
					//If we have a way tag
					else if(startElement.getName().getLocalPart() == (WAY)){
						LinkedList<Node> nodes = new LinkedList<Node>();
						MapObject mapObject = null;
						boolean ignore = false;
						
						//Go through all node references for way
						while(true){
							//Is event a start element?
							if(event.isStartElement()){
								startElement = event.asStartElement();
	
								//A new node has been detected for the way
								if(startElement.getName().getLocalPart() == (ND)){
									Iterator<Attribute> attributes = startElement.getAttributes();
									//Take the single attribute the node reference
									while(attributes.hasNext()){
										Attribute attribute = attributes.next();
										if(attribute.getName().toString().equals(REF)) nodes.add(nodeMap.get(attribute.getValue()));
									}
								}
								//A tag has been detected for the way
								if(startElement.getName().getLocalPart() == (TAG)){
									Iterator<Attribute> attributes = startElement.getAttributes();
									//Take the attribute the tag references
									while(attributes.hasNext()){
										Attribute attribute = attributes.next();
										//Value
										if(attribute.getName().toString().equals(V)) {
											//FOOTWAY
											if(attribute.getValue().toString().equals(FOOTWAY)){
												mapObject = new Way();
												mapObject.setCost(FOOTWAY_COST);
												((Way)mapObject).setWidth(FOOTWAY_WIDTH);
												mapObject.setColor(FOOTWAY_COLOR);
											}
											//PEDESTRIAN
											else if(attribute.getValue().toString().equals(PEDESTRIAN)){
												//AREA
												if(mapObject instanceof Area){
													mapObject.setCost(PEDESTRIAN_COST);
													mapObject.setColor(PEDESTRIAN_COLOR);
												}
												//WAY
												else{
													mapObject = new Way();
													mapObject.setCost(PEDESTRIAN_COST);
													((Way)mapObject).setWidth(PEDESTRIAN_WIDTH);
													mapObject.setColor(PEDESTRIAN_COLOR);
												}
											}
											//PARK
											else if(attribute.getValue().toString().equals(PARK)){
												mapObject = new Area();
												mapObject.setCost(PARK_COST);
												mapObject.setColor(PARK_COLOR);
											}
											//WOOD
											else if(attribute.getValue().toString().equals(WOOD)){
												mapObject = new Area();
												mapObject.setCost(WOOD_COST);
												mapObject.setColor(WOOD_COLOR);
											}
											//WATER
											else if(attribute.getValue().toString().equals(WATER)){
												mapObject = new Area();
												mapObject.setCost(WATER_COST);
												mapObject.setColor(WATER_COLOR);
											}
											//STEPS
											else if(attribute.getValue().toString().equals(STEPS)){
												mapObject = new Way();
												mapObject.setCost(FOOTWAY_COST);
												((Way)mapObject).setWidth(FOOTWAY_WIDTH);
												mapObject.setColor(STEPS_COLOR);
											}
											//SERVICE
											else if(attribute.getValue().toString().equals(SERVICE)){
												mapObject = new Way();
												mapObject.setCost(SERVICE_COST);
												((Way)mapObject).setWidth(SERVICE_WIDTH);
												mapObject.setColor(SERVICE_COLOR);
											}
											//CYCLE WAY
											else if(attribute.getValue().toString().equals(CYCLEWAY)){
												mapObject = new Way();
												mapObject.setCost(CYCLEWAY_COST);
												((Way)mapObject).setWidth(CYCLEWAY_WIDTH);
												mapObject.setColor(CYCLEWAY_COLOR);
											}
											//PLATFORM
											else if(attribute.getValue().toString().equals(PLATFORM)){
												mapObject = new Way();
												mapObject.setCost(RAILWAY_PLATFORM_COST);
												((Way)mapObject).setWidth(RAILWAY_PLATFORM_WIDTH);
												mapObject.setColor(RAILWAY_PLATFORM_COLOR);
											}
											//NARROW GAUGE (RAILWAY)
											else if(attribute.getValue().toString().equals(NARROW_GAUGE)){
												mapObject = new Way();
												mapObject.setCost(RAILWAY_COST);
												((Way)mapObject).setWidth(RAILWAY_WIDTH);
												mapObject.setColor(RAILWAY_COLOR);
											}
											//SUBWAY
											else if(attribute.getValue().toString().equals(SUBWAY)){
												ignore = true;
												break;
											}
											//EDUCATION
											else if(attribute.getValue().toString().equals(EDUCATION)){
												mapObject.setTag(EDUCATION);
											}
										}
										//Key
										else if(attribute.getName().getLocalPart() == (K)){
											//BUILDING
											if(attribute.getValue().toString().equals(BUILDING)){
												mapObject = new Building();
												mapObject.setColor(BUILDING_COLOR);
												mapObject.setCost(BUILDING_COST);
												ignore = false;
											}
											//WATERWAY
											else if(attribute.getValue().toString().equals(WATERWAY)){
												mapObject = new Way();
												mapObject.setColor(WATER_COLOR);
												mapObject.setCost(WATER_COST);
												((Way)mapObject).setWidth(WATERWAY_WIDTH);
											}
											//AREA
											else if(attribute.getValue().toString().equals(AREA)){
												mapObject = new Area();
											}
											//BARRIER
											else if(attribute.getValue().toString().equals(BARRIER)){
												mapObject = new Way();
												mapObject.setCost(BARRIER_COST);
												((Way)mapObject).setWidth(BARRIER_WIDTH);
											}
											//AMENITY
											else if(attribute.getValue().toString().equals(AMENITY)){
												ignore = true;
											}
											//LANDUSE
											else if(attribute.getValue().toString().equals(LANDUSE)){
												ignore = true;
											}
										}
									}
								}
							}
							//Is event an end element?
							else if(event.isEndElement()){
								EndElement endElement = event.asEndElement();
								
								//At the end of a Way element? (add way and onwards to next tag)
								if(endElement.getName().getLocalPart() == (WAY)){
									break;
								}
							}
							event = eventReader.nextEvent();
						}
						if(mapObject == null && !ignore){
							mapObject = new Way();
						}
						if(mapObject != null && !ignore) {
							mapObject.addNodes(nodes);
						
							//Calculate collision and add to collection
							if(mapObject instanceof Building){
								((Building)mapObject).checkTargetsInside(targets);
								buildings.add((Building)mapObject);
							}
							else if(mapObject instanceof Area){
								areas.add((Area)mapObject);
							}
							else if(mapObject instanceof Way){
								ways.add((Way)mapObject);
							}
							mapObject.calculateCollision(collisionMatrix);
						}
					}
				}
			}
			progress += 5;
			setProgress(progress);
			checkTargetsReachable();
			setProgress(97);
			readIcons();
			setProgress(98);
			setTargetCosts();
			setProgress(99);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (XMLStreamException e) {
    	  	e.printStackTrace();
		}
	}
	
	/**
	 * Checks with available targets and adds matching icons to these into a HashMap.
	 * The images can then be retrieved by corresponding tag.
	 */
	private void readIcons(){
		String tag = "";
		
		//Handles tags that have no related icon
		Image unknownImg = null;
		try {
			unknownImg = ImageIO.read(new File(ICON_FILE_PATH + "Unknown.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		for(LinkedList<Node> list : targets){
			for(Node node : list){
				tag = node.getTag();
				if(!iconMap.containsKey(tag)){				
					try {
						Image img = ImageIO.read(new File(ICON_FILE_PATH + tag + ".png"));
						iconMap.put(tag, img);
					} catch (IOException e) {
						iconMap.put(tag, unknownImg);
						System.err.println(tag + " tag has no related icon");
					}
				}
			}
		}
		
		for(Node node : nonTargetNodes){
			tag = node.getTag();
			if(!iconMap.containsKey(tag)){				
				try {
					Image img = ImageIO.read(new File(ICON_FILE_PATH + tag + ".png"));
					iconMap.put(tag, img);
				} catch (IOException e) {
					iconMap.put(tag, unknownImg);
					System.err.println(tag + " tag has no related icon");
				}
			}
		}
	}
	
	/**
	 * Set the name of the file to read.
	 * @param name
	 */
	public void setFilename(String name){
		filename = name;
	}
	
	/**
	 * Make sure that targets inside of buildings (or near buildings) are walkable.
	 */
	private void setTargetCosts(){
		//Check which targets that are inside of this building
		for(LinkedList<Node> list : targets){
			for(Node node : list){
				//Make a walkable square at the position so that the target is reachable
				for(int k = node.getXPos()-PedestriansSimulator.PEDESTRIAN_RADIUS; k < node.getXPos()+PedestriansSimulator.PEDESTRIAN_RADIUS; k++) {
					if(Math.round(scaleCollision * k) >= 0 && Math.round(scaleCollision * k) < COLLISION_ROWS){
						for(int l = node.getYPos()-PedestriansSimulator.PEDESTRIAN_RADIUS; l < node.getYPos()+PedestriansSimulator.PEDESTRIAN_RADIUS; l++) {
							if(Math.round(scaleCollision * l) >= 0 && Math.round(scaleCollision * l) < COLLISION_ROWS){
								collisionMatrix[Math.round(scaleCollision * k)][Math.round(scaleCollision * l)] = FOOTWAY_COST;
							}
						}
					}
				}
			}
		}
	}
}