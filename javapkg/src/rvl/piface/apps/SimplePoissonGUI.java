package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class SimplePoissonGUI extends Piface {

    private static String
        title = "Power of a Simple Poisson Test";

    public double lambda0, lambda, alpha, power, size, lower, upper, n;
    public int alt;

/**
 * Set up the GUI
 */
    public void gui () {
        bar ("lambda0", 1);
        choice("alt","alternative",
            new String[]{"lambda < lambda0","lambda != lambda0","lambda > lambda0"}, 1);
        field ("alpha", .05);
        label ("Boundaries of acceptance region");
        beginSubpanel(2,false);
            otext ("lower", "lower", 0);
            otext ("upper", "upper", 0);
        endSubpanel();
        otext ("size", "size", 0);
        bar ("lambda", 1);
        bar ("n", 50);
        ointerval ("power", 0, 0, 1);
        menuItem("localHelp","This dialog", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        double a =  alt == 1 ? alpha / 2 : alpha;
        size = power = 0;
        if (alt < 2) {
            lower = Poisson.quantile (a, n * lambda0) + 1;
            size += Poisson.cdf ((int)lower - 1, n * lambda0);
            power += Poisson.cdf ((int)lower - 1, n * lambda);
        }
        if (alt > 0) {
            upper = Poisson.quantile (1 - a, n * lambda0);
            size += 1 - Poisson.cdf ((int)upper + 1, n * lambda0);
            power += 1 - Poisson.cdf ((int)upper + 1, n * lambda);
        }
    }

/**
 *
 */
    public void alt_changed() {
        setVisible ("lower", alt < 2);
        setVisible ("upper", alt > 0);
        click();
    }

/**
 * Help menu
 */
    public void localHelp() {
        showText(SimplePoissonGUI.class, "SimplePoissonGUIHelp.txt",
            "Power analysis help: Simple Poisson test", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public SimplePoissonGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        SimplePoissonGUI app = new SimplePoissonGUI();
        app.setStandalone(true);
    }

}
