package rvl.stat.anova;

import java.awt.*;
import rvl.awt.*;

public class EffectAdvisor extends Frame {

    private int rows, cols, wid=6, prec=-2;
    private double rm[], cm[], gm, rc, cell[][], rsd, csd, rcsd,
                   re[], ce[], celle[][], minY, maxY, rrng, crng, rcrng,
                   fuzz = .0001;
    private DoubleField rmFld[], cmFld[], gmFld, cellFld[][],
                        rsdFld, csdFld, rcsdFld, rrngFld, crngFld, rcrngFld;
    private IntField nRowFld, nColFld;
    private Canvas rowPlot, colPlot;
    private Panel advisorCard, setupCard, dataPan;
    private CardLayout layout;
    private MenuBar menuBar;

    public EffectAdvisor() {
        nRowFld = new IntField(2,4);
        nColFld = new IntField(2,4);
        gmFld = new DoubleField(0.0, wid, prec);
        gmFld.setBackground(Color.gray);
        rsdFld = new DoubleField(0.0, 5, -3);
        csdFld = new DoubleField(0.0, 5, -3);
        rcsdFld = new DoubleField(0.0, 5, -3);
        rsdFld.setBackground(Color.darkGray);
        csdFld.setBackground(Color.darkGray);
        rcsdFld.setBackground(Color.darkGray);
        rrngFld = new DoubleField(0.0, 4, -2);
        crngFld = new DoubleField(0.0, 4, -2);
        rcrngFld = new DoubleField(0.0, 4, -2);
        rrngFld.setBackground(Color.darkGray);
        crngFld.setBackground(Color.darkGray);
        rcrngFld.setBackground(Color.darkGray);

    /*** Set up effect-size panel ***/
        Panel effPan = new Panel();
        effPan.setLayout(new RVLayout(3)); 
        effPan.setBackground(Color.gray);
        effPan.setForeground(Color.white);
        effPan.add(new Label("Term", Label.LEFT));
        effPan.add(new Label("Effect SD", Label.CENTER));
        effPan.add(new Label("Range", Label.CENTER));
        effPan.add(new GridLine(GridLine.HORIZ, Color.lightGray));
        effPan.add(new GridLine(GridLine.HORIZ, Color.lightGray));
        effPan.add(new GridLine(GridLine.HORIZ, Color.lightGray));
        effPan.add(new Label("Rows", Label.LEFT));
        effPan.add(rsdFld);
        effPan.add(rrngFld);
        effPan.add(new Label("Cols", Label.LEFT));
        effPan.add(csdFld);
        effPan.add(crngFld);
        effPan.add(new Label("Rows*Cols", Label.LEFT));
        effPan.add(rcsdFld);
        effPan.add(rcrngFld);

    /*** Set up plot panel ***/
        Panel plotPan = new Panel();
        plotPan.setLayout(new GridLayout(1,2));
        rowPlot = new Canvas();
        colPlot = new Canvas();
        plotPan.add(rowPlot);
        plotPan.add(colPlot);

        Panel botPan = new Panel();
        botPan.setLayout(new BorderLayout());
        botPan.add("West", effPan);
        botPan.add("Center", plotPan);
	botPan.add("North", new GridLine(GridLine.HORIZ, Color.black));

    /*** set up menu ***/
        menuBar = new MenuBar();
        Menu m = new Menu("Options");
        m.add(new MenuItem("New setup"));
        m.add(new MenuItem("Reset values"));
        m.add(new MenuItem("Reset window size"));
        m.addSeparator();
        m.add(new MenuItem("Min SD row effs"));
        m.add(new MenuItem("Linear row effs"));
        m.add(new MenuItem("Max SD row effs"));
        m.addSeparator();
        m.add(new MenuItem("Min SD col effs"));
        m.add(new MenuItem("Linear col effs"));
        m.add(new MenuItem("Max SD col effs"));
        m.addSeparator();
        m.add(new MenuItem("Min SD row*col effs"));
        m.add(new MenuItem("Lin*lin row*col effs"));
        m.add(new MenuItem("Max SD row*col effs"));
        m.addSeparator();
        m.add(new MenuItem("Quit"));
        menuBar.add(m);

    /*** Assemble the advisor card ***/
        setTitle("Effect Advisor setup");
        layout = new CardLayout();
        setLayout(layout);

        setupCard = new Panel();
        setupCard.add(new Label("Rows"));
        setupCard.add(nRowFld);
        setupCard.add(new Label("        Columns"));
        setupCard.add(nColFld);
        setupCard.add(new Label("        "));
        setupCard.add(new Button("OK"));
        add("setup", setupCard);

        advisorCard = new Panel();
        advisorCard.setLayout(new BorderLayout());
        advisorCard.add("South", botPan);
        add("advisor",advisorCard);
        layout.show(this, "setup");
        pack();
        show();
    }

/**
 *  This function allocates memory and components for the specified
 *  number of rows and columns, and sets up and returns a panel
 *  containing the input windows for cells and marginal means
 */
    private Panel dataPanel(int r, int c) {
    /*** Initialize arrays ***/
        rows = r;
        cols = c;
        rc = r*c;
        rm = new double[r];
        re = new double[r];
        rmFld =  new DoubleField[r];
        cm = new double[c];
        ce = new double[c];
        cmFld =  new DoubleField[c];
        cell = new double[r][];
        celle = new double[r][];
        cellFld = new DoubleField[r][];
        for (int i=0; i<r; i++) {
            rmFld[i] = new DoubleField(0.0, wid, prec);
            rmFld[i].setBackground(Color.pink);
            cell[i] = new double[c];
            celle[i] = new double[c];
            cellFld[i] = new DoubleField[c];
            for (int j=0; j<c; j++) {
                cellFld[i][j] = new DoubleField(0.0, wid, prec);
                cellFld[i][j].setBackground(Color.white);
            }
        }
        for (int j=0; j<c; j++) {
            cmFld[j] = new DoubleField(0.0, wid, prec);
            cmFld[j].setBackground(Color.pink);
        }

    /*** Set up effect-entry panel ***/
        Panel dataPan = new Panel();
        dataPan.setLayout(new RVLayout(c+2, 0,0, false,true));
        for (int i=0; i<r; i++) {
            for (int j=0; j<c; j++)
                dataPan.add(cellFld[i][j]);
            dataPan.add(new GridLine(GridLine.VERT, Color.red));
            dataPan.add(rmFld[i]);
        }
        for (int j=0; j<c; j++)
            dataPan.add(new GridLine(GridLine.HORIZ, Color.red));
        dataPan.add(new GridLine(GridLine.CROSS, Color.red));
        dataPan.add(new GridLine(GridLine.HORIZ, Color.red));
        for (int j=0; j<c; j++)
            dataPan.add(cmFld[j]);
        dataPan.add(new GridLine(GridLine.VERT, Color.red));
        dataPan.add(gmFld);

        return dataPan;
    }


/**
 * Use current cell values and update the means and effects.
 * Assumes all cells and cell fields are set
 */
    private synchronized void updateEffs() {
        double reMin, reMax, ceMin, ceMax, rcMin, rcMax;

        reMin = reMax = ceMin = ceMax = rcMin = rcMax = 0.0;
        gm = 0.0;
        for (int j=0; j<cols; cm[j++] = 0.0);
        minY = maxY = cell[0][0];
        for (int i=0; i<rows; i++) {
            rm[i] = 0.0;
            for (int j=0; j<cols; j++) {
                rm[i] += cell[i][j] / cols;
                cm[j] += cell[i][j] / rows;
                gm += cell[i][j] / rc;
                if (cell[i][j] < minY) minY = cell[i][j];
                if (cell[i][j] > maxY) maxY = cell[i][j];
            }
            rmFld[i].setValue(rm[i]);
        }
        for (int j=0; j<cols; j++)
            cmFld[j].setValue(cm[j]);
        gmFld.setValue(gm);
        rsd = csd = rcsd = 0.0;
        for (int i=0; i<rows; i++) {
            re[i] = rm[i] - gm;
            if (re[i] < reMin) reMin = re[i];
            if (re[i] > reMax) reMax = re[i];
            rsd += re[i] * re[i];
            for (int j=0; j<cols; j++) {
                celle[i][j] = cell[i][j] - re[i] - cm[j];
                rcsd += celle[i][j] * celle[i][j];
                if (celle[i][j] < rcMin) rcMin = celle[i][j];
                if (celle[i][j] > rcMax) rcMax = celle[i][j];
            }
        }
        for (int j=0; j<cols; j++) {
            ce[j] = cm[j] - gm;
            if (ce[j] < ceMin) ceMin = ce[j];
            if (ce[j] > ceMax) ceMax = ce[j];
            csd += ce[j] * ce[j];
        }
        rsd = Math.sqrt(rsd / (rows - 1));
        csd = Math.sqrt(csd / (cols - 1));
        rcsd = Math.sqrt(rcsd / (rows - 1) / (cols - 1));
        rsdFld.setValue(rsd);
        csdFld.setValue(csd);
        rcsdFld.setValue(rcsd);
        rrng = reMax - reMin;
        crng = ceMax - ceMin;
        rcrng = rcMax - rcMin;
        rrngFld.setValue(rrng);
        crngFld.setValue(crng);
        rcrngFld.setValue(rcrng);
        updatePlots();
    }

