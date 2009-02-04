/**
 * Component for manipulating a set of numbers in a dotplot.
 * Implements ActionComponent -- as such, name is the
 * name of the method to call when this generates an action
 * and label is the display label.
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import rvl.awt.*;
import rvl.piface.*;
import rvl.util.*;

public class PiDotplot extends Panel
    implements ActionComponent, ActionListener
{
    private String name, label, lastAction = "none";
    private Font font = new Font("Serif", Font.BOLD, 12);
    private Dotplot dotplot;
    private transient ActionListener actionListener = null;

/**
 * constructor - initialize with given name, label, and value
 */
    public PiDotplot(String name, String label, double value[]) {
        setName(name,label);
        setLayout(new RVLayout(1,false,true));
        Label lbl = new Label(label);
        lbl.setFont(font);
        if (label.length() > 0) add(lbl);
        dotplot = new Dotplot(value);
        setValue(value);
        add(dotplot);
        dotplot.addActionListener(this);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setName(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public void addActionListener(ActionListener al) {
        actionListener = AWTEventMulticaster.add(actionListener, al);
    }

/**
 * @return an array of the values in the dotplot
 */
    public double[] getValue() {
    return dotplot.getValues();
    }

/**
 * Sets values in dotplot
 */
    public void setValue(double x[]) {
    dotplot.setValues(x);
    }

    public void setEditable(boolean e) {
        //xxxx dotplot.setEditable(e);
    }

    public boolean isEditable() {
        return true; //xxxx dotplot.isEditable();
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        dotplot.setBackground(c);
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        dotplot.setForeground(c);
    }

    /**
     * Mimics method of same name in ActionEvent.
     * May use this to find out which type of action was generated
     */
    public String getActionCommand() {
    return lastAction;
    }

    public void actionPerformed(ActionEvent ae) {
    lastAction = ae.getActionCommand();
    ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PiDotplot");
        actionListener.actionPerformed(ae);
    }

}
