package rvl.piface;

import java.awt.*;

import rvl.awt.*;
import rvl.piface.*;
import rvl.util.*;

/**
 * Output-only component to display a double-precision value
 * Similar to the top part of a Slider
 */
public class PiDoubleText
    extends Component
    implements DoubleComponent
{

    private String name, label;
    private double value;
    private int digits;

    public PiDoubleText(String name, String label, double value, int digits) {
        setFont(new Font("Serif", Font.BOLD, 12));
        setName(name,label);
        setDigits(digits);
    }

    public PiDoubleText(String name, String label, double value) {
        this(name, label, value, 4);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

    public void setName(String name, String label) {
        this.name = name;
        this.label = label;
        if (isValid()) repaint();
    }

    public void setValue(double x) {
        value = x;
        repaint();
    }

    public void setDigits(int d) {
        digits = d;
        if (isValid()) repaint();
    }

    public void addActionListener(java.awt.event.ActionListener al) {
        // Do nothing
    }

    public void paint(Graphics g) {
        g.setColor(getForeground());
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics(getFont());
        int leftPos = fm.stringWidth("m")/2,
            topPos = 3 * fm.getAscent() / 2;
        g.drawString(label + " = " + Utility.format(value,digits),
            leftPos, topPos);
    }

    public Dimension getPreferredSize() {
        FontMetrics fm = getGraphics().getFontMetrics(getFont());
        int em = fm.stringWidth("m"), h = fm.getHeight();
        return new Dimension(fm.stringWidth(label) + digits*em, 3*h/2);
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
