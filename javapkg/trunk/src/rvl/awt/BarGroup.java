package rvl.awt;

import rvl.util.*;
import java.util.Vector;
import java.awt.*;

public class BarGroup extends Canvas {
    protected double maxVal = 0, limit, tickInterval,
        factor = Math.pow(2,.25); 
    protected Frame parentFrame;
    protected Color barColor=Color.red, tickColor=Color.blue,
        lineColor = Color.white;
    protected boolean rescalable = true, readOnly = false;

    Vector bar;
    private Font labFont;
    private FontMetrics labMetrics;
    private boolean fontSet=false;
    private int labHeight, xDown;

    public BarGroup (double lim, Frame f) {
        bar = new Vector();
        setLimit(lim, false);
        parentFrame = f;
    }

    public void addBar(Bar b, double v){
        bar.addElement(b);
        maxVal = Math.max(v, maxVal);
        if (maxVal > limit) setLimit(maxVal,false);
    }

    public void setRescalable(boolean a) {
        rescalable = a;
    }

    public void setReadOnly(boolean a) {
        readOnly = a;
    }

    Bar getBar(int i) {
        return (Bar)bar.elementAt(i);
    }

    static final double ln10 = Math.log(10);
    static double log10(double x) {
        return Math.log(x) / ln10;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double lim, boolean Paint) {
        maxVal = 0;
        if (rescalable) {
            for (int i=0; i<bar.size(); i++)
                maxVal = Math.max(maxVal, getBar(i).getValue());
            if (lim < maxVal) lim = maxVal*1.01;
            limit = lim;
        }
        double nice = Math.pow(10,Math.floor(log10(limit))),
            s = limit/nice;
        tickInterval = 
            (s<=1.5) ? nice/5 : (s<=4.0) ? nice/2 : nice;

        if (Paint) {
            for (int i=0; i<bar.size(); i++)
                getBar(i).repaint();
            repaint();
        }
    }

/** used by bars to notify of new value;
  * will set new limit if necessary.
  * @return true if scale limit has changed
  */
    boolean checkValue(double v) {
        if (v <= maxVal) return false;
        setLimit(v*1.05, true);
        return true;
    }

    public void grow() {
        setLimit(factor*limit, true);
    }

    public void shrink() {
        setLimit(limit/factor, true);
    }

    private void setFont(Graphics g) {
        labFont = new Font("Helvetica", Font.PLAIN, 9);
        labMetrics = g.getFontMetrics(labFont);
        labHeight = labMetrics.getAscent();
        fontSet = true;
    }

/** set color of bars */
    public void setColor(Color c) {
        barColor = c;
    }
/** set color of tickmarks */
    public void setTickColor(Color c) {
        tickColor = c;
    }
/** set color of center line */
    public void setBackground(Color c) {
        lineColor = c;
    }

    public Dimension preferredSize() {
        return new Dimension(300,15);
    }

    public Dimension minimumSize() {
        return preferredSize();
    }

    public void paint(Graphics g) {
        if(!fontSet) setFont(g);
        int w = size().width-10, 
            c = (int)((size().height + labHeight)/2);
        double s = w / limit;
        g.setFont(labFont);
        g.setColor(tickColor);
        int tail = -100;
        for (double x = 0; x < 1.01*limit; x += tickInterval) {
            String lab = niceLabel(x);
            int wid = labMetrics.stringWidth(lab);
            int h = (int)(5.5 + x*s - wid/2);
            if (h+wid > 5+w) h = 5+w-wid;
            if (h > 5 + tail) {
                g.drawString(lab, h, c);
                tail = h+wid;
            }
        }
    }
    
    private String niceLabel(double x) {
        int digits = 1;
        String lab = Utility.format(x,digits);
        double fuzz = .01 * Math.abs(x);
        while((digits<10) && (Math.abs(Utility.strtod(lab) - x) > fuzz))
            lab = Utility.format(x, ++digits);
        return lab;             
    }

    public boolean mouseEnter (Event e, int x, int y) {
        if (!rescalable) return true;
        parentFrame.setCursor(Frame.E_RESIZE_CURSOR);
        return true;
    }
    public boolean mouseExit (Event e, int x, int y) {
        if (!rescalable) return true;
        parentFrame.setCursor(Frame.DEFAULT_CURSOR);
        return true;
    }

    public boolean mouseDown (Event e, int x, int y) {
        requestFocus();
        if (!rescalable) return true;
        xDown = x >= 10 ? x : 10;
        return true;
    }
    public boolean mouseUp (Event e, int x, int y) {
        if (!rescalable) return true;
        x = x >= 10 ? x : 10;
        double s = (xDown + 0.0) / x;
        setLimit(s*limit, true);
        return true;
    }

}
