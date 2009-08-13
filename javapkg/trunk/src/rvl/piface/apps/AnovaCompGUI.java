package rvl.piface.apps;

import java.awt.*;
import java.util.*;

import rvl.awt.*;
import rvl.piface.*;
import rvl.stat.dist.*;
import rvl.stat.anova.*;
import rvl.util.*;

public class AnovaCompGUI extends Piface implements PiListener {

    private static String
        title = "ANOVA Comparisons";

    public double
        effSize,// detectable diff or contrast
        n[],    // numbers of levels
        effSD[],// SDs of effects (only random ones are used)
        power,  // power
        alpha,  // sig level
        famSize,// # of means in the family
        bonfDiv,// divisor for Bonferroni
        coef[]; // contrast coefficients

    public int
        comp,   // which comparison
        cvType, // type of crit value (LSD, etc.)
        autoBonf,// Bonferroni levels = (famSize choose 2)
        restr;  // restriction (same levels of...)

    public PiArrayField coefFld;    // to enter contrast

    protected String
        restrStg[],     // text for restrictions
        cvStg[],       // type of critical value
        fixedNames[];   // names of fixed terms

    protected Model model;

    protected Term fixedTerms[];

    protected Vector restrVec;  // holds restrictions (fac[])
                                // on comparisons

    protected AnovaGUI ag;    // linked AnovaGUI dialog
    protected boolean ignoreActions = false;  // switch used by piAction

    private int bonfIndex;  // index of "Bonferroni" choice
    private Component bonfComp,     // the component for bonfDiv
    famComp,                    // the component for famSize
    restrComp;                  // the compoent for restr
    private double vdf[],   // used by reporting facility.
        cv;     // crit value   "   "      "        "

/**
 * Set up the GUI
 */
    public void gui () {
    // Sample-size panel
        label("Levels / Sample size");
        beginSubpanel(1,false);
        for (int i=0; i<model.nFac(); i++) {
            Factor f = model.getFac(i);
            String pfx = f.isRandom() ? "n[" : "levels[";
            String lab = pfx + f.getName() + "]";
            if (lab.equals("n[Residual]")) lab = "Replications";
            bar("n[" + i + "]", lab, (double)f.getLevels());
        }
        //filler();
        endSubpanel();

    // Random effects panel
        newColumn();
        label("Random effects");
        beginSubpanel(1,false);
        for (int i=0; i<model.nTerm(); i++) {
            Term t = model.getTerm(i);
            if (t.isRandom())
                bar("effSD[" + i + "]",
                    "SD[" + t.getName() + "]", effSD[i]);
        }
        //filler();
        endSubpanel();

    // Contrasts panel
        newColumn();
        label("Contrasts across fixed levels");
        beginSubpanel(1,false);
        choice("comp","Contrast levels of", fixedNames, 0);
        coefFld = arrayField("click","Contrast coefficients",
            new double[] {-1, 1});
        choice("restr","Restriction",restrStg,0);
        beginSubpanel(2);
          choice("cvType","Method", cvStg,2);
          field("famSize","# means",1,2,3);
          choice("alpha","Alpha ",new double[]{.005,.01,.02,.05,.1,.2}, 3);
          field("bonfDiv","# tests",1,2,3);
        endSubpanel();
        //filler();
        beginSubpanel(1,true);
          bar("effSize","Detectable contrast",1);
          ointerval("power","Power",0,0,1);
        endSubpanel();
        endSubpanel();

    // save components for bonfDiv and restr
        bonfComp = (Component)getComponent("bonfDiv");
        bonfComp.setVisible(false);
    famComp = (Component)getComponent("famSize");
    restrComp = (Component)getComponent("restr");
    restrComp.setVisible(false);

        menuCheckbox("autoBonf", "Auto Bonferroni", 1);
        menuItem("linkAOV", "ANOVA dialog");
        menuItem("showEMS", "Show EMS");
        menuItem("report", "Report");
        menuItem("help", "ANOVA contrasts help", helpMenu);
    }

/**
 * Set up variables before calling gui()...
 */
    public void beforeSetup() {
        n = new double[model.nFac()];
        if (effSD == null) {
            effSD = new double[model.nTerm()];
            for (int i=0; i<model.nTerm(); i++)
            effSD[i] = 1.0;
        }
        int nf = 0, ft = 0;
        for (int i=0; i<model.nTerm(); i++)
            if (!model.getTerm(i).isRandom()) nf++;
        if (nf == 0) {
            Utility.error("There are no fixed factors, so no contrasts can be studied", this);
            return;
        }
        fixedNames = new String[nf];
        fixedTerms = new Term[nf];
        for (int i=0; i<model.nTerm(); i++)
            if (!model.getTerm(i).isRandom()) {
                fixedTerms[ft] = model.getTerm(i);
                fixedNames[ft] = fixedTerms[ft].getName();
                ft++;
            }
        restrStg = new String[] {"(no restrictions)"};
        restrVec = model.getAllCompRestr(fixedTerms[0]);
        cvStg = new String[]{"t","Dunnett","Tukey/HSD",
            "Bonferroni","Scheffe"};
        bonfIndex = 3;
    }

