/**
* This class provides various numerical analysis methods
*/

package rvl.util;

import java.lang.reflect.*;
import rvl.util.Utility;

public class NumAnal {

// Constants for numerical quadrature
    static double aiFac = 10,   // factor for tol in adaptive integration
        AA = 0, BB = 1;                 // params of transformation
    static int fCount = 0,              // number of function calls
        maxFcnCalls = 2000;             // limit on fCount
    static final Class[] oneDoubleArg = new Class[] { Double.TYPE };
    static Method fMethod, gMethod;      // Methods used by f()
    static Object caller, gCaller;       // Object containing fMethod or gMethod



/**
* The function being integrated
*/
    private static double f(double x) {
        if (++fCount > maxFcnCalls)
            return Utility.NaN("integral(): too many function calls");
        try {
            return ((Double)fMethod.invoke(caller,
                new Double[] { new Double(x) })).doubleValue();
        }
        catch (Exception e) {
            Utility.warning("Error in f(): " + e);
            Utility.warning("fMethod = " + fMethod +", caller = " + caller);
        }
        return Double.NaN;
    }

/**
* Utility fcn to do substitution x = (y - AA) / {BB + (y - AA)}
* to transform y in [AA,infty) or (-infty,AA] to x in [0,1).  This will be
* set up to be called by f().
*/
    public static double fHalfLine(double x) {
        try {
            double y = AA + BB * x / (1 - x),
                dy = BB / (1 - x) / (1 - x),
                fy = ((Double)gMethod.invoke(gCaller,
                    new Double[] { new Double(y) })).doubleValue();
            return fy * dy;
        }
        catch (Exception e) {
            return Utility.NaN("Error in fHalfLine(): " + e);
        }
    }

/**
* @return integral of the static function named fcn in Class cls
* from a to b.  Uses adaptive Simpson's rule
*/
    public static synchronized double integral(Class cls, String fcn,
        double a, double b, double tol)
    {
        return integral(cls, fcn, a, b, tol, false, Double.NaN, Double.NaN);
    }

/**
* @return integral of the function named fcn in Object obj
* from a to b.  Uses adaptive 3-point rule, open or closed
* depending on open
*/
    public static synchronized double integral(Class cls, String fcn,
        double a, double b, double tol, boolean open)
    {
        return integral(cls, fcn, a, b, tol, open, Double.NaN, Double.NaN);
    }

/**
* @return integral of the function named fcn in Object obj
* from a to b.  Uses adaptive Simpson's rule
*/
    public static synchronized double integral(Object obj, String fcn,
        double a, double b, double tol)
    {
        return integral(obj, fcn, a, b, tol, false, Double.NaN, Double.NaN);
    }

/**
* @return integral of the function named fcn in Object obj
* from a to b.  Uses adaptive 3-point rule, open or closed
* depending on open
*/
    public static synchronized double integral(Object obj, String fcn,
        double a, double b, double tol, boolean open)
    {
        return integral(obj, fcn, a, b, tol, open, Double.NaN, Double.NaN);
    }

/**
* @return integral of the function named fcn in Object obj
* from a to b.  Uses adaptive open or closed 3-point rule
* depending on value of <tt>open</tt>.  If
* <tt>open == false</tt>, this routine
* uses provided values of <tt>fa</tt> = f(a),
* <tt>fb</tt> = f(b) if they are
* proper.  They are computed if equal to <TT>Double.NaN</TT>.
* The values of <TT>fa</TT> and <TT>fb</TT> are ignored when <TT>open == true</TT>.
* <p>
* One or both of the limits a and b may be infinite
* (<TT>Double.POSITIVE_INFINITY</TT> or <TT>Double.NEGATIVE_INFINITY</TT>);
* in such cases, the transformation
* y = (x - <TT>AA</TT>) / (<TT>BB</TT> + (x - <TT>AA</TT>)
* is applied to make it an integral on (0,1).  When only
* one of the limits is infinite, AA is set to the finite endpoint
* and BB is made negative or positive as appropriate.  When both
* limits are infinite, the current value of AA is used to break
* the integral into two regions (-infty,<TT>AA</TT>) and (<TT>AA</TT>,infty).
* Programmers may want to set <TT>AA</TT> or <TT>BB</TT> to suitable values
* to improve performance.
* When infinite limits are used, open is forced to be true.
*/
    public static synchronized double integral(Object obj, String fcn,
        double a, double b, double tol, boolean open,
        double fa, double fb)
    {
        caller = obj;
        return integral(obj.getClass(), fcn, a, b, tol, open,fa, fb);
    }

/**
* @return integral of the static function named fcn in Class cls
* from a to b.  Uses adaptive open or closed 3-point rule
* depending on value of open.  If open==false, this routine
* uses provided values of fa = f(a), fb = f(b) if they are
* proper.  They are computed if equal to Double.NaN.
* The values of fa and fb are ignored when open==true.
*/
    public static synchronized double integral(Class cls, String fcn,
        double a, double b, double tol, boolean open,
        double fa, double fb)
    {
        boolean aInf = Double.isInfinite(a), bInf = Double.isInfinite(b);
        if (aInf || bInf) {
            if (aInf && bInf) {
                double left = integral(cls,fcn,a,AA,tol/2,true,0,0);
                int cnt = fCount;
                double right = integral(cls,fcn,AA,b,tol/2,true,0,0);
                fCount += cnt;
                return left + right;
            }
            else if (aInf) {
                AA = b;
                BB = - Math.abs(BB);
                gMethod = setupMethod(cls, fcn);
                return - integral(NumAnal.class, "fHalfLine", 0, 1, tol, true, 0, 0);
            }
            else {
                AA = a;
                BB = Math.abs(BB);
                gMethod = setupMethod(cls, fcn);
                return integral(NumAnal.class, "fHalfLine", 0, 1, tol, true, 0, 0);
            }
        }
        else
            fMethod = setupMethod(cls, fcn);

        fCount = 0;
        double m = (a + b)/2, fm = f(m), s;
        if (Double.isNaN(fa+fb+fm))
            return Utility.NaN("integral: NaN in function evaluation");
        if (open) {
            double h = (b - a) / 4,
                f1 = f(a + h),
                f2 = f(b - h);
            s = 4 * h * (2 * (f1 + f2) - fm) / 3;
            return openRefineIntegral(a, m, b, f1, fm, f2, s, tol);
        }
        else {
            if (Double.isNaN(fa)) fa = f(a);
            if (Double.isNaN(fb)) fb = f(b);
            s = (fa + 4*fm + fb) * (b - a) / 6;
            return refineIntegral(a, m, b, fa, fm, fb, s, tol);
        }
    }

