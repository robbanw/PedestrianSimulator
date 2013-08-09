import java.awt.Color;
import java.util.LinkedList;
import java.util.Random;

/**
 * A Pedestrian is an object used to represent a simulated pedestrian.
 * It has different variables for controlling things like its color, speed and "needs".
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 09-08-2013
 */
public class Pedestrian {
	//Attributes
	private Color myColor;
	private PedestriansSimulator.PEDESTRIAN_TYPE type; //The type of Pedestrian (e.g Student)
	private int myId;
	
	//Basic needs
	private static enum BasicNeeds {HUNGER,TOILET,WASTE,CAFE,SHOP,BANK}; 
	private LinkedList<Float> basicNeeds;
	private Boolean[] needStatus;
	private int currentNeedIndex = -1;
	private long arriveTime;            //At what time did Pedestrian arrive at target? In Simulator-Minutes  
	private int currentWaitTime;        //How long should we wait at target (to carry out need)
	private boolean isWalking = false;	//Pedestrian is on his/her way somewhere

	//At tile needs increase
	private float hungerNeedIncrease = 0.006f;
	private float toiletNeedIncrease = 0.002f;
	private float cafeNeedIncrease   = 0.001f;
	private float shopNeedIncrease   = 0.0005f;
	
	//At target needs increase
	private float fastFoodWasteIncrease   = 0.5f;
	private float foodToiletIncrease      = 0.1f;
	private float cafeToiletIncrease      = 0.04f;
	private float cafeBankIncrease        = 0.08f;
	private float restaurantBankIncrease  = 0.1f;
	private float fastFoodBankIncrease    = 0.05f;
	private float shopBankIncrease        = 0.5f;
	
	//At target needs decrease
	private final float cafeFoodDecrease = 0.1f;
	
	//Time at target (how long should it take to carry out the need?) (In Simulator-Minutes )
	private final int fastFoodTime   = 5;
	private final int restaurantTime = 20;
	private final int toiletTime     = 1;
	private final int cafeTime       = 15;
	private final int shopTime       = 5;
	private final int wasteTime      = 0;
	private final int bankTime       = 1;
	
	//Near-path check
	private LinkedList<Node> currentPath;
	private boolean avoiding = false;
	private final double EPSILON = 1E-14;
	
	//Pathfinding
	private Pathfinder pathfinder;
	private int collisionPosX;				//Pedestrian's position inside of the collision matrix (i.e current "tile" x-wise)
	private int collisionPosY;				//Pedestrian's position inside of the collision matrix (i.e current "tile" y-wise)
	private int targetCollisionPosX;		//Target position inside of the collision matrix (x-wise)
	private int targetCollisionPosY;		//Target position inside of the collision matrix (y-wise)
	private int[][] collisionMatrix;
	private float scaleCollision;
	
	//Interpolation
	private final int AVOID_LENGTH = 2*PedestriansSimulator.PEDESTRIAN_RADIUS; //Length of avoiding action that pedestrians use in order to avoid collisions
	private double updatePosX;	//Approximately how many pixels in the x-axis that Pedestrian should be moved at each interpolation update
	private double updatePosY;	//Approximately how many pixels in the y-axis that Pedestrian should be moved at each interpolation update 
	private double renderPosX;
	private double renderPosY;
	private double interpolationProgress = 0;
	private final int INTERPOLATION_OFFSET = 10;
	private double interpolationLength;
	private double degree;
	private double interpolationStep;
	
	//Schedule
	private LinkedList<ScheduleEvent> schedule;
	private int wakeHour;
	private int wakeMinute;
	private int sleepHour;
	private int sleepMinute;
	private boolean sleeping = true;
	private boolean scheduleEvent = false;
	private Node start;
	
	//The min and max nr of hours that different events could take
	private final float STUDY_DURATION_MIN      = 1;
	private final float STUDY_DURATION_MAX      = 4;
	private final float RESEARCH_DURATION_MIN   = 2; 
	private final float RESEARCH_DURATION_MAX   = 4; 
	private final float DOCTORS_APPOINTMENT_MIN = 0.1f;
	private final float DOCTORS_APPOINTMENT_MAX = 1;
	
	//Misc
	Random randomGenerator;
	
