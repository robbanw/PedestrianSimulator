import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

/**
 * Creates a JFrame object and adds the simulator to it.
 * 
 * @author Christoffer Wiss & Robert Wideberg
 * @version 09-08-2013
 */
@SuppressWarnings("serial")
public class Frame extends JFrame implements ActionListener {
	public static int SIM_WINDOW_LENGTH = 800;	//Resolution of the simulation (cubic e.g. 800 -> 800x800)
	public static boolean DEBUG = false;
	public static boolean USE_PRECALCULATED_PATHS = true; //Paths can be pre-calculated for optimization purposes
	public static final String VERSION= "1.00";
	private PedestriansSimulator simulator;
	private JDialog simulatorInfo; //Dialog for showing information about simulator
	
	//Standard variables
	private final int NORMAL_FONT_SIZE    = 12;
	private final int DISPLAY_FONT_SIZE   = 16;
	private final Color NORMAL_FONT_COLOR = Color.BLACK;
	
	//Clock
	public static Clock clock;	
	private final String CLOCK_FONT_DIRECTORY = "data/digital-7-mono.ttf";
	private final int CLOCK_FONT_SIZE = 40;
	private final int START_HOUR = 5;
	private final int START_MINUTE = 45;
	
	//East panel
	public static final int EAST_PANEL_WIDTH = 150;
	public static final int SOUTH_EAST_PANEL_HEIGHT = 170;
	private final int NEED_STAPLE_WIDTH = EAST_PANEL_WIDTH;
	public static JLabel clockSpeedDisplay;
	public static JLabel runStatusDisplay;
	public static JComboBox filterList;
	public static final String FILTER_NORMAL = "Normal";
	public static final String FILTER_COST = "Cost";
	public static final String FILTER_CELL = "Heat";
	public static JRadioButton renderTargetYesBtn;
	public static JRadioButton renderTargetNoBtn;
	public static JRadioButton renderAntialiasingYesBtn;
	public static JRadioButton renderAntialiasingNoBtn;
	private JButton decreaseButton;
	public static JButton playButton;
	private JButton increaseButton;
	public static JButton loadNewMapButton;
	public static JButton aboutButton;
	public static JButton infoButton;
	public static final String pauseSymbol = "||";
	private final String backArrow = Character.toString((char)0x25C4);
	public static final String forwardArrow = Character.toString((char)0x25BA);
	public static NeedStaple hungerNeedBar;
	public static NeedStaple cafeNeedBar;
	public static NeedStaple toiletNeedBar;
	public static NeedStaple wasteNeedBar;
	public static NeedStaple bankNeedBar;
	public static NeedStaple shopNeedBar;
	
	//Need icons
	private final String HUNGER_ICON_NAME = "restaurant";
	private final String CAFE_ICON_NAME = "cafe";
	private final String TOILET_ICON_NAME = "toilets";
	private final String WASTE_ICON_NAME = "waste_basket";
	private final String BANK_ICON_NAME = "bank";
	private final String SHOP_ICON_NAME = "shop";
	
