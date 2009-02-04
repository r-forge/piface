package rvl.piface;

import java.awt.*;
import rvl.awt.*;
import java.awt.event.*;
import rvl.piface.*;

public class PiDoubleField
    extends Panel
    implements DoubleComponent, ActionListener
{
    private String name, label;
    private Font font = new Font("Serif", Font.BOLD, 12);
    private DoubleField field;
    private Label lbl;
    private transient ActionListener actionListener = null;

    public PiDoubleField(String name, String label, double value) {
        this(name,label,value,8,5);
    }

    public PiDoubleField(String name, String label, double value,
            int width, int digits)
    {
        setLayout(new RVLayout(2,false,true));
        lbl = new Label(label);
        lbl.setFont(font);
        add(lbl);
        field = new DoubleField(value, width, digits);
        field.addActionListener(this);
        add(field);
        setName(name,label);
        setVisible(true);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return field.getValue();
    }

    public void setName(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public void setValue(double x) {
        field.setValue(x);
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

    public void addActionListener(java.awt.event.ActionListener al) {
        actionListener = AWTEventMulticaster.add(actionListener, al);
    }

    public Dimension getPreferredSize() {
	Dimension dl = lbl.getPreferredSize(), 
	    df = field.getPreferredSize();
	int w = dl.width + df.width,
	    h = Math.max(dl.height, df.height);
	return new Dimension(w,h);
    }

    public Dimension getMinimumSize() {
	Dimension dl = lbl.getMinimumSize(), 
	    df = field.getMinimumSize();
	int w = dl.width + df.width,
	    h = Math.max(dl.height, df.height);
	return new Dimension(w,h);
    }


    public Dimension preferredSize() {
	return getPreferredSize();
    }

    public Dimension minimumSize() {
	return getMinimumSize();
    }

    public void actionPerformed(ActionEvent e) {
        if (!field.isEditable()) return;
        ActionEvent ae = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,
            "" + getValue());
        actionListener.actionPerformed(ae);
    }
}
