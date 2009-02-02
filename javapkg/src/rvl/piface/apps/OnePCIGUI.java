package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class OnePCIGUI extends Piface {

    private static String
        title = "CI for a proportion"; // title of the dialog

    public double conf, N, n, pi, Sigma = .5, ME, zCrit = 1.96;     // double variables
    public int isFinite, worstCase;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        beginSubpanel(2);
            checkbox("isFinite", "Finite population", 1);
            field("N", "N", 1000, 8, 6);
            checkbox("worstCase", "Worst case", 1);
            field("pi", .5);
        endSubpanel();
        choice("conf", "Confidence", new double[]{.90,.95,.98,.99,.995},1);
        bar("ME","Margin of Error", .09297);
        bar("n", 25);

        menuItem("localHelp", "This dialog", helpMenu);

        n = 100;
        conf = .95;
    }

/**
 * Default event handler - handles changes to sigma, conf, etc.
 */
    public void click() {
        zCrit = Normal.quantile(1 - (1-conf)/2);
        worstCase = (abs(pi - .5) < 1e-12) ? 1 : 0;
        Sigma = sqrt(pi*(1 - pi));
        ME = max(ME, .001*Sigma);
        for (int i=0; i<3; i++) {
            n = zCrit * Sigma / ME;
            n *= n;
            if (isFinite == 1)
                n = 1 + n / (1 + n/N);
            n = max(2, n);
        }
    }

    public void worstCase_changed() {
        if (worstCase == 1) pi = .5;
        click();
        // Note: if worstCase==0 && pi==.5, click() resets worstCase
    }

    public void n_changed() {
        N = max(2, round(N));
        n = max(2, round(n));
        zCrit = Normal.quantile(1 - (1-conf)/2);
        ME = zCrit * Sigma / sqrt(n - 1);
        if (isFinite == 1)
            ME *= sqrt(1 - n/N);
    }

    public void isFinite_changed() {
        setVisible("N", isFinite==1);
        click();
    }

    public void localHelp() {
        showText(OnePCIGUI.class, "OnePCIGUIHelp.txt",
             "Help: One-sample CI for a mean", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public OnePCIGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        new OnePCIGUI();
    }

}
