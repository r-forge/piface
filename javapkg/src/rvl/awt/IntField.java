package rvl.awt;

import java.awt.*;

/**
 * GUI component for I/O of integers.
 * Also generates an event if RETURN is pressed
 */
public class IntField extends TextField {

boolean hasFocus = false;
int currentValue = 0;

/**
 * @param x initial value
 * @param width width of field (approx # characters)
 */
    public IntField(int x, int width) {
        super("", width);
        setValue(x);
    }

/**
 * @param x initial value
 */
    public IntField(int x) {
        super("");
        setValue(x);
    }

/**
 * @param x new value to put in window
 */
    public void setValue(int x) {
        currentValue = x;
        setText(""+x);
    }

 
/**
 * @return current value in window (or MIN_VALUE if invalid)
 */
    public int getValue() {
        try {
            currentValue = Double.valueOf(getText().trim()).intValue();
	    setText(""+currentValue);
	    return currentValue;
        }
        catch(Exception NumberFormatException) {
            return Integer.MIN_VALUE;
        }
    }

/**
 * Look for RETURN key to create an event,
 * else pass on to superclass
 */
    public boolean keyDown(Event evt, int key) {
        if (key==13) {
            deliverEvent(new Event(this, 
                Event.ACTION_EVENT, new Integer(getValue())));
            return true;
        }
        else 
            return super.keyDown(evt,key);
    }

/**
 * Interpret focus events
 */
    public boolean gotFocus(Event e, Object o) {
        hasFocus = true;
        return true;
    }    
    public boolean lostFocus(Event e, Object o) {
        if (hasFocus) {
            hasFocus = false;
            int y = currentValue;
            if (y != getValue()) {
            	deliverEvent(new Event(this, 
                	Event.ACTION_EVENT, new Integer(currentValue)));
            }
        }
        return true;
    }

}
