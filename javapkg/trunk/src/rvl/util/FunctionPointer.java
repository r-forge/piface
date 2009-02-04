package rvl.util;

import rvl.util.*;
import java.lang.reflect.*;

/**
* This class allows you to set up a UniFunction
* object for use in Solve, etc.
* You give the constructor the object or the class and
* the name of the function, which must have one double argument
* and return a double value.
* Accessors are provided to set up parameters
*/

public class FunctionPointer extends UniFunction {

    static final Class[] oneDoubleArg = new Class[] { Double.TYPE };
    private Object obj;     // obj instance where meth resides
    private Method meth;    // method to call


/**
* Constructor to set up obj's method: public double fcn(double)
* as a UniFunction
*/
    public FunctionPointer(Object obj, String fcn) {
        this(obj.getClass(), fcn);
        this.obj = obj;
    }

/**
* Constructor to set up a static method: public double fcn(double)
* as a UniFunction
*/
    public FunctionPointer(Class cls, String fcn) {
        try {
            meth = cls.getMethod(fcn, oneDoubleArg);
            if (!meth.getReturnType().equals(Double.TYPE))
                Utility.warning(meth.toString() + ": Wrong return type");
        }
        catch (Exception e) {
            Utility.warning("method \"" + fcn + "\" not found\n" + e);
        }
    }

/**
* Required method of UniFunction.  Uses reflection
* to evaluate the function and return
*/
    public double of (double x) {
        try {
            return ((Double)meth.invoke(obj,
                new Double[] { new Double(x) })).doubleValue();
        }
        catch (Exception e) {
            Utility.warning("Error in FunctionPointer: " + e);
        }
        return Double.NaN;
    }

/**
* Set up minimum of interval for solution
*/
    void setMin(double xMin, boolean closed) {
        this.xMin = xMin;
        closedMin = closed;
    }

/**
* Set up maximum of interval for solution
*/
    void setMax(double xMax, boolean closed) {
        this.xMax = xMax;
        closedMax = closed;
    }

/**
* Set convergence parameters
*/
    void setConv(int maxIter, int maxSearch, double feps, double xeps) {
        this.maxIter = maxIter;
        this.maxSearch = maxSearch;
        this.feps = feps;
        this.xeps = xeps;
    }

    public static void main(String argv[]) {
        FunctionPointer fp = new FunctionPointer (Math.class,"exp");
        fp.setMin(0,false);
        fp.setMax(2,false);
        fp.verbose = true;
        double x = Solve.search(fp, 2, 1, .1);
    }

}