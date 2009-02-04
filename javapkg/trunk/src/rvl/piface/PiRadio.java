/**
 * Radio buttons
 * Return value is the index of the selected button (0 based)
 * For a vertical arrangement, specify columns = 1;
 * For a horizontal arrangement, specify columns = items.length + 1
 * (this is the default for the constructor with no columns arg)
 */

package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import rvl.awt.*;
import rvl.piface.*;

public class PiRadio
    extends Panel
    implements IntComponent, ItemListener
{
    private String name, lbl;
    private transient ActionListener actionListener = null;
    private Font font = new Font("Serif", Font.PLAIN, 12),
        bfont = new Font("Serif", Font.BOLD, 12);
    private Label label;
    private CheckboxGroup group = new CheckboxGroup();
    private Checkbox buttons[];
    private String itemLabels[];
    private int nButtons = 0;

/**
 * Constructor for a horizontal arrangement
 */
    PiRadio(String name, String lbl, String items[], int value) {
        this(name, lbl, items, value, items.length + 1);
    }

    PiRadio(String name, String lbl, String items[], int value, int columns) {
        setLayout(new RVLayout(columns,false,true));
        label = new Label(lbl);
        label.setFont(bfont);
        add(label);
        buttons = new Checkbox[items.length];
        itemLabels = new String[items.length];
        for (int i=0; i<items.length; i++) {
            if (items[i].trim().length() == 0) {    // create a blank cell
                add(new Label(""));
                continue;
            }
            buttons[nButtons] = new Checkbox(items[i]);
            buttons[nButtons].setCheckboxGroup(group);
            buttons[nButtons].setFont(font);
            buttons[nButtons].addItemListener(this);
            itemLabels[nButtons] = items[i];
            add(buttons[nButtons]);
            nButtons++;
        }
        setName(name, lbl);
        setValue(value);
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
        int sel = 0;
        while (!buttons[sel].getState()) sel++;
        return sel;
    }

    public String getTextValue() {
        return itemLabels[getValue()];
    }

    public void setValue(int value) {
        if (value < 0) value = 0;
        else if (value >= nButtons)
            value = nButtons - 1;
        group.setSelectedCheckbox(buttons[value]);
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
