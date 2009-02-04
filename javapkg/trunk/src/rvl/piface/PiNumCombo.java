/**
 * Choice of several numbers
 * Return value is the value selected,
 * or the value entered in the field
 * This requires JDK 1.2 or higher
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import rvl.awt.*;
import rvl.piface.*;
import rvl.util.*;

public class PiNumCombo
    extends Panel
    implements DoubleComponent, ItemListener
{
    private String name, lbl;
    private transient ActionListener actionListener = null;
    private Font font = new Font("Serif", Font.PLAIN, 12),
        bfont = new Font("Serif", Font.BOLD, 12);
    private Label label;
    private JComboBox combo = new JComboBox();
    private double values[];

    PiNumCombo(String name, String lbl, double items[], int index) {
        setLayout(new RVLayout(2,false,true));
        label = new Label(lbl);
        label.setFont(bfont);
        combo.setFont(font);
        add(label);
        setName(name, lbl);
        values = new double[items.length];
        for (int i=0; i<items.length; i++) {
            combo.addItem("" + items[i]);
            values[i] = items[i];
        }
        combo.setSelectedIndex(index);
        combo.setEditable(true);
        combo.addItemListener(this);
        add(combo);
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
        String s = (String)(combo.getEditor().getItem());
        return Utility.strtod(s);
    }

    public void setValue(double value) {
        if (Math.abs(value - getValue()) < 1e-10) return;
        combo.getEditor().setItem(Utility.format(value,5));
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
