/**
 * Choice of several numbers
 * Return value is the value selected
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import rvl.awt.*;
import rvl.piface.*;

public class PiNumChoice
    extends Panel
    implements DoubleComponent, ItemListener
{
    private String name, lbl;
    private transient ActionListener actionListener = null;
    private Font font = new Font("Serif", Font.PLAIN, 12),
        bfont = new Font("Serif", Font.BOLD, 12);
    private Label label;
    private Choice choice = new Choice();
    private double values[];

    PiNumChoice(String name, String lbl, double items[], int index) {
        setLayout(new RVLayout(2,false,true));
        label = new Label(lbl);
        label.setFont(bfont);
        choice.setFont(font);
        add(label);
        setName(name, lbl);
        values = new double[items.length];
        for (int i=0; i<items.length; i++) {
            choice.add("" + items[i]);
            values[i] = items[i];
        }
        choice.addItemListener(this);
        choice.select(index);
        add(choice);
    }
    public String getName() {
        return name;
    }

    public String getLabel() {
        return lbl;
    }

    public void setName(String name, String lbl) {
        this.name = name;
        this.lbl = lbl;
    }

    public double getValue() {
        return values[choice.getSelectedIndex()];
    }

// setValue finds the choice closest to the one provided
    public void setValue(double value) {
        if (Math.abs(value - values[choice.getSelectedIndex()]) < 1e-10) return;
        int sel = 0;
        double minDiff = Math.abs(value - values[0]);
        for (int i=1; i<values.length; i++) {
            double d = Math.abs(value - values[i]);
            if (d < minDiff) {
                sel = i;
                minDiff = d;
            }
        }
        choice.select(sel);
    }

    public void addActionListener(ActionListener al) {
        actionListener = AWTEventMulticaster.add(actionListener, al);
    }

    public void itemStateChanged(ItemEvent ie) {
        ActionEvent ae = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            "" + getValue());
        actionListener.actionPerformed(ae);
    }

}
