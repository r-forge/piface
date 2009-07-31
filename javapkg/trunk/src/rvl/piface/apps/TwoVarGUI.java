package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;

public class TwoVarGUI extends Piface {

    private static String
        title = "Test of equality of two variances"; // title of the dialog

    public double var1,var2,n1,n2,Alpha,Power;     // double variables
    public int eq, alt, cc;        // int variables

/**
 * Set up the GUI
 */
    public void gui () {
        beginSubpanel(2,true);
            bar("n1",100);
            bar("var1","Variance 1", 1.5);
            bar("n2",100);
            bar("var2","Variance 2", 1.0);
            checkbox("eq","Equal ns",1);
            choice("alt","Alternative",
                new String[]{"Var1 < Var2","Var1 != Var2","Var1 > Var2"}, 1);
        endSubpanel();
        beginSubpanel(2,true);
            bar("Alpha", .05);
            ointerval("Power", .5, 0, 1);
        endSubpanel();

        menuItem("localHelp", "This dialog", helpMenu);
   }

/**
 * Default event handler
 */
    public void click() {
        n1 = max (2, round(n1));
        if (eq == 1) n2 = n1;
        calcPower();
   }

    public void n2_changed() {
        n2 = max(round(n2), 2);
        if (eq == 1) n1 = n2;
        calcPower();
    }

    private void calcPower() {
        double df1, df2, ratio;
        if (alt >= 1) {
            df1 = n1-1; df2 = n2-1; ratio = var1/var2;
        }
        else {
            df1 = n2-1; df2 = n1-1; ratio = var2/var1;
        }
        double alp = alt==1 ? Alpha/2 : Alpha,
            critval = F.quantile(1 - alp, df1, df2);
        Power = 1 - F.cdf(critval/ratio, df1, df2);
        if (alt==1) {
            critval = F.quantile(1 - alp, df2, df1);
            Power += 1 - F.cdf(critval*ratio, df2, df1);
        }
    }

    public void localHelp() {
        showText(AnovaPicker.class, "TwoVarGUIHelp.txt",
            "Power analysis help: Comparing two variances", 25, 60);
    }
/**
 * The following code makes it self-standing...
 */
    public TwoVarGUI() {
        super(title);
    }
    public static void main(String argv[]) {
        TwoVarGUI app = new TwoVarGUI();
        app.setStandalone(true);
    }

}
