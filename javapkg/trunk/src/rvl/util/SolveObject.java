package rvl.util;


/**
 * This class allows one to set up a hook in any
 * object to reference in <tt>Solve.illinois()</tt> or
 * <tt>Solve.search()</tt>.  Variables exist that allow
 * a specification of the domain, whether the endpoints are
 * open or closed, and various convergence criteria.
 * Instances should define the "of" method, and modify the
 * bounds and other parameters as appropriate.
 * To use, have the class implement <tt>Solvable</tt>, set
 * up a suitable <tt>solveHook(m,x)</tt> method that returns
 * a function value at <tt>x</tt> for each desired mode <tt>m</tt>.
 * Define new <tt>SolveObject</tt> variables where needed;
 * modify the bounds and convergence parameters as appropriate.
 * @author Russ Lenth
 * @version 1.0 June 29,1996
 * @see Solve
 */
public class SolveObject extends UniFunction {

/**
 * Instantiate a SolveObject.
 * @param m     mode number passed to solveHook
 * @param p     object containing the solveHook
 */
    public SolveObject(int m, Solvable p) {
        mode = m;
        parent = p;
    }

    public double
    of (double x) {
        return parent.solveHook(mode, x);
    }

/** Domain of the function */
    public double xMin = -9e300, xMax = 9e300;
    public boolean closedMin = false, closedMax = false;

/** Controls for solving process */
    public int maxIter = 200, maxSearch = 50;
    public double feps = 1e-10, xeps = 1e-6;
    public boolean verbose = false;

/** parent info */
    private Solvable parent;
    private int mode;
}
