/**
 * Component for entering a list of numbers
 * separated by commas or spaces.  generates no events
 */

package rvl.awt;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import rvl.awt.*;
import rvl.piface.*;
import rvl.util.*;

public class DoubleArrayField extends Panel
{
    private String label;
    private Font font = new Font("Serif", Font.BOLD, 12);
    private TextField field;

    public DoubleArrayField(String label, double value[]) {
        this(label,value,12);
    }

    public DoubleArrayField(String label, double value[], int width) {
        setLayout(new RVLayout(2,false,true));
        Label lbl = new Label(label);
        lbl.setFont(font);
        add(lbl);
        field = new TextField(width);
        setValue(value);
        add(field);
    }

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

}
