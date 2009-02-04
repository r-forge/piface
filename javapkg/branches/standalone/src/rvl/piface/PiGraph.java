/**
* Graphics add-on for Piface class
*/

package rvl.piface;

import rvl.piface.*;
import rvl.util.*;
import rvl.awt.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


public class PiGraph
    extends Frame
    implements PiListener, ActionListener, WindowListener
{

    Piface piface;        // parent Piface

    Choice          // labels of variables to plot
        yChoice = new Choice(),
        xChoice = new Choice();

    DoubleField     // ranges of x variable
        fromFld = new DoubleField("",5),
        toFld = new DoubleField("",5),
        byFld = new DoubleField("",5);

    Checkbox
        accumChk = new Checkbox("Persistent");

    Button
        dataButton = new Button("Show Data"),
        drawButton = new Button("Redraw"),
        quitButton = new Button("Close");

    MenuBar
    menuBar = new MenuBar();

    Menu
        helpMenu = new Menu("Help");

    int cpHeight = 0,       // height of control panel
        xVar, yVar;         // indices of x and y vars

    boolean init = true;    // have not drawn a plot yet

    double xMin, xMax, yMin, yMax;  // ranges of plot variables

    String xName, yName;    //

    Vector
        vars = new Vector(),    // names of double variables
        intComps = new Vector(),
        labs = new Vector(),    // labels of doube variables
        data = new Vector();    // data to plot: {info1,x1[],y1[],
                                //     info2,x2[],y2[],...}

    Font scaleFont = new Font("SansSerif", Font.PLAIN, 10);

/**
* Constructor
*/
    public PiGraph (Piface pif) {
        piface = pif;

    // Control panel at bottom of frame.
    // ctrlPan contains varPan, rngPan, butPan in a stack
        Panel
            ctrlPan= new Panel(new RVLayout(1,true,true)),
            varPan = new Panel(new RVLayout(2,false,true)),
            rngPan = new Panel(new RVLayout(6,false,true)),
            butPan = new Panel(new RVLayout(4,false,true));

        varPan.add(new Label("Vertical (y) axis"));
        varPan.add(yChoice);
        varPan.add(new Label("Horizontal (x) axis"));
        varPan.add(xChoice);

        rngPan.add(new Label("from", Label.RIGHT));
        rngPan.add(fromFld);
        rngPan.add(new Label("to", Label.RIGHT));
        rngPan.add(toFld);
        rngPan.add(new Label("by", Label.RIGHT));
        rngPan.add(byFld);

        butPan.add(accumChk);
        butPan.add(dataButton);
        butPan.add(drawButton);
        butPan.add(quitButton);

        ctrlPan.add(varPan);
        ctrlPan.add(rngPan);
        ctrlPan.add(butPan);

        Enumeration enu = piface.actors.elements();
        int n = 0;
        while (enu.hasMoreElements()) {
            PiComponent actor = (PiComponent)enu.nextElement();
            if (actor instanceof DoubleComponent) {
                vars.addElement(actor.getName());
                labs.addElement(actor.getLabel());
                xChoice.addItem(actor.getLabel());
                yChoice.addItem(actor.getLabel());
                n++;
            }
            else if (actor instanceof IntComponent) {
                intComps.addElement(actor);
            }
        }
        if (n==0) {
            yChoice.addItem("Sorry,");
            xChoice.addItem("No variables are available!");
        }
        else {
            xChoice.select(Math.max(0,n-2));
            yChoice.select(n-1);
        }

        setLayout(new BorderLayout());
        add(ctrlPan,"South");

        setTitle("PiFace Graph");
//        setBackground(Color.lightGray);
        setBackground(pif.getBackground());
        piface.addPiListener(this);
        quitButton.addActionListener(this);
        drawButton.addActionListener(this);
        dataButton.addActionListener(this);
        dataButton.setVisible(false);
        addWindowListener(this);

        MenuItem helpMI = new MenuItem("Help"),
        abtMI = new MenuItem("About Piface");
        helpMI.addActionListener(this);
    abtMI.addActionListener(this);
    setMenuBar(menuBar);
    menuBar.setHelpMenu(helpMenu);
        helpMenu.add(helpMI);
    helpMenu.add(abtMI);


        pack();
        drawButton.setLabel("Draw");    // relabel AFTER packing so it's big enough later

        cpHeight = ctrlPan.getSize().height;
        Point loc = piface.getLocation();
        loc.x += piface.getSize().width;
        setLocation(loc);
        show();
    }

    // Called when window is to be closed
    public void close() {
        piface.removePiListener(this);
        dispose();
    }

/**
* This routine is called to compute a new set of (x,y) values
* to plot, based on current settings
*/
    private synchronized void computePlotData() {
        double xlo = fromFld.getValue(),
            xinc = byFld.getValue(),
            xhi = toFld.getValue();
        if (Double.isNaN(xlo+xhi+xinc) || xlo==xhi || xinc==0) return;
        int n = (int) Math.abs((xhi - xlo) / xinc + 1);
        if (n < 2 /*|| n > 501*/) return;

        if (!accumChk.getState()
            || xVar != xChoice.getSelectedIndex()
            || yVar != yChoice.getSelectedIndex())
                data.removeAllElements();

        xVar = xChoice.getSelectedIndex();
        yVar = yChoice.getSelectedIndex();
        String xName = (String)vars.elementAt(xVar),
            yName = (String)vars.elementAt(yVar);

    // Assemble information string
        StringBuffer b = new StringBuffer("# "
            + yChoice.getSelectedItem() + " vs. "
            + xChoice.getSelectedItem() + "\n");
        for (int i=0; i<intComps.size(); i++) {
            IntComponent c = (IntComponent)intComps.elementAt(i);
            b.append("#   " + c.getLabel() + ": "
                + c.getTextValue() + "\n");
        }
        for (int i=0; i<vars.size(); i++) {
            String vn = (String)vars.elementAt(i);
            if (!vn.equals(xName) && !vn.equals(yName)) {

                b.append("#   " + labs.elementAt(i) + " = "
                    + Utility.format(piface.getDVar(vn),5) + "\n");
            }
        }
        b.append(xName + "\t" + yName + "\n");
        data.addElement(new String(b));

        double x[] = new double[n], y[] = new double[n];
        double saved[] = piface.saveVars();
        for (int i=0; i<n; i++) {
            y[i] = piface.eval(yName, xName, xlo + i * xinc);
            x[i] = piface.getDVar(xName);
        }
        piface.restoreVars(saved);
        data.addElement(x);
        data.addElement(y);
    }

/**
* update xMin,...,yMax based on current data
*/
    void computeRanges() {
        boolean started = false;
        xMin = xMax = yMin = yMax = 0;

        if (data.size()==0) return;

        Enumeration enu = data.elements();
        while (enu.hasMoreElements()) {
            enu.nextElement();     // throw away info string
            double x[] = (double[]) enu.nextElement();
            double y[] = (double[]) enu.nextElement();
            int n = x.length;
            for (int i=0; i<n; i++) {
                if (Double.isNaN(x[i]+y[i])) continue;
                if (!started) { // catch the 1st valid data point
                    xMin = xMax = x[i];
                    yMin = yMax = y[i];
                    started = true;
                }
                if (x[i] < xMin) xMin = x[i];
                if (x[i] > xMax) xMax = x[i];
                if (y[i] < yMin) yMin = y[i];
                if (y[i] > yMax) yMax = y[i];
            }
        }
    }

/**
* Put plot data in a text window
*/
    void showData() {
        int n = 0;
        StringBuffer b = new StringBuffer();
        Enumeration enu = data.elements();
        while (enu.hasMoreElements()) {
            b.append("# Curve number " + ++n + "\n");
            b.append((String)enu.nextElement());
            double x[] = (double[])enu.nextElement(),
                y[] = (double[])enu.nextElement();
            for (int i=0; i<x.length; i++)
                b.append(Utility.format(x[i],5) + "\t"
                    + Utility.format(y[i],5) + "\n");
        }
        b.append("\n");
        ViewWindow vw = new ViewWindow("Plot data",25,40);
        vw.setText(new String(b));
    }



//====== Event handlers =============================

    public void paint (Graphics g) {
        if (init) return;

        setTitle (yChoice.getSelectedItem() + " vs. "
            + xChoice.getSelectedItem());
        Insets insets = getInsets();
        Dimension size = getSize();
        int i,
            w = size.width - 10 - insets.left - insets.right,
            h = size.height - 10 - insets.top - insets.bottom - cpHeight,
            lx = 5 + insets.left,
            ly = 5 + insets.top;
        if (w<=0 || h<=0) return;

        g.setColor(Color.white);
        g.fillRect(lx,ly,w,h);
        g.setColor(Color.lightGray);
        g.draw3DRect(lx,ly,w,h,false);
        g.setColor(Color.lightGray.darker());
        g.draw3DRect(lx+1,ly+1,w-2,h-2,false);

        if(xVar != xChoice.getSelectedIndex()
            || yVar != yChoice.getSelectedIndex()) computePlotData();

        if (data.size() == 0) return;

        computeRanges();
        if (w<0 || h<0) return;
        double xrng = 1.1*(xMax - xMin), yrng = 1.1*(yMax - yMin),
            x0 = xMin - .05*(xMax - xMin), y1 = yMax + .05*(yMax - yMin);
        if (xrng==0) return;
        if (yrng==0) {
            yrng = 1;
            y1 += yrng/2;
        }

        // figure out y axis
        double tick[] = Utility.nice(y1-yrng, y1, 5, false);
        String tickLab[] = Utility.fmtNice(tick);
        FontMetrics fm = getFontMetrics(scaleFont);
        int fontHeight = fm.getAscent(),
            ymarg = 2*fontHeight,
            xmarg = 0,
            t0, t1;
        for (i=0; i<tick.length; i++) {
            int wd = fm.stringWidth(tickLab[i]);
            if (wd > xmarg) xmarg = wd;
        }

        xmarg += fontHeight;
        lx += xmarg;
        ly += 5;
        w -= xmarg + 5;
        h -= ymarg + 5;

        // draw axes & y ticks
        g.setColor(Color.blue.darker());
        g.drawRect(lx,ly,w,h);
        g.setFont(scaleFont);
        t1 = lx - fontHeight/3;
        for (i=0; i<tick.length; i++) {
            int ty = (int)(ly + h * (y1 - tick[i]) / yrng);
            g.drawLine (lx, ty, t1, ty);
            int labw = fm.stringWidth(tickLab[i]);
            g.drawString(tickLab[i], lx - labw - fontHeight/2, ty + fontHeight/3);
        }

        // x ticks
        tick = Utility.nice(x0, x0+xrng, 5, false);
        tickLab = Utility.fmtNice(tick);
        t0 = ly + h;
        t1 = t0 + fontHeight/3;
        int laby = t1 + fontHeight;
        for (i=0; i<tick.length; i++) {
            int tx = (int)(lx + w * (tick[i] - x0) / xrng);
            g.drawLine(tx, t0, tx, t1);
            int labw = fm.stringWidth(tickLab[i]);
            g.drawString(tickLab[i], tx - labw/2, laby);
        }



        Color col[] = new Color[] {
            Color.black, Color.blue, Color.red, Color.orange,
            Color.green.darker(), Color.magenta };
        int c = 0;
        Enumeration enu = data.elements();
        while (enu.hasMoreElements()) {
            enu.nextElement();     // toss the info string
            g.setColor(col[c++ % col.length]);
            double x[] = (double[]) enu.nextElement();
            double y[] = (double[]) enu.nextElement();
            boolean hasStart = false;
            int ix0=0, iy0=0, ix1, iy1;
        plotLoop:
            for (i=0; i<x.length; i++) {
                if (Double.isNaN(x[i]+y[i])) {
                    hasStart = false;
                    continue plotLoop;
                }
                ix1 = (int)(lx + w * (x[i] - x0) / xrng);
                iy1 = (int)(ly + h * (y1 - y[i]) / yrng);
                if (!hasStart) {
                    ix0 = ix1;
                    iy0 = iy1;
                    hasStart = true;
                }
                g.drawLine (ix0, iy0, ix1, iy1);
                ix0 = ix1;
                iy0 = iy1;
            }
        }
    }

// A variable in the Piface has changed...
    public void piAction (String varName) {
        computePlotData();
        repaint();
    }

/**
* Display text from a file in a separate window
* The file should be in the same directory as clas
*/
    public static ViewWindow showText(Class clas, String filename,
        String title, int rows, int cols)
    {
        ViewWindow vw = new ViewWindow(title,rows,cols);
        try {
            vw.ta.setVisible(false);
            InputStream in = clas.getResourceAsStream(filename);
            BufferedReader br = new BufferedReader
                (new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null)
                vw.append(line + "\n");
            in.close();
            vw.setTop();
            vw.ta.setVisible(true);
        }
        catch (Exception e) { }
        return vw;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand().toString();
        if (cmd.equals("Close")) {
            close();
        }
        else if (cmd.equals("Help")) {
            showText(PiGraph.class, "PiGraphHelp.txt",
                "Graphics help", 25, 50);
    }
    else if (cmd.equals("About Piface")) {
        new AboutPiface();
    }
        else if (cmd.equals("Show Data")) {
            showData();
        }
        else if (cmd.equals("Draw")) {
//            if (init) { // first time - set up plot area with 4:3 aspect ratio
            setVisible(false);
            init = false;
            Dimension size = getSize();
            int h = 3 * (size.width - 10) / 4 + size.height + 10;
            setSize(size.width, h);
            dataButton.setVisible(true);
            drawButton.setLabel("Redraw");
            setVisible(true);
            xVar = xChoice.getSelectedIndex();
            yVar = yChoice.getSelectedIndex();
//            }
            computePlotData();
            repaint();
        }
        else if (cmd.equals("Redraw")) {
            computePlotData();
            repaint();
        }
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowActivated(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }

}
