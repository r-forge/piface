package rvl.util;
import rvl.util.Utility;

/** Class for specifying numerical parameters
 * @author Russ Lenth
 * @version 1.0 July 1, 1996
 */
public class Param implements Value {

    public String name, label;
    public double value;
    public double max = Double.POSITIVE_INFINITY,
        min = Double.NEGATIVE_INFINITY;
    public boolean closedMin = false, closedMax = false;

/**
 * Construct a <tt>Param</tt> object
 * @param nam name of parameter
 * @param val starting value
 */
    public Param(String nam, double val) {
        name = new String(nam);
        label = name;
        value = val;
    }

/**
 * Construct a <tt>Param</tt> object
 * @param nam name of parameter
 * @param val starting value
 * @param range range specification:
 *    must begin with a "(" or "[", end with a ")" or "]",
 *    and there must be a "," in between.  One number is
 *    allowed before the "," and one after, to specify a min and
 *    a max, respectively.<br>
 *  <i>Examples:</i> <b>"[1,5)"</b> specifies the interval [1,5),
 *  <b>"(0,)"</b> specifies that the parameter must be positive.
 */
    public Param(String nam, double val, String range) {
        name = new String(nam);
        label = name;
        value = val;
        closedMin = range.startsWith("[");
        closedMax = range.endsWith("]");
        int comma = range.indexOf(",");
        String Smin = new String(range.substring(1,comma).trim()),
            Smax = new String(range.substring(comma+1,range.length()-1).trim());
        if (Smin.length()>0)
            min = Double.valueOf(Smin).doubleValue();
        if (Smax.length()>0)
            max = Double.valueOf(Smax).doubleValue();
    }

    public boolean isValid(double val) {
        boolean leftOK = closedMin ? val >= min : val > min;
        boolean rightOK = closedMax ? val <= max : val < max;
        return leftOK && rightOK;
    }

    public double val() {
        return value;
    }

    public void setValue (double v) {
        if (isValid(v))
            value = v;
        else
            Utility.warning("Illegal value of " + label + ": " +v);
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
