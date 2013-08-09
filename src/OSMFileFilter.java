import java.io.File;

import javax.swing.filechooser.FileFilter;
 
/**
 * This class provides a filter for the file chooser so that only .osm files are visible.
 * 
 * @author Robert Wideberg
 * @version 17-07-2013
 */
public class OSMFileFilter extends FileFilter {
	
	 public final static String osm = "osm";
 
    /**
     * Accept all directories and all osm files.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(osm)){
                    return true;
            } else {
                return false;
            }
        }
 
        return false;
    }
    
    /**
     * Get the extension of a file.
     */  
    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
 
    /**
     * Returns the description of this filter
     */
    public String getDescription() {
        return ".OSM";
    }
}