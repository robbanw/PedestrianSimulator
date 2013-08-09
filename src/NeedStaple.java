import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

/**
 * This class paints and handles a need progress bar in the simulation.
 * 
 * @author Christoffer Wiss
 * @version 12-07-2013
 */
@SuppressWarnings("serial")
public class NeedStaple extends JLabel{
	private JLabel progressBar;
	private JLabel statusBackground;
	private float currentNeedLevel;
	private Color needLow = Color.GREEN;
	private Color needHigh = Color.RED;
	
	private Image icon;
	
	/**
	 * Constructor for class NeedStaple.
	 * @param icon 		The icon displayed next to the need bar
	 * @param width		Width of the bar
	 * @param height	Height of the bar
	 */
	public NeedStaple(Image icon, int width, int height){
		this.icon = icon;
		setPreferredSize(new Dimension(width,height));
		setSize(getPreferredSize());
		setHorizontalAlignment(JLabel.LEFT);
		progressBar = new JLabel();
		statusBackground = new JLabel();
		setVerticalAlignment(JLabel.TOP);
		
		statusBackground.setBackground(Color.BLACK);
		statusBackground.setPreferredSize(new Dimension(Math.round(this.getWidth()-icon.getWidth(null)*4.0f), this.getHeight()));
		statusBackground.setSize(statusBackground.getPreferredSize());
		statusBackground.setLocation(Math.round(this.getWidth()/2-statusBackground.getWidth()/2), 0);
		statusBackground.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(176,176,176), new Color(128,128,128)));
		statusBackground.setOpaque(true);
		
		progressBar.setBackground(needLow);
		progressBar.setLocation(statusBackground.getInsets().left, statusBackground.getInsets().bottom);
		progressBar.setSize(0, statusBackground.getHeight()-statusBackground.getInsets().bottom*2);
		progressBar.setOpaque(true);

		statusBackground.add(progressBar);
		add(statusBackground);
	}
	
	/**
     * Draws all components that the need staple uses.
     */
    @Override
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
    	paintChildren(g);
    	Graphics2D graphics = (Graphics2D) g;
    	graphics.drawImage(icon,Math.round(icon.getWidth(null)/2), Math.round(this.getSize().height/2 - icon.getHeight(null)/2),null);
    	g.dispose(); //Release resources allocated to this graphics object
    }
    
	/**
	 * Updates the progress bar so that it matches the new input.
	 * @param newLevel A floating number, the new progress of the displayed need.
	 */
	public void updateNeedLevel(float newLevel){
		currentNeedLevel = Math.max(0,Math.min(newLevel,1.0f)); //Make sure that progress is between 0 and 1
		progressBar.setBackground(Frame.colorLinearInterpolation(needLow, needHigh, currentNeedLevel));
		progressBar.setSize(Math.round(statusBackground.getWidth()*currentNeedLevel),statusBackground.getHeight()-statusBackground.getInsets().bottom*2);
	}
}
