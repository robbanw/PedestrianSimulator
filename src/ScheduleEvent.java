/**
 * A ScheduleEvent is an event which starts and ends 
 * at a specified time at a specified place (Node) on the map.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 12-07-2013
 */
public class ScheduleEvent {
	private int startHour;
	private int duration;
	private Node target;
	
	/**
	 * Initializes a SceduleEvent with input parameters.
	 * @param startHour Hour at which the event starts
	 * @param duration  Duration of the event in minutes 
	 * @param target    Node at where the event is
	 */
	public ScheduleEvent(int startHour, int duration, Node target){
		this.startHour = startHour;
		this.duration = duration;
		this.target = target;
	}

	/**
	 * Returns the hour at which the event ends.
	 */
	public int getDuration(){
		return duration;
	}
	
	/**
	 * Returns the hour at which the event starts.
	 */
	public int getStartHour(){
		return startHour;
	}
	
	/**
	 * Returns the target node of the event (i.e where on the map that it will take place).
	 * @return Node object
	 */
	public Node getTarget(){
		return target;
	}
}
