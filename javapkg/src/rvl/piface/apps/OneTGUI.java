package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;
import java.awt.*;

public class OneTGUI extends Piface {

    private static String
        title = "One-sample (or paired) t test"; // title of the dialog

    public double
        sigma, n,
        diff, alpha, df, power, // for GUI
        delta;  // extras
    public int eqs, eqn, tt, opt;       // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        bar("sigma",1);
        bar("diff", "True |mu - mu_0|", .5);
        bar("n",25);
        beginSubpanel(1,true);
            interval("power", .5, 0, 1);
            choice("opt", "Solve for",
                new String[]{"n","Effect size"}, 0);
        endSubpanel();
        beginSubpanel(2);
            choice("alpha", new double[]{.005,.01,.02,.05,.1,.2}, 3);
            checkbox("tt","Two-tailed",1);
        endSubpanel();

        menuItem("localHelp", "t test info", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        n = max(round(n), 2);
        delta = sqrt(n) * diff / sigma;
        power = T.power(delta, n - 1, 1 - tt, alpha);
    }

    public void power_changed () {
        sw:
        switch (opt) {
            case 0: // solve for n
                if (abs(diff) < .001 * sigma) return;
                for (int i = 0; i < 3; i++) {
                    double n0 = n;
                    delta = T.delta (power, n - 1, 1 - tt, alpha);
                    n = pow (delta * sigma / diff, 2);
                    if (Double.isNaN(n)) {
                        n = n0;
                        break sw;
                    }
                }
                break;
            case 1: // solve for diff
                double diff0 = diff;
                delta = T.delta(power, n - 1, 1 - tt, alpha);
                diff = sigma * delta / sqrt(n);
                if (Double.isNaN(diff)) diff = diff0;
                break;
        }
        click();
    }

    public void localHelp() {
        showText(AnovaPicker.class, "OneTGUIHelp.txt",
            "Power analysis help: One-sample t test", 25, 60);
    }


/**
 * The following code makes it self-standing...
 */
    public OneTGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        new OneTGUI();
    }

}
