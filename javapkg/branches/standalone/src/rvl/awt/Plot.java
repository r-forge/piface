/** <h2>General-purpose plot object</h2>
 * @author Russ Lenth
 * @version 1.0 January 9, 2001
 * Simple point and/or line plots.  Not too elaborate, but allows
 * multiple sets of points, different point sizes, and point/line colors.
 * <p>
 * Default plots use points but no lines, with a prescribed sequence of
 * sizes and colors.  Call appropriate methods to change these properties.
 * <p>
 * Accessor methods setData(), setDotColor(), setLineColor(), etc. exist to
 * set data and attributes.  Plotting routines will "wrap" the data and
 * attributes as needed; for example,
 * <code>
 * setDotSize(new int[]{new int[]{5,3}, new int[]{0}});
 * </code>
 * specifies that (x[0],y[0]) is plotted using points of alternating
 * sizes of 5 and 5, and (x[1],y[1]) is plotted using dots of size 0;
 * if the data exist, (x[2],y[2]) is plotted the same way as (x[0],y[0]),
 * (x[3],y[3]) is plotted the same way as (x[1],y[1]), etc.  Data are
 * wrapped in the same way; if there are more y vectors than x vectors,
 * x vectors are re-used in cyclic order.  If there are fewer points in
 * some y vector than its corresponding x vector, then the y values are
 * re-used.  If anything is null, it is skipped.  So to plot dots only for
 * the 0th and 2nd data set, use:
 * <code>
 * setLineColor(new Color[]{null, Color.Red, null});
 * </code>
 */

package rvl.awt;


import java.awt.*;

import rvl.awt.*;
import java.awt.image.*;
import rvl.util.*;

public class Plot extends Component {

    // Data arrays.  1st index is curve #
    protected double x[][], y[][];

    // pixel diam of dots
    protected int defaultDotSize = 5,
        dotSize[][] = new int[][] {new int[]{defaultDotSize}};

    // Default color sequence for dots and lines
    protected Color defaultColor[]
        = new Color[]{Color.black, Color.blue,
                      Color.red, Color.green.darker(),
                      Color.orange, Color.darkGray,
                      Color.red.darker(), Color.blue.darker()};

    // Axis, dot and line colors
    protected Color dotColor[][], lineColor[][],
        scaleColor = Color.blue.darker();

    // Tick values and labels, title and axis labels
    protected double xTick[], yTick[];
    protected String xTickLab[], yTickLab[],
        title[]=null, xAxisLab[]=null, yAxisLab[]=null;

    // Custom-label flags -- if false, numeric labels are used,
    // otherwise text labels are used.
    protected boolean xCustomLab = false, yCustomLab = false;

    // Flag for whether horiz. and vert. axes use same pixels per unit length
    protected boolean sameScale = false;

    protected double xMin, xMax, yMin, yMax;  // ranges of plot variables

    protected double h0, v0, h1, v1, mh, mv,
        hMin, hMax, vMin, vMax;    // scaling constants

    //!!!! protected Font scaleFont = new Font("SansSerif", Font.PLAIN, 9);

    protected boolean needsRescaling = true, swap = false;

    /**
     * Constructor for a simple plot of one variable against another.
     */
    public Plot(double x[], double y[]) {
        this (new double[][]{x}, new double[][]{y});
    }

    /**
     * Constructor for a multiple plot of several y[]s, all with same x[].
     */
    public Plot(double x[], double y[][]) {
        this (new double[][]{x}, y);
    }

    /**
     * General constructor: Plots y[0][] vs x[0][], y[2][] vs x[1][], ...
     * <p>
     * However, if x.length != y.length, array indices are modded-out.
     * For example, suppose x.length = 2 and y.length = 5, then plots are
     * of (x[0],y[0]), (x[1],y[1]), (x[0],y[2]), (x[1],y[3]), (x[0],y[4]).
     */
    public Plot(double x[][], double y[][]) {
        setData(x,y);
        setDotMode(true);
        setLineMode(false);
        setBackground(Color.white);
        setFont(new Font("SansSerif", Font.PLAIN, 9));
    }


//===== METHODS FOR SETTING DATA =====

