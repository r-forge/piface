package rvl.util;

/** 
 * Interface for things that have a value, a name, and a label
 * @author Russ Lenth
 * @version 1.0 July 1, 1996
 */

public interface Value {
    public double val();
    public boolean isValid(double v);
    public void setValue(double v);
    public String getName();
    public String getLabel();
    public void setLabel(String l);
}
