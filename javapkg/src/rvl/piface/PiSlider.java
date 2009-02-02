package rvl.piface;

import rvl.awt.*;
import rvl.piface.*;

public class PiSlider
        extends Slider
        implements DoubleComponent
{
        private String name, label;

        public PiSlider(String name, String label, double value) {
                super(label,value);
                setName(name,label);
        }

        public String getName() {
                return name;
        }

        public String getLabel() {
                return label;
        }

        //public double getValue()              // (in superclass)

        public void setName(String name, String label) {
                this.name = name;
                this.label = label;
                super.setLabel(label);
        }

        //public void setValue(double x)        // (in superclass)

        //public void addActionListener(java.awt.event.ActionListener al)
                                                                                // in superclass
}