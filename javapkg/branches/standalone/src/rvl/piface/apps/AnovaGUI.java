package rvl.piface.apps;

import rvl.piface.*;
import rvl.stat.dist.*;
import rvl.stat.anova.*;
import rvl.awt.*;
import rvl.util.*;

import java.awt.*;

public class AnovaGUI extends Piface implements PiListener
{

    public double n[], effSD[], power[], alpha;     // double variables
    public int random[];          // int variables

    private Model model;
    protected AnovaCompGUI acg;    // linked ANOVACompGUI dalog
    protected boolean ignoreActions = false;  // switch used in piAction()
    protected AnovaHelper helper = null;  // linked AnovaHelper dialog

/**
 * Set up the GUI
 */
    public void gui () {
        n = new double[model.nFac()];
        if (effSD == null) {
			effSD = new double[model.nTerm()];
			for (int i=0; i<model.nTerm(); i++) effSD[i] = 1.0;
		}
        power = new double[model.nTerm()];
        random = new int[model.nFac()];

        String fr[] = new String[] { "Fixed", "Random" };

        beginSubpanel(1,false); //new Color(0,0,100));
        for (int i=0; i<model.nFac(); i++) {
            beginSubpanel(1,true);
                Factor f = model.getFac(i);
                String lab = f.isRandom()
					? "n[" + f.getName() + "]"
					: "levels[" + f.getName() + "]";
                if (lab.equals("n[Residual]")) lab = "Replications";
                hradio("random[" + i + "]", f.getName(), fr, f.isRandom() ? 1 : 0);
                bar("n[" + i + "]", lab, (double)f.getLevels());
            endSubpanel();
        }
        endSubpanel();

        newColumn();
        boolean split = (model.nTerm() > 10);
        int splitPoint = model.nTerm() / 2;

//        filler();
        beginSubpanel(1,false);
            panel.setBackground(new Color(255,255,200));
            beginSubpanel(2,true);
                for (int i=0; i<model.nTerm(); i++) {
                    if (split && (i == splitPoint)) {
                        endSubpanel();
                        endSubpanel();
                        newColumn();
                        beginSubpanel(1,false);
                        panel.setBackground(new Color(255,255,200));
                        beginSubpanel(2,true);
                    }
                    Term t = model.getTerm(i);
                    bar("effSD[" + i + "]", "SD[" + t.getName() + "]", effSD[i]);
                    if (i < model.nTerm() - 1)
                        interval("power[" + i + "]", "Power["
                            + t.getName() + "]", .5, 0, 1);
                    else choice("alpha","Significance level",
                        new double[] {.001,.005,.01,.02,.05,.10,.20}, 4);
                }
            endSubpanel();
        endSubpanel();

        menuItem("linkHelper", "Effect SD helper");
        menuItem("linkComps", "Contrasts/Comparisons");
        menuItem("showEMS", "Show EMS");
        menuItem("report", "Report");
        menuItem("help", "ANOVA help", helpMenu);
    }

/**
 * Default event handler
 */
    public void click() {
        power = model.power(effSD, alpha);
        for (int i=0; i<model.nTerm(); i++)
            if (power[i] < 0) power[i] = Double.NaN;
    }

    public void random_changed() {
        if (acg != null) {
            errmsg("WARNING: Changing between fixed and random may invalidate");
            errmsg("results in the associated comparisons/contrasts dialog!");
		}
        Factor f = model.getFac(sourceIndex);
        f.setRandom(random[sourceIndex]==1);
        random[sourceIndex] = f.isRandom() ? 1 : 0;
        click();
    }

    public void n_changed() {
        n[sourceIndex] = max(1, round(n[sourceIndex]));
        model.getFac(sourceIndex).setLevels((int)n[sourceIndex]);
        for (int i=0; i<model.nFac(); i++)
            n[i] = model.getFac(i).getLevels();
        click();
    }

/**
* We need to override parent method because n[] values
* are also stored and used by the model object!
*/
    public synchronized void restoreVars(double saved[]) {
        super.restoreVars(saved);
        for (int i=0; i<model.nFac(); i++) {
            model.getFac(i).setLevels((int)n[i]);
        }
    }

    public void power_changed(){
        double c[] = model.getPowerInfo(sourceIndex), v;
        Term term = model.getTerm(sourceIndex);

        if (!term.isRandom()) {
            double lambda = F.lambda(power[sourceIndex],
                c[2], c[3], alpha);
            v = c[1] * lambda / c[2] / c[0];
        }
        else {
            v = (c[1] / c[0]) * (F.quantile(1 - alpha, c[2], c[3])
                / F.quantile(1 - power[sourceIndex], c[2], c[3]) - 1);
        }
        if (v > 0) effSD[sourceIndex] = sqrt(v);
        click();
    }

