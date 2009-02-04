package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class RsquareGUI extends Piface {

    private static String
        title = "Power of a Test of R-square";

    public double rho2, n, alpha, power, preds;

/**
 * Set up the GUI
 */
    public void gui () {
        field ("alpha", .05);
        bar ("rho2", "True rho^2 value", .1);
        bar ("n", "Sample size", 50);
        bar ("preds", "No. of regressors", 1);
        ointerval ("power", 0, 0, 1);
        menuItem("localHelp","This dialog", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        preds = preds < 1 ? 1 : round(preds);
        int p = (int)preds + 1;
        n = max(n, preds + 1);
        rho2 = min(rho2, .999);
        alpha = max(.0001, min(.5, alpha));
        double critval = Rsquare.quantile (1-alpha, n, p);
        power = 1 - Rsquare.cdf(critval, n, p, rho2);
    }


/**
 * Help menu
 */
    public void localHelp() {
        showText(RsquareGUI.class, "RsquareGUIHelp.txt",
            "Power analysis help: Generic Chi^2 test", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public RsquareGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        RsquareGUI app = new RsquareGUI();
        app.setStandalone(true);
    }

}
