package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;
import java.awt.*;

public class TwoTGUI extends Piface {

    private static String
        title = "Two-sample t test (general case)"; // title of the dialog

    public double
        sigma1, sigma2, n1, n2,
        diff, thresh, alpha, df, power, // for GUI
        v1, v2, delta, mult, saveN1, saveN2;  // extras
    public int eqs, alloc, tt, prevTT, opt, equiv, rocMeth;


/**
 * Set up the GUI
 */
    public void gui () {
        setBackground(new Color(230,230,230));
        beginSubpanel(1, Color.blue.darker());
            bar("sigma1",1);
            bar("sigma2",1);
            checkbox("eqs", "Equal sigmas", 1);
        endSubpanel();
        filler();
        beginSubpanel(1, Color.blue.darker());
            bar("n1",25);
            bar("n2",25);
            choice("alloc", "Allocation",
           new String[]{"Independent","Equal","Optimal"}, 1);
        endSubpanel();

        newColumn();

        beginSubpanel(2);
            checkbox("tt","Two-tailed",1);
            //choice("alpha", new double[]{.005,.01,.02,.05,.1,.2}, 3);
            field("alpha", "Alpha", .05);
            checkbox("equiv","Equivalence",0);
            field("thresh", "Threshold", 1);
        endSubpanel();
        otext("df", "Degrees of freedom", n1+n2-2);
        filler();
        bar("diff", "True difference of means", .5);
        beginSubpanel(1, Color.blue.darker());
            interval("power", "Power", .5, 0, 1);
            choice("opt", "Solve for",
                new String[]{"Sample size","Diff of means"}, 0);
        endSubpanel();

        menuCheckbox("rocMeth", "Use integrated power", 0);

        prevTT = tt;
        equiv_changed();

        menuItem("localHelp", "t test info", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        n1 = max(2, round(n1));
        if (equiv == 1) tt = 1;
        if (eqs == 1) sigma2 = sigma1;
        if (alloc == 1) //xxxxxxxxxx if (eqs == 1 || alloc == 1)
        n2 = n1;
        else if (alloc == 2)
        n2 = max(2, round(n1 * sigma2 / sigma1));
        sattPower();
    }

    public void sigma2_changed() {
        if (eqs == 1) sigma1 = sigma2;
        if (alloc == 2)
            n2 = max(2, round(n1 * sigma2 / sigma1));
        sattPower();
    }

    public void n2_changed() {
    n2 = max(2, round(n2));
        if (alloc == 1) //xxxxxx if (eqs == 1 || alloc == 1)
        n1 = n2;
    else if (alloc == 2)
        n1 = max(2, round(n2 * sigma1 / sigma2));
        sattPower();
    }

    public void sattPower() {
        v1 = sigma1 * sigma1 / n1;
        v2 = sigma2 * sigma2 / n2;
        double se = sqrt(v1 + v2);
        delta = diff / se;
        df = (eqs==1) ? (n1 + n2 - 2)
            : (v1 + v2) * (v1 + v2)
            / ( v1 * v1 / (n1 - 1) + v2 * v2 / (n2 - 1) );
        if (equiv == 0)
            power = (rocMeth == 0) ?
                T.power(delta, df, 1 - tt, alpha)
              : T.rocArea(delta, df, 1 - tt);
        else
            power = (rocMeth == 0) ?
                T.powerEquiv(diff, thresh, se, df, alpha)
              : T.rocEquiv(diff, thresh, se, df);
    }

    public void power_changed () {
        if (equiv == 1 || rocMeth == 1) {
            power_changed_numerical();
            return;
        }
        // We've got a more efficient method for non-equiv tests
        // when the criterion is power.
        switch (opt) {
            case 0:     // solve for n
                double pwr = power;
                diff = max(diff, .01*(sigma1 + sigma2));
                for (int i=0; i<3; i++) {
                    delta = T.delta(pwr, df, 1 - tt, alpha);
                    mult = (v1 + v2) * delta * delta / diff / diff;
                    n1 *= mult;
                    n2 *= mult;
                    sattPower();
                }
                n1 = max(round(n1), 2);
                n2 = max(round(n2), 2);
                sattPower();
                break;
            case 1:     // solve for diff
                delta = T.delta(power, df, 1 - tt, alpha);
                diff = delta * sqrt(v1 + v2);
                sattPower();
                break;
        }
    }

    public void power_changed_numerical() {
        switch(opt) {
            case 0:
                saveN1 = n1;
                saveN2 = n2;
                PifaceAux aux = new PifaceAux("mult","power",this);
                aux.xMin = max(2/n1, 2/n2);
                aux.closedMin = true;
                aux.xeps = max(.5/n1, .5/n2);
                mult = solve(aux, power, 1, .1);
                n1 = max(round(n1 * mult), 2);
                n2 = max(round(n2 * mult), 2);
                break;
            case 1:
                PifaceAux aux2 = new PifaceAux("diff","power",this);
                aux2.xeps = .005*(sigma1 + sigma2);
                diff = Math.max(diff, .1*(sigma1+sigma2));
                diff = solve(aux2, power, diff, .1*diff);
                break;
        }
        sattPower();
    }

    public void mult_changed() {
        n1 = saveN1 * mult;
        n2 = saveN2 * mult;
        sattPower();
    }

    public void localHelp() {
        showText(AnovaPicker.class, "TwoTGUIHelp.txt",
            "Power analysis help: Two-sample t test", 25, 60);
    }

    public void equiv_changed() {
        setVisible("thresh", equiv==1);
        if (equiv==1) {
            prevTT = tt;
            tt = 1;
        }
        else {
            tt = prevTT;
        }
        sattPower();
    }

    public void rocMeth_changed() {
        if (rocMeth==1)
            relabel("power","Integrated power");
        else
            relabel("power","Power");
        setVisible("alpha", rocMeth==0);
        sattPower();
    }


/**
 * The following code makes it self-standing...
 */
    public TwoTGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        new TwoTGUI();
    }

}
