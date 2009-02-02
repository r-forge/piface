/**
 * Standalone GUI to choose and run a class selected from a list
 * The list of classes is given in a standard property file
 * This is a non-applet version of PiPicker
 */

package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;
import rvl.util.*;
import java.util.*;
import java.io.*;

public class PiPickerSA extends Piface {

    private static String
    appList = "PiPicker.txt",  // file name of application info
        title = "Piface Application Selector";          // title of the dialog

    public int sel;       // index of selected application

    public Vector classes;
    public String pkg;     // class package name to append to class names

/**
 * Set up the GUI
 */
    public void gui () {
        Properties pl = new Properties();
    Vector labels = new Vector();
    classes = new Vector();
         try {
            InputStream in = getClass().getResourceAsStream(appList);
            pl.load(in);
            pkg = pl.getProperty("package","rvl.piface.apps");
            String keys = pl.getProperty("keys");
            StringTokenizer st = new StringTokenizer(keys,",");
            while (st.hasMoreTokens()) {
                String cls = st.nextToken();
                classes.addElement (cls);
                labels.addElement (pl.getProperty(cls,"???"));
            }
       }
        catch (IOException ioe) {
            Utility.error(ioe);
        }
    catch(Exception e) {
        Utility.error(e);
    }
    int nc = labels.size();
        String choices[] = new String[nc];
    for (int i=0; i<nc; i++)
        choices[i] = (String) labels.elementAt(i);
    label ("Type of analysis");
    choice ("sel", "", choices, 0);
    beginSubpanel(2);
        label("                           ");
            button ("runSelApp", "Run dialog");
    endSubpanel();
    }

    protected void afterSetup() {
        optMenu.remove(4); // separator
        optMenu.remove(3); // Cohen dialog
        optMenu.remove(2); // Retro dialog
        optMenu.remove(1); // separator
        optMenu.remove(0); // Graph dialog
        helpMenu.remove(1); // separator
        helpMenu.remove(0); // GUI help
    }


/**
 * Default event handler
 */
    public void click () {
    // (do nothing)
    }

/**
 * Action when the button is clicked
 */
    public void runSelApp () {
        String cname = (String) classes.elementAt(sel);
        try {
            Object obj = (Class.forName(pkg + "." + cname)).newInstance();
            ((Piface)obj).setMaster(this);
        }
        catch(Exception exc) {
            Utility.warning(exc,true);
        }
    }

/**
 * The following code makes it self-standing...
 */
    public PiPickerSA() {
        super (title);
    }
    public static void main(String argv[]) {
    if (argv.length > 0)  appList = argv[0];

        PiPickerSA ppsa = new PiPickerSA();
        ppsa.setStandalone(true);
        new AboutPiface();
    }

}