    private static Method setupMethod (Class cls, String fcn) {
        Method meth;
        try {
            meth = cls.getMethod(fcn, oneDoubleArg);
            if (meth.getReturnType().equals(Double.TYPE))
                return meth;
            else
                Utility.warning(meth.toString() + ": Wrong return type");
        }
        catch (Exception e) {
            Utility.warning("method \"" + fcn + "\" not found\n" + e);
        }
        return (Method) null;
    }

// adaptive 3-point closed rule (Simpson)
    static double refineIntegral(double a, double m, double b,
        double fa, double fm, double fb, double s, double tol)
    {
        double h = (b - a) / 4,
            fm1 = f(a + h), fm2 = f(b - h),
            s1 = (fa + 4*fm1 + fm) * h / 3,
            s2 = (fm + 4*fm2 + fb) * h / 3;
        if (Double.isNaN(fm1+fm2))
            return Utility.NaN("refineIntegral: NaN in function evaluation");
        if (Math.abs(s1 + s2 - s) < aiFac * tol)
            return s1 + s2;
        else
            return refineIntegral(a, a+h, m, fa, fm1, fm, s1, tol/2)
                + refineIntegral(m, b-h, b, fm, fm2, fb, s2, tol/2);
    }

    // adaptive open 3-point rule.  The points for f1, fm, f2
    // are used in a closed rule for the center half, and the
    // open rule is applied to the 1st and last quarters
    static double openRefineIntegral(double a, double m, double b,
        double f1, double fm, double f2, double s, double tol)
    {
        double h = (b - a) / 16, h43 = 4 * h / 3,
            fm1 = f(a + h), fm2 = f(a + 2*h), fm3 = f(a + 3*h),
            fm4 = f(b - 3*h), fm5 = f(b - 2*h), fm6 = f(b - h),
            sL = h43 * (2 * (fm1 + fm3) - fm2),
            sM = h43 * (f1 + 4*fm + f2),
            sR = h43 * (2 * (fm4 + fm6) - fm5),
            sum = sL + sM + sR;
        if (Double.isNaN(sL+sR))
            return Utility.NaN("openRefineIntegral: NaN in function evaluation");
        if (Math.abs(sum - s) < aiFac * tol)
            return sum;
        else
            return openRefineIntegral(a, a+2*h, a+4*h, fm1, fm2, fm3, sL, tol/4)
                + refineIntegral(a+4*h, m, b-4*h, f1, fm, f2, sM, tol/2)
                + openRefineIntegral(b-4*h, b-2*h, b, fm4, fm5, fm6, sR, tol/4);
    }

    public static int getCallCount() {
        return fCount;
    }

}