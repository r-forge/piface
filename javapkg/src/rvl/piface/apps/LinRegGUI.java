package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;
import java.awt.*;

public class LinRegGUI extends Piface {

    private static String
        title = "Linear Regression"; // title of the dialog

    public double k, n, sdx, vif, beta, sderr, alpha, power;     // double variables
    public int tt, opt;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        setBackground(new Color(230,230,230));
        beginSubpanel(1, Color.blue.darker());
//            label("Predictor information");
            slider ("k", "No. of predictors", 1,   1, 8,   1, true, false, true);
            bar("sdx", "SD of x[j]", 1);
            slider ("vif", "VIF[j]", 1,   1, 10, 4, true, false, true);
        endSubpanel();
        bar("alpha", "Alpha", .05);
        checkbox("tt", "Two-tailed", 1);

        newColumn();

        bar("sderr", "Error SD", 1);
        bar("beta", "Detectable beta[j]", 1);
        bar("n", "Sample size", 10);
        beginSubpanel(1, Color.blue.darker());
            interval("power", "Power", .5, 0, 1);
            choice("opt", "Solve for",
                new String[]{"Sample size","Detectable beta[j]"}, 0);
        endSubpanel();

        menuItem("localHelp", "Regression dialog help", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        k = round(max(1,k));
        n = round(max(k+2, n));
        alpha = min(.999, max(.001, alpha));
        if (k>1.5) {
            vif = max(1,vif);
            setVisible("vif", true);
        }
        else {
            vif = 1.0;
            setVisible("vif", false);
        }
        calcPower();
    }

    public void power_changed() {
        double delta, fac;
        power = min(.999, max(.001, power));
        switch (opt) {
            case 0:
                beta = max(.001*sderr/sdx, beta);
                fac = vif * pow(sderr / (beta * sdx), 2);
                for (int i=0; i<3; i++) {
                    delta = T.delta(power, n-k-1, 1-tt, alpha);
                    n = max(k + 1, fac * delta * delta);
                }
                n = round(n);
                break;
            case 1:
                delta = T.delta(power, n-k-1, 1-tt, alpha);
                beta = delta * sqrt(vif / n) * sderr / sdx;
        }
        calcPower();
    }

    private void calcPower() {
        double se = sqrt(vif / n) * sderr / sdx,
            nc = beta / se,
            df = n - k - 1;
        power = T.power (nc, df, 1-tt, alpha);
    }

    public void localHelp() {
        showText(AnovaPicker.class, "LinRegGUIHelp.txt",
            "Power analysis help: Linear regression", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public LinRegGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        new LinRegGUI();
    }

}
