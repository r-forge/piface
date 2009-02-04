package rvl.util;
import rvl.util.Utility;

/** Class for specifying categorical parameters
 * @author Russ Lenth
 * @version 1.0 July 1, 1996
 */
public class Category implements Value {

    public String name, label, catName[];
    public double value;
    
/**
 * Construct a <tt>Category</tt> object
 * @param nam name of parameter
 * @param cats names of categories
 * @param init initial index (in 0 ... cats.length - 1)
 */
    public Category(String nam, String[] cats, int init) {
        name = new String(nam);
        label = name;
        value = 0.0+init;
        catName = new String[cats.length];
        for (int i=0; i<cats.length; i++)
            catName[i] = new String(cats[i]);
    }
    
    public boolean isValid(double val) {
        return (val > -.5) && (val < catName.length - .5);
    }
    
    public double val() {
        return Math.round(value);
    }

    public void setValue (double v) {
        if (isValid(v))
            value = v;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel (String l) {
        label = new String(l);
    }    
}