    private synchronized void updateRow(int i, double mean, boolean upEffs) {
        double inc = mean - rm[i];
        for (int j=0; j<cols; j++) {
            cell[i][j] += inc;
            cellFld[i][j].setValue(cell[i][j]);
        }
        if (upEffs) updateEffs();
    }

    private synchronized void updateCol(int j, double mean, boolean upEffs) {
        double inc = mean - cm[j];
        for (int i=0; i<rows; i++) {
            cell[i][j] += inc;
            cellFld[i][j].setValue(cell[i][j]);
        }
        if (upEffs) updateEffs();
    }

    private synchronized void updateGM(double mean) {
        for (int i=0; i<rows; i++)
            updateRow(i, mean + re[i], false);
        updateEffs();
    }

/**
 *  Rescales the row, col, row*col effects according to the factors given.
 *  A factor of 0 will set all those effects equal to zero.
 *  A negative factor will rescale to the current range value, if
 *  positive; otherwise a scale factor of 1 is used
 */
    private synchronized void rescaleEffs(double fr, double fc, double frc) {
        for (int i=0; i<rows; i++)
            for (int j=0; j<cols; j++) {
                cell[i][j] = gm + fr*re[i] + fc*ce[j] + frc*celle[i][j];
                cellFld[i][j].setValue(cell[i][j]);
            }
        updateEffs();
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) 
            dispose();  //--WAS: System.exit(0);
        return super.handleEvent(e);
    }

    public boolean action(Event evt, Object what) {
        if (what.equals("OK")) {
            setTitle("Setting up Effect Advisor ...");
            if (dataPan != null) advisorCard.remove(dataPan);
            int r = nRowFld.getValue(), c = nColFld.getValue();
            r = Math.max(r,2);
            c = Math.max(c,2);
            dataPan = dataPanel(r,c);
            updateGM(0.0);
            rescaleEffs(0.0, 0.0, 0.0);
            advisorCard.add("North", dataPan);
            setTitle("Effect Advisor");
            setMenuBar(menuBar);
            layout.show(this, "advisor");
            pack();
            return true;
        }
        if (what.equals("New setup")) {
            setTitle("Effect Advisor Setup");
            remove(menuBar);
            layout.show(this, "setup");
            pack();
            return true;
        }
        if (what.equals("Quit")) {
            dispose();
            //System.exit(0);
        }
        if (evt.target.equals(gmFld)) {
            updateGM(gmFld.getValue());
            return true;
        }
        if (evt.target.equals(rsdFld)) {
            rescaleEffs(rsdFld.getValue() / rsd, 1.0, 1.0);
            return true;
        }
        if (evt.target.equals(csdFld)) {
            rescaleEffs(1.0, csdFld.getValue() / csd, 1.0);
            return true;
        }
        if (evt.target.equals(rcsdFld)) {
            rescaleEffs(1.0, 1.0, rcsdFld.getValue() / rcsd);
            return true;
        }
        if (evt.target.equals(rrngFld)) {
            rescaleEffs(rrngFld.getValue() / rrng, 1.0, 1.0);
            return true;
        }
        if (evt.target.equals(crngFld)) {
            rescaleEffs(1.0, crngFld.getValue() / crng, 1.0);
            return true;
        }
        if (evt.target.equals(rcrngFld)) {
            rescaleEffs(1.0, 1.0, rcrngFld.getValue() / rcrng);
            return true;
        }
        for (int i=0; i<rows; i++)
            if (evt.target.equals(rmFld[i])) {
                updateRow(i, rmFld[i].getValue(), true);
                return true;
            }
        for (int j=0; j<cols; j++)
            if (evt.target.equals(cmFld[j])) {
                updateCol(j, cmFld[j].getValue(), true);
                return true;
            }
        for (int i=0; i<rows; i++)
            for (int j=0; j<cols; j++)
                if(evt.target.equals(cellFld[i][j])) {
                    cell[i][j] = cellFld[i][j].getValue();
                    updateEffs();
                    return true;
                }
        if (what.equals("Reset values")) {
            rescaleEffs(0.0, 0.0, 0.0);
            updateGM(0.0);
            return true;
        }
        if (what.equals("Reset window size")) {
            pack();
            return true;
        }
        if(what.equals("Min SD row effs")) {
            double scal = rrng > fuzz ? .5 * rrng : 1;
            rescaleEffs(0.0, 1.0, 1.0);
            re[0] = -1; re[rows-1] = 1;
            rescaleEffs(scal, 1.0, 1.0);
            return true;
        }
        if(what.equals("Linear row effs")) {
            double scal = rrng > fuzz ? .5 * rrng : 1;
            for (int i=0; i<rows; i++) 
                re[i] = (2 * i - rows + 1.0) / (rows - 1.0);
            rescaleEffs(scal, 1.0, 1.0);
            return true;
        }
        if(what.equals("Max SD row effs")) {
            double scal = rrng > fuzz ? .5 * rrng : 1;
            re[(rows-1)/2] = 0.0;
            for (int i=0; i<rows/2; i++) {
                re[i] = -1;
                re[rows-i-1] = 1;
            }
            rescaleEffs(scal, 1.0, 1.0);
            return true;
        }
        if(what.equals("Min SD col effs")) {
            double scal = crng > fuzz ? .5 * crng : 1;
            rescaleEffs(1.0, 0.0, 1.0);
            ce[0] = -1; ce[cols-1] = 1;
            rescaleEffs(1.0, scal, 1.0);
            return true;
        }
        if(what.equals("Linear col effs")) {
            double scal = crng > fuzz ? .5 * crng : 1;
            for (int j=0; j<cols; j++) 
                ce[j] = (2 * j - cols + 1.0) / (cols - 1.0);
            rescaleEffs(1.0, scal, 1.0);
            return true;
        }
        if(what.equals("Max SD col effs")) {
            double scal = crng > fuzz ? .5 * crng : 1;
            ce[(cols-1)/2] = 0.0;
            for (int i=0; i<cols/2; i++) {
                ce[i] = -1;
                ce[cols-i-1] = 1;
            }
            rescaleEffs(1.0, scal, 1.0);
            return true;
        }
        if(what.equals("Min SD row*col effs")) {
            double scal = rcrng > fuzz ? .5 * rcrng : 1;
            rescaleEffs(1.0, 1.0, 0.0);
            celle[0][0] = celle[rows-1][cols-1] = 1;
            celle[0][cols-1] = celle[rows-1][0] = -1;
            rescaleEffs(1.0, 1.0, scal);
            return true;
        }
        if(what.equals("Lin*lin row*col effs")) {
            double scal = rcrng > fuzz ? .5 * rcrng : 1;
            for (int i=0; i<rows; i++) 
                for (int j=0; j<cols; j++) 
                    celle[i][j] = (2 * i - rows + 1) * (2 * j - cols + 1)
                                / (rows - 1.0) / (cols - 1.0);
            rescaleEffs(1.0, 1.0, scal);
            return true;
        }
        if(what.equals("Max SD row*col effs")) {
            double scal = rcrng > fuzz ? .5 * rcrng : 1;
            rescaleEffs(1.0, 1.0, 0.0);
            for (int i=0; i<rows/2; i++) for (int j=0; j<cols/2; j++) {
                celle[i][j] = celle[rows-i-1][cols-j-1] = 1;
                celle[i][cols-j-1] = celle[rows-i-1][j] = -1;
            }
            rescaleEffs(1.0, 1.0, scal);
            return true;
        }
        return super.action(evt, what);
    }

    private void updatePlots() {
        Color colorset[] = {Color.black, Color.blue, Color.red, 
            Color.orange, Color.cyan, Color.magenta};
        int w = rowPlot.size().width, h = rowPlot.size().height, refY = h/2;
        double yScal = 0.0;

        if (minY<maxY) {
            yScal = (h - 30) / (maxY - minY);
            refY = 20;
        }

        Graphics g = drawCanv(rowPlot, "Row profiles");
        int xInc = (w - 20) / (cols - 1);
        for (int i=rows-1; i>=0; i--) {
            g.setColor(colorset[i % 6]);
            int lastY = 0, lastX = 0;
            for (int j=0; j<cols; j++) {
                int y = refY + (int)(yScal*(maxY - cell[i][j])+.5);
                int x = 10 + j*xInc;
                g.fillOval(x-2,y-2,5,5);
                if (j>0) 
                    g.drawLine(lastX, lastY, x, y);
                lastY = y;
                lastX = x;
            }          
        }

        g = drawCanv(colPlot, "Column profiles");
        xInc = (w - 20) / (rows - 1);
        for (int j=cols-1; j>=0; j--) {
            g.setColor(colorset[j % 6]);
            int lastY = 0, lastX = 0;
            for (int i=0; i<rows; i++) {
                int y = refY + (int)(yScal*(maxY - cell[i][j])+.5);
                int x = 10 + i*xInc;
                g.fillOval(x-2,y-2,5,5);
                if (i>0) 
                    g.drawLine(lastX, lastY, x, y);
                lastY = y;
                lastX = x;
            }          
        }
    }

    private Graphics drawCanv(Canvas c, String title) {
        Graphics g = c.getGraphics();
        int w = c.size().width, h = c.size().height;
        g.setColor(Color.white);
        g.fill3DRect(5, 15, w-10, h-20, true);
        g.setColor(Color.black);
        g.setFont(new Font("Helvetica", Font.PLAIN, 8));
        g.drawString(title, 5, 10);
        return g;
    }

    public void paint(Graphics g) {
        updatePlots();
    }


    public static void main (String argv[]) {
        EffectAdvisor ea = new EffectAdvisor();
    }
}