    /**
     * Set data to be plotted (see constructor for details)
     * @param x Arrays of  <i>x</i> values
     * @param y Arrays of <i>y</i> values
     * @param rescale If true, the scaling of the plot is changed
     * to fit the data; otherwise, the same scale is used.
     * @see #Plot(double x[][], double y[][])
     */
    public void setData(double x[][], double y[][], boolean rescale) {
        this.x = x;
        this.y = y;
        needsRescaling = rescale;
        repaint();
    }

    /**
     * Set data and rescale the plot (see constructor for details).
     * Same as <code>setData (x, y, true)</code>
     * @param x Arrays of  <i>x</i> values
     * @param y Arrays of <i>y</i> values
     * @see setData(double x[][], double y[][], boolean rescale)
     */
    public void setData(double x[][], double y[][]) {
        setData(x,y,true);
    }

    /**
     * Set data for one set of <i>x</i> values and several sets
     * of <i>y</i> values.
     * @param x Array of  <i>x</i> values
     * @param y Arrays of <i>y</i> values
     * @param rescale If true, the scaling of the plot is changed
     * to fit the data; otherwise, the same scale is used.
     */
    public void setData(double x[], double y[][], boolean rescale) {
        setData(new double[][]{x}, y, rescale);
    }

    /**
     * Set data for one set of <i>x</i> values and several sets
     * of <i>y</i> values; and rescale the plot.
     * @param x Array of  <i>x</i> values
     * @param y Arrays of <i>y</i> values
     */
    public void setData(double x[], double y[][]) {
        setData(new double[][]{x}, y, true);
    }

    /**
     * Set data for one set of <i>x</i> values and one set
     * of <i>y</i> values.
     * @param x Array of  <i>x</i> values
     * @param y Array of <i>y</i> values
     * @param rescale If true, the scaling of the plot is changed
     * to fit the data; otherwise, the same scale is used.
     */
    public void setData(double x[], double y[], boolean rescale) {
        setData(new double[][]{x}, new double[][]{y}, rescale);
    }

    /**
     * Set data for one set of <i>x</i> values and one set
     * of <i>y</i> values; and rescale the plot.
     * @param x Array of  <i>x</i> values
     * @param y Array of <i>y</i> values
     */
    public void setData(double x[], double y[]) {
        setData(new double[][]{x}, new double[][]{y}, true);
    }

    /**
     * @return the <i>x</i> arrays
     */
    public double[][] getXData() {
        return x;
    }

    /**
     * Set <i>x</i> data to be plotted
     * @param x Arrays of  <i>x</i> values
     * @param rescale If true, the scaling of the plot is changed
     * to fit the data; otherwise, the same scale is used.
     */
    public void setXData(double x[][], boolean rescale) {
        this.x = x;
        needsRescaling = rescale;
        repaint();
    }

    /**
     * Set <i>x</i> data and rescale the plot
     * @param x Arrays of  <i>x</i> values
     */
    public void setXData(double x[][]) {
        setXData (x, true);
    }

    /**
     * @return the <i>y</i> arrays
     */
    public double[][] getYData() {
        return y;
    }

    /**
     * Set <i>y</i> data to be plotted
     * @param y Arrays of  <i>y</i> values
     * @param rescale If true, the scaling of the plot is changed
     * to fit the data; otherwise, the same scale is used.
     */
    public void setYData(double y[][], boolean rescale) {
        this.y = y;
        needsRescaling = rescale;
        repaint();
    }

    /**
     * Set <i>y</i> data and rescale the plot
     * @param y Arrays of  <i>y</i> values
     */
    public void setYData(double y[][]) {
        setYData (y, true);
    }



//===== METHODS FOR SETTING ATTRIBUTES OF POINTS AND LINES =====

