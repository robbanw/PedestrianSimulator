import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
 
/**
 * Main class of the Pedestrian Simulator. The map is selected and loaded into the simulator here.
 * 
 * @author Robert Wideberg
 * @version 09-08-2013
 */
@SuppressWarnings("serial")
public class Main extends JPanel implements ActionListener, PropertyChangeListener {
 
    private JButton startButton;
    public static OSM_Reader osm_reader;
    
	//Map loader
	private JFileChooser fc;
	private JButton selectButton;
	private String path;
	private JFrame frame;
	private JProgressBar progressBar;
	private JLabel label;
 
    public Main(JFrame frame) {
        super(new BorderLayout());
 
        this.frame = frame;
        
        //Create the UI.
        label = new JLabel("<html>Welcome to the Pedestrian Simulator.<br>Please select a map:</html>");
        
        startButton = new JButton("Load");
        startButton.setEnabled(false);
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        //Create a file chooser
        fc = new JFileChooser("data");
        OSMFileFilter osm_filter = new OSMFileFilter();
        fc.addChoosableFileFilter(osm_filter);
        fc.setFileFilter(osm_filter);
        fc.setAcceptAllFileFilterUsed(false);
 
        //Create the select button.
        selectButton = new JButton("Browse...");
        selectButton.setFocusable(false);
        selectButton.addActionListener(this);
        
        JPanel panel = new JPanel();
        panel.add(selectButton);
        panel.add(startButton);
        panel.add(progressBar);
 
        add(label, BorderLayout.PAGE_START);
        add(panel, BorderLayout.WEST);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
 
 
    /**
     * Invoked when the user presses any of the buttons.
     */
    public void actionPerformed(ActionEvent evt) {
    	if(startButton == evt.getSource()){
    		label.setText("Loading map...");
    		progressBar.setVisible(true);
    		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	        osm_reader = new OSM_Reader();
	        osm_reader.setFilename(path);
	        osm_reader.addPropertyChangeListener(this);
	        osm_reader.execute();
	        startButton.setEnabled(false);
	        selectButton.setEnabled(false);
    	}
       	else if(selectButton == evt.getSource()){
	        int returnVal = fc.showOpenDialog(this);
	 
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            path = fc.getSelectedFile().getPath();
	            label.setText("Selected map: " + fc.getSelectedFile().getName());
	            startButton.setEnabled(true);
	        }
    	}
    }
 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
    	if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            if(osm_reader.isDone()){
            	frame.setVisible(false);
            	frame.dispose();
            	new Frame();
            }
        } 
    }
 
    /**
     * Create the GUI and show it.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Pedestrian Simulator v." + Frame.VERSION);
        frame.setResizable(false);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(400,100);

        //Create and set up the content pane.
        JComponent newContentPane = new Main(frame);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}