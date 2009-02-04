package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class TEquivGUI extends Piface {

    private static String
        title = "Two-sample t test for equivalence"; // title of the dialog

    public double alpha, power, tol, diff, sigma, n;     // double variables
    //    public int ;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
    bar("tol", "Maximum negligible difference", .5);
    bar("diff", "True difference, |mu1-mu2|", .1);
    bar("sigma", "True SD of each population", 1);
    bar("n", "n for each sample", 25);
    bar("alpha", .05);
    ointerval("power", 0, 0, 1);
    }

/**
 * Default event handler
 */
    public void click() {
    n = max(2, round(n));
    double se = sigma * sqrt(2 / n);
    power = T.powerEquiv(diff, tol, se, 2*(n-1), alpha);
    }

/**
 * The following code makes it self-standing...
 */
    public TEquivGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        TEquivGUI app = new TEquivGUI();
        app.setStandalone(true);
    }

}
