package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class SimpleChi2GUI extends Piface {

    public double proChi2, proN, n, df, Alpha, Power;

/**
 * Set up the GUI
 */
    public void gui() {
        beginSubpanel(1,false);
            label("Prototype data");
            beginSubpanel(2);
                field("proChi2", "Chi2*", 10);
                field("proN", "n*", 100);
            endSubpanel();
        endSubpanel();
        label("Study parameters");
        beginSubpanel(2);
            field("df", 3);
            field("Alpha", .05);
        endSubpanel();
        bar("n", 50);
        interval("Power", 0, 0, 1);
        menuItem("localHelp","This dialog", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        n = max(round(n), 2);
        df = max(round(df), 1);
        Power = Chi2.power(n * proChi2 / proN, df, Alpha);
    }

/**
 * Event handler for changes in power
 */
    public void Power_changed() {
        n = Chi2.lambda(Power, df, Alpha) * proN / proChi2;
        click();
    }

/**
 * Constructor
 */
    public SimpleChi2GUI() {
        super("Chi-Square Power");
    }

/**
 * Help menu
 */
    public void localHelp() {
        showText(AnovaPicker.class, "SimpleChi2GUIHelp.txt",
            "Power analysis help: Generic Chi^2 test", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public static void main(String argv[]) {
        new SimpleChi2GUI();
    }

}
