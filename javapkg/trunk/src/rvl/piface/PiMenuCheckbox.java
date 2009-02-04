package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import rvl.piface.*;

public class PiMenuCheckbox
    extends CheckboxMenuItem
    implements IntComponent, ItemListener
{
    private String name, label;
    private transient ActionListener actionListener = null;

    PiMenuCheckbox(String name, String label, int value) {
        super(label, value != 0);
        setName(name, label);
        addItemListener(this);
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

    public int getValue() {
        return getState() ? 1 : 0;
    }

    public String getTextValue() {
        return getState() ? "true" : "false";
    }

    public void setValue(int value) {
        setState(value != 0);
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
