package rvl.util;


/**
 * This class allows one to set up a univariate function object, including
 * a specification of its domain and whether the endpoints are open
 * or closed.
 * Instances should define the "of" method, and modify the 
 * bounds and other parameters as appropriate.
 * @author Russ Lenth
 * @version 1.0 June 29,1996
 * @see Solve
 */

public abstract class UniFunction {

    public abstract double 
    of (double x);
    
/** Domain of the function */    
    public double xMin = -9e300, xMax = 9e300;
    public boolean closedMin = false, closedMax = false;

/** Controls for solving process */
    public int maxIter = 100, maxSearch = 25;
    public double feps = 1e-10, xeps = 1e-6;
    public boolean verbose = false;    
}
