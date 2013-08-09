import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * This class handles the clock element in the simulation.
 * 
 * @author Robert Wideberg & Christoffer Wiss
 * @version 12-07-2013
 */
@SuppressWarnings("serial")
public class Clock extends JLabel implements ActionListener{
	private final int speedLimit = 64; //How many times faster can the clock speed become? (should be a factor of two e.g 2x, 4x...)
	private long nrOfMinutes = 0;      //Nr of simulator minutes since the start of the program
	private int currentMinute;
	private int currentHour;
	public static final int originalMinuteLength = 3200;
	public static int minuteLength = originalMinuteLength; //The length of a minute in the simulation (ms)
	public static int speed = 1;                           //Current speed factor of simulation (e.g. 1x, 2x, 4x...)
	private Timer minuteTimer;
	
	/**
	 * Initializes the Clock to a start time.
	 * @param startHour Hour to which the Clock starts at
	 * @param startMinute Minute to which the Clock starts at
	 */
	public Clock(int startHour, int startMinute){
		minuteTimer = new Timer(minuteLength, this);
		minuteTimer.start();
		setHorizontalAlignment(JLabel.CENTER);
		setVerticalAlignment(JLabel.TOP);
		setIconTextGap(0);
		currentHour   = startHour;
		currentMinute = startMinute;
	}
	
    /**
     * Used as update method (will run each time timer has passed its interval).
     */
	@Override
	public void actionPerformed(ActionEvent event) {	
		nrOfMinutes++;
		currentMinute++;
		
		if(currentMinute >= 60){
			currentMinute = 0;
			currentHour++;
		}
		if(currentHour >= 24){
			currentHour = 0;
		}
		
    	String minuteString = ""  + currentMinute;
    	String hourString   = "" + currentHour;
    	
    	//Pad minutes & hours with a leading 0 if single digit
    	if(currentMinute < 10){
    		minuteString = "0" + minuteString;
    	}
    	if(currentHour < 10){
    		hourString = "0" + hourString;
    	}    	
    	
    	setText(hourString + ":" + minuteString);
	}
	
	/**
	 * Decrements the speed twofold.
	 */
	public void decrementSpeed(){
		if(speed > 1){
			speed /= 2;
			setMinuteLength(originalMinuteLength/speed);
		}
	}
	
	/**
	 * Returns the current hour.
	 * @return int representing the current hour
	 */
	public int getHour(){
		return currentHour;
	}
	
	/**
	 * Returns the current minute.
	 * @return int representing the current minute
	 */
	public int getMinute(){
		return currentMinute;
	}
	
	/**
	 * Returns the number of minutes since the start of the simulator.
	 * @return long representing the nr of minutes that have passed since the start
	 */
	public long getNrMinutes(){
		return nrOfMinutes;
	}
	
	/**
	 * Increments the speed twofold.
	 */
	public void incrementSpeed(){
		if(speed < speedLimit){
			speed *= 2;
			setMinuteLength(originalMinuteLength/speed);
		}
	}
	
	/**
	 * Resets the minutelength to its original length.
	 */
	private void resetMinuteLength(){
		setMinuteLength(originalMinuteLength);
	}
	
	/**
	 * Sets the minutelength to a new length.
	 * @param newMinuteLength New minute length (in ms)
	 */
	private void setMinuteLength(int newMinuteLength){
		minuteLength = newMinuteLength;
		minuteTimer.setDelay(minuteLength);
		minuteTimer.setInitialDelay(0);
		minuteTimer.restart();
		Frame.clockSpeedDisplay.setText(speed+"x");
	}
	
	/**
	 * Sets the clock to its highest speed.
	 */
	public void setToMaxSpeed(){
		speed = speedLimit;
		setMinuteLength(originalMinuteLength/speed);
		
	}
	
	/**
	 * Sets the clock to its lowest speed (i.e 1x speed).
	 */
	public void setToMinSpeed(){
		speed = 1;
		resetMinuteLength();
	}
	
	/**
	 * Starts the clock.
	 */
	public void startTime(){
		minuteTimer.start();
		Frame.runStatusDisplay.setText("running");
	}
	
	/**
	 * Stops the clock.
	 */
	public void stopTime(){
		minuteTimer.stop();
		Frame.runStatusDisplay.setText("paused");
	}
}