	/**
	 * Initializes a new pedestrians with input values.
	 * @param startX X-value for starting position
	 * @param startY Y-value for starting position
	 * @param collisionMatrix A reference to the collisionMatrix (used when moving)
	 * @param type The type of Pedestrians that we wish to create (will affect its schedule)
	 * @param id The id that will be associated with this Pedestrian
	 */
	public Pedestrian(int startX, int startY, int[][] collisionMatrix, PedestriansSimulator.PEDESTRIAN_TYPE type, int id){
		currentPath = new LinkedList<Node>();
		schedule = new LinkedList<ScheduleEvent>();
		randomGenerator = new Random();
		basicNeeds = new LinkedList<Float>();
		needStatus = new Boolean[BasicNeeds.values().length];
		
		this.collisionMatrix = collisionMatrix; //Reference to collisionMatrix
		pathfinder = new Pathfinder(collisionMatrix);
		scaleCollision = OSM_Reader.scaleCollision;
		this.type  = type;
		myColor = type.color();
		renderPosX = startX;
		renderPosY = startY;
		collisionPosX = Math.round(scaleCollision * startX);
		collisionPosY = Math.round(scaleCollision * startY);
		myId = id;
		
		//Introduce some randomness to the needs parameters
		//At tile increase
		hungerNeedIncrease = (float) (hungerNeedIncrease + Math.abs(randomGenerator.nextGaussian()*(hungerNeedIncrease/2)));
		toiletNeedIncrease = (float) (toiletNeedIncrease + Math.abs(randomGenerator.nextGaussian()*(toiletNeedIncrease/2)));
		cafeNeedIncrease   = (float) (cafeNeedIncrease + Math.abs(randomGenerator.nextGaussian()*(cafeNeedIncrease/2)));
		shopNeedIncrease   = (float) (shopNeedIncrease + Math.abs(randomGenerator.nextGaussian()*(shopNeedIncrease/2)));
		
		//At target increase
		fastFoodWasteIncrease   = (float) (fastFoodWasteIncrease + Math.abs(randomGenerator.nextGaussian()*(fastFoodWasteIncrease/2)));
		foodToiletIncrease      = (float) (foodToiletIncrease + Math.abs(randomGenerator.nextGaussian()*(foodToiletIncrease/2)));
		cafeToiletIncrease      = (float) (cafeToiletIncrease + Math.abs(randomGenerator.nextGaussian()*(cafeToiletIncrease/2)));
		cafeBankIncrease        = (float) (cafeBankIncrease + Math.abs(randomGenerator.nextGaussian()*(cafeBankIncrease/2)));
		restaurantBankIncrease  = (float) (restaurantBankIncrease + Math.abs(randomGenerator.nextGaussian()*(restaurantBankIncrease/2)));
		fastFoodBankIncrease    = (float) (fastFoodBankIncrease + Math.abs(randomGenerator.nextGaussian()*(fastFoodBankIncrease/2)));
		shopBankIncrease        = (float) (shopBankIncrease + Math.abs(randomGenerator.nextGaussian()*(shopBankIncrease/2)));
		
		//Init. basic needs
		for(@SuppressWarnings("unused") BasicNeeds b : BasicNeeds.values()){
			basicNeeds.add(randomGenerator.nextFloat());
		}
		
		//Check which needs that can not be fulfilled at this map
		needStatus[BasicNeeds.HUNGER.ordinal()] = (!(OSM_Reader.targets.get(OSM_Reader.TargetEnums.FASTFOOD_I.ordinal()).isEmpty()) || !(OSM_Reader.targets.get(OSM_Reader.TargetEnums.RESTAURANT_I.ordinal()).isEmpty()));
		needStatus[BasicNeeds.CAFE.ordinal()] = !OSM_Reader.targets.get(OSM_Reader.TargetEnums.CAFE_I.ordinal()).isEmpty();
		needStatus[BasicNeeds.TOILET.ordinal()] = !OSM_Reader.targets.get(OSM_Reader.TargetEnums.TOILET_I.ordinal()).isEmpty();
		needStatus[BasicNeeds.SHOP.ordinal()] = !OSM_Reader.targets.get(OSM_Reader.TargetEnums.SHOP_I.ordinal()).isEmpty();
		needStatus[BasicNeeds.BANK.ordinal()] = !OSM_Reader.targets.get(OSM_Reader.TargetEnums.BANK_I.ordinal()).isEmpty();
		needStatus[BasicNeeds.WASTE.ordinal()] = !OSM_Reader.targets.get(OSM_Reader.TargetEnums.WASTE_I.ordinal()).isEmpty();
		
		start = new Node((int)Math.round(renderPosX),(int)Math.round(renderPosY));
		
		generateTarget();
		
		generateSchedule();
	}
	