    /**
     * Enable or disable plotting of symbols at <i>all</i> points
     * @param mode If true, all points be plotted with dots of
     * default size and colors.  If false, no symbols are plotted
     */
    public void setDotMode(boolean mode) {
        if (mode) {
            setDotSize(new int[]{defaultDotSize});
            setDotColor(defaultColor);
        }
        else
            setDotSize((int[][])null);
   }

    /**
     * Enable or disable plotting of lines for <i>all</i> data.
     * @param mode If true, each (<i>x,y</i>) pair of arrays will
     * be connected with lines of
     * default colors.  If false, no lines are plotted.
     */
    public void setLineMode(boolean mode) {
        if (mode) setLineColor(defaultColor);
        else setLineColor((Color[][])null);
    }

    /** Set dot color (same color for all points) */
    public void setDotColor(Color color) {
        dotColor = new Color[][] {new Color[] {color}};
    }

    /**
     * Set dot colors (constant color for each data set)
     * @param color array of colors.  The <i>i</i>th element specifies
     * the color to be used in plotting the <i>i</i>th curve
     */
    public void setDotColor(Color color[]) {
        dotColor = new Color[color.length][1];
        for (int i=0; i<color.length; i++) dotColor[i][0] = color[i];
    }

    /** Set dot colors (potentially individual for each point) */
    public void setDotColor(Color color[][]) {
        dotColor = color;
    }

    /** Set dot size (same size for all points) */
    public void setDotSize(int size) {
        dotSize = new int[][] {new int[] {size}};
    }

    /**
     * Set dot sizes (constant size for each data set)
     * @param size array of sizes.  The <i>i</i>th element specifies
     * the size of dots to be used in plotting the <i>i</i>th curve
     */
    public void setDotSize(int size[]) {
        dotSize = new int[size.length][1];
        for (int i=0; i<size.length; i++) dotSize[i][0] = size[i];
    }

    /** Set dot sizes (potentially individual for each point) */
    public void setDotSize(int size[][]) {
        dotSize = size;
    }

    /** Set line color (constant color for all curves) */
    public void setLineColor(Color color) {
        lineColor = new Color[][] {new Color[] {color}};
    }

    /** Set line colors (constant color for each data set) */
    public void setLineColor(Color color[]) {
        lineColor = new Color[color.length][1];
        for (int i=0; i<color.length; i++) lineColor[i][0] = color[i];
    }

    /** Set line colors (potentially individual for each point) */
    public void setLineColor(Color color[][]) {
        lineColor = color;
    }

    /** Set default sequence of colors to cycle through */
    public void setDefaultColor(Color color[]) {
        defaultColor = color;
    }

    /** Set default size of dots (width, in pixels) */
    public void setDefaultDotSize(int size) {
        defaultDotSize = size;
    }

    /** set Colors of axes */
    public void setScaleColor(Color color) {
        scaleColor = color;
    }

    /**
     * Enable/disable transposing the plot
     * @param swap If true, the <i>x</i> variable is the vertical
     * coordinate and the <i>y</i> variable is horizontal.
     */
    public void setTranspose(boolean swap) {
        this.swap = swap;
        needsRescaling = true;
    }

    /**
     * Enable/disable provision to keep scale constants equal
     * @param flag If true, scaling is done so that both axes use
     * the same number of pixels per unit scale value.
     */
    public void setSameScale(boolean flag) {
	this.sameScale = flag;
    }


// ===== METHODS TO SPECIFY LABELING AND TICK MARKS =====

    /**
     * Set axis labels.  The elements of each array are stacked
     * vertically.  When a label starts with "<" (">"), it is
     * left- (right-) justified when used with the horizonal axis.
     * Otherwise, it is centered.
     */
    public void setAxisLabels(String xLab[], String yLab[]) {
        xAxisLab = xLab;
        yAxisLab = yLab;
    }

    /**
     * Set single-line axis labels.
     * When a label starts with "<" (">"), it is
     * left- (right-) justified when used with the horizonal axis.
     * Otherwise, it is centered.
     */
    public void setAxisLabels(String xLab, String yLab) {
        setAxisLabels(new String[]{xLab}, new String[]{yLab});
    }

