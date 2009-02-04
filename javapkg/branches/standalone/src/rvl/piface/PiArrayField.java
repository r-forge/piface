/**
 * Component for entering a list of numbers
 * separated by commas or spaces.
 * Implements ActionComponent -- as such, name is the
 * name of the method to call when this generates an action
 * and labe is the display label.
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import rvl.awt.*;
import rvl.piface.*;
import rvl.util.*;

public class PiArrayField extends Panel
    implements ActionComponent, KeyListener
{
    private String name, label;
    private Font font = new Font("Serif", Font.BOLD, 12);
    private TextField field;
    private transient ActionListener actionListener = null;

/**
 * constructor - initialize with given name, label, value
 */
    public PiArrayField(String name, String label, double value[]) {
        this(name,label,value,12);
    }

/**
 * constructor - initialize with given name, label, value, and field width
 */
    public PiArrayField(String name, String label, double value[], int width) {
        setName(name,label);
        setLayout(new RVLayout(2,false,true));
        Label lbl = new Label(label);
        lbl.setFont(font);
        add(lbl);
        field = new TextField(width);
        setValue(value);
        add(field);
        field.addKeyListener(this);
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
 * @return an array of the values in the field, as delimited by
 * spaces or commas.  Non-numeric tokens are returned as NaNs.
 */
    public double[] getValue() {
        StringTokenizer st =
            new StringTokenizer(field.getText(), " ,\t");
        int n = st.countTokens();
        double x[] = new double[n];
        for (int i=0; i<n; i++) {
            String txt = st.nextToken();
            x[i] = Utility.strtod(txt);
        }
        return x;
    }

/**
 * Sets values in field, separated by " "
 */
    public void setValue(double x[]) {
        String contents = "";
        for (int i=0; i<x.length; i++)
            contents += Utility.format(x[i],3) + " ";
        field.setText(contents);
    }

    public void setEditable(boolean e) {
        field.setEditable(e);
    }

    public boolean isEditable() {
        return field.isEditable();
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        field.setBackground(field.isEditable() ? Color.white : c);
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        field.setForeground(c);
    }

    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() != KeyEvent.VK_ENTER) return;
        ActionEvent ae = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            "" + this.toString());
        actionListener.actionPerformed(ae);
    }
    public void keyReleased(KeyEvent ke) {}
    public void keyTyped(KeyEvent ke) {}
}
