package rvl.piface.apps;

import rvl.awt.*;
import rvl.piface.*;
import rvl.stat.*;
import rvl.stat.anova.*;
import rvl.stat.dist.*;

import java.awt.*;
import java.awt.event.*;

public class AnovaHelper extends Piface
    implements PiListener, ActionListener
{

    private static String
        title = "ANOVA Effects Helper"; // title of the dialog

    public int sel, distn, fixedRange, adjust;     // int variables

    private AnovaGUI agui;
    private Model model;
    private int termNo, modFacNo, facNo[] = new int[2]; // index of selected term and factor in the model
    private PiChoice chooser;
    private PiDotplot dotplot;
    private IntPlot plot;
    private CardLayout card;
    private PiPanel cardPanel;
    private String facName[];
    private boolean MEmode = true;

/**
 * Set up the GUI
 */
    public void beforeSetup() {
        optMenu.remove(0);  // take off Graphics menu
    }

    public void gui () {
        // remove(menuBar);
        choice("sel", "Effect", new String[] {"~"}, 0);
        chooser = (PiChoice) getComponent("sel");

        // === bottom section uses a CardLayout ===
        beginSubpanel(1);
            card = new CardLayout();
            cardPanel = panel;
            cardPanel.setLayout(card);

        //---- Main-effects card
            panel = new PiPanel(new RVLayout(1, true, true));
            dotplot = dotplot("dotplot_changed", "Pattern of means",
                              new double[] {-1, 1});
            choice("distn","Distribution",
                   new String[] {"(general)","min SD", "normal",
                         "uniform", "max SD"}, 1);
            choice("fixedRange", "Dist. option",
                   new String[]{"Fixed SD", "Fixed Range"}, 1);
            cardPanel.add("MEPanel", panel);

        //---- Two-way card
            panel = new PiPanel(new RVLayout(1, true, true));
            cardPanel.add("2wayPanel", panel);
	    // Add panel BEFORE calling setStretchable(),
	    // since it has to pass "stretchable" attribute on to parents
            panel.setStretchable(true);

            facName = new String[]{"",""};
            double x[] = new double[]{1,2},
                   y1[] = new double[]{-1,1},
                   y2[] = new double[]{1,-1},
                   y[][] = new double[][]{y1, y2};
            plot = new IntPlot(x,y);
            component("plot_changed", plot);
            plot.setConstraints(true,false);
            plot.setLineMode(true);
            plot.setDotMode(true);
            plot.setTickMode(false,true);
            plot.setTitle(">(Drag dots to modify)");
            labelAxes();
            beginSubpanel(2);
                checkbox("adjust","Remove main effects", 1);
                button("swapFac","Switch factors");
            endSubpanel();

            panel = cardPanel;

        endSubpanel(); // bottom section
    }

    public void afterSetup() {
        setupFixedFactors();
        optMenu.remove(0);  // take off divider
        //helpMenu.remove(0);  // take off divider
    }


/**
 * Default event handler -- will never be called
 */
    public void click() {
    }

    public void showMEs() {
        card.show(cardPanel, "MEPanel");
    }
    public void show2way() {
        card.show(cardPanel, "2wayPanel");
    }

    private void labelAxes() {
        plot.setAxisLabels(new String[]
            {"Levels of " + facName[0],
            ">Profiles: " + facName[1]},
            new String[]{"Response"});
    }

    public void adjustMEs(boolean updatePlot) {
        double y[][] = plot.getYData();
        int r = y.length, c = y[0].length;
        double mean = 0,
            rowmean[] = new double[r],
            colmean[] = new double[c];
        for (int i=0; i<r; i++)
                rowmean[i] = 0;
        for (int j=0; j<c; j++)
                colmean[j] = 0;
        for (int i=0; i<r; i++) for (int j=0; j<c; j++) {
            rowmean[i] += y[i][j] / c;
            colmean[j] += y[i][j] / r;
            mean += y[i][j] / r / c;
        }

        double yy[][] = y;
        if(!updatePlot) yy = new double[r][c];
        for (int i=0; i<r; i++) for (int j=0; j<c; j++)
            yy[i][j] = y[i][j] - rowmean[i] - colmean[j] + mean;
        if (updatePlot) plot.setYData(yy);

        agui.effSD[termNo] = sd2(yy);
        String name = "effSD[" + termNo + "]";
        agui.callMethodFor(name);
        agui.updateVars();
        agui.notifyListeners(name, this);
    }

    public void plot_changed() {
        adjustMEs(adjust == 1);
    }

    public void adjust_changed() {
        adjustMEs(adjust == 1);
    }

    public void swapFac() {
        String temp = facName[0];
        facName[0] = facName[1];
        facName[1] = temp;
        int tmp = facNo[0];
        facNo[0] = facNo[1];
        facNo[1] = tmp;
        labelAxes();
        double y[][] = plot.getYData();
        int r = y.length, c = y[0].length;
        double newY[][] = new double[c][r], newX[] = new double[r];
        for (int i=0; i<r; i++) {
            newX[i] = i;
            for (int j=0; j<c; j++)
                newY[j][i] = y[i][j];
        }
        plot.setData(newX,newY);
    }


/**
 * Selected term has changed
 */
    public void sel_changed() {
        termNo = -1; // causes an error if anything goes wrong (shouldn't)
        String selName = chooser.getChoice().getItem(sel);
        for (int i=0; i<model.nTerm(); i++)
            if (model.getTerm(i).getName() .equals(selName))
                termNo = i;
        Term t = model.getTerm(termNo);
        if (t.order() == 2) {
            MEmode = false;
            modFacNo = -1;  // will cause error if I screwed up coding
            Factor fac[] = t.getFactors();
            facName[1] = fac[0].getName();
            facName[0] = fac[1].getName();
            for (int i=0; i<model.nFac(); i++) {
                String name = model.getFac(i).getName();
                if (name.equals(facName[0])) facNo[0] = i;
                if (name.equals(facName[1])) facNo[1] = i;
            }
            show2way();
            setupIntPlot(fac);
        }
        else {
            MEmode = true;
            showMEs();
            for (int i=0; i<model.nFac(); i++)
                if (model.getFac(i).getName() .equals(selName))
                modFacNo = i;
            int fr = fixedRange;
            fixedRange = 0;
            reviseDotplot(true);
            fixedRange = fr;
        }
    }

/**
 * @returns a linear contrast for <code>lev</code> levels
 */
    public double[] linCon(int lev) {
        double c[] = new double[lev], m = .5*(lev - 1);
        for (int i=0; i<lev; i++)
            c[i] = i - m;
        return c;
    }

/**
 * Selected a distributional pattern
 */
    public void distn_changed() {
        if (distn != 0)
            revisePatternDotplot((int)(agui.n[modFacNo]+.05), false);
    }

/**
 * Changed scaling option -- only needed response is to change labels
 * in distn choices.
 */
    public void fixedRange_changed() {
        Choice ch = ((PiChoice)getComponent("distn")).getChoice();
        int sel = ch.getSelectedIndex();
        ch.remove(4);
        ch.remove(1);
        if (fixedRange == 0) {
            ch.insert("max range", 1);
            ch.insert("min range", 4);
        }
        else {
            ch.insert("min SD", 1);
            ch.insert("max SD", 4);
        }
        ch.select(sel);
    }

    private void setupIntPlot(Factor fac[]) {
        int r = fac[0].getLevels(), c = fac[1].getLevels();
        double x[] = new double[c], y[][] = new double[r][c],
            cr[] = linCon(r), cc[] = linCon(c);
        for (int i=0; i<r; i++) for (int j=0; j<c; j++) {
            x[j] = j;
            y[i][j] = cr[i]*cc[j];
        }
        labelAxes();
        plot.setData(x, y, false);
        reviseIntPlotSD(y);
    }

    private void reviseIntPlotSD(double y[][]) {
        int r=y.length, c=y[0].length;
        double s = agui.effSD[termNo] / sd2(y);
        for (int i=0; i<r; i++) for (int j=0; j<c; j++)
            y[i][j] *= s;
        plot.setYData(y);
    }

    private void reviseIntPlotSD() {
        reviseIntPlotSD(plot.getYData());
    }

    private void reviseIntPlotN(String s) {
        if (s.equals("n["+ facNo[0] +"]")) { // # cols changed
            double y[][] = plot.getYData();
            int newC = (int)(agui.n[facNo[0]]+.05),
                r = y.length, c= y[0].length, minC = Math.min(c,newC);
            double newY[][] = new double[r][newC], x[] = new double[newC];
            for (int i=0; i<r; i++) {
                for (int j=0; j<minC; j++) newY[i][j] = y[i][j];
                for (int j=minC; j<newC; j++) newY[i][j] = 0;
            }
            for (int j=0; j<newC; j++) x[j] = j;
//vecPrt("x:",x);
//for (int i=0; i<r; i++) vecPrt("y["+i+"]:", newY[i]);
            plot.setData(x,newY);
            adjustMEs(adjust==1);
        }
        if (s.equals("n["+ facNo[1] +"]")) { // # rows changed
            double y[][] = plot.getYData();
            int newR = (int)(agui.n[facNo[1]] + .05),
                r = y.length, c= y[0].length, minR = Math.min(r,newR);
            double newY[][] = new double[newR][];
            for (int i=0; i<minR; i++) newY[i] = y[i];
            for (int i=minR; i<newR; i++) {
                newY[i] = new double[c];
                for (int j=0; j<c; j++) newY[i][j] = 0;
            }
//for (int i=0; i<newR; i++) vecPrt("y["+i+"]:", newY[i]);
            plot.setYData(newY);
            adjustMEs(adjust==1);
        }
    }

/***
    private void vecPrt(String lbl, double x[]) {
        System.out.print(lbl);
        for (int i=0; i<x.length; i++)
            System.out.print("\t" + rvl.util.Utility.format(x[i],3));
        System.out.print("\n");
    }
***/

    // assumes values are alraedy centered
    private double sd2(double x[][]) {
        double sum2=0;
        int r = x.length, c = x[0].length;
        for (int i=0; i<r; i++) for (int j=0; j<c; j++)
            sum2 += x[i][j] * x[i][j];
        return sqrt(sum2 / (r-1) / (c-1));
    }

    private void reviseDotplot(boolean requireSD) {
        int n = (int)(agui.n[modFacNo] + .05);

        if (distn == 0)
            reviseGeneralDotplot(n, requireSD);
        else
            revisePatternDotplot(n, requireSD);
    }

    private void reviseGeneralDotplot(int n, boolean requireSD) {
        double oldX[] = dotplot.getValue(),
        newX[] = new double[n],
        oldStats[] = Stat.meanSD(oldX);

        if (oldStats[1] == 0) {  // handle case with zero SD
            oldX[0] -= oldStats[0];
            oldX[oldX.length-1] += oldStats[0];
        }
        for (int i=1; i<n-1; i++) // fill center part with mean
            newX[i] = oldStats[0];
            // Copy from ends of old array
        for (int i=0; i < Math.min(n, oldX.length) / 2; i++) {
            newX[i] = oldX[i];
            newX[n-i-1] = oldX[oldX.length-i-1];
        }
        scaleAndDisplay(newX, false);
    }

    private void revisePatternDotplot(int n, boolean requireSD) {
        double x[] = new double[n];
        switch (distn) {
            case 1: // max range (min SD)
                x[0] = -1; x[n-1] = 1;
                for (int i=1; i<n-1; i++) x[i] = 0;
                break;
            case 2: // normal
                for (int i=0; i<n; i++)
                    x[i] = Normal.quantile((i+.5)/n);
                break;
            case 3: // uniform
                    for (int i=0; i<n; i++) x[i] = i;
                break;
            case 4: // min range (max SD)
                for (int i=0; i<1+n/2; i++) {
                    x[i] = -1;
                    x[n-i-1] = 1;
                }
                break;
        }
        scaleAndDisplay(x, !requireSD && (fixedRange==1));
    }

    private void scaleAndDisplay(double x[], boolean rangeFixed) {
        double loc, scale, oldLoc, oldScale,
            oldX[] = dotplot.getValue();
        int n = x.length;

        if (rangeFixed) {
            loc = x[0];
            scale = x[n-1] - x[0];
            oldLoc = oldX[0];
            oldScale = oldX[oldX.length - 1] - oldX[0];
        }
        else {
            double s[] = Stat.meanSD(x);
            loc = s[0];
            scale = s[1];
            oldLoc = Stat.mean(oldX);
            oldScale = agui.effSD[termNo];
        }

        double mult = oldScale / scale;
        for (int i=0; i<n; i++)
            x[i] = oldLoc + mult * (x[i] - loc);
        dotplot.setValue(x);
        dotplot.repaint();
        updateSD();
    }

    private void updateSD() {
        agui.effSD[termNo] = Stat.sd(dotplot.getValue());
        String name = "effSD[" + termNo + "]";
        agui.callMethodFor(name);
        agui.updateVars();
        agui.notifyListeners(name, this);
    }

/**
 * Figure out which factors to include & put in the factor list
 */
    private void setupFixedFactors() {
        Choice ch = chooser.getChoice();
        String currentChoice = ch.getItem(sel);
        ch.removeAll();
        sel = 0;  // insurance; we'll change it below if possible

        // add MEs & 2-ways
        for (int i=0; i<model.nTerm(); i++) {
            Term t = model.getTerm(i);
            if (t.order() <= 2 && !t.isRandom()) {
                String name = t.getName();
                if (name.equals(currentChoice))
                    sel = ch.getItemCount();
                ch.add(name);
            }
        }
        if (ch.getItemCount() == 0) {
            ch.add("(disabled)");  // we'll never see this
            dotplot.setVisible(false);
        }
        else {
            dotplot.setVisible(true);
            sel_changed();
            ch.select(sel);
        }
    }

    // Called when something changes in agui
    public void piAction(String varName) {
        if (varName.equals("effSD[" + termNo + "]")
                || varName.equals("power[" + termNo + "]")) {
            if(MEmode) reviseDotplot(true);
            else reviseIntPlotSD();
        }
        else if (varName.startsWith("n[")) {
            if (!MEmode) reviseIntPlotN(varName);
            else if (varName.equals("n[" + modFacNo + "]"))
                reviseDotplot(true);
        }
        else if (varName.startsWith("random")) {
            setupFixedFactors();
        }
    }

    public void dotplot_changed() {
        if (dotplot.getActionCommand().equals("Dotplot:Shift"))
            return;
        distn = 0;
        updateSD();
    }

    public void close() {
        agui.helper = null;
        agui.removePiListener(this);
        dispose();
    }



/**
 * Constructor
 */
    public AnovaHelper(AnovaGUI agui) {
        super(title, false);
        this.agui = agui;
        model = agui.getModel();
        agui.addPiListener(this);
        build();
    }

/**
 * The following code makes it self-standing...
 */
    public static void main(String argv[]) {
        AnovaGUI ag = new AnovaGUI("Test of AnovaHelper", "row | col | trt",
            10, "row 5 col 3 trt 4", "");
        ag.linkHelper();
    }

}