    public void afterSetup() {
        setFamSize();
        click();
        updateVars();
    }

/**
 * Default event handler
 */
    public void click() {
        bonfDiv = max(1, round(bonfDiv));
        Factor r[] = (Factor[])restrVec.elementAt(restr);
        Term t = fixedTerms[comp];
        vdf = model.getCompVariance(t, r, effSD);

        double v = vdf[0], df = vdf[1],
            c[] = coefFld.getValue(), sc = 0, sc2 = 0;
        for (int i=0; i < min(t.span(),c.length); i++) {
            sc += c[i];
            sc2 += c[i]*c[i];
        }
        if (abs(sc)>.001 || sc2<.001) {
            power = Double.NaN;
            return;
        }

// NOTE - Still need to take care of Dunnett
        double delta = effSize / sqrt(sc2 * v);
        switch (cvType) {
            case 0: // Regular t, no multiple-testing correction
                cv = -T.quantile(alpha/2, df);
                break;
            case 1: // Dunnett
// Note - right now, this is improvised using a Bonferroni corr.
                cv = -T.quantile(alpha/(2*(famSize-1)), df);
                break;  // needs impl
            case 2: // Tukey (Studentized Range)
                cv = Tukey.quantile(1 - alpha, famSize, df) / sqrt(2);
                break;
            case 3: // Bonferroni
                cv = -T.quantile(alpha/(2*bonfDiv), df);
                break;
            case 4: // Scheffe
                cv = sqrt((famSize-1)*F.quantile(1-alpha,famSize-1,df));
                break;
        }
    famComp.setVisible(cvType > 0);
        power = 1 - T.cdf(cv, df, delta);
    }


    public void cvType_changed() {
        bonfComp.setVisible(cvType == bonfIndex);
        if (cvType == bonfIndex) {
            show();
            setBonf();
        }
        click();
    }

    public void n_changed() {
        model.recalcLU = true;
        n[sourceIndex] = max(1, round(n[sourceIndex]));
        Factor f = model.getFac(sourceIndex);
        f.setLevels((int)n[sourceIndex]);
        for (int i=0; i<model.nFac(); i++)
            n[i] = model.getFac(i).getLevels();
        if (!f.isRandom()) restr_changed();    // sets famSize & maybe bonfDiv
        click();
    }

    public void comp_changed() {
        restrVec = model.getAllCompRestr(fixedTerms[comp]);
        Choice choice = ((PiChoice)restrComp).getChoice();
        choice.removeAll();
        choice.add("(no restrictions)");
        restr = 0;
    if (restrVec.size() < 2) {
        restrComp.setVisible(false);
    }
    else {
        restrComp.setVisible(true);
        for (int i=1; i<restrVec.size(); i++) {
        Factor f[] = (Factor[])restrVec.elementAt(i);
        String r = "Same ";
        for (int j=0; j<f.length; j++) {
            if (j > 0) r += " and ";
            r += f[j].getShortName();
        }
        choice.add(r);
        }
    }
        setFamSize();
        setBonf();
        click();
    }

    public void restr_changed() {
        setFamSize();
        if (autoBonf == 1) setBonf();
        click();
    }

    public void autoBonf_changed() {
        restr_changed();
    }

    public void setBonf() {
        if (famSize > 1) bonfDiv = famSize * (famSize - 1) / 2;
        else bonfDiv = 1;
    }

