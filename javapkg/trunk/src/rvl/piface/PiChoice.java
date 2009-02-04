/**
 * Choice of several strings
 * Return value is the index of the selection (0 based)
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import rvl.awt.*;
import rvl.piface.*;

public class PiChoice
    extends Panel
    implements IntComponent, ItemListener
{
    private String name, lbl;
    private transient ActionListener actionListener = null;
    private Font font = new Font("Serif", Font.PLAIN, 12),
        bfont = new Font("Serif", Font.BOLD, 12);
    private Label label;
    private Choice choice = new Choice();

    PiChoice(String name, String lbl, String items[], int value) {
        setLayout(new RVLayout(2,false,true));
        label = new Label(lbl);
        label.setFont(bfont);
        choice.setFont(font);
        add(label);
        setName(name, lbl);
        for (int i=0; i<items.length; i++)
            choice.add(items[i]);
        choice.addItemListener(this);
        setValue(value);
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

    public int getValue() {
        return choice.getSelectedIndex();
    }

    public String getTextValue() {
        return choice.getSelectedItem();
    }

    public void setValue(int value) {
        if (value < 0) value = 0;
        else if (value >= choice.getItemCount())
            value = choice.getItemCount() - 1;
        choice.select(value);
    }

    public Choice getChoice() {
        return choice;
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