	/**
	 * Tries to avoid a potential collision by introducing an avoiding motion away from the collision.
	 * @param p2PosX X-position of the other colliding pedestrian
	 * @param p2PosY Y-position of the other colliding pedestrian
	 * @param avoidLengthMultitude How many "pedestrian" lengths that the avoiding action should be
	 * @param intersectionLength How many pixels into the other object that Pedestrian is intersecting
	 */
	public void avoidCollision(int p2PosX, int p2PosY, int avoidLengthMultitude, int intersectionLength){	
		//Make sure that pedestrian isn't already doing an avoiding action
		if(!avoiding){
			//Move pedestrian back from collision (so that it is no longer intersecting)
			renderPosX -= Math.cos(degree)*intersectionLength;
			renderPosY -= Math.sin(degree)*intersectionLength;
			
			interpolationProgress = 0;
			interpolationLength = AVOID_LENGTH*avoidLengthMultitude;
			
			//Calculate avoidance position
			int newPosX = getPosX() - p2PosX;
			int newPosY = getPosY() - p2PosY;
			
			double vecLength = Math.sqrt((newPosX*newPosX + newPosY*newPosY)) + EPSILON; //Length between pedestrians  
			double invPow    = Math.pow((vecLength),-2);                                 //Avoid zero divsion by adding epsilon
			
			//Calculate separation target (i.e where pedestrian should avoid to) and normalize this
			double separationX = (newPosX*invPow)/vecLength;  
			double separationY = (newPosY*invPow)/vecLength;
			
			double originalDegree = degree;
			degree = Math.atan2(separationY, separationX);
			
			//Since avoiding action likely will be the opposite way of target, weigh in original direction in order to produce a better avoiding direction  
			degree = (originalDegree*0.4 + degree*0.6);
			
			//Make sure that interpolation is at maximum the length of the interpolation line
			if(PedestriansSimulator.PEDESTRIAN_SPEED > interpolationLength){
				updatePosX = Math.cos(degree)*interpolationLength;
				updatePosY = Math.sin(degree)*interpolationLength;				
			}
			else{
				updatePosX = Math.cos(degree)*PedestriansSimulator.PEDESTRIAN_SPEED;
				updatePosY = Math.sin(degree)*PedestriansSimulator.PEDESTRIAN_SPEED;
			}
			interpolationStep = Math.sqrt(Math.pow(updatePosX, 2) + Math.pow(updatePosY, 2));
			avoiding = true;
			currentPath.addFirst(new Node((int)Math.round(renderPosX),(int)Math.round(renderPosY)));
		}
	}
	
	/**
	 * Calculates and returns the current wait time for the pedestrian.
	 * @param currentHour Current hour of time
	 * @param currentMinute Current minute of time
	 * @param durationMinutes Duration of event
	 * @param eventStartHour Start hour of event
	 */
	private int calculateWaitTime(int currentHour, int currentMinute, int durationMinutes, int eventStartHour){
		int waitTime = 0;
		//Check if we passed midnight before arriving to event (need some special calculations in this case)
		if(currentHour < schedule.getFirst().getStartHour()){
			waitTime = (durationMinutes - (24*60 - eventStartHour*60 + (currentHour*60 + currentMinute)));
		}
		else{
			waitTime = (durationMinutes - ((currentHour*60 + currentMinute) - eventStartHour*60));
		}
		
		return waitTime;
	}
	
	/**
	 * Checks if input time has passed
	 * @param hour 		The hour to check against current simulator hour	
	 * @param minute	The minute to check against current simulator minute
	 * @return	True if the time has passed else False
	 */
	private boolean checkTime(int hour, int minute){
		if(hour <= Frame.clock.getHour() && minute <= Frame.clock.getMinute()){			
			return true;
		}
		return false;
	}
	