	/**
	 * Constructs necessary programs (simulator) and initializes them.
	 */
	public Frame(){
        setResizable(false); //Turn of resizable as fast as possible to avoid weird Ubuntu related resizing of frame 
        //Create containing panel for simulator info dialog
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        //Create and position icons and text within panel for simulator info dialog
        /**Normal view**/
        JPanel subContentPanel = new JPanel();
        
        JLabel textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Map color guide");
        textLabel.setFont(new Font("Arial", Font.BOLD, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);

        //Park color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        JLabel colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.PARK_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Park.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Wood color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.WOOD_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Wood.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Water color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.WATER_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Water.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Footway color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.FOOTWAY_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Footway.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Steps color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.STEPS_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Steps.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Cycleway color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.CYCLEWAY_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Cycleway.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Service road color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.SERVICE_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Service road.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Structure road color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.BUILDING_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Building.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Railway platform color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.RAILWAY_PLATFORM_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Railway platform.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Railway color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(OSM_Reader.RAILWAY_COLOR);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Railway.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Road color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(Color.BLACK);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Road/Wall (medium/small line).");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        /**Path cost section**/
        subContentPanel = new JPanel();
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Path cost filter color guide");
        textLabel.setFont(new Font("Arial", Font.BOLD, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);

        //Red collision color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(PedestriansSimulator.highestCollisionColor);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - High cost, unfavorable for pedestrians to walk over.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);
        contentPanel.add(subContentPanel);
        
        //Green collision color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(PedestriansSimulator.lowestCollisionColor); 
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Low cost, favorable for pedestrians to walk over.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        contentPanel.add(subContentPanel);
        
        //White collision color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(Color.WHITE);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Neutral cost, not the worst but not the best.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);    
        contentPanel.add(subContentPanel);
        
        //Black collision color and explanation
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
       
        colorLabel = new JLabel();
        colorLabel.setBackground(Color.BLACK);
        colorLabel.setPreferredSize(new Dimension(20,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(" - Unwalkable, pedestrians cannot walk here.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        contentPanel.add(subContentPanel);
        
        /**Map cell section**/
        subContentPanel = new JPanel();
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Map heat filter guide");
        textLabel.setFont(new Font("Arial", Font.BOLD, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        contentPanel.add(subContentPanel);
        
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Shows current subdivsion of map into cells.");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        contentPanel.add(subContentPanel);
        
        subContentPanel = new JPanel();
        subContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Color of cell is scaled by amount of pedestrians from");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        
        colorLabel = new JLabel();
        colorLabel.setBackground(PedestriansSimulator.lowestCollisionColor);
        colorLabel.setPreferredSize(new Dimension(12,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);  
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("to");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        
        colorLabel = new JLabel();
        colorLabel.setBackground(PedestriansSimulator.highestCollisionColor);
        colorLabel.setPreferredSize(new Dimension(12,12));
        colorLabel.setOpaque(true);
        
        subContentPanel.add(colorLabel);  
        
        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText(".");
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        textLabel.setForeground(Color.BLACK);
        
        subContentPanel.add(textLabel);  
        contentPanel.add(subContentPanel);
        
        JOptionPane options = new JOptionPane(contentPanel, JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION);
        
        simulatorInfo = options.createDialog(this,"Simulator info");
        simulatorInfo.pack();
		
        //Create and setup the clock used by the simulation
		//Loading clock font
		try {
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(CLOCK_FONT_DIRECTORY)));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        clock = new Clock(START_HOUR,START_MINUTE);
        clock.setFont(new Font("Digital-7 Mono", Font.BOLD, CLOCK_FONT_SIZE));
        clock.setPreferredSize(new Dimension(SIM_WINDOW_LENGTH,CLOCK_FONT_SIZE));
        clock.setOpaque(true);
        clock.setBackground(Color.BLACK);
        clock.setForeground(Color.GREEN);
        clock.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(176,176,176), new Color(128,128,128)));
		
        //Main East panel
  		JPanel mainEastPanel = new JPanel();
  		mainEastPanel.setLayout(new FlowLayout());
  		mainEastPanel.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,SIM_WINDOW_LENGTH-clock.getHeight()));
        
        //East panel
		JPanel eastPanel = new JPanel();
		eastPanel.setBackground(Color.LIGHT_GRAY);
		eastPanel.setLayout(new FlowLayout());
		eastPanel.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(176,176,176), new Color(128,128,128)));  
		eastPanel.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,SIM_WINDOW_LENGTH-clock.getHeight() - SOUTH_EAST_PANEL_HEIGHT));
		