    /**
     * Set title.  Elements are stacked vertically at the top of the plot.
     * Justification rules are same as in <code>setAxisLabels</code>
     * @see #setAxisLabels
     */
    public void setTitle(String title[]) {
        this.title = title;
    }

    /**
     * Set a one-line title.
     * Justification rules are same as in <code>setAxisLabels</code>
     * @see setAxisLabels
     */
    public void setTitle(String title) {
        setTitle(new String[]{title});
    }

    /**
     * Enable or disable default tick marks and labels for each axis.
     * An argument of <code>true</code> enables them and places
     * tick marks at nice numbers.  An argument of <code>false</code>
     * disables both custom and default tick labeling for that axis.
     * @see setXTicks
     * @see setYTicks
     */
    public void setTickMode(boolean xHasThem, boolean yHasThem) {
        xCustomLab = !xHasThem;
        yCustomLab = !yHasThem;
        if (xCustomLab) {
            xTick = null;
            xTickLab = null;
        }
        if (yCustomLab) {
            yTick = null;
            yTickLab = null;
        }
    }

    /**
     * Set tick positions and labels for the x variable.
     * Calling this disables default tick labeling; to re-enable
     * the default behavior, call setTicks();
     * @see setTickMode
     */
    public void setXTicks(double tick[], String tickLab[]) {
        xTick = tick;
        xTickLab = tickLab;
        xCustomLab = true;
    }

