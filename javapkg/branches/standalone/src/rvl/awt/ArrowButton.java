package rvl.awt;

import java.awt.*;

/**
 * Button that has an up- or down-arrow on it
 */
public class ArrowButton extends Canvas {
    private boolean upward, incFlag=false;
    private Polygon arrow;
    private int wid, ht;

    public ArrowButton(boolean up, int width, int height) {
        upward = up;
        wid = width;  
        ht = height;
        resize(width,height);
    }

    public Dimension preferredSize() {
        return new Dimension(wid,ht);
    }

    public void resize(int width, int height) {
        super.resize(width, height);
        int w = size().width, h = size().height, w2=w/2, w4=w/4, h4=h/4;       
        arrow = new Polygon();
        if (upward) {
            arrow.addPoint(w4, h-h4);
            arrow.addPoint(w2, h4);
            arrow.addPoint(w-w4, h-h4);
        }
        else {
            arrow.addPoint(w4, h4);
            arrow.addPoint(w2, h-h4);
            arrow.addPoint(w-w4, h4);
        }
    }

    public void draw(Graphics g, boolean raised) {
        int w = size().width, h = size().height;
        g.setColor(Color.lightGray);
        g.fill3DRect(0,0,w,h,raised);
        g.setColor(Color.black);
        g.fillPolygon(arrow);
    }

    public void paint(Graphics g) {
        draw(g, true);
    }

    public boolean mouseDown(Event e, int x, int y) {
        draw(getGraphics(), false);
        incFlag = true;
        return true;
    }
    public boolean mouseExit(Event e, int x, int y) {
        draw(getGraphics(), true);
        incFlag = false;
        return true;
    }
    public boolean mouseUp(Event e, int x, int y) {
        draw(getGraphics(), true);
        if (incFlag)
            deliverEvent(new Event(this, Event.ACTION_EVENT, 
                new String(upward ? "up" : "down")));
        incFlag = false;
        return true;
    }  
}
