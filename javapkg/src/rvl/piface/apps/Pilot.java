package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class Pilot extends Piface {

    private static String
        title = "Pilot study"; // title of the dialog

    public double risk, pctUnder, df;     // double variables
    // public int ;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        bar ("pctUnder","Percent by which N is under-estimated", 20);
        bar ("risk","Risk of exceeding this percentage", .1);
        bar ("df", "d.f. for error in pilot study", 80);
        menuItem("localHelp", "Pilot study help", helpMenu);
    }

    protected void afterShow() {
        // setSize(300, getSize().height);
        // This works, but doesn't resize the sliders within.  Need to investigate further.
    }

/**
 * Default event handler
 */
    public void click() {
        df = round(df);
        risk = Chi2.cdf((1-.01*pctUnder)*df, df);
    }

/**
 * Handler for risk
 */
    public void risk_changed() {
        df = solve("df", "risk", risk, df, .1*max(10,df));
        click();
    }

/**
 * Help for this GUI
 */
    public void localHelp() {
        showText(AnovaPicker.class, "PilotHelp.txt",
            "Help: Pilot study", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public Pilot() {
        super(title);
    }
    public static void main(String argv[]) {
        new Pilot();
    }

}
