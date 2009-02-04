/** <h2>Interactive plot</h2>
 * @author Russ Lenth
 * @version 1.0 January 15, 2001
 */

package rvl.awt;

import rvl.awt.*;

import java.awt.*;
import java.awt.event.*;

public class IntPlot 
    extends Plot 
    implements MouseListener, MouseMotionListener
{

    /**
     * Place to register actionListeners
     * @see #addActionListener
     */
    private transient ActionListener actionListener = null;

    private boolean 
	isMoving = false,
	enableMoves = true,
	constrainX = false,
	constrainY = false;

    private int index[];        // holds indices for point bing moved
    private double point[];     // holds orig x[jx][ix], y[jy][iy]

    private Cursor finger = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
	pointer = Cursor.getDefaultCursor();
	

    /**
     * Constructor for general case.
     * @see Plot
     */
    public IntPlot(double x[][], double y[][]) {
	super(x,y);
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    /**
     * Constructor for a simple plot of one variable against another.
     */
    public IntPlot(double x[], double y[]) {
        this (new double[][]{x}, new double[][]{y});
    }

    /**
     * Constructor for a multiple plot of several y[]s, all with same x[].
     */
    public IntPlot(double x[], double y[][]) {
        this (new double[][]{x}, y);
    }

    /**
     * @param moveable if true, the user may move points using the mouse
     */
    public void setMoveable(boolean moveable) {
	enableMoves = moveable;
	isMoving = false;
    }
    public boolean isMoveable() {
	return enableMoves;
    }

    /** Set constraints on directions points may be moved
     * Warning: If <i>both</i> arguments are <code>true</code>,
     * we are saying neither coordinate can be changed; so this
     * has the side effect of calling <code>setMoveable(false)</code>.
     * @param cx if true, the <i>x</i> coordinate cannot be changed
     * @param cy if true, the <i>y</i> coordinate cannot be changed
     *
     * @see #setMoveable
     */
    public void setConstraints(boolean cx, boolean cy) {
	constrainX = cx;
	constrainY = cy;
	enableMoves = ! (cx & cy);
    }

    /**
     * Add an action listener for this component
     */
    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }

    /**
     * Double-buffers so we don't get flickering when moving points
     */
    public void update(Graphics g) {
	Dimension d = getSize();
	Image bimg = createImage(d.width, d.height);
	Graphics bg = bimg.getGraphics();
	super.paint(bg);
	bg.dispose();
	g.drawImage(bimg, 0, 0, this);
    }

    /**
     * Locate a point near given screen coordinates
     * @param H Horizontal screen coordinate of point
     * @param V Vertical screen coordinate of point 
     * @param radius Specifies how near is near enough.
     * @return an integer vector of indices {ix,jx,iy,jy} 
     * corresponding to the data point (x[jx][ix], y[jy][iy]).
     * The <i>first</i> point for which the screen coordinates are
     * within <code>radius</code> of (<code>x,y</code>) is identified.
     * If there are no such points, a null is returned.
     */
    protected int[] locate(int H, int V, int radius) {
	double h[][] = (swap ? y : x),   // Horiz. data,
	    v[][] = (swap ? x : y),      // Vert. data
	    hh = hMin + (H - h0) / mh,   // H conv. to data scale
	    vv = vMax - (V - v0) / mv,   // V conv. to data scale
	    hRad = radius / mh,     // horiz. error margin
	    vRad = radius / mv;     // vert. error margin
	int njh = h.length, njv = v.length, nj = Math.max(njh, njv);
	for (int j=0; j<nj; j++) {
	    int jh = j % njh, jv = j % njv;
	    double hj[] = h[jh], vj[] = v[jv];
	    int nih = hj.length, niv = vj.length, ni = Math.max(nih,niv);
	    for (int i=0; i<ni; i++) {
		int ih = i % nih, iv = i % niv;
		if ( (Math.abs(hh - hj[ih]) < hRad) &&
		     (Math.abs(vv - vj[iv]) < vRad) )
		    return (swap
			    ? new int[]{iv, jv, ih, jh}
			    : new int[]{ih, jh, iv, jv});
	    }
	}
	return null;   // if we got here, nothing was found
    }

    /** 
     * create and propagate an action event to any listeners 
     * @param cmd Command passed in actionEvent
     */
    private void doAction(String cmd) {
	if (actionListener != null)
	    actionListener.actionPerformed
		(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
			cmd));
    }

    // ============== EVENT HANDLERS ==============
    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    /**
     * If mouse button is pressed, register which point (if any) is to be moved
     */
    public void mousePressed(MouseEvent e) {
	if (!enableMoves) return;
	index = locate(e.getX(), e.getY(), 5);
	if (index == null) return;
        point = new double[]{x[index[1]][index[0]],y[index[3]][index[2]]};
	isMoving = true;
	setCursor(finger);
    }

    public void mouseReleased(MouseEvent e) {
	setCursor(pointer);
	isMoving = false;
	needsRescaling = true;
	doAction("IntPlot");
	repaint();
    }

    public void mouseMoved(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {
	if (!isMoving) return;
	double h = hMin + (e.getX() - h0) / mh, 
	    v = vMax - (e.getY() - v0) / mv;
	needsRescaling = (h < hMin || h > hMax || v < vMin || v > vMax);
	if (swap) {
	    if (!constrainX) x[index[1]][index[0]] = v;
	    if (!constrainY) y[index[3]][index[2]] = h;
	}
	else {
	    if (!constrainX) x[index[1]][index[0]] = h;
	    if (!constrainY) y[index[3]][index[2]] = v;
	}
	update(getGraphics());
    }
}