	/**
	 * Is pedestrian done at current target (i.e. has she/he carried out her/his need?)
	 * @return Boolean value signaling if we are done waiting at the target.
	 */
	public boolean doneAtTarget(){
		long currentTime = Frame.clock.getNrMinutes();
		if(currentTime-arriveTime >= currentWaitTime){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Finds the closest target in the list from this position.
	 * @param targets The list of possible targets
	 * @return The closest target
	 */
	private Node findNearestTarget(LinkedList<Node> targets){
		Node closest = targets.getFirst();		
		int xdist = (int)Math.round(Math.abs(renderPosX - closest.getXPos()));
		int ydist = (int)Math.round(Math.abs(renderPosY - closest.getYPos()));;
		int mindist = (int)Math.round(Math.sqrt(Math.pow(xdist,2)+Math.pow(ydist, 2)));
		int distance;
		for(int i = 1; i < targets.size(); i++){
			xdist = (int)Math.round(Math.abs(renderPosX - targets.get(i).getXPos()));
			ydist = (int)Math.round(Math.abs(renderPosY - targets.get(i).getYPos()));
			distance = (int)Math.round(Math.sqrt(Math.pow(xdist,2)+Math.pow(ydist, 2)));
			if(distance < mindist){
				mindist = distance;
				closest = targets.get(i);
			}
		}
		return closest;
	}
	
	/**
	 * Tries to first locate a precalculated path, else calculates the path between current collision pos and target collision pos.
	 */
	private void findPath(){
		String key = collisionPosX + "," + collisionPosY + "-" + targetCollisionPosX + "," + targetCollisionPosY;
		//Use precalculated path if it exists
		if(OSM_Reader.preCalculatedPaths.containsKey(key)){
			currentPath.clear();
			currentPath.addAll(OSM_Reader.preCalculatedPaths.get(key));
		}
		else{		
			pathfinder.findPath(collisionPosX,collisionPosY,targetCollisionPosX,targetCollisionPosY,currentPath);
		}
	}
	
	/**
	 * Generates a schedule for the Pedestrian by filling it with Pedestrian type specific events.
	 */
	private void generateSchedule(){
		float startHour   = wakeHour + wakeMinute/60f;
		float currentHour = startHour + randomGenerator.nextFloat()*0.5f; //Make sure that Pedestrian do not have a scheduled event at start
		float endHour     = sleepHour + sleepMinute/60f;
		float lunchHour   = -randomGenerator.nextFloat() + randomGenerator.nextFloat() + 12;
		float eventDurationHour;
		int schedulePos = 0;
		boolean lunchPlaced = false;
		boolean isMotivated;
		if(Frame.DEBUG)System.out.println("Lunch hour " + lunchHour);
		if(Frame.DEBUG)System.out.println("Current hour " + currentHour);
		
		switch(type){
			case STUDENT:
			{
				//Continue to fill the schedule until the day is over
				while(currentHour < endHour){
					//Place the lunch hour
					if(!lunchPlaced && currentHour >= lunchHour){
						currentHour++;
						lunchPlaced = true;
					}
					else{
						eventDurationHour = (randomGenerator.nextFloat()*STUDY_DURATION_MAX + STUDY_DURATION_MIN);
						if(Frame.DEBUG)System.out.println("Event time: " + eventDurationHour);
						//Try to place this event
						if(lunchPlaced || (currentHour + eventDurationHour) < lunchHour){
							if(currentHour + eventDurationHour < endHour){
								isMotivated = randomGenerator.nextBoolean();
								
								//Is the student motivated to study?
								if(isMotivated){
									schedule.add(schedulePos,new ScheduleEvent((int)Math.floor(currentHour),(int)Math.round(eventDurationHour*60),OSM_Reader.targets.get((OSM_Reader.TargetEnums.STUDY.ordinal())).get(randomGenerator.nextInt(OSM_Reader.targets.get(OSM_Reader.TargetEnums.STUDY.ordinal()).size()))));
									schedulePos++;
								}
								//Let's have some tea-time/coffee-time/energy-drink/... instead
								else{
									currentHour += randomGenerator.nextFloat();
								}
							}
							//Time to end this day's schedule
							else{
								break;
							}
						}
						else{
							currentHour += 0.1;
						}
					}
				}
				break;
			}
			
			case PROFESSOR:
			{
				//Continue to fill the schedule until the day is over
				while(currentHour < endHour){
					//Place the lunch hour
					if(!lunchPlaced && currentHour > lunchHour){
						currentHour++;
						lunchPlaced = true;
					}
					else{
						eventDurationHour = (randomGenerator.nextFloat()*RESEARCH_DURATION_MAX + RESEARCH_DURATION_MIN);
						//Try to place this event
						if(lunchPlaced || (currentHour + eventDurationHour) < lunchHour){
							if(currentHour + eventDurationHour < endHour){
								isMotivated = randomGenerator.nextBoolean();
								
								//Is the professor motivated to research?
								if(isMotivated){
									schedule.add(schedulePos,new ScheduleEvent((int)Math.floor(currentHour),(int)Math.round(eventDurationHour*60),OSM_Reader.targets.get((OSM_Reader.TargetEnums.STUDY.ordinal())).get(randomGenerator.nextInt(OSM_Reader.targets.get(OSM_Reader.TargetEnums.STUDY.ordinal()).size()))));
									schedulePos++;
								}
								//Let's have some coffee-time instead
								else{
									currentHour += randomGenerator.nextFloat();
								}
							}
							//Time to end this day's schedule
							else{
								break;
							}
						}
						else{
							currentHour += 0.1;
						}
					}
				}
				break;
			}
			
			case CIVILIAN:
			{
				//We may only add doctors appointments if hospitals are present on the map
				if(!OSM_Reader.targets.get(OSM_Reader.TargetEnums.WASTE_I.ordinal()).isEmpty()){
					currentHour += (endHour-currentHour)*randomGenerator.nextFloat(); //Have an offset for the potential doctor's appointment
					//Should this Pedestrian go to the doctor?
					if(randomGenerator.nextBoolean() && currentHour < endHour){
						eventDurationHour = (randomGenerator.nextFloat()*DOCTORS_APPOINTMENT_MAX + DOCTORS_APPOINTMENT_MIN);
						//Try to place this event
						if(currentHour + eventDurationHour < endHour){
							schedule.add(schedulePos,new ScheduleEvent((int)Math.floor(currentHour),(int)Math.round(eventDurationHour*60),OSM_Reader.targets.get((OSM_Reader.TargetEnums.HEALTH.ordinal())).get(randomGenerator.nextInt(OSM_Reader.targets.get(OSM_Reader.TargetEnums.HEALTH.ordinal()).size()))));
							schedulePos++;
						}
					}
				}
				break;
			}
			
			default:
			break;
		}
	}
	
	/**
	 * Checks the Pedestrian's schedule for certain timed events, if none can be found we will
	 * go through the Pedestrian's list of BasicNeeds and set a new target accordingly to these needs
	 * (e.g a hungry pedestrian will go to a restaurant).
	 */
	private void generateTarget(){
		int duration = 0;
		double startTime = 0;
		int startAtHour = 0;
		int startAtMinute = 0;
		double endTime = 0;
		int sleepHour = 0;
		int indexOfMaxNeed = -1;
		float currentMaxNeedValue = -1;
		boolean fastFood;
		Node newTarget = null;

		//Is today's schedule done?
		if(schedule.isEmpty()){
			startTime     = randomGenerator.nextGaussian()*PedestriansSimulator.WIDTH_START_HOUR + PedestriansSimulator.CENTRAL_START_HOUR;
			startAtHour   = (int)Math.floor(startTime);
			startAtMinute = (int)Math.floor(startTime%1*60);
			
			endTime   = randomGenerator.nextGaussian()*PedestriansSimulator.WIDTH_END_HOUR + PedestriansSimulator.CENTRAL_END_HOUR;
			sleepHour = (int)Math.floor(endTime);

			//Did we go to sleep after midnight?
			if(sleepHour < startAtHour){
				duration = startAtHour*60 + startAtMinute - sleepHour*60;
			}
			else{
				duration = 24*60 - sleepHour*60 + startAtHour*60 + startAtMinute; 
			}
			newTarget = start;
			schedule.add(new ScheduleEvent(sleepHour,duration,newTarget)); //Add a "go home" event to schedule
			this.sleepHour   = sleepHour;
			this.sleepMinute = 0;
			this.wakeHour    = startAtHour;
			this.wakeMinute  = startAtMinute;
			generateSchedule();
		}
		else{
			//On our way toward an event
			if(scheduleEvent){
				//Have we already passed the event?
				if(calculateWaitTime(Frame.clock.getHour(),Frame.clock.getMinute(),schedule.getFirst().getDuration(),schedule.getFirst().getStartHour()) < 0){
					schedule.removeFirst();
					scheduleEvent = false;
				}
				//Escape method so that we don't by mistake try to generate a new target if target list is empty
				return;
			}
			//Check schedule (i.e do we have an important target which we need to go to?)
			if(checkTime(schedule.getFirst().getStartHour(),0)){
				scheduleEvent = true;
				newTarget = schedule.getFirst().getTarget();
			}
			//No schedule event at this time - Time to check ze needs
			else{
				//Check which basic need that currently is the largest 
				for(int i = 0; i < basicNeeds.size(); i++){
					if(basicNeeds.get(i) > currentMaxNeedValue && needStatus[i]){
						currentMaxNeedValue = basicNeeds.get(i);
						indexOfMaxNeed = i;
					}
				}
				//Do not change target if we still have the same need
				if(indexOfMaxNeed == currentNeedIndex){
					return;
				}
				currentNeedIndex = indexOfMaxNeed;
				
				//Find a target from the need
				LinkedList<Node> possibleTargets = null;
				//HUNGER
				if(indexOfMaxNeed == BasicNeeds.HUNGER.ordinal()){
					fastFood = randomGenerator.nextBoolean();
					if(OSM_Reader.targets.get(OSM_Reader.TargetEnums.FASTFOOD_I.ordinal()).isEmpty()){
						fastFood = false;
					}
					else if(OSM_Reader.targets.get(OSM_Reader.TargetEnums.RESTAURANT_I.ordinal()).isEmpty()){
						fastFood = true;
					}
					//Let's get some fastfood
					if(fastFood){
						possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.FASTFOOD_I.ordinal());
					}
					//Let's get some better food
					else{
						possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.RESTAURANT_I.ordinal());
					}
					newTarget = possibleTargets.get(randomGenerator.nextInt(possibleTargets.size()));
				}
				//CAFE
				else if(indexOfMaxNeed == BasicNeeds.CAFE.ordinal()){
					possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.CAFE_I.ordinal());
					newTarget = possibleTargets.get(randomGenerator.nextInt(possibleTargets.size()));
				}
				//TOILET
				else if(indexOfMaxNeed == BasicNeeds.TOILET.ordinal()){
					possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.TOILET_I.ordinal());
					newTarget = findNearestTarget(possibleTargets);
				}
				//WASTE
				else if(indexOfMaxNeed == BasicNeeds.WASTE.ordinal()){
					possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.WASTE_I.ordinal());
					newTarget = findNearestTarget(possibleTargets);
				}
				//SHOP
				else if(indexOfMaxNeed == BasicNeeds.SHOP.ordinal()){
					possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.SHOP_I.ordinal());
					newTarget = possibleTargets.get(randomGenerator.nextInt(possibleTargets.size()));
				}
				//BANK
				else if(indexOfMaxNeed == BasicNeeds.BANK.ordinal()){
					possibleTargets = OSM_Reader.targets.get(OSM_Reader.TargetEnums.BANK_I.ordinal());
					newTarget = findNearestTarget(possibleTargets);
				}
				//If we only have schedule targets in the .OSM file
				else{
					newTarget = new Node((int)Math.round(renderPosX),(int)Math.round(renderPosY));
				}
			}
		}
		
		targetCollisionPosX = newTarget.getCollisionXPos(scaleCollision);
		targetCollisionPosY = newTarget.getCollisionYPos(scaleCollision);
		findPath();
	}
	
	/**
	 * Returns the current bank need.
	 */
	public float getBankNeed(){
		return basicNeeds.get(BasicNeeds.BANK.ordinal());
	}
	
	/**
	 * Returns the current cafe need.
	 */
	public float getCafeNeed(){
		return basicNeeds.get(BasicNeeds.CAFE.ordinal());
	}
	
	/**
	 * Returns the current collision x-position of Pedestrian.
	 */
	public int getCollisionPosX(){
		return collisionPosX;
	}
	
	/**
	 * Returns the current collision y-position of Pedestrian.
	 */
	public int getCollisionPosY(){
		return collisionPosY;
	}
	
	/**
	 * Returns the color of the Pedestrian.
	 */
	public Color getColor(){
		return myColor;
	}	
	
	/**
	 * Returns the degree of the direction of the pedestrian
	 */
	public double getDegree(){
		return degree;
	}
	
	/**
	 * Returns the current hunger need.
	 */
	public float getHungerNeed(){
		return basicNeeds.get(BasicNeeds.HUNGER.ordinal());
	}
	
	/**
	 * Returns the ID of the pedestrian.
	 */
	public int getID(){
		return myId;
	}
	
	/**
	 * Returns the current render x-position of Pedestrian.
	 */
	public int getPosX(){
		return (int)Math.round(renderPosX);
	}
	
	/**
	 * Returns the current render y-position of Pedestrian.
	 */
	public int getPosY(){
		return (int)Math.round(renderPosY);
	}

	/**
	 * Returns the current shop need.
	 */
	public float getShopNeed(){
		return basicNeeds.get(BasicNeeds.SHOP.ordinal());
	}
	
	/**
	 * Returns the target render x-position of Pedestrian.
	 */
	public int getTargetPosX(){
		return Math.round(targetCollisionPosX / scaleCollision);
	}
	
	/**
	 * Returns the target render y-position of Pedestrian.
	 */
	public int getTargetPosY(){
		return Math.round(targetCollisionPosY / scaleCollision);
	}		
	
	/**
	 * Returns the current toilet need.
	 */
	public float getToiletNeed(){
		return basicNeeds.get(BasicNeeds.TOILET.ordinal());
	}
	
	/**
	 * Returns the current waste need.
	 */
	public float getWasteNeed(){
		return basicNeeds.get(BasicNeeds.WASTE.ordinal());
	}
	
	/**
	 * Checks if we can go along a line between the start position and end position without going over tiles with a higher cost then specified.
	 * @param startX The start position on the x-axis (collision position)
	 * @param startY The start position on the y-axis (collision position)
	 * @param endX The end position on the x-axis (collision position)
	 * @param endY The end position on the y-axis (collision position)
	 * @param cost Maximum allowed cost
	 * @return True if end point can be reached without encountering a tile which exceeds the max cost
	 */
	private boolean isPathOk(int startX, int startY, int endX, int endY, int cost){
		int temp,error,y, stepX, stepY;
		int deltaX, deltaY;
		boolean steep;		

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
				if(outsideBoundary(y,x)) return false;
				
				//Do not allow positive cost changes
				if(collisionMatrix[y][x] > cost || collisionMatrix[y][x] == Integer.MAX_VALUE){
					return false;
				}
			}
			//Not steep
			else{
				if(outsideBoundary(x,y)) return false;
				
				//Do not allow positive cost changes
				if(collisionMatrix[x][y] > cost || collisionMatrix[x][y] == Integer.MAX_VALUE){
					return false;
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
		return true;
	}
	
	/**
	 * This method attempts to linearize the current path.
	 */
	private void linearizePath(){
		//Only check path if it isn't empty
		if(!currentPath.isEmpty()){
			int cost = collisionMatrix[collisionPosX][collisionPosY];
			int maxFoundIndex = -1;
	
			//See how many tiles that we can reach from current tile (with a line)
			for(int i = 0; i < INTERPOLATION_OFFSET && i < currentPath.size()-1; i++){
				if(isPathOk(collisionPosX,collisionPosY,currentPath.get(i).getCollisionXPos(scaleCollision),currentPath.get(i).getCollisionYPos(scaleCollision),cost)){
					maxFoundIndex = i;
				}
				else{
					break;
				}
			}
			
			//Remove tiles up to maxFoundIndex
			if(maxFoundIndex >= 0){
				for(int i = 0; i <= maxFoundIndex; i++){
					currentPath.removeFirst();
				}
			}
	
			//Calculate new interpolation variables
			int deltaX = currentPath.getFirst().getXPos()-getPosX();
			int deltaY = currentPath.getFirst().getYPos()-getPosY();
			interpolationLength = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY,2));
			degree = Math.atan2((double)deltaY, (double)deltaX);
			//Make sure that interpolation is at maximum the length of the interpolation line
			if(PedestriansSimulator.PEDESTRIAN_SPEED > interpolationLength){
				updatePosX = Math.cos(degree)*interpolationLength;
				updatePosY = Math.sin(degree)*interpolationLength;				
			}
			else{
				updatePosX = Math.cos(degree)*PedestriansSimulator.PEDESTRIAN_SPEED;
				updatePosY = Math.sin(degree)*PedestriansSimulator.PEDESTRIAN_SPEED;
			}
			interpolationStep = Math.sqrt(Math.pow(updatePosX, 2) + Math.pow(updatePosY, 2));
		}
	}
	
	/**
	 * Checks if input x and y are outside the boundary box (i.e the collisionMatrix used for the map).
	 * @param x X-coordinate in collisionMatrix
	 * @param y Y-coordinate in collisionMatrix
	 * @return True if outside the boundary box, else false.
	 */
	private boolean outsideBoundary(int x, int y){
		return (x < 0 || x >= collisionMatrix.length || y < 0 || y >= collisionMatrix.length);
	}
	
	/**
	 * Updates the wait time for a schedule event or calls updateNeeds if we are not at a scheduleEvent target.
	 */
	private void updateAtTarget(){
		if(scheduleEvent){
			int currentHour = Frame.clock.getHour();
			int currentMinute = Frame.clock.getMinute();
			currentWaitTime = calculateWaitTime(currentHour,currentMinute,schedule.getFirst().getDuration(),schedule.getFirst().getStartHour());

			schedule.removeFirst();
			scheduleEvent = false;
		}
		else{
			updateNeeds(true);
		}
	}
	
	/**
	 * Update the Pedestrian's needs.
	 */
	private void updateNeeds(boolean atTarget){
		//If at target, then do a special need update
		if(atTarget){
			//HUNGER
			if(currentNeedIndex == BasicNeeds.HUNGER.ordinal()){
				Boolean fastFood = randomGenerator.nextBoolean();
				//Let's get some fastfood
				if(fastFood){
					currentWaitTime = fastFoodTime;
					basicNeeds.set(BasicNeeds.WASTE.ordinal(),basicNeeds.get(BasicNeeds.WASTE.ordinal()) + fastFoodWasteIncrease);
					basicNeeds.set(BasicNeeds.BANK.ordinal(),basicNeeds.get(BasicNeeds.BANK.ordinal()) + fastFoodBankIncrease);
				}
				//Let's get some better food
				else{
					currentWaitTime = restaurantTime;
					basicNeeds.set(BasicNeeds.BANK.ordinal(),basicNeeds.get(BasicNeeds.BANK.ordinal()) + restaurantBankIncrease);
				}
				basicNeeds.set(BasicNeeds.HUNGER.ordinal(),0f);
				basicNeeds.set(BasicNeeds.TOILET.ordinal(),basicNeeds.get(BasicNeeds.TOILET.ordinal()) + foodToiletIncrease);
			}
			//CAFE
			else if(currentNeedIndex == BasicNeeds.CAFE.ordinal()){
				currentWaitTime = cafeTime;
				basicNeeds.set(BasicNeeds.CAFE.ordinal(),0f);
				basicNeeds.set(BasicNeeds.HUNGER.ordinal(),basicNeeds.get(BasicNeeds.HUNGER.ordinal()) - cafeFoodDecrease);
				basicNeeds.set(BasicNeeds.TOILET.ordinal(),basicNeeds.get(BasicNeeds.TOILET.ordinal()) + cafeToiletIncrease);
				basicNeeds.set(BasicNeeds.BANK.ordinal(),basicNeeds.get(BasicNeeds.BANK.ordinal()) + cafeBankIncrease);
			}
			//TOILET
			else if(currentNeedIndex == BasicNeeds.TOILET.ordinal()){
				currentWaitTime = toiletTime;
				basicNeeds.set(BasicNeeds.TOILET.ordinal(), 0f);
			}
			//WASTE
			else if(currentNeedIndex == BasicNeeds.WASTE.ordinal()){
				currentWaitTime = wasteTime;
				basicNeeds.set(BasicNeeds.WASTE.ordinal(), 0f);
			}
			//SHOP
			else if(currentNeedIndex == BasicNeeds.SHOP.ordinal()){
				currentWaitTime = shopTime;
				basicNeeds.set(BasicNeeds.SHOP.ordinal(), 0f);
				basicNeeds.set(BasicNeeds.BANK.ordinal(),basicNeeds.get(BasicNeeds.BANK.ordinal()) + shopBankIncrease);
			}
			//BANK
			else if(currentNeedIndex == BasicNeeds.BANK.ordinal()){
				currentWaitTime = bankTime;
				basicNeeds.set(BasicNeeds.BANK.ordinal(), 0f);
			}
		}
		//Update Basic needs (a bit)
		else{
			basicNeeds.set(BasicNeeds.HUNGER.ordinal(),basicNeeds.get(BasicNeeds.HUNGER.ordinal()) + hungerNeedIncrease);
			basicNeeds.set(BasicNeeds.TOILET.ordinal(),basicNeeds.get(BasicNeeds.TOILET.ordinal()) + toiletNeedIncrease);
			basicNeeds.set(BasicNeeds.CAFE.ordinal(),basicNeeds.get(BasicNeeds.CAFE.ordinal())     + cafeNeedIncrease);
			basicNeeds.set(BasicNeeds.SHOP.ordinal(),basicNeeds.get(BasicNeeds.SHOP.ordinal())     + shopNeedIncrease);
		}
	}	
	
	/**
	 * Updates the position of the pedestrian towards the target position.
	 */
	public void updatePosition(){
		if(sleeping){
			sleeping = !(checkTime(wakeHour,wakeMinute));
			isWalking = !sleeping;
			return;
		}
		
		//Check that we aren't already there (at target)
		if(!(collisionPosX == targetCollisionPosX && collisionPosY == targetCollisionPosY)){
			//Have we moved one collision tile?
			if((interpolationProgress+interpolationStep) >= interpolationLength){
				interpolationProgress = 0;
				updateNeeds(false);
				if(avoiding){
					avoiding = false;
				}
				else{
					//Get the next tile from the path
					Node nextPos = currentPath.poll();
					if(nextPos == null){
						System.err.println("target: " + targetCollisionPosX + "," + targetCollisionPosY);
						System.err.println("current: " + collisionPosX + "," + collisionPosY);
						System.err.println("Path was empty even though Pedestrian was not at target!");
						System.exit(1);
					}
					collisionPosX = nextPos.getCollisionXPos(scaleCollision);
					collisionPosY = nextPos.getCollisionYPos(scaleCollision);
					if(collisionMatrix[collisionPosX][collisionPosY] != Integer.MAX_VALUE) generateTarget();
				}

				if(!currentPath.isEmpty()){
					linearizePath();
					updateRenderPos(false);
				}
			}
			//Update render position
			else{
				updateRenderPos(true);			
			}
		}
		//At target, time to get a new one
		else{
			if(isWalking){
				//Update render position one final time so that the Pedestrian is rendered exactly at the target
				renderPosX = Math.round(targetCollisionPosX / scaleCollision);
				renderPosY = Math.round(targetCollisionPosY / scaleCollision);
				arriveTime = Frame.clock.getNrMinutes();
				updateAtTarget();
				isWalking = false;
			}
			if(doneAtTarget()){
				generateTarget();
				linearizePath();
				isWalking = true;  //Time to get out into the world again
			}
		}
	}
	
	/**
	 * Updates which map cells that the pedestrian currently occupies.
	 */
	public void updateOccupiedCells()
	{
		if(isWalking){		
			//Top left corner of pedestrian
			int cornerCellX = (getPosX()-PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			int cornerCellY = (getPosY()-PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			int cell = cornerCellX + cornerCellY*PedestriansSimulator.NR_CELL_ROW;
			if(cell > 0 && cell < PedestriansSimulator.mapCells.size()){
				PedestriansSimulator.mapCells.get(cell).add(myId);
			}
			
			//Top right corner of pedestrian
			cornerCellX = (getPosX()+PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cornerCellY = (getPosY()-PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cell = cornerCellX + cornerCellY*PedestriansSimulator.NR_CELL_ROW;
			if(cell > 0 && cell < PedestriansSimulator.mapCells.size()){
				PedestriansSimulator.mapCells.get(cell).add(myId);
			}
			
			//Bottom left corner of pedestrian
			cornerCellX = (getPosX()-PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cornerCellY = (getPosY()+PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cell = cornerCellX + cornerCellY*PedestriansSimulator.NR_CELL_ROW;
			if(cell > 0 && cell < PedestriansSimulator.mapCells.size()){
				PedestriansSimulator.mapCells.get(cell).add(myId);
			}
			
			//Bottom right corner of pedestrian
			cornerCellX = (getPosX()+PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cornerCellY = (getPosY()+PedestriansSimulator.PEDESTRIAN_RADIUS)/PedestriansSimulator.CELL_SIZE;
			cell = cornerCellX + cornerCellY*PedestriansSimulator.NR_CELL_ROW;
			if(cell > 0 && cell < PedestriansSimulator.mapCells.size()){
				PedestriansSimulator.mapCells.get(cell).add(myId);
			}
		}
	}
	
	/**
	 * Updates the render position via interpolation.
	 */
	private void updateRenderPos(boolean updateCollision){	
		boolean canInterpolate = true;
		
		int newCollisionPosX = (int)Math.round((renderPosX+updatePosX) * scaleCollision);
		int newCollisionPosY = (int)Math.round((renderPosY+updatePosY) * scaleCollision);
		//Check only at each new tile
		if(!updateCollision || (newCollisionPosX != collisionPosX || newCollisionPosY != collisionPosY)){
			//Avoid interpolating into buildings
			if(!outsideBoundary(newCollisionPosX,newCollisionPosY) && 
			    collisionMatrix[newCollisionPosX][newCollisionPosY] != Integer.MAX_VALUE){
				if(updateCollision){
					collisionPosX = newCollisionPosX;
					collisionPosY = newCollisionPosY;
				}
			}
			else{
				canInterpolate = false;
			}
		}
		
		//Update renderpos if possible
		if(canInterpolate || !avoiding){	
			renderPosX += updatePosX;
			renderPosY += updatePosY;
			interpolationProgress  += interpolationStep;
		}
		else{
			interpolationProgress = interpolationLength;
		}
	}
}