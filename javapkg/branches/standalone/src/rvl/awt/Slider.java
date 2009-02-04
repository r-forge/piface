// NOTE: Uses java 1.1 event model.

package rvl.awt;

import java.awt.*;
import java.awt.event.*;
import rvl.util.*;

/**
 * GUI Object that implements a slider for display/input of real values.
 * When 0 is included in the scale, there is the option of using a
 * bar connecting the value to 0.
 */
public class Slider extends Container
    implements ActionListener, ItemListener, KeyListener, FocusListener {

// Attributes ----------------------------------------------------
    private String label;                     // Label to display
    private double value, min, max;           // basic values
    private boolean minMutable=true, maxMutable=true,   // endpoint flags
            editable = true,
            showBar = true,                   // display a bar too
            configMode = false,
            isShifted = false,                // shift key flag
            rescaling=false, adjustMax;
                                              // flags for mouse actions
    private int mainInc = -1, subInc, em;     // Increments used in painting
    private int digits = 4;                   // precision for display
    private int scaleWidth = 0;               // # pixels in scale
    private double roundFactor = 1;           // factor used in rounding mouse click
    private int hotMinY=0, hotMaxY=0;         // defines mouse-click region
    private int mouseStart;                   // x where mouse down
    private Color dotColor, scaleColor, buttonColor;
    private Font scaleFont, labelFont, fieldFont;
    private FontMetrics sfm;                  // scaleFont metrics
    private transient ActionListener actionListener = null;
    private double tick[];                    // ticks
    private String tickLab[];                 // tick labels
    private Cursor arrowCursor = Cursor.getDefaultCursor(),
                leftCursor = new Cursor(Cursor.W_RESIZE_CURSOR),
                rightCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    private String choices[] = new String[]
        {"Value","Min","Max","Min!","Max!","Digits"};
    private Choice choice = new Choice();     // config menu
    private TextField valField;               // for inputting values
    private Button /*xbutton = new Button("-+-"),*/
        setbutton = new Button("OK");


// Methods --------------------------------------------------------

/**
 * Construct a slider with given label and initial value.  Tries to set scale
 * endpoints in a reasonable way
 */
    public Slider(String label, double value) {
        this.label = label;
        this.value = value;
        if (value > 0) {
            this.min = 0;
            this.max = 1.25 * value;
        }
        else if (value < 0) {
            this.min = 1.25 * value;
            this.max = 0;
        }
        else {
            this.min = 0;
            this.max = 1;
        }
        init();
    }

/**
 * Construct a slider with given initial value, min, and max
 */
    public Slider (String label, double value, double min, double max) {
        this.label = label;
        this.value = value;
        this.min = min;
        this.max = max;
        init();
    }

    private void init() {
        valField = new TextField("0");
        setForeground(Color.black);
        dotColor = Color.red;
        scaleColor = Color.blue;
        buttonColor = Color.lightGray;
        setFont(new Font("Serif", Font.PLAIN, 12));
        scaleFont = new Font("SansSerif", Font.PLAIN, 8);
//        fieldFont = new Font("MonoSpace", Font.PLAIN, 10);
//        fieldFont = new Font("SansSerif", Font.PLAIN, 10);
        for (int i=0; i<choices.length; i++)
            choice.add(choices[i]);
        setConfig(false);
        setLayout(null);
        valField.setFont(fieldFont);
//        xbutton.setFont(scaleFont);
        setbutton.setFont(scaleFont);
        add(choice);
        add(valField);
        add(setbutton);
//        add(xbutton);
    }

    private synchronized void setConfig(boolean b) {
        choice.setVisible(b);
        valField.setVisible(b);
        setbutton.setVisible(b);
//        xbutton.setVisible(b);
        configMode = b;
        if (b) {
//            xbutton.addActionListener(this);
            setbutton.addActionListener(this);
            choice.addItemListener(this);
            valField.addKeyListener(this);
            valField.addFocusListener(this);
            valField.setText(Utility.format(value,digits));
            valField.setEditable(editable);
//            disableEvents(AWTEvent.MOUSE_EVENT_MASK);
            disableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            choice.select(0);
        }
        else {
//            xbutton.removeActionListener(this);
            setbutton.removeActionListener(this);
            choice.removeItemListener(this);
            valField.removeKeyListener(this);
            valField.removeFocusListener(this);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            scaleWidth = 0;     // forces recomputation of roundFactor
        }
    }

    private synchronized void checkRange() {
        if (Double.isNaN(value)) {  // missing value - ensure min and max exist
            if (Double.isNaN(min))
                min = Double.isNaN(max) ? -1 : max - 1;
            if (Double.isNaN(max))
                max = min + 2;
        }
        else {
            if (minMutable)
                min = Math.min(min, Math.min(value, max));
            if (maxMutable)
                max = Math.max(max, Math.max(value, min));
            value = Math.min(max, Math.max(value, min));
            if (min == max) min = max - Math.max(Math.abs(value), .1);
        }
        tick = Utility.nice(min,max,5,false);
        tickLab = Utility.fmtNice(tick);
    }

/**
 * Set whether endpoints are allowed to be changed.  An argument
 * of true enables changing that endpoint
 */
    public void setMutable(boolean minMut, boolean maxMut) {
        minMutable = minMut;
        maxMutable = maxMut;
    }
/**
 * Set value of slider
 */
    public void setValue(double x) {
        value = x;
        checkRange();
        if (configMode)
            valField.setText(Utility.format(value,digits));
        if (isShowing()) repaint();
    }
/**
 * Set minimum of slider (if mutable)
 * @see setMutable
 */
    public void setMinimum(double x) {
        if (!minMutable) return;
        min = x;
        checkRange();
        scaleWidth = 0;     // forces recalculation of roundFactor
        if (isShowing()) repaint();
    }
/**
 * Set maximum of slider (if mutable)
 * @see setMutable
 */
    public void setMaximum(double x) {
        if (!maxMutable) return;
        max = x;
        checkRange();
        scaleWidth = 0;     // forces recalculation of roundFactor
        if (isShowing()) repaint();
    }
/**
 * Set # digits
 */
    public void setDigits(int d) {
        digits = d;
    }

    public void setEditable(boolean e) {
        editable = e;
    }

    public boolean isEditable() {
        return editable;
    }
/**
 * Set label
 */
    public void setLabel(String label) {
        this.label = label;
    }
/**
 * Get current value of slider
 */
    public double getValue () {
        return value;
    }
/**
 * Get minimum of slider
 */
    public double getMinimum() {
        return min;
    }
/**
 * Get maximum of slider
 */
    public double getMaximum() {
        return max;
    }

/**
 * Set color of dot and connecting bar.
 * NOTE: May also use setForeground() and setBackground() to
 * change the most important colors
 */
    public void setDotColor(Color c) {
        dotColor = c;
    }
/**
 * Set color of buttons
 */
    public void setButtonColor(Color c) {
        buttonColor = c;
    }
/**
 * Set color of axis, tick marks, and tick labels
 */
    public void setScaleColor(Color c) {
        scaleColor = c;
    }

    private void setIncs() {
        FontMetrics fm = getFontMetrics(getFont());
        mainInc = fm.getAscent();
        em = fm.stringWidth("M");
        sfm = getFontMetrics(scaleFont);
        subInc = sfm.getAscent();
        labelFont = new Font(getFont().getName(), Font.BOLD,
            getFont().getSize());
    }

    public Dimension getPreferredSize() {
        if (mainInc < 0) setIncs();
//        return new Dimension(22*em, 5 * (mainInc + subInc) / 2);
        return new Dimension(18*em, 5 * (mainInc + subInc) / 2);
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
 * Compute the value corresponding to a position on the scale.
 * Tries to come up with round values based on available pixels.
 */
    private double scaleValue(int pos, int leftPos, int width) {
        if (width != scaleWidth) { // need to update rounding rules
            scaleWidth = width;
            /* Note: fudge controls scale granularity.  Each rounding
               unit is at most (width of 1 pixel) / fudge.   */
            double fudge = 1.5,
                pixPerUnit = fudge * width / (max - min),
                z = Math.log(pixPerUnit) / Math.log(10.0);
            roundFactor = Math.pow(10, Math.floor(z));
            z = pixPerUnit / roundFactor;
            if (z >= 5) roundFactor *= 5;
            else if (z >= 2) roundFactor *= 2;
//System.out.println("Unit/Pix = " + (fudge/pixPerUnit) + "\tRounding to nearest " + (1/roundFactor));
        }
        double x = min + (max - min) * (pos - leftPos) / width;
        return Math.round(roundFactor * x) / roundFactor;
    }

/**
 * Return the scale position of a value - inverse of scaleValue
 */
    private int scalePos(double value, int leftPos, int width) {
        if (Double.isNaN(value))
            return -1;
        return (int)(leftPos + (value - min) * width / (max - min));
    }

    public void repaint() {
        Dimension d = getSize();
        if (d.width == 0) {
            super.repaint();
            return;
        }
        Image bufImg = createImage(d.width,d.height);
        Graphics bg = bufImg.getGraphics();
        paint(bg);
        bg.dispose();
        getGraphics().drawImage(bufImg, 0, 0, null);
    }

    public void paint(Graphics g) {
        if (!isVisible()) return;
        if (tick == null) checkRange();
        if (configMode)
            paintConfig(g);
        else
            paintSlider(g);
    }

/**
 * Paint method when in config mode
 */
    private void paintConfig(Graphics g) {
        int w = getSize().width,
            z = w/40,
            y = 2*mainInc;

    // Draw the label
        g.setFont(labelFont);
        g.setColor(getForeground());
        g.drawString(label, em/2, 3 * mainInc/2);

    // Draw config button...
        g.setColor(getBackground().darker());
        g.fillRect(w - subInc, subInc/2, subInc, subInc);
        g.draw3DRect(w - subInc, subInc/2, subInc, subInc, false);
        int si3 = subInc/3;
        g.draw3DRect(w - subInc + si3, subInc/2 + si3, si3, si3, false);

    // Draw the components
        int ww = choice.getPreferredSize().width,
            hh = choice.getPreferredSize().height;
        choice.setBounds(0, y, ww, hh);
        valField.setBounds(ww + z, y, w - ww - 8*z, hh);
        setbutton.setBounds(w - 6*z, y, 6*z, hh);

        String txt = "";
        switch (choice.getSelectedIndex()) {
            case 0:
                txt = Utility.format(value,digits);
                break;
            case 1: case 3:
                txt =Utility.format(min,digits);
                break;
            case 2: case 4:
                txt = Utility.format(max,digits);
                break;
            case 5:
                txt = "" + digits;
                break;
        }
        super.paint(g);
    }

/**
 * Paint method for the slider object (not in config mode)
 */
    private void paintSlider(Graphics g) {
        int y = 3 * mainInc / 2,
            w = getSize().width,   // box width
            sw = w - 2*em;         // scale width
    // Draw the label
        if (mainInc < 0) setIncs();    // set up fonts etc. if not initialized
        g.setFont(labelFont);
        g.setColor(getForeground());
        g.drawString(label + " = " + Utility.format(value,digits), em/2, y);

    // Draw config button...
        g.setColor(getBackground().darker());
        g.fillRect(w - subInc, subInc/2, subInc, subInc);
        g.setColor(getBackground());
        g.draw3DRect(w - subInc, subInc/2, subInc, subInc, true);
        int si3 = subInc/3;
        g.draw3DRect(w - subInc + si3, subInc/2 + si3, si3, si3, true);

    // Stop here if value is missing
        if (Double.isNaN(value)) return;

        y += (mainInc + subInc) / 2;
        hotMinY = y - subInc/2;
        hotMaxY = y + subInc/2;
    // background of slider
        int xv = scalePos(value, em, sw);
        if (editable) {
            g.setColor(getBackground().darker());
            g.fillRect(xv - subInc/2, y-subInc, 2*(subInc/2), 2*subInc);
        }
    // scale line
        g.setColor(scaleColor);
        g.drawLine(em, y, w - em, y);
    // tick marks and labels
        int y0 = y - subInc/2, y1 = y0 + subInc, y2 = y1 + 3 * subInc / 2;
        g.setFont(scaleFont);
        int prevLabel = 0;
        for (int i=0; i<tick.length; i++) {
            int x = scalePos(tick[i], em, sw);
            g.drawLine(x, y0, x, y1);
            int twid = sfm.stringWidth(tickLab[i]);
            x = Math.max(em/2, Math.min(x - twid/2, w - em/2 - twid));
            if (x - prevLabel > subInc/2) {
                g.drawString(tickLab[i], x, y2);
                prevLabel = x + twid;
            }
        }
        if (showBar && min*max <= 0) {
            int x0 = (int) (em - (w - 2*em) * min / (max - min));
            g.setColor(dotColor);
            g.drawLine(x0,y,xv,y);
            g.drawLine(x0,y-1,xv,y-1);
            g.drawLine(x0,y+1,xv,y+1);
        }
    // slider itself...
        g.setColor(getForeground());
        g.drawLine(xv, y-subInc, xv, y+subInc);
        if (editable) {     // make a button-like slider
            g.setColor(getBackground());
            g.draw3DRect(xv - subInc/2, y-subInc, 2*(subInc/2), 2*subInc, true);
        }
        else {              // make an outline only
            g.setColor(getBackground().darker());
            g.drawRoundRect(xv - subInc/2, y-subInc, 2*(subInc/2), 2*subInc, 2*subInc/3, subInc);
        }
        super.paint(g);
    }

/**
 * Print method
 */
    public String toString() {
        return "Slider: " + label + " = " + Utility.format(value,4)
            + " on [" + Utility.format(min,4) + "," + Utility.format(max,4)
            + "] mutable: [" + minMutable + "," + maxMutable + "]";
    }

/**
 * Add an action listener for this component
 */
    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }

// ----- Mouse event handlers ---------------------------------------------

    public synchronized void processMouseEvent(MouseEvent e) {
        int x = e.getX() - em;
        int w = getSize().width - 2*em;
        int y = e.getY();
        switch (e.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                if (x > em + w - subInc && y < 3*subInc/2) { // config button
                    setConfig(!configMode);
                    repaint();
                    return;
                }
                if (x < 0 || x > w || configMode) return;
                if (y >= hotMinY && y <= hotMaxY) {
                    if ( x > w || !editable) return;
                    setValue(scaleValue(x,0,w));
                    repaint();
                    if (actionListener != null)
                        actionListener.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                label));
                }
                break;
            case MouseEvent.MOUSE_PRESSED:
                if (x < 0 || x > w || configMode) return;
                else {
                    requestFocus();
                    if (y >= hotMaxY + subInc/2) {
                        rescaling = false;    // ...until we're sure!
                        if (!maxMutable) {
                            adjustMax = false;
                            if (!minMutable) return;  // neither end adjustable
                        }
                        else
                            adjustMax = minMutable ? (x > w/2) : true;
                        mouseStart = x;
                        rescaling = true;
                        setCursor(adjustMax ? rightCursor : leftCursor);
                    }
                }
                break;
            case MouseEvent.MOUSE_RELEASED:
                setCursor(arrowCursor);
                if (rescaling) {
                    rescaling = false;
                    x = Math.max(0, Math.min(x, w));
                    if (x == mouseStart) return;
                    double start = scaleValue(mouseStart, 0, w);
//                    if (adjustMax)
//                        max = min + (start - min) * w / (x + 2);
//                    else
//                        min = max - (max - start) * w / (w - x + 2);
                // (denoms above padded by 2 to avoid too-dramatic rescaling)
// --- replacement -- choose end based on where we stop
                    if (maxMutable && x > w/2)
                        max = min + (start - min) * w / x;
                    else if(minMutable)
                        min = max - (max - start) * w / (w - x);
                    scaleWidth = 0; // forces recomputation of roundFactor
                    checkRange();
                    repaint();
                }
                break;
            case MouseEvent.MOUSE_EXITED:
                rescaling = false;
                //setCursor(getParent().getCursor()); //arrowCursor);
            //case MouseEvent.MOUSE_ENTERED:
            //    setCursor(getParent().getCursor()); //arrowCursor);
        }
    }

    public synchronized void processMouseMotionEvent(MouseEvent e) {
        int x = e.getX() - em;
        int w = getSize().width - 2*em;
        int y = e.getY();
        if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
	    // if (x < 0 || x > w || !editable) return;
	    //=== New section - provides for gradual expansion of scale
	    if (!editable) return;
	    if (x < 0 && minMutable) {
		min -= .005*(max - min);
		checkRange();
		repaint();
	    }
	    else if (x > w && maxMutable) {
		max += .005*(max-min);
		checkRange();
		repaint();
	    }
	    //=== end of new section
            if (y >= hotMinY && y <= hotMaxY) {
                setValue(scaleValue(x,0,w));
                repaint();
                if (actionListener != null)
                    actionListener.actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            label));
            }
        }
    }

    private void updateChoice(int item, double val) {
        switch(item) {
            case 0: // Value
                if (!editable) return;
                value = val;
                if (actionListener != null)
                    actionListener.actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            label));
                return;     // finished if displaying value
            case 1: // Min
                setMutable(true, maxMutable);
                min = val;
                break;
            case 2: // Max
                setMutable(minMutable, true);
                max = val;
                break;
            case 3: // Min!
                setMutable(false, maxMutable);
                min = val;
                break;
            case 4: // Max!
                setMutable(minMutable, false);
                max = val;
                break;
            case 5: // Digits
                digits = Math.max(1, (int) Math.round(val));
        }
        checkRange();
      // revert to displaying the value
        choice.select(0);
        valField.setEditable(editable);
        valField.setText(Utility.format(value,digits));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(setbutton)) {
            try {
                double val = Utility.strtod(valField.getText());
                updateChoice(choice.getSelectedIndex(), val);
            }
            catch(NumberFormatException nfe) {
                valField.setText(valField.getText() + " - INVALID!");
                valField.selectAll();
            }
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource().equals(choice)) {
            String txt = "";
            valField.setEditable(true); // always editable unless value
            switch (choice.getSelectedIndex()) {
                case 0:
                    txt = Utility.format(value,digits);
                    valField.setEditable(editable);
                    break;
                case 1: case 3:
                    txt = Utility.format(min,digits);
                    break;
                case 2: case 4:
                    txt = Utility.format(max,digits);
                    break;
                case 5:
                    txt = "" + digits;
                    break;
            }
            valField.setText(txt);
        }
    }

    public void keyPressed (KeyEvent ke)  {
        if ( ke.getKeyCode() == KeyEvent.VK_SHIFT
                || ke.getKeyCode() == KeyEvent.VK_ALT
                || ke.getKeyCode() == KeyEvent.VK_CONTROL )
            isShifted = true;
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
/**** no longer needed
            if (isShifted)
                actionPerformed (new ActionEvent (
                    xbutton, ActionEvent.ACTION_PERFORMED, "-+-"));
            else
*/
            actionPerformed (new ActionEvent (
                setbutton, ActionEvent.ACTION_PERFORMED, "OK"));
        }
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setConfig(false);
            repaint();
        }
    }
    public void keyReleased (KeyEvent ke) {
        if ( ke.getKeyCode() == KeyEvent.VK_SHIFT
                || ke.getKeyCode() == KeyEvent.VK_ALT
                || ke.getKeyCode() == KeyEvent.VK_CONTROL )
            isShifted = false;
    }
    public void keyTyped (KeyEvent ke) {}

    public void focusGained (FocusEvent fe) {
        if (fe.getSource().equals(valField))
            valField.selectAll();
    }
    public void focusLost (FocusEvent fe) {
        if (fe.getSource().equals(valField)) {
            valField.select(0,0);
            actionPerformed (new ActionEvent (
                setbutton, ActionEvent.ACTION_PERFORMED, "OK"));
        }
    }

}
