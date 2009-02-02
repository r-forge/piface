// NOTE: Uses java 1.1 event model.

/**
* GUI interface to display a set of numbers as a dotplot and to
* provide for manipulating values with the mouse by dragging dots
*/

package rvl.awt;

import java.awt.*;
import java.awt.event.*;
import rvl.stat.*;
import rvl.util.*;

public class Dotplot
    extends Component
    implements MouseListener, MouseMotionListener
{
    private double values[],    // values to be displayed
        tick[];                 // tick marks
    private int n,              // length of values, for convenience
        em, w, y,               // scale start, width, and centerline
        which = -1;             // index of point being dragged, <0 if none,
                                // -99 if moving the mean
    private double min, max;    // range of values
    private double mean;        // mean of values
    private double binW;        // bin width for dots
    private double prevVal;     // used to track value of point being dragged
    private String tickLab[];   // tick labels
    private Cursor pointer = Cursor.getDefaultCursor(),
        finger = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
	crosshairs = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private transient ActionListener actionListener = null;

/**
* Constructor: Give initial values
*/
    public Dotplot(double values[]) {
        setValues(values);
        Font font = new Font("SansSerif", Font.PLAIN, 9);
        setFont(font);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setValues(double v[]) {
        if (v==null) v = new double[]{0};   // protection
        n = v.length;
        values = new double[n];
        for (int i=0; i<n; i++)
            values[i] = v[i];
        setRange();
    }

    public double[] getValues() {
        return values;
    }

    private void setRange() {
        Sort.qsort(values);
        min = values[0];
        max = values[n-1];
        if (min == max) {
            min -= .5;
            max += .5;
        }
        double slop = .05 * (max - min);
        min -= slop;
        max += slop;
        tick = Utility.nice(min,max,5,false);
        tickLab = Utility.fmtNice(tick);
	mean = Stat.mean(values);
    }

    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int em = fm.stringWidth("m"),
            ascent = fm.getAscent();
        return new Dimension(18*em, 6*ascent);
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

 /**
 * Add an action listener for this component
 */
    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }

    public void paint(Graphics g) {
        if (!isVisible()) return;
        FontMetrics fm = getFontMetrics(getFont());
        em = fm.stringWidth("m");
        w = getSize().width - 2*em;
        int ascent = fm.getAscent(),
            h = getSize().height,
            inc, x, x0, y0, y1;

    // Draw scale, ticks, and labels
        g.setFont(getFont());
        y = h - 2 * ascent;
        g.setColor(Color.blue);
        g.drawLine(em, y, w + em, y);
        y0 = y + ascent/3;
        y1 = y + 3 * ascent / 2;
        inc = (int)((w + 0.0) * (tick[1] - tick[0]) / (max - min));
        for (int i=0; i<tick.length; i++) {
            x = em + (int)((w + 0.0)*(tick[i] - min)/(max - min));
            x0 = x - fm.stringWidth(tickLab[i]) / 2;
            g.drawLine(x, y, x, y0);
            g.drawString(tickLab[i], x0, y1);
        }

    // draw dots
        int nBins = inc / em;       // # dot bins in each tick interval
        if (nBins == 0) nBins = 1;
        binW = (tick[1] - tick[0]) / nBins;  // bin width
        x0 = 0;     // previous dot pos
        for (int i=0; i<n; i++) {
            double rndVal = binW * Math.round(values[i] / binW);
            x = em/2 + 1 + (int)((w + 0.0) * (rndVal - min) / (max - min));
            y0 = (x == x0 ? y0 - em : y - em);
            x0 = x;
            if (i==which) g.setColor(Color.lightGray);
            else g.setColor(Color.black);
            if (y0 < -em/2) {
                y0 += em;
                g.setColor(Color.red);
            }
            g.drawOval(x0,y0,em-2,em-2);
            g.fillOval(x0,y0,em-2,em-2);
        }
	// draw mean
            double rndVal = binW * Math.round(mean / binW);
            x = em + (int)((w + 0.0) * (rndVal - min) / (max - min));
	    Polygon poly = new Polygon(new int[]{x-em/2,x,x+em/2},
				       new int[]{y+em,y,y+em}, 3);
	    if (which == -99)
		g.setColor(Color.lightGray);
	    else
		g.setColor(Color.red.darker());
	    g.drawPolygon(poly);
	    g.fillPolygon(poly);
    }

    // draw dot at prevVal, in XOR mode
    private void drawMovingDot() {
        int x0 = em/2 + 1 + (int)((w + 0.0) * (prevVal - min) / (max - min));
        Graphics g = getGraphics();
        g.setColor(Color.red);
        g.setXORMode(Color.green);
        if (which == -99) { // draw the mean
	    x0 -= 1 - em/2;
	    g.drawPolygon(new int[] {x0-em/2, x0, x0+em/2},
			  new int[] {y+em, y, y+em}, 3);
	}
	else
	    g.drawOval(x0, y-em, em-2, em-2);
    }

    // Rescale temporariliy for mouse-drag purposes
    private void tempRescale(double mn, double mx) {
        while (mn >= mx) {
            mn -= .5;
            mx += .5;
        }
        min = mn;
        max = mx;
        tick = Utility.nice(min,max,5,false);
        tickLab = Utility.fmtNice(tick);
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        which = -1;
        double x = min + (max - min) * (e.getX() - em + 0.0) / w;
        if (e.getY() < y) for (int i=0; i<n && which < 0; i++) {
            if (2 * Math.abs(values[i] - x) < binW) {
                which = i;
                repaint();
                prevVal = values[i];
                drawMovingDot();
            }
	}
	else if (2 * Math.abs(mean - x) < binW) {
	    which = -99;
	    repaint();
	    prevVal = mean;
	    drawMovingDot();
	}
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
	if (which == -99) {
	    setCursor(pointer);
	    which = -1;
	    double shift = binW*Math.round(prevVal/binW) - mean;
	    for (int i=0; i<values.length; i++)
		values[i] += shift;
	    setCursor(pointer);
	    setRange();
	    repaint();
	    if (actionListener != null)
		actionListener.actionPerformed(
		    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
			"Dotplot:Shift"));
	    return;
	}
        else if (which < 0) return;
        values[which] = binW*Math.round(prevVal/binW);
        which = -1;
        setCursor(pointer);
        setRange();
        repaint();
        if (actionListener != null)
            actionListener.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    "Dotplot:Point"));
    }

    public void mouseDragged(MouseEvent e) {
        if (which < 0 && which != -99) return;
        double x = min + (max - min) * (e.getX() - em + 0.0) / w,
            smidge = .01 * (max - min);
        drawMovingDot();
        if (x < min + smidge)
            tempRescale(x - smidge, max);
        if (x > max - smidge)
            tempRescale(min, x + smidge);
        prevVal = x;
        drawMovingDot();
    }

    public void mouseMoved(MouseEvent e) {
        double x = min + (max - min) * (e.getX() - em + 0.0) / w;
        int wh = -1, y0 = e.getY();
        Cursor cur = getCursor();
	if (y0 >= y && y0 <= y + em) {
	    if (2 * Math.abs(x - mean) < binW) {
		if (cur != crosshairs) setCursor(crosshairs);
		wh = 99;
	    }
	}
        else if (y0 < y && y0 > y - em) {
            for (int i=0; i<n && wh < 0; i++)
                if (2 * Math.abs(values[i] - x) < binW) {
                    wh = i;
                    if (cur != finger) setCursor(finger);
                }
        }
        if (wh < 0 && cur != pointer) setCursor(pointer);
    }
}