    public void linkHelper() {
        if (helper == null) {
            helper = new AnovaHelper(this);
            helper.setMaster(this);
		}
        else
            helper.show();
    }

    public void linkComps() {
        acg = new AnovaCompGUI(getTitle(), model, effSD);
        addPiListener(acg);
        acg.addPiListener(this);
        acg.ag = this;
    }


    public void piAction(String varName) {
        if (ignoreActions || acg == null) return;
        setVar(varName, acg);
        callMethodFor(varName);
        updateVars();
        acg.ignoreActions = true;
        notifyListeners(varName);
        acg.ignoreActions = false;
    }


    public void close() {
		if (helper != null)
		    helper.close();
        if (acg != null) {
            removePiListener(acg);
            acg.removePiListener(this);
            acg.ag = null;
        }
        dispose();    //super.close();
    }

    public void showEMS() {
        showText(model.EMSString(), "Expected mean squares", 25, 50);
    }

    public void report() {
        showText(reportString(), "Power-analysis report", 25, 60);
    }

    public void help() {
        showText(AnovaGUI.class, "AnovaGUIHelp.txt",
        "Help for ANOVA power analysis", 25, 60);
    }

    public String reportString() {
        StringBuffer b = new StringBuffer();
        b.append("Below is a summary of the factors and terms for the\n");
        b.append("balanced ANOVA model under study.  Further explanation is\n");
        b.append("given after the results.\n\n");
        b.append(getTitle() + "\n\n");
        b.append(Utility.format("Factor",14) + "\tlevels\n");
        for (int i=0; i<model.nFac(); i++) {
            Factor f = model.getFac(i);
            b.append(Utility.format(f.getName(),14) + "\t  " + f.getLevels());
            if (f.isRandom())
                b.append("\trandom\n");
            else
                b.append("\tfixed\n");
        }
        b.append(Utility.format("\nTerm",22) + "\tdf\tStdDev\tPower\n");
        for (int i=0; i<model.nTerm(); i++) {
            Term t = model.getTerm(i);
            b.append(Utility.format(t.getName(),22) + "\t" + t.df() + "\t");
            b.append(Utility.format(effSD[i],4));
            if (power[i] >= 0.0)
                b.append("\t" + Utility.format(power[i],4) + "\n");
            else
                b.append("\n");
        }
    b.append("\nAlpha = " + alpha + "\n");
        b.append("\n\nNOTES:\n\n");
        b.append("Effect sizes for each term are expressed as a standard\n");
        b.append("deviation.  In the case of a random effect, the standard\n");
        b.append("deviation is the square root of its variance component.\n");
        b.append("For a fixed effect, the standard deviation is the square\n");
        b.append("root of the sum of squares of the model effects (in the\n");
        b.append("constrained model), divided by the degrees of freedom for\n");
        b.append("the term in question.\n\n");

        b.append("In both cases, the expected mean square for a term is\n\n");

        b.append("\tK*(StdDev)^2 + EMS(ET)\n\n");

        b.append("where K is the number of observations at each distinct\n");
        b.append("level of the term, and EMS(ET) is the expected mean square\n");
        b.append("for the error term for testing the significance of the\n");
        b.append("term.  Please note that the error term ET is based on the\n");
        b.append("random (and mixed) effects in the model, and that the\n");
        b.append("computed power depends on both the effect size shown for\n");
        b.append("the term under test, but also the standard deviations shown\n");
        b.append("for all random terms involved in the error term.\n\n");

        b.append("This power analysis is based on the \"unrestricted\"\n");
        b.append("parameterization of the balanced mixed ANOVA model.  Where\n");
        b.append("necessary, error terms are constructed using linear\n");
        b.append("combinations of mean squares, and the degrees of freedom\n");
        b.append("for the denominator of the approximate F test are computed\n");
        b.append("using the Satterthwaite method.\n");
        return b.toString();
    }

    public Model getModel() {
    return model;
    }


/**
 * Constructors...
 */
    public AnovaGUI(String title, String modSpec, int reps,
            String levels, String randList) {
        super(title, false);
        model = new Model(modSpec);
        if (reps <= 1)
            model.addTerm(new Residual(model));
        else
            model.addFactor(new WithinCells(model, reps));
        if (levels != null)
            model.setLevels(levels);
        if (randList != null)
            model.setRandom(randList);
        build();
    }

    /**
     * This constructor is used to make a linked GUI based on the same model
     */
    public AnovaGUI(String title, Model model, double effSD[]) {
		super(title, false);
		this.model = model;
		this.effSD = effSD;
		build();
    }

// For testing...
    public static void main(String argv[]) {
        new AnovaGUI("Nested-factorial model",
            "grp + Subj(grp) + trt + grp*trt",
            2,
            "grp 4   Subj 5   trt 3",
            "Subj");
    }

}
