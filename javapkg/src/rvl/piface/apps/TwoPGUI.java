package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class TwoPGUI extends Piface {

    private static String
        title = "Test of equality of two proportions"; // title of the dialog

    public double p1,p2,n1,n2,Alpha,Power;     // double variables
    public int eq, alt, cc;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        beginSubpanel(1,true);
            interval("p1",.5, 0, 1);
            interval("p2",.6, 0, 1);
        endSubpanel();
        beginSubpanel(1,true);
            bar("n1",100);
            bar("n2",100);
            checkbox("eq","Equal ns",1);
        endSubpanel();
        beginSubpanel(1,true);
            bar("Alpha", .05);
            ointerval("Power", .5, 0, 1);
        endSubpanel();
        beginSubpanel(2);
            checkbox("cc", "Continuity corr.", 1);
            choice("alt","Alternative",
                new String[]{"p1 < p2","p1 != p2","p1 > p2"}, 1);
            //*****choice("Alpha", new double[]{.005,.01,.02,.05,.1,.2}, 3);
        endSubpanel();

        menuItem("localHelp", "This dialog", helpMenu);
   }

/**
 * Default event handler
 */
    public void click() {
        n1 = max (2, round(n1));
        if (eq == 1) n2 = n1;
        double bound1 = 5/n1, bound2 = 5/n1;
        p1 = min ( max (p1, bound1), 1 - bound1);  // restrict to np >= 5
        p2 = min ( max (p2, bound2), 1 - bound2);  // and n(1-p) >= 5
        calcPower();
   }

    public void n2_changed() {
        n2 = max(round(n2), 2);
        if (eq == 1) n1 = n2;
        calcPower();
    }

    private void calcPower() {
        double pbar = (n1*p1 + n2*p2)/(n1 + n2),
            SE0 = sqrt (pbar*(1-pbar)*(1/n1 + 1/n2)),
            SE1 = sqrt (p1*(1-p1)/n1 + p2*(1-p2)/n2),
            diff = p1 - p2,
            corr = min (abs(diff), .5 * (1/n1 + 1/n2));
        Alpha = max (.000001, min(Alpha, .999999));
        if (cc == 1)
            diff = diff > 0 ? diff - corr : diff + corr;
        Power = Normal.power (diff/SE0, alt-1, Alpha, SE1/SE0);
    }

    public void localHelp() {
        showText(AnovaPicker.class, "TwoPGUIHelp.txt",
            "Power analysis help: Comparing two proportions", 25, 60);
    }
/**
 * The following code makes it self-standing...
 */
    public TwoPGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        TwoPGUI app = new TwoPGUI();
        app.setStandalone(true);
    }

}
