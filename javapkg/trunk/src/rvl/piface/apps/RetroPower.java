package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class RetroPower extends Piface {

    private static String
        title = "Retrospective Power"; // title of the dialog

    public double Power;     // double variables
    public int didRej;       // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        beginSubpanel(1, false);
            label("Was the test \"significant\"?");
            hradio("didRej","", new String[]{"No","Yes"}, 0);
        endSubpanel();
        ointerval("Power", "Retrospective power", 0, 0, 1);
        menuItem("localHelp","This dialog", helpMenu);
    }

    protected void afterSetup() {
        optMenu.remove(4); // separator
        optMenu.remove(3); // Cohen dialog
        optMenu.remove(2); // Retro dialog
        optMenu.remove(1); // separator
        optMenu.remove(0); // Graph dialog
        helpMenu.remove(2); // GUI help
    }

/**
 * Default event handler
 */
    public void click() {
        Power = didRej;
    }

/**
 * Help dialog
 */
    public void localHelp() {
        showText(RetroPower.class, "RetroPowerHelp.txt",
            "Why retrospective power is silly", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public RetroPower() {
        super(title);
    }
    public static void main(String argv[]) {
        RetroPower app = new RetroPower();
        app.setStandalone(true);
    }

}
