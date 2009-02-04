package rvl.stat.dist;
import rvl.util.*;
import rvl.stat.dist.*;

/**
 * Functions relating to the <i>t</i> distribution (central and noncentral)
 * @author Russ Lenth
 * @version 1.3 November 8, 2000
 */

public class T {

/**
 * @return noncentral cdf at <tt>t</tt> with d.f. <tt>df</tt>
 * and noncentrality parameter <tt>delta</tt>
 */

    public static double
    cdf (double t, double df, double delta) {
        double CONST = 0.398942280401432703,
            maxErr = 5e-9;   // actually bounds half the error
        int maxIter = 400;
        double   f,p,q,s,x, a,b,lambda,gamodd,gameven, ixodd,ixeven;
        int     n;

        if (Double.isNaN(t+df+delta))
            return Double.NaN;

        if (df<=0)
            return Utility.NaN("T.cdf: negative d.f.");

        if (t<0)
            return ( 1 - cdf(-t, df, -delta) );

    /* double series in Guenther 1978 */
        lambda = delta*delta;
        p = Math.exp(-lambda/2)/2;
        q = CONST * 2 * p * delta;
        s = 0.5 - p;
        x = t * t;
        x /= x + df;
        a = 0.5;
        b = a * df;
        ixodd = Beta.cdf (x, a, b);
        gamodd = 2 * Math.pow(x,a) * Math.pow(1-x,b) / MoreMath.beta(a,b);
        if (Math.abs(delta) > 1e-8) {
            gameven = Math.pow(1-x, b);
            ixeven = 1 - gameven;
            gameven = b*x*gameven;
        }
        else {
            ixeven = gameven = 0;
        }
        n = 1;
        f = p*ixodd + q*ixeven;
        while ( (s*(ixodd-gamodd) > maxErr) && (n<=maxIter) ) {
            a++;
            ixodd -= gamodd;
            ixeven -= gameven;
            gamodd *= x * (a+b-1) / a;
            gameven *= x * (a+b-0.5)/(a+0.5);
            p *= lambda/(2*n);
            q *= lambda/(2*n+1);
            s -= p;
            n++;
            f += p*ixodd + q*ixeven;
        }
        if (n > maxIter) {
            return Utility.NaN("T.cdf: too many iterations");
        }
        return (f + Normal.cdf(-delta));
    }

/**
 * @return central cdf at <tt>t</tt> with d.f. <tt>df</tt>
 */
    public static double
    cdf (double t, double df) {
        return cdf (t, df, 0.0d);
    }

/**
 * @return <tt>p</tt>th quantile of the noncentral <i>t</i>
 * with d.f. <tt>df</tt>
 * and noncentrality parameter <tt>delta</tt>
 */
    public static double
    quantile (double p, double df, double delta) {
        if (Double.isNaN(p+df+delta))
            return Double.NaN;
        if (p<=0 || p>= 1)
            return Utility.NaN("T.quantile: p not in (0,1)");
        TAux f = new TAux(df,delta);
        double x = delta + 5.0d * ( Math.pow(p,.14) - Math.pow(1-p,.14) );
        return Solve.search(f, p, x-.005, .01);
    }

    public static double quantile (double p, double df) {
        return quantile (p, df, 0.0d);
    }

/**
 * @return power of normal-theory <tt>t</tt> test of
 * H<sub>0</sub>: delta = 0  vs H<sub>1</sub>: delta = <tt>delta</tt>
 * with d.f. <tt>df</tt> and size level <tt>alpha</tt>.
 * <tt>tail</tt> is ( <0 | ==0 | >0 ) for a (left | two | right)-tailed
 * test.
 */
    public static double
    power (double delta, double df, int tail, double alpha) {
        if (Double.isNaN(delta+df+alpha))
            return Double.NaN;
        double critval;
        if (tail < 0)
            return power (-delta, df, 1, alpha);
        if (tail == 0) {
            critval = quantile (alpha/2, df);
            return cdf (critval, df, delta)
                + 1.0d - cdf (-critval, df, delta);
        }
        critval = -quantile(alpha, df);
        return 1.0d - cdf(critval, df, delta);
    }