		//South east panel
		JPanel southEastPanel = new JPanel();
		southEastPanel.setBackground(Color.LIGHT_GRAY);
		southEastPanel.setLayout(new FlowLayout());
		southEastPanel.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(176,176,176), new Color(128,128,128)));  
		southEastPanel.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,SOUTH_EAST_PANEL_HEIGHT));
		
		//Add components to the east panel
		Image needBarIcon = null;
		clockSpeedDisplay = new JLabel();
		runStatusDisplay  = new JLabel();
		
		//Loads images for need bar icons
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + HUNGER_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		hungerNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + CAFE_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		cafeNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + TOILET_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		toiletNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + SHOP_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		shopNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + BANK_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		bankNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		try {
			needBarIcon = ImageIO.read(new File(OSM_Reader.ICON_FILE_PATH + WASTE_ICON_NAME + ".png"));
		} catch (IOException e) {
			System.err.println(HUNGER_ICON_NAME + " tag has no related icon");
		}
		wasteNeedBar = new NeedStaple(needBarIcon,NEED_STAPLE_WIDTH,DISPLAY_FONT_SIZE);
		
		//Creates a display that shows the run status of the simulation
		runStatusDisplay.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		runStatusDisplay.setBackground(Color.BLACK);
		runStatusDisplay.setForeground(Color.GREEN);
		runStatusDisplay.setOpaque(true);
		runStatusDisplay.setText("running");
		runStatusDisplay.setPreferredSize(new Dimension(DISPLAY_FONT_SIZE*4,DISPLAY_FONT_SIZE));
		runStatusDisplay.setHorizontalAlignment(JLabel.CENTER);
		
		//Creates a display that shows the simulation speed
		clockSpeedDisplay.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		clockSpeedDisplay.setBackground(Color.BLACK);
		clockSpeedDisplay.setForeground(Color.GREEN);
		clockSpeedDisplay.setOpaque(true);
		clockSpeedDisplay.setText("1x");
		clockSpeedDisplay.setPreferredSize(new Dimension(DISPLAY_FONT_SIZE*5+5,DISPLAY_FONT_SIZE));
		clockSpeedDisplay.setHorizontalAlignment(JLabel.CENTER);
		
		//Creates buttons to control simulation speed
		decreaseButton = new JButton();
		decreaseButton.setText(backArrow + backArrow);
		decreaseButton.setFocusable(false);
		decreaseButton.addActionListener(this);
		playButton = new JButton();
		playButton.setText("||");
		playButton.setPreferredSize(new Dimension(75,25));
		playButton.setFocusable(false);
		playButton.addActionListener(this);
		increaseButton = new JButton();
		increaseButton.setText(forwardArrow + forwardArrow);
		increaseButton.setFocusable(false);
		increaseButton.addActionListener(this);
		
		//Creates drop down menu for filters
		String[] filterOptions = {FILTER_NORMAL, FILTER_COST, FILTER_CELL};
		filterList = new JComboBox(filterOptions);
		filterList.addActionListener(this);
		filterList.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		filterList.setBackground(Color.BLACK);
		filterList.setForeground(Color.GREEN);
		filterList.setOpaque(true);
		filterList.setFocusable(false);
		
		//Creates radio buttons for toggling target lines
		ButtonGroup targetGroup = new ButtonGroup();
		renderTargetYesBtn = new JRadioButton("Yes", false);
		renderTargetNoBtn = new JRadioButton("No", true);
		renderTargetYesBtn.addActionListener(this);
		renderTargetNoBtn.addActionListener(this);
		renderTargetYesBtn.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		renderTargetYesBtn.setBackground(Color.BLACK);
		renderTargetYesBtn.setForeground(Color.GREEN);
		renderTargetYesBtn.setFocusable(false);
		renderTargetNoBtn.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		renderTargetNoBtn.setBackground(Color.BLACK);
		renderTargetNoBtn.setForeground(Color.GREEN);
		renderTargetNoBtn.setFocusable(false);
		targetGroup.add(renderTargetYesBtn);
		targetGroup.add(renderTargetNoBtn);
		
		//Creates radio buttons for toggling anti aliasing
		targetGroup = new ButtonGroup();
		renderAntialiasingYesBtn = new JRadioButton("Yes", true);
		renderAntialiasingNoBtn = new JRadioButton("No", false);
		renderAntialiasingYesBtn.addActionListener(this);
		renderAntialiasingNoBtn.addActionListener(this);
		renderAntialiasingYesBtn.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		renderAntialiasingYesBtn.setBackground(Color.BLACK);
		renderAntialiasingYesBtn.setForeground(Color.GREEN);
		renderAntialiasingYesBtn.setFocusable(false);
		renderAntialiasingNoBtn.setFont(new Font("Digital-7 Mono", Font.BOLD, DISPLAY_FONT_SIZE));
		renderAntialiasingNoBtn.setBackground(Color.BLACK);
		renderAntialiasingNoBtn.setForeground(Color.GREEN);
		renderAntialiasingNoBtn.setFocusable(false);
		targetGroup.add(renderAntialiasingYesBtn);
		targetGroup.add(renderAntialiasingNoBtn);
		
        loadNewMapButton = new JButton("Load new map");
        loadNewMapButton.addActionListener(this);
        loadNewMapButton.setFocusable(false);
        loadNewMapButton.setPreferredSize(new Dimension(EAST_PANEL_WIDTH - 10,50));
        
        aboutButton = new JButton("About");
        aboutButton.addActionListener(this);
        aboutButton.setFocusable(false);
        aboutButton.setPreferredSize(new Dimension(EAST_PANEL_WIDTH - 10,50));
        
        infoButton = new JButton("Simulator info");
        infoButton.addActionListener(this);
        infoButton.setFocusable(false);
        infoButton.setPreferredSize(new Dimension(EAST_PANEL_WIDTH - 10,50));
        
		//Create labels and add components to panel
		JLabel label = new JLabel();
        label.setText("Current speed:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        eastPanel.add(clockSpeedDisplay);
        eastPanel.add(decreaseButton);
        eastPanel.add(increaseButton);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("Status:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        eastPanel.add(runStatusDisplay);
        eastPanel.add(playButton);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("Filter:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        eastPanel.add(filterList);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("Render targets:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        eastPanel.add(renderTargetYesBtn);
        eastPanel.add(renderTargetNoBtn);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("Use Anti-aliasing:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        eastPanel.add(renderAntialiasingYesBtn);
        eastPanel.add(renderAntialiasingNoBtn);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(EAST_PANEL_WIDTH,NORMAL_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setText("Needs:");
        label.setFont(new Font("Arial", Font.BOLD, NORMAL_FONT_SIZE));
        label.setForeground(NORMAL_FONT_COLOR);
        eastPanel.add(label);
        
        ArrayList<LinkedList<Node>> targets = Main.osm_reader.getTargets();
        
        //Only add the need bars if the needs can be fulfilled
        if(!(targets.get(OSM_Reader.TargetEnums.FASTFOOD_I.ordinal()).isEmpty()) || !(targets.get(OSM_Reader.TargetEnums.RESTAURANT_I.ordinal()).isEmpty())){
        	eastPanel.add(hungerNeedBar);
        }
        if(!targets.get(OSM_Reader.TargetEnums.CAFE_I.ordinal()).isEmpty()){
        	eastPanel.add(cafeNeedBar);
        }
        if(!targets.get(OSM_Reader.TargetEnums.TOILET_I.ordinal()).isEmpty()){
        	eastPanel.add(toiletNeedBar);
        }
        if(!targets.get(OSM_Reader.TargetEnums.SHOP_I.ordinal()).isEmpty()){
        	eastPanel.add(shopNeedBar);
        }
        if(!targets.get(OSM_Reader.TargetEnums.BANK_I.ordinal()).isEmpty()){
        	eastPanel.add(bankNeedBar);
        }
        if(!targets.get(OSM_Reader.TargetEnums.WASTE_I.ordinal()).isEmpty()){
        	eastPanel.add(wasteNeedBar);
        }
        
        southEastPanel.add(loadNewMapButton);
        southEastPanel.add(aboutButton);
        southEastPanel.add(infoButton);
        
        mainEastPanel.add(eastPanel);
        mainEastPanel.add(southEastPanel);
        
        //Remove the default vertical padding that all panels use
        ((FlowLayout)mainEastPanel.getLayout()).setVgap(0);
        
        this.pack();
        setVisible(true); //Set visible so that we may obtain the insets
        Insets insets = this.getInsets(); //Make sure to account for taskbar size


		setSize(SIM_WINDOW_LENGTH + EAST_PANEL_WIDTH + insets.left, SIM_WINDOW_LENGTH + clock.getPreferredSize().height + insets.top);
		
        setTitle("Pedestrian Simulator v." + VERSION);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());
        
        add(mainEastPanel,BorderLayout.EAST);
        add(clock, BorderLayout.NORTH);
        simulator = new PedestriansSimulator();
        add(simulator, BorderLayout.CENTER); 
        simulator.requestFocusInWindow();
	}
	
	@Override
	/**
	 * Handles action events from drop down menus and toggle buttons.
	 */
    public void actionPerformed(ActionEvent e) {
		//Filter drop down events
    	if(filterList == e.getSource()){
	        String filter = (String)filterList.getSelectedItem();
	        if(filter.equals("Normal")){
	        	PedestriansSimulator.renderCollision = false;
	        	PedestriansSimulator.renderCells = false;
	        }
	        else if(filter.equals("Cost")){
	        	PedestriansSimulator.renderCollision = true;
	        	PedestriansSimulator.renderCells = false;
	        }
	        else{
	        	PedestriansSimulator.renderCollision = false;
	        	PedestriansSimulator.renderCells = true;
	        }
    	}
    	//Target line toggle events
    	else if(renderTargetYesBtn == e.getSource()){
    		PedestriansSimulator.renderTargets = true;
    	}
    	else if(renderTargetNoBtn == e.getSource()){
    		PedestriansSimulator.renderTargets = false;
    	}
    	//Toggle anti-aliasing
    	else if(renderAntialiasingYesBtn == e.getSource()){
    		PedestriansSimulator.setAntialiasing(true);
    	}
    	else if(renderAntialiasingNoBtn == e.getSource()){
    		PedestriansSimulator.setAntialiasing(false);
    	}
    	//Simulation speed buttons events
       	else if(decreaseButton == e.getSource()){
    		PedestriansSimulator.decreaseSpeed();
    	}
    	else if(increaseButton == e.getSource()){
    		PedestriansSimulator.increaseSpeed();
    	}
       	else if(playButton == e.getSource()){
    		PedestriansSimulator.toggleRunStatus();
    	}
    	//Load map
       	else if(loadNewMapButton == e.getSource()){
       		reset();
    		setVisible(false);
    		dispose();
    		Main.main(null);
    		return;
    	}
    	//About
       	else if(aboutButton == e.getSource()){
       		JOptionPane.showMessageDialog(this, "Pedestrian Simulator v." + VERSION + " - 2013\nCreated by:\nRobert Wideberg & Christoffer Wiss as part of their Bachelor's thesis in Computer Science.\nAt KTH (Royal Institute of Technology) in Stockholm, Sweden.\n\nUses:\nStyle-7 font (Sizenko Alexander)\nIcons from www.sjjb.co.uk/mapicons/", "About", JOptionPane.INFORMATION_MESSAGE);
       	}
    	//Legend
       	else if(infoButton == e.getSource()){
            simulatorInfo.setVisible(true);
       	}
    	simulator.requestFocusInWindow();
    }
    
	/** 
     * Linear Interpolation between two colors
     * 
     * @param c1 The first color (start, lowest value color)
     * @param c2 The other color (end, highest value color)
     * @param distance The weight, a number between 0 and 1 (distance between c1 to c2)
     * @return A new color, a mix between color c1 and c2 
     */ 
     public static Color colorLinearInterpolation(Color c1, Color c2, float distance)
     {
         return new Color((int)((1 - distance) * c1.getRed() + distance * c2.getRed()),
                          (int)((1 - distance) * c1.getGreen() + distance * c2.getGreen()),
                          (int)((1 - distance) * c1.getBlue() + distance * c2.getBlue()));
     }
     
     /**
      * Resets all radiobuttons and simulator settings to default values.
      */
     private void reset(){
 		if(simulator != null) simulator.stopTimer();	//This is necessary since the timer will keep ticking otherwise causing the simulator to double its speed (crazy I know)
   		clock.setToMinSpeed();
   		filterList.setSelectedItem(Frame.FILTER_NORMAL);
   		renderTargetNoBtn.setSelected(true);
   		renderAntialiasingYesBtn.setSelected(true);
   		simulator.deselectPedestrian();
   		PedestriansSimulator.PEDESTRIAN_SPEED = Math.round(10000f/Clock.minuteLength);
     }
}
