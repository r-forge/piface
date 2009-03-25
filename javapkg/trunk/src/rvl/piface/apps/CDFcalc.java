package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class CDFcalc extends Piface {

    private static String
        title = "Online tables"; // title of the dialog

    public double x, Fx, df1, df2, ncp;     // double variables
    public int dist, RT;        // int variables

    private static final int
        NORMAL = 0,
        TDIST = 1,
        FDIST = 2,
        CHISQ = 3,
        BETA = 4,
        TUKEY = 5,
        BINOMIAL = 6,
        POISSON = 7;

    // Default values of {x, df1, df2} when dist changes
    private static final double dflts[][] = {
        new double[] {0, 0, 1}, // normal
        new double[] {0, 10, .009},  // t
        new double[] {1, 2, 10}, // F
        new double[] {5, 10, .009}, // chisq
        new double[] {.5, 1, 1}, // beta
        new double[] {1, 30, 3}, // tukey
        new double[] {5, 10, .5}, // binomial
        new double[] {5, 10, .009} // Poisson
    };


/**
 * Set up the GUI
 */
    public void gui () {
        choice("dist", "Distribution",
            new String[]{"Normal", "t", "F", "Chi-square", "Beta",
                         "Studentized range", "Binomial", "Poisson"},
                         TDIST);
        slider("x", 0);
        interval("Fx", "F(x)", .5, 0, 1);
        checkbox("RT", "Right tail", 0);

        newColumn();

        beginSubpanel(1, false);
            label("Parameter values...");
            slider("df1", "df", 10);
            slider("df2", 10);
            slider("ncp", 0);
        endSubpanel();
        setVisible("df2", false); // we're starting with TDIST

        menuItem("localHelp", "Using this calculator", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() { // computes new Fx value...
        double cdf = 0;
        switch(dist) {
            case NORMAL: Fx = Normal.cdf(x, df1, df2); break;
            case TDIST: Fx = T.cdf(x, df1, ncp); break;
            case FDIST: Fx = F.cdf(x, df1, df2, ncp); break;
            case CHISQ: Fx = Chi2.cdf(x, df1, ncp); break;
            case BETA: Fx = Beta.cdf(x, df1, df2, ncp); break;
            case TUKEY: Fx = Tukey.cdf(x, df2, df1); break;
            case BINOMIAL: Fx = Binomial.cdf((int)x, (int)df1, df2); break;
            case POISSON: Fx = Poisson.cdf((int)x, df1);
        }
        if (RT > 0)
            Fx = 1 - Fx;
    }

    public void Fx_changed() {
        double p = (RT > 0) ? 1 - Fx : Fx;
        switch(dist) {
            case NORMAL: x = Normal.quantile(p, df1, df2); break;
            case TDIST: x = T.quantile(p, df1, ncp); break;
            case FDIST: x = F.quantile(p, df1, df2, ncp); break;
            case CHISQ: x = Chi2.quantile(p, df1, ncp); break;
            case BETA: x = Beta.quantile(p, df1, df2, ncp); break;
            case TUKEY: x = Tukey.quantile(p, df2, df1); break;
            case BINOMIAL: x = Binomial.quantile(p, (int)df1, df2);
                Fx = Fx = Binomial.cdf((int)x, (int)df1, df2);
                if (RT > 0) Fx = 1 - Fx;
                break;
            case POISSON: x = Poisson.quantile(p, df1);
                Fx = Poisson.cdf((int)x, df1);
                if (RT > 0) Fx = 1 - Fx;
        }

    }

    public void dist_changed() {
        setVisible("df2", dist!=TDIST && dist!=CHISQ && dist!=POISSON);
        setVisible("ncp", dist==TDIST || dist==FDIST || dist==CHISQ || dist==BETA);
        relabel("df1","df"); relabel("df2","df2");
        switch(dist) {
            case NORMAL:
                relabel("df1", "mu"); relabel("df2", "sigma"); break;
            case BETA:
                relabel("df1", "alpha"); relabel("df2", "beta"); break;
            case TUKEY:
                relabel("df1", "df"); relabel("df2", "k"); break;
            case BINOMIAL:
                relabel("df1", "n"); relabel("df2", "p"); break;
            case POISSON:
                relabel("df1", "lambda"); break;
            case FDIST:
                relabel("df1","df1");
        }
        x = dflts[dist][0];
        df1 = dflts[dist][1];
        df2 = dflts[dist][2];
        ncp = 0;
        click();
    }

    public void RT_changed() {
        if (RT == 0) relabel("Fx", "F(x)");
        else relabel("Fx", "1 - F(x)");
        Fx = 1 - Fx;
    }


    public void localHelp() {
        showText(AnovaPicker.class, "CDFcalcHelp.txt",
            "How to use online tables", 25, 60);
    }


/**
 * The following code makes it self-standing...
 */
    public CDFcalc() {
        super(title);
    }
    public static void main(String argv[]) {
        CDFcalc app = new CDFcalc();
        app.setStandalone(true);
    }

}