    public void setFamSize() {
        famSize = fixedTerms[comp].span();
        if (restr > 0) {
            Factor f[] = (Factor[])restrVec.elementAt(restr);
            if (f != null) for (int i=0; i<f.length; i++)
                famSize /= f[i].getLevels();
        }
    }
/**
* We need to override parent method because the n[] values
* are also stored and used by the model object!
*/
    public synchronized void restoreVars(double saved[]) {
        super.restoreVars(saved);
        for (int i=0; i<model.nFac(); i++) {
            model.getFac(i).setLevels((int)n[i]);
        }
    }

    public void linkAOV() {
        ag = new AnovaGUI(getTitle(), model, effSD);
        addPiListener(ag);
        ag.addPiListener(this);
        ag.acg = this;
    }

    public synchronized void piAction(String varName) {
        if (ignoreActions || ag == null) return;
        setVar(varName, ag);
        callMethodFor(varName);
        updateVars();
        ag.ignoreActions = true;
        notifyListeners(varName);
        ag.ignoreActions = false;
    }

    public void close() {
    if (ag != null) {
        removePiListener(ag);
        ag.removePiListener(this);
        ag.acg = null;
    }
        dispose();  //super.close();
    }

    public void showEMS() {
        showText(model.EMSString(), "Expected mean squares", 25, 50);
    }

    public void report() {
        Term t = fixedTerms[comp];
        StringBuffer sb = new StringBuffer
            ("Power analysis of comparisons/contrasts\n");
        sb.append("\nModel:\n");
        for (int i=0; i<model.nTerm(); i++) {
            Term trm = model.getTerm(i);
            sb.append("  " + Utility.format(trm.getName(),20));
            int n = (trm instanceof Factor)
                ? ((Factor)trm).getLevels() : trm.span();
            if (trm.isRandom())
                sb.append(" \trandom \t " + n + " levels \tSD = "
                    + effSD[i] + "\n");
            else sb.append(" \tfixed \t " + n + " levels\n");
        }
        sb.append("\nContrast of means at levels of "
            + fixedNames[comp] + "\n");
        sb.append("Contrast coefficients: ");
        double c[] = coefFld.getValue(), sumc2 = 0;
        for (int i=0; i<c.length; i++) {
            sb.append(Utility.format(c[i],3) + " ");
            sumc2 += c[i]*c[i];
        }
        String sc2Str = Utility.format(sumc2,3);
        sb.append("\nEffect size of interest = " + Utility.format(effSize,3) + "\n");
        sb.append("Critical value: " + cvStg[cvType] + "\n");
        double save[]= saveVars();
        for (restr=0; restr<restrVec.size(); restr++) {
            Factor f[] = (Factor[])restrVec.elementAt(restr);
            if (restr == 0) sb.append("\nNo restrictions");
            else {
                sb.append("\nSame ");
                for (int j=0; j<f.length; j++) {
                    if (j > 0) sb.append(" and ");
                    sb.append(f[j].getShortName());
                }
            }

            restr_changed();
            click();

            sb.append("  (" + (int)famSize + " means");
            if (cvType == bonfIndex) sb.append(", " + (int)bonfDiv + " tests");
            sb.append(")\n");
            String s[] = model.getCompVarString(t,f);
            double se = sqrt(vdf[0]);
            sb.append("  Variance = " + sc2Str + " * [" + s[0] + "]\n");
            sb.append("  Estimator = " + sc2Str + " * [" + s[1] + "]\n");
            sb.append("  SE = " + Utility.fixedFormat(se,4));
            sb.append("    LSC = " + Utility.fixedFormat(cv*se,4));
            sb.append("    d.f. = " + Utility.fixedFormat(vdf[1],1));
            sb.append("    Power = " + Utility.format(power,4) + "\n");
        }
        sb.append("\n(\"LSC\" = \"Least significant contrast\")\n");
        restoreVars(save);
        showText(sb.toString(), "Power analysis of comparisons/contrasts", 25, 60);
    }

    public void help() {
        showText(AnovaGUI.class, "AnovaCompHelp.txt",
        "Help for contrasts power analysis", 25, 60);
    }


/**
 * The following code makes it self-standing...
 */
    public AnovaCompGUI(String title, String modSpec, int reps,
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
    public AnovaCompGUI(String title, Model model, double effSD[]) {
    super(title, false);
    this.model = model;
    this.effSD = effSD;
    build();
    }

// For testing...
    public static void main(String argv[]) {
        new AnovaCompGUI("Nested-factorial model",
            "grp + Subj(grp) + trt + grp*trt",
            2,
            "grp 4   Subj 5   trt 3",
            "Subj");
    }

}
