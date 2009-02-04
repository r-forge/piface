package rvl.util;
import rvl.util.UniFunction;

/**
 * Routines for numerical solution of univariate equations
 * @author Russ Lenth
 * @version 1.0  June 29, 1996
 */

public class Solve {

    static void
    showParams (UniFunction f) {
        System.out.println("rvl.util.Solve - parameters");
        System.out.println("maxIter = " +f.maxIter+
            ", maxSearch = " +f.maxSearch+ ", feps = " +f.feps+
            ", xeps = " +f.xeps);
        System.out.println("Function bounds: "
            +f.xMin+ " (closed=" +f.closedMin+ "), "
            +f.xMax+ " (closed=" +f.closedMax +")");
    }

/**
 * Solve <tt>f.of(x) == target</tt> in the interval <tt>(x1,x2)</tt>
 * using the Illinois method.
 * @return the solution
 */
    public static double
    illinois (UniFunction f, double target, double x1, double x2)
    {
        double  f1, f2;

        if(Double.isNaN(x1+x2))
            return Utility.NaN("solve: NaNs in starting interval");

        f1 = f.of(x1) - target;
        f2 = f.of(x2) - target;

        if (f.verbose) {
            System.out.println("solve: x1 = " + x1);
            System.out.println("solve: x2 = " + x2);
        }
        return illinois (f, target, x1, x2, f1, f2);
    }

// Here's the meat of the solver.  this version requires that you already
// have evaluated value() - target at x1 and x2

    protected static double
    illinois (UniFunction f, double target, double x1, double x2,
              double f1, double f2) {

        double  xnew, fnew;
        int     iter = 0;

        if (f.verbose) {
            System.out.println("Call to rvl.util.Solve.illinois");
            System.out.println("target = " +target+ ", interval = ["
                +x1+ "," +x2+ "], fcn values = ["
                +f1+ "," +f2+ "]");
            showParams (f);
        }

        if(Double.isNaN(f1+f2))
            return Utility.NaN("solve: function evaluates to NaN");

        if (f1*f2 > 0)       // invalid starting interval
            return Utility.NaN("solve: bad starting interval");

        while (iter++ < f.maxIter) {      // main loop
            if (Math.abs(f1 - f2) <= 1e-99)   // divide by zero
                return Utility.NaN("solve: divide by zero");
            xnew = x2 - f2 * (x2 - x1) / (f2 - f1); // New trial value
            if (f.verbose)
                System.out.println("solve: xnew = " + xnew);
            fnew = f.of(xnew) - target;
            if(Double.isNaN(fnew))
                return Utility.NaN("solve: function evaluates to NaN");
            if (fnew*f2 > 0)                    // same side as x2:
                f1 /= 2.0;
            else {                      // different sides
                x1 = x2;
            f1 = f2;
            }
            x2 = xnew;
            f2 = fnew;
            if (Math.abs(x1 - x2) <= f.xeps || Math.abs(fnew) <= f.feps) {      // converged?
                return (xnew);
            }
        }
        return Utility.NaN("solve: too many iterations");

    } //----[ end of Illinois() ]----


// search for a solution with one starting value
// uses start and start+incr to begin search for a starting interval,
// using a secant search; calls solve once it finds one.

/**
 * Solve <tt>f.of(x) == target</tt> using starting value <tt>start</tt>.
 * Searching is initialized based on function values at <tt>start</tt>
 * and <tt>start + incr</tt>, both of which are assumed to lie in the
 * domain of <tt>f</tt>.  Once an enclosing interval is found, solution
 * is completed using the Illinois method.
 * @return the solution
 */
    public static double
    search (UniFunction f, double target, double start, double incr) {
        double xnew = start + incr, fstart, fnew;
        int tries = 0;

        if (f.verbose) {
            System.out.println("Call to rvl.util.Solve.search");
            System.out.println("target = " +target+ ", start = "
                +start+ ", incr = " +incr);
            showParams (f);
        }

        if (Double.isNaN(start + target + incr))
            return Utility.NaN("solve: NaN encountered in initialization");

        fstart = f.of(start) - target;
        fnew = f.of(xnew) - target;

        if (f.verbose) {
            System.out.println("search: start = " + start);
            System.out.println("search: xnew = " + xnew);
        }
        if (Double.isNaN(fnew))
            return Utility.NaN("solve: function evaluates to NaN");

        while (fstart*fnew > 0) {
            if (++tries > f.maxSearch)
                return Utility.NaN("search: can't find enclosing interval after "
                    +f.maxSearch+ " tries");
            incr = -1.5 * (xnew - start) * fnew / (fnew - fstart);
                // This is an inflated secant-method iteration.
            start = xnew;
            fstart = fnew;
            xnew += incr;
            if (xnew <= f.xMin)
                xnew = f.closedMin ? f.xMin : f.xMin + .1*(start - f.xMin);
            if (xnew >= f.xMax)
                xnew = f.closedMax ? f.xMax : f.xMax - .1*(f.xMax - start);
            if (f.verbose)
                System.out.println("search: xnew = " + xnew);
            fnew = f.of(xnew) - target;
            if (Double.isNaN(fnew))
                return Utility.NaN("solve: function evaluates to NaN");
        }
        // If we got here, we have found an enclosing interval
        return illinois (f, target, start, xnew, fstart, fnew);

    } //----[ end of Search() ]----

}
