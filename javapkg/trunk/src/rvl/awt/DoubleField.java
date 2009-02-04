package rvl.awt;

import java.awt.*;
import java.awt.event.*;

/**
 * GUI component for I/O of double-precision numbers.
 * Also generates an event if RETURN is pressed or focus is lost
 * after value has changed
 */
public class DoubleField extends TextField implements FocusListener {

private int digits;
private double currentValue = Double.NaN;
private String currentText = "NaN";
private boolean hasFocus = false;

/**
 * @param x initial value
 */
    public DoubleField(double x) {
        this(x, 3, 3);
    }

/**
 * Double field with the default of 3 digits
 * @param x initial value
 * @param width width of field (approx # characters)
 */
    public DoubleField(double x, int width) {
        this(x, width, 3);
    }

/**
 * @param x initial value
 * @param width width of field (approx # characters)
 * @param dig number of digits (allow 7 spaces in <tt>width</tt>
 *        for exponents and signs)
 */
    public DoubleField(double x, int width, int dig) {
        super("",width);
        digits = dig;
        setValue(x);
        addFocusListener(this);
    }


/**
 * Initialize like a Textfield (initial value is Double.NaN)
 * @param width width of field (approx # characters)
 */
    public DoubleField(String msg, int width) {
        super(width);
        digits = 3;
        setValue(Double.NaN);
        setText(msg);
    }


/**
 * @param x new value to put in window
 */
    public void setValue(double x) {
        currentText = rvl.util.Utility.format(x,digits);
        setText(currentText);
        currentValue = x;
    }


/**
 * @return current value in window (or NaN if invalid)
 */
    public double getValue() {
        if (currentText == getText().trim())
            return currentValue;
        try {
            currentText = getText().trim();
            currentValue = Double.valueOf(currentText).doubleValue();
            return currentValue;
        }
        catch(Exception NumberFormatException) {
            currentValue = Double.NaN;
            return currentValue;
        }
    }

/**
 * Look for RETURN key to create an event,
 * else pass on to superclass
 */
    public boolean keyDown(Event evt, int key) {
        if (key==13) {
            deliverEvent(new Event(this,
                Event.ACTION_EVENT, new Double(getValue())));
            return true;
        }
        else
            return super.keyDown(evt, key);
    }

/**
 * Interpret focus events (1.0 model)
 */
    public boolean gotFocus(Event e, Object o) {
        hasFocus = true;
        return true;
    }
    public boolean lostFocus(Event e, Object o) {
        if (hasFocus) {
            hasFocus = false;
            if (currentText != getText().trim()) {
                deliverEvent(new Event(this,
                    Event.ACTION_EVENT, new Double(getValue())));
            }
        }
        return true;
    }

/**
 * Handle focus events (1.1 model)
 */
    public void focusGained(FocusEvent e) {
        if (e.isTemporary()) return;
        hasFocus = true;
    }

    public void focusLost(FocusEvent e) {
        if (e.isTemporary()) return;
        hasFocus = false;
        if (currentText != getText().trim()) {
            getValue();      // replaces currentText
            processEvent(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                currentText));
        }
    }

}


