package rvl.piface;

import java.awt.*;
import rvl.piface.*;

public class PiButton
    extends Button
    implements ActionComponent
{
    private String methodName, label;

    PiButton(String methodName, String label) {
        super(label);
        setName(methodName,label);
    }

    public String getName() {
        return methodName;
    }

    public String getLabel() {
        return label;
    }

    public void setName(String methodName, String label) {
        this.methodName = methodName;
        this.label = label;
    }

    //public void addActionListener(java.awt.event.ActionListener al)
                                        // in superclass
}