    /**
     * Set tick positions and labels for the y variable.
     * Calling this disables default tick labeling; to re-enable
     * the default behavior, call setTicks();
     * @see setTickMode
     */
    public void setYTicks(double tick[], String tickLab[]) {
        yTick = tick;
        yTickLab = tickLab;
        yCustomLab = true;
    }





    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0,0,getSize().width,getSize().height);
        if (needsRescaling)
            rescale();
        setScales();
        drawBox(g);
        if (swap) {
            drawVAxis(g, xTick, xTickLab, xAxisLab);
            drawHAxis(g, yTick, yTickLab, yAxisLab);
            drawLines(g, y, x);
            drawDots(g, y, x);
        }
        else {
            drawVAxis(g, yTick, yTickLab, yAxisLab);
            drawHAxis(g, xTick, xTickLab, xAxisLab);
            drawLines(g, x, y);
            drawDots(g, x, y);
        }
    }

    /** @return true for any kind of invalid double value */
    protected boolean isBad(double x) {
        return Double.isNaN(x) || Double.isInfinite(x);
    }

    /** Set values if xMin, xMax, yMin, yMax */
    protected void rescale() {
        xMin = yMin = Double.POSITIVE_INFINITY;
        xMax = yMax = Double.NEGATIVE_INFINITY;

        for (int j=0; j< x.length; j++) {
            for (int i=0; i<x[j].length; i++) {
                double xx = x[j][i];
                if(isBad(xx)) continue;
                xMax = Math.max(xMax, xx);
                xMin = Math.min(xMin, xx);
            }
        }
        if (Double.isInfinite(xMin) || Double.isInfinite(xMax))
            xMin = xMax = 0;
        if (xMin >= xMax) {
            xMin -= .5;
            xMax += .5;
        }
        double f = .05 * (xMax - xMin);
        xMax += f;
        xMin -= f;

        for (int j=0; j< y.length; j++) {
            for (int i=0; i<y[j].length; i++) {
                double yy = y[j][i];
                if(isBad(yy)) continue;
                yMax = Math.max(yMax, yy);
                yMin = Math.min(yMin, yy);
            }
        }
        if (Double.isInfinite(yMin) || Double.isInfinite(yMax))
            yMin = yMax = 0;
        if (yMin >= yMax) {
            yMin -= .5;
            yMax += .5;
        }
        f = .05 * (yMax - yMin);
        yMax += f;
        yMin -= f;

        if (!xCustomLab) {
            xTick = Utility.nice(xMin, xMax, 5, false);
            xTickLab = Utility.fmtNice(xTick);
        }

        if (!yCustomLab) {
            yTick = Utility.nice(yMin, yMax, 5, false);
            yTickLab = Utility.fmtNice(yTick);
        }

        needsRescaling = false;
    }

    /** @return maximum width of tick labels, in pixels

    *** Commented-out ***

    private int maxWidth(String tickLab[]) {
        if (tickLab == null) return 0;
        int i, w = 0;
        FontMetrics fm = getFontMetrics(getFont());
        for (i=0; i<tickLab.length; i++)
            w = Math.max(w, fm.stringWidth(tickLab[i]));
        return w;
    }
    */

    /** set scaling constants for horiz. and vert. coordinates */
    private void setScales() {
        Dimension s = getSize();
        FontMetrics fm = getFontMetrics(getFont());
        int fh = fm.getHeight(), vth, hth, ttlh;

	if (sameScale) { // adjust one variable's range so scaling is right
	    double hRng = swap ? yMax - yMin : xMax - xMin,
		vRng = swap ? xMax - xMin : yMax - yMin,
		hPix = s.width, vPix = s.height,
		newRng, incr;
	    boolean itsX;
	    if (vRng / vPix < hRng / hPix) { // expand vert scale
		newRng = vPix * hRng / hPix;
		incr = 0.5 * (newRng - vRng);
		itsX = swap;
	    }
	    else {                           // expand horz scale
		newRng = hPix * vRng / vPix;
		incr = 0.5 * (newRng - hRng);
		itsX = !swap;
	    }
	    if (itsX) {
		xMin -= incr;
		xMax += incr;
		if (!xCustomLab) {
		    xTick = Utility.nice(xMin, xMax, 5, false);
		    xTickLab = Utility.fmtNice(xTick);
		}
	    }
	    else {
		yMin -= incr;
		yMax += incr;
		if (!yCustomLab) {
		    yTick = Utility.nice(yMin, yMax, 5, false);
		    yTickLab = Utility.fmtNice(yTick);
		}
	    }
	}

        if (swap) {
            hMin = yMin;
            hMax = yMax;
            vMin = xMin;
            vMax = xMax;
            hth = (yTickLab == null ? 0 : fh);
            if (yAxisLab != null)
                hth += fh * yAxisLab.length;
            vth = (xTickLab == null ? 0 : fh);
            if (xAxisLab != null)
                vth += fh * xAxisLab.length;
        }
        else {
            hMin = xMin;
            hMax = xMax;
            vMin = yMin;
            vMax = yMax;
            hth = (xTickLab == null ? 0 : fh);
            if (xAxisLab != null)
                hth += fh * xAxisLab.length;
            vth = (yTickLab == null ? 0 : fh);
            if (yAxisLab != null)
                vth += fh * yAxisLab.length;
        }

        ttlh = (title == null ? -fh / 2 : fh * title.length);
        v0 = fh + ttlh;
        v1 = s.height - fh - hth;
        h0 = fh + vth;
        h1 = s.width - fh / 2;

        mh = (h1 - h0) / (hMax - hMin);
        mv= (v1 - v0) / (vMax - vMin);
    }

    /** Draw box around plotting region */
    private void drawBox(Graphics g) {
        g.setColor(scaleColor);
        g.drawRect((int)h0, (int)v0, (int)(h1-h0), (int)(v1-v0));
    }

    /** Draw horizontal axis and title(s) */
    private void drawHAxis(Graphics g, double tick[], String tickLab[],
                           String label[]) {
        if (tick==null && label==null) return;
        FontMetrics fm = getFontMetrics(getFont());
        int x, fh = fm.getHeight(),
            t0 = (int)v1, t1 = t0 + fh/3,
            laby = (int)(v1 + 1.25*fh);
        g.setColor(scaleColor);
        g.setFont(getFont());
        if (tick != null) for (int i=0; i<tick.length; i++) {
            x = (int)(h0 + mh * (tick[i] - hMin));
            g.drawLine(x, t0, x, t1);
            if (tickLab!=null) {
                x -= fm.stringWidth(tickLab[i]) / 2;
                g.drawString(tickLab[i], x, laby);
            }
        }
        else
            laby -= fh;

        if (label != null) {
            laby += fh/2 - fm.getDescent();
            for (int i=0; i<label.length; i++) {
                laby += fh;
                String lab = label[i];
                char first = lab.toCharArray()[0];
                switch (first) {
                case '>' :
                    lab = lab.substring(1);
                    x = (int)(h1 - fm.stringWidth(lab));
                    break;
                case '<' :
                    lab = lab.substring(1);
                    x = (int) h0;
                    break;
                default:
                    x = (int)(.5*(h0 + h1 - fm.stringWidth(lab)));
                }
                g.drawString(lab, x, laby);
            }
        }

        if (title == null) return;
        laby = fh;
        int ww = getSize().width;
        for (int i=0; i<title.length; i++) {
            String lab = title[i];
            char first = lab.toCharArray()[0];
            switch (first) {
            case '>' :
                lab = lab.substring(1);
                x = ww - fm.stringWidth(lab) - fh / 2;
                break;
            case '<' :
                lab = lab.substring(1);
                x = fh / 2;
                break;
            default:
                x = (ww - fm.stringWidth(lab)) / 2;
            }
            g.drawString(lab, x, laby);
            laby += fh;
        }

    }

    /* ******* OLD VERSION
    private void drawVAxis(Graphics g, double tick[], String tickLab[],
                           String label[]) {
        if (tick==null && label==null) return;
        FontMetrics fm = getFontMetrics(getFont());
        int x, y, fh = fm.getAscent(), t0 = (int)h0, t1 = (int)(t0 - fh/3),
            labx = (int)(h0 - .5*fh);
        g.setColor(scaleColor);
        g.setFont(getFont());
        if (tick != null) for (int i=0; i<tick.length; i++) {
            y = (int)(v0 + mv * (vMax - tick[i]) );
            g.drawLine(t0, y, t1, y);
            if (tickLab != null) {
                y += fh/2;
                x = labx - fm.stringWidth(tickLab[i]);
                g.drawString(tickLab[i], x, y);
            }
        }
        if (label == null) return;
        x = fh/2;
        y = 0;
        for (int i=0; i<label.length; i++) {
            String lab = label[i];
            y += fh;
            char first = lab.toCharArray()[0];
            if (first == '<' || first == '>')
                lab = lab.substring(1);
            g.drawString(lab, x, y);
        }
    }
    *********************/

    /** Draw vertical axis */
    private void drawVAxis(Graphics g, double tick[], String tickLab[],
                           String label[]) {

        if (tick==null && label==null) return;

        FontMetrics fm = getFontMetrics(getFont());
        int x, fh = fm.getHeight(),
            t0 = (int)h0, t1 = (int)(t0 - fh/3),
            laby = (int)(h0 - .5*fh),
            ww = (int)h0, hh = getSize().height;
        double left = hh - v1, right = hh - v0;

        Image img = createImage(hh, ww);
        Graphics ig = img.getGraphics();
        ig.setColor(getBackground());
        ig.fillRect(0,0,hh,ww);
        ig.setColor(scaleColor);
        ig.setFont(getFont());
        if (tick != null) for (int i=0; i<tick.length; i++) {
            x = (int)(left + mv * (tick[i] - vMin));
            ig.drawLine(x, t0, x, t1);
            if (tickLab != null) {
                x -= fm.stringWidth(tickLab[i]) / 2;
                ig.drawString(tickLab[i], x, laby);
            }
        }
        else
            laby += fh;

        if (label==null) return;
        laby -= fh;
        for (int i=0; i<label.length; i++) {
            String lab = label[i];
            char first = lab.toCharArray()[0];
            switch (first) {
            case '>' :
                lab = lab.substring(1);
                x = (int)(right - fm.stringWidth(lab));
                break;
            case '<' :
                lab = lab.substring(1);
                x = (int) left;
                break;
            default:
                x = (int)(.5*(left + right - fm.stringWidth(lab)));
            }
            ig.drawString(lab, x, laby);
            if (i == 0)
                laby = fh;
            else
                laby += fh;
        }

        ig.dispose();
        PixelGrabber grabber = new PixelGrabber(img, 0, 0, -1, -1, true);
        try {
            if (grabber.grabPixels()) {
                int pix[] = (int[]) grabber.getPixels(),
                    rot[] = new int[pix.length],
                    h1 = hh - 1, w1 = ww - 1;
                for (int i=0; i<hh; i++)  for (int j=0; j<ww; j++)
                    rot[j + ww*(h1 - i)] = pix[i + hh*j];
                img = createImage(new MemoryImageSource(ww, hh, rot, 0, ww));
                g.drawImage(img,0,0,null);
            }
        }
        catch (InterruptedException ie) {}
    }

    /** Draw lines specified by horiz. & vert. coords */
    private void drawLines(Graphics g, double h[][], double v[][]) {
        if (lineColor == null) return;
        int kh = h.length, kv = v.length,
            kl = lineColor.length, k = Math.max(kh,kv);
        for (int j=0; j<k; j++)
            drawLines(g, h[j % kh], v[j % kv], lineColor[j % kl]);
    }

    /** Draw one set of lines */
    private void drawLines(Graphics g, double h[], double v[], Color c[]) {
        if (c==null) return;
        int nh = h.length, nv = v.length, nc = c.length, n = Math.max(nh, nv);
        int oldH = 0, oldV = 0, H, V;
        boolean prevPointExists = false;
    loop:
        for (int i=0; i<n; i++) {
            double hh = h[i % nh], vv =  v[i % nv];
            if (isBad(hh) || isBad(vv)) {
                prevPointExists = false;
                continue loop;
            }
            H = (int)(h0 + mh * (hh - hMin));
            V = (int)(v0 + mv * (vMax - vv));
            if (prevPointExists)
                g.drawLine(oldH, oldV, H, V);
            oldH = H;
            oldV = V;
            Color cc = c[i % nc];
            if (cc == null)
                prevPointExists = false;
            else {
                g.setColor(cc);
                prevPointExists = true;
            }
        }
    }

    /** Draw dots specified by horiz. & vert. coords */
    private void drawDots(Graphics g, double h[][], double v[][]) {
        if (dotColor == null || dotSize == null) return;
        int kh = h.length, kv = v.length, ks = dotSize.length,
            kc = dotColor.length, k = Math.max(kh,kv);
        for (int j=0; j<k; j++)
            drawDots(g, h[j % kh], v[j % kv],
                dotColor[j % kc], dotSize[j % ks]);
    }

    /** Draw one set of dots */
    private void drawDots(Graphics g, double h[], double v[],
        Color c[], int size[]) {
        if (c==null || size==null) return;
        int nh = h.length, nv = v.length, nc = c.length, ns = size.length,
            n = Math.max(nh, nv);
        int H, V, s, s2;
    loop:
        for (int i=0; i<n; i++) {
            s = size[i % ns];
            Color col = c[i % nc];
            if ((s <= 0) || col == null) continue loop;
            double hh = h[i % nh], vv =  v[i % nv];
            if (isBad(hh) || isBad(vv)) continue loop;
            s2 = s/2;
            H = (int)(h0 - s2 + mh * (hh - hMin));
            V = (int)(v0 - s2 + mv * (vMax - vv));
            g.setColor(col);
            g.drawOval(H, V, s, s);
            g.fillOval(H, V, s, s);
        }
    }

    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int em = fm.stringWidth("m"),
            ascent = fm.getAscent();
        return new Dimension(24*em, 16*ascent);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

// for compatibility with 1.0 layout stuff...
    public Dimension preferredSize() {
        return getPreferredSize();
    }

    public Dimension minimumSize() {
        return getMinimumSize();
    }



}
