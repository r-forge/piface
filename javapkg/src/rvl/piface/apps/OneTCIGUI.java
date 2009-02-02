package rvl.piface.apps;

import java.awt.*;
import rvl.piface.*;
import rvl.stat.dist.*;

public class OneTCIGUI extends Piface {

    private static String
        title = "CI for a mean"; // title of the dialog

    public double conf, N, n, Sigma, ME, tCrit;     // double variables
    public int isFinite;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        beginSubpanel(2);
            checkbox("isFinite", "Finite population", 1);
            field("N", "N", 1000, 8, 6);
        endSubpanel();
        choice("conf", "Confidence", new double[]{.90,.95,.98,.99,.995},1);
        bar("Sigma", 1.0);
        bar("ME","Margin of Error", .20);
        bar("n", 25);

        menuItem("localHelp", "This dialog", helpMenu);

        n = 25;
        conf = .95;
        n_changed();
    }

/**
 * Default event handler - handles changes to sigma, conf, etc.
 */
    public void click() {
        tCrit = Normal.quantile (1 - (1-conf)/2);
        ME = max(ME, .001*Sigma);
        for (int i=0; i<3; i++) {
            n = tCrit * Sigma / ME;
            n *= n;
            if (isFinite == 1)
                n = n / (1 + n/N);
            n = max(2, n);
            tCrit = T.quantile(1 - (1-conf)/2, n-1);
        }
    }

    public void n_changed() {
        N = max(2, round(N));
        n = max(2, round(n));
        tCrit = T.quantile(1 - (1-conf)/2, n-1);
        ME = tCrit * Sigma / sqrt(n);
        if (isFinite == 1)
            ME *= sqrt(1 - n/N);
    }

    public void isFinite_changed() {
        setVisible("N", isFinite==1);
        click();
    }

    public void localHelp() {
        showText(OneTCIGUI.class, "OneTCIGUIHelp.txt",
             "Help: One-sample CI for a mean", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public OneTCIGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        OneTCIGUI app = new OneTCIGUI();
        app.setStandalone(true);
    }

}
