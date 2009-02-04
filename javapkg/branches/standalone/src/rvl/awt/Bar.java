package rvl.awt;

import java.awt.*;
import java.awt.event.*;	// addition for 1.1 implementation

/**
 * GUI object for implementing a bar-graph-style method
 * for I/O of nonnegative quantities.  A single mouse click
 * in the active range will set its value.<br>
 * Typically, several <b>Bar</b> objects will be stacked
 * in a single panel.  They share a common scale determined
 * by the <b>BarGroup</b> object passed in the constructor.
 * You will probably want to add the associated BarGroup 
 * to the same panel, since it shows the scale values.
 * @author Russ Lenth
 * @version 1.0 July 11, 1996
 * @see BarGroup
 */
public class Bar extends java.awt.Canvas {
    private double value;   // current value
    private int intVal;     // current value if integer
    private BarGroup group; // common-scale family
    private int hotMaxX, hotMinY, hotMaxY;  // cursor hotspots
    private Object barID;   // argument passed to event handlers
    private boolean intMode = false;
    protected transient ActionListener actionListener = null;

/**
 * @param   ID  object that will be passed as event-handler argument
 *              when a new value is selected
 * @param   val initial value
 * @param   grp which bar group is used for scaling
 */
    public Bar (Object ID, double val, BarGroup grp) {
        barID = ID;
        group = grp;
        value = Double.isNaN(val) ? 0d : Math.max(val, 0d);
        grp.addBar(this, val);
    }

/**
 * @param   ID  object that will be passed as event-handler argument
 *              when a new value is selected
 * @param   val initial value
 * @param   grp which bar group is used for scaling
 */
    public Bar (Object ID, int val, BarGroup grp) {
        barID = ID;
        group = grp;
        intMode = true;
        intVal = val;
        value = (double)intVal;
        grp.addBar(this, value);
    }

// Required for Java 1.1-style event handling...

    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }

    public double getValue() {
        return value;
    }
    
    public int getIntVal() {
        if (intMode)
            return intVal;
        else
            return (int)(value + .5);
    }

    public void setValue(double v) {
        if (Double.isNaN(v)) v = 0;
        value = Math.max(v, 0d);
        if (!group.rescalable) {
            value = Math.min(value, group.limit);
            repaint();
            return;
        }
        if (!group.checkValue(value)) repaint();
    }
    
    public void setValue(int v) {
        intVal = v;
        setValue((double)v);
    }

    public Dimension preferredSize() {
        return new Dimension(300,15);
    }

    public Dimension minimumSize() {
        return preferredSize();
    }

    public void paint(Graphics g) {
        int w = size().width-10, 
            c = (int)(size().height/2),
            c1 = c-4, c2 = c+4;
        hotMaxX = w+5;
        hotMinY = c-2;
        hotMaxY = c+2;
        double s = w / group.limit;
        g.setColor(group.lineColor);
        g.drawLine(5,c, hotMaxX,c);
        g.setColor(group.tickColor);
        for (double x = 0; x < 1.01*group.limit; x += group.tickInterval) {
            int h = (int)(5.5 + x*s);
            g.drawLine(h,c1, h,c2);
        }
        int h = (int)(.5 + value*s);
        g.setColor(group.barColor);
        g.fillRect(5,hotMinY, h,5);
    }

    private boolean isHot(int x, int y) {
        return y>=hotMinY && y<=hotMaxY 
            && x>=5 && x<=hotMaxX;
    }

    public boolean mouseEnter(Event e, int x, int y) {
        if (group.readOnly)
            group.parentFrame.setCursor(Frame.DEFAULT_CURSOR);
        else {
            group.parentFrame.setCursor(Frame.CROSSHAIR_CURSOR);
        }
        return true;
    }

    public boolean mouseExit(Event e, int x, int y) {
        group.parentFrame.setCursor(Frame.DEFAULT_CURSOR);
        return true;
    }

/******************************
    public boolean mouseMove(Event e, int x, int y) {
        if (isHot(x,y))
            group.parentFrame.setCursor(Frame.CROSSHAIR_CURSOR);
        else
            group.parentFrame.setCursor(Frame.DEFAULT_CURSOR);
        return true;
    }
*********************/

    public boolean mouseDown(Event e, int x, int y) {
        requestFocus();
        return true;
    }

    public boolean mouseUp(Event e, int x, int y) {
        if (group.readOnly) return true;
        if (!isHot(x,y)) return true;
        value = group.limit*(x - 5)/(size().width - 10);
        if (intMode) {
            intVal = (new Double(value+.5)).intValue();
            value = (int)intVal;
        }
        repaint();
// 1.1 addition:
        if (actionListener != null) {       // 1.1 impl
            ActionEvent ae = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED, barID.toString());
            actionListener.actionPerformed(ae);
        }
        else                                // 1.0 impl
            deliverEvent(new Event(this, Event.ACTION_EVENT, barID));
        group.parentFrame.setCursor(Frame.CROSSHAIR_CURSOR);
        return true;
    }

    public boolean mouseDrag(Event e, int x, int y) {
        if (group.readOnly) return true;
        int xx = (x<5) ? 5 : (x>hotMaxX) ? hotMaxX : x;
        value = group.limit*(xx - 5)/(size().width - 10);
        if (intMode) {
            intVal = (new Double(value)).intValue();
            value = (int)intVal;
        }
        repaint();
        deliverEvent(new Event(this, Event.ACTION_EVENT, barID));
        return true;
    }

}
