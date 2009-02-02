package rvl.piface.apps;

import java.awt.*;

import rvl.piface.*;
import rvl.stat.dist.*;

public class OnePGUI extends Piface {

    private static String
        title = "Sample size for one proportion";     // title of the dialog
    private Component sizeComp;           // component that shows size

    public double p0,p,n,Alpha,Power,size;         // double variables
    public int Method,alt;            // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        interval("p0","Null value (p0)", .5, 0, 1);
        interval("p","Actual value (p)", .6, 0, 1);
        bar("n","Sample size", 50);
        beginSubpanel(2,false);
            choice("alt","Alternative", new String[]{"p < p0","p != p0","p > p0"}, 1);
            choice("Alpha", new double[]{.005,.01,.02,.05,.1,.2}, 3);
            choice("Method", new String[]{"Exact","Normal approx","Beta approx"}, 1);
            otext("size", "Size", .06);
        endSubpanel();
        interval("Power", 0, 0, 1);

        sizeComp = (Component)getComponent("size");
        sizeComp.setVisible(false);

        menuItem("localHelp","This dialog", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        double pc, se0, se;
        int tail = alt - 1;
        n = max(2, round(n));
        p0 = min ( max (p0, .01), .99);
        p = min ( max (p, .01), .99);
        switch (Method) {
            case 0: // exact
                double info[] = Binomial.power (p0, p, (int)n, tail, Alpha);
                Power = info[0];
                size = info[1];
                break;
            case 1: // normal
                se0 = sqrt(p0 * (1 - p0) / n);
                se = sqrt(p * (1 - p) / n);
                Power = nPower (p0, se0, p, se, tail, Alpha);
                size = Alpha;
                break;
            case 2: // beta
                Power = bPower ((n-1)*p0, (n-1)*(1-p0),
                    (n-1)*p, (n-1)*(1-p), tail, Alpha);
                size = Alpha;
                break;
        }
    }

    public void Method_changed() {
        sizeComp.setVisible(Method==0);
        click();
    }


//power of a normal test when sigma is not constant
    double nPower (double mu0, double sig0,
        double mu1, double sig1, int tail, double alpha)
    {
        double cv, cv1, cv2;

        if (tail > 0) {
            cv = Normal.quantile (1 - alpha, mu0, sig0);
            return 1 - Normal.cdf (cv, mu1, sig1);
        }
        else if (tail < 0) {
            cv = Normal.quantile (alpha, mu0, sig0);
            return Normal.cdf (cv, mu1, sig1);
        }
        else {
            cv1 = Normal.quantile (alpha/2, mu0, sig0);
            cv2 = Normal.quantile (1 - alpha/2, mu0, sig0);
            return 1 + Normal.cdf (cv1, mu1, sig1)
                     - Normal.cdf (cv2, mu1, sig1);
        }
    }

// power function using beta
    double bPower (double a0, double b0, double a1, double b1,
        int tail, double alpha)
    {
        double cv, cv1, cv2;
        if (tail > 0) {
            cv = Beta.quantile (1 - alpha, a0, b0);
            return 1 - Beta.cdf (cv, a1, b1);
        }
        else if (tail < 0) {
            cv = Beta.quantile (alpha, a0, b0);
            return Beta.cdf (cv, a1, b1);
        }
        else {
            cv1 = Beta.quantile (alpha/2, a0, b0);
            cv2 = Beta.quantile (1 - alpha/2, a0, b0);
            return 1 + Beta.cdf (cv1, a1, b1)
                     - Beta.cdf (cv2, a1, b1);
        }
    }

    public void Power_changed() {
        Power = min(.99, max(Alpha, Power));  // set bounds
        PifaceAux aux = new PifaceAux("n", "Power", this);
        aux.closedMin = true;
        aux.xMin = 2;
        aux.xeps = .5;
    // (used for debugging) // aux.verbose=true;
        n = solve(aux, Power, n, 20);
        click();
    }

    public void localHelp() {
        showText(AnovaPicker.class, "OnePGUIHelp.txt",
            "Power analysis help: Test of one proportion", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
        public OnePGUI() {
                super(title);
        }
        public static void main(String argv[]) {
                new OnePGUI();
        }

}