    public static double
    delta (double power, double df, int tail, double alpha) {
        if (Double.isNaN(power+df+alpha))
            return Double.NaN;
        if (power <= 0 || power >= 1)
            return Utility.NaN("T.delta: power not in (0,1)");
        if (alpha <=0 || alpha >= 1)
            return Utility.NaN("T.delta: alpha not in (0,1)");
        double del;
        if (tail < 0)
            return -delta(power, df, 1, alpha);

        TAux2 f = new TAux2 (tail, alpha, df);
        if (tail>0)
            del = T.quantile(1 - alpha, df) + T.quantile(1 - power, df);
        else
            del = T.quantile(1 - 0.5*alpha, df) + T.quantile(1 - power, df);
        return Solve.search (f, power, del-.05, .1);
    }

/**
 * @return area under the ROC curve with ncp
 * <tt>delta</tt> and d.f. <tt>df</tt>
 * This is the integral of <tt>power(..., alpha)</tt>
 * over <tt>alpha</tt>.
 * @param eps Bound on the error of numerical integration
 */
    public static double
    rocArea(double delta, double df, int tail, double eps) {
        saveDelta = delta;
        saveDf = df;
        saveTail = tail;
        return NumAnal.integral(rvl.stat.dist.T.class,
            "power", 0, 1, eps, false, 0, 1);
    }
/**
 * @return rocArea(delta, df, 1e-4)
 */
    public static double rocArea(double delta, double df, int tail) {
        return rocArea(delta, df, tail, 1e-4);
    }

/**
 * @return power at alpha for internally stored parameters.
 * This is an auxiliary function used by rocArea, not
 * intended for other uses
 */
    public static double power(double alpha) {
        return power(saveDelta, saveDf, saveTail, alpha);
    }

// Parameters used by rocArea, power(alpha), rocEquiv, rocArea(alpha)
    static private double saveDelta, saveDf, saveMu, saveTol, saveSE;
    static private int saveTail = 0;

    /**
     * @return power of test of equivalence when the true mean is
     * <tt>mu</tt>, the tolerance bound for a negligible effect is
     * <tt>tol</tt>, the population value of the standard error is
     * , degrees of<tt>se</tt>, degrees of freedom <tt>df</tt>,
     * and significance level <tt>alpha</tt>.
     * <p>
     * Uses a crude trapezoidal-rule integration over 100 subintervals
     */
    public static double
    powerEquiv(double mu, double tol, double se, double df, double alpha) {
        if (tol <= 0) return 0;
        double ta = quantile(1 - alpha, df),
        delta = mu / se,
        cutoff = tol / se,
        bound,
        inc,
        d2 = .5 * df,
        sum = 0;

    /*
       If ta is 0, the integrand is constant.
    */
        if (Math.abs(ta) < .0001)
            return Normal.cdf(cutoff - delta) - Normal.cdf(-cutoff - delta);

        /*
           Integration is over a triangular region when ta > 0.
           if ta < 0, we get a fan-shaped region.  In any case, as ta gets
           small or negative, the vertex of the triangle is way beyond the
           effective support of the distn of W = sqrt(chi^2/df), and we
           limit the region of integration at the .9999 quantile.
        */
        if (ta > 0) bound = cutoff / ta;
        else bound = Double.POSITIVE_INFINITY;
        double wBound = Math.sqrt(Chi2.quantile(.9999, df, 0) / df);
        bound = Math.min(bound, wBound);
        inc = bound / 100;
        for (double w = inc; w < bound; w += inc) {
            double p = Normal.cdf(cutoff - delta - ta*w)
            - Normal.cdf(ta*w - cutoff - delta);
            double f = Math.pow(w, df-1) * Math.exp(-d2 * w * w);
            sum += p * f;
        }
        double logMult = d2 * Math.log(d2) - MoreMath.logGamma(d2);
        return sum * 2 * inc * Math.exp(logMult);
    }

/**
 * Used for integration in rocEquiv -- not meant to be called by user
 */
    public static double
    powerEquiv(double alpha) {
        return powerEquiv(saveMu, saveTol, saveSE, saveDf, alpha);
    }


/**
 * @return ROC area for an equivalence test
 */
    public static double
    rocEquiv(double mu, double tol, double se, double df, double eps) {
        saveMu = mu;
        saveTol = tol;
        saveSE = se;
        saveDf = df;
        return NumAnal.integral(rvl.stat.dist.T.class,
            "powerEquiv", 0, 1, eps, false, 0, 1);
    }

/**
 * @return ROC area for an equivalence test, with eps = 1e-4
 */
    public static double
    rocEquiv(double mu, double tol, double se, double df) {
        return rocEquiv(mu, tol, se, df, 1e-4);
    }

// Test routine
    public static void main(String[] args) {
        double t = rvl.util.Utility.strtod(args[0]),
               df = rvl.util.Utility.strtod(args[1]),
               delta = rvl.util.Utility.strtod(args[2]),
               cdf = cdf(t, df, delta);
        System.out.println("cdf: " + cdf);
    }

} // T.class ////////////////////////////////////////////////////





class TAux extends UniFunction {
    private double df, delta;
    public TAux (double nu, double del) {
        df = nu;
        delta = del;
    }
    public double of (double x) {
        return T.cdf(x, df, delta);
    }
}

class TAux2 extends UniFunction {
    private double df, critval;
    private int tail;

    public TAux2 (int Tail, double Alpha, double Df) {
        tail = Tail;
        df = Df;
        if (Tail>0)
            critval = T.quantile(1-Alpha, Df);
        else
            critval = T.quantile(1-0.5*Alpha, Df);
    }
    public double of (double delta) {
        if (tail > 0)
            return 1.0d - T.cdf(critval, df, delta);
        else
            return T.cdf(-critval, df, delta)
                + 1.0d - T.cdf(critval, df, delta);
    }
}
