package rvl.stat.dist;

import rvl.util.*;
import rvl.stat.dist.*;

/**
 * The chi-square distribution (central and noncentral)
 * @author Russ Lenth
 * @version 1.2 April 17, 2000
 */

public class Chi2 {

/**
 * @return cdf at <tt>x</tt> of Chi2(<tt>df</tt>) distribution
 */
    public static double
    cdf (double y, double df) {
        return cdf (y, df, 0.0);
    }

/**
 * @return cdf at <tt>y</tt> of noncentral Chi2(<tt>df</tt>) distribution
 */
    public static double
    cdf (double y, double df, double lambda) {
        double          y2, df2, term, sum, p, start,
                        a0, a1, aj, b0, bj, maxerr = 1.0e-8;
        int             j, maxiter = 500;

        if (y <= 0)
            return 0;
        if (df < 0.01) {
            rvl.util.Utility.warning("Chi2.cdf: df must be positive");
            return Double.NaN;
        }
        if (lambda < 0) {
        rvl.util.Utility.warning("Chi2.cdf: lambda must be nonnegative");
            return Double.NaN;
        }

        sum = 0;
    y2 = y / 2;
    df2 = df / 2;
    start = Math.exp(df2 * Math.log(y2) - y2 - MoreMath.logGamma(df2));
    j = 1;
    if (y / df > 1) {   // cont'd fraction for large y
        a0 = 0;
        a1 = start / y2;
        b0 = 1 / y2;    // b1 is always 1
        do {
        j++;
        if (2 * (j / 2) < j) {  /* j is odd  */
            aj = (j - 1.0) / 2;
            bj = y2;
        } else {    /* j is even */
            aj = (j - df) / 2;
            bj = 1;
        }
        b0 = 1 / (aj * b0 + bj);
        term = a1;
        a1 = b0 * (aj * a0 + bj * a1);
        a0 = b0 * term;
        term = sum;
        sum = 1 - a1;
        }
        while ((Math.abs(term - sum) >= maxerr) && (j <= maxiter));
        if (j > maxiter)
        rvl.util.Utility.warning("Chi2.cdf: convergence failure");
    } else {        // series for small y
        term = start / df2;
        sum = term;
        do {
        term *= y / (df + 2 * j);
        sum += term;
        j++;
        }
        while ((term >= maxerr) && (j <= maxiter));
        if (j > maxiter)
        rvl.util.Utility.warning("Chi2.cdf: convergence failure");
    }

    // Noncentral cdf...
    if (lambda > 0) {
        p = Math.exp(-lambda / 2);
        b0 = 1 - p;
        a0 = sum - start / df2;
        start = start * y2 / (df2 * (df2 + 1));
        df2 += 2;
        sum *= p;
        j = 0;
        do {
        j++;
        p *= lambda / (2 * j);
        b0 -= p;
        sum += p * a0;
        a0 -= start;
        start *= y2 / df2;
        df2++;
        }
        while ((b0 * a0 >= maxerr) && (j <= maxiter));
        if (j > maxiter)
        rvl.util.Utility.warning("Chi2.cdf: convergence failure");
    }
        return sum;
    }

    public static double quantile (double p, double df) {
        return quantile (p, df, 0.0);
    }

    public static double quantile (double p, double df, double lambda) {
        if ( p < 0 || p >= 1) {
        rvl.util.Utility.warning("Chi2.quantile: p must be in [0,1)");
            return Double.NaN;
        }
        if ( lambda < 0 ) {
        rvl.util.Utility.warning("Chi2.quantile: lambda must be nonnegative");
            return Double.NaN;
        }
        if ( df < 0.01 ) {
        rvl.util.Utility.warning("Chi2.quantile: df must be positive");
            return Double.NaN;
        }

    // Starting value is based on normal(df + lambda, 2df + 4lambda) distn.
        double start = df + lambda + Normal.quantile(p) * Math.sqrt(2 * df + 4 * lambda);
        Chi2Aux fcn = new Chi2Aux (df, lambda);
        return Solve.search (fcn, p, start, .1);
    }

/**
 *  Power for a right-tailed test of size alpha
 */
    public static double power (double lambda, double df, double alpha) {
        if ( alpha <= 0 || alpha >= 1) {
        rvl.util.Utility.warning("Chi2.power: alpha must be in (0,1)");
            return Double.NaN;
        }
        if ( lambda < 0 ) {
        rvl.util.Utility.warning("Chi2.power: lambda must be nonnegative");
            return Double.NaN;
        }
        if ( df < 0.01 ) {
        rvl.util.Utility.warning("Chi2.power: df must be positive");
            return Double.NaN;
        }
        double critVal = quantile (1 - alpha, df, 0 );
        return 1 - cdf (critVal, df, lambda);
    }

/**
 *  @return lambda such that power (lambda, df, alpha) = power
 */
    public static double lambda (double power, double df, double alpha) {
        if ( alpha <= 0 || alpha >= 1) {
        rvl.util.Utility.warning("Chi2.lambda: alpha must be in (0,1)");
            return Double.NaN;
        }
        if ( power <= 0 || power >= 1) {
        rvl.util.Utility.warning("Chi2.lambda: power must be in (0,1)");
            return Double.NaN;
        }
        if ( df < 0.01 ) {
        rvl.util.Utility.warning("Chi2.lambda: df must be positive");
            return Double.NaN;
        }

    // Starting value is based on normal(df + lambda, 2df + 4lambda) distn.
    // Have to solve a quadratic equation to get lambda.
        double z2 = Math.pow (Normal.quantile(1 - power), 2),
            critVal = quantile(1 - alpha, df, 0),
            b = 2 * (critVal - df) + 4 * z2,
            c = Math.pow(critVal - df, 2) - 2 * df * z2,
            discrim = Math.pow (b, 2) - 4 * c,
            sgn = (power > .5) ? 1 : -1,
            start = (discrim < 0) ?  critVal : (b + sgn * Math.sqrt(discrim)) / 2;
        start = Math.max(.5, start);
        Chi2Aux2 fcn = new Chi2Aux2 (df, alpha);
        return Solve.search (fcn, power, start, .5);
    }

/**
 * @return area under the ROC curve with ncp
 * <tt>lambda</tt> and d.f. <tt>df</tt>
 * This is the integral of <tt>power(..., alpha)</tt>
 * over <tt>alpha</tt>.
 * @param eps Bound on the error of numerical integration
 */
    public static double
    rocArea(double lambda, double df, double eps) {
        saveLambda = lambda;
        saveDf = df;
        return NumAnal.integral(rvl.stat.dist.Chi2.class,
            "power", 0, 1, eps, false, 0, 1);
    }
/**
 * @return rocArea(lambda, df, 1e-4)
 */
    public static double rocArea(double lambda, double df) {
        return rocArea(lambda, df, 1e-4);
    }

/**
 * @return power at alpha for internally stored parameters.
 * This is an auxiliary function used by rocArea, not
 * intended for other uses
 */
    public static double power(double alpha) {
        return power(saveLambda, saveDf, alpha);
    }

// Parameters used by rocArea and power(alpha)
    static private double saveLambda = 0, saveDf = 1;

}




class Chi2Aux extends UniFunction {
    private double df, lambda;

    public Chi2Aux (double df, double lambda) {
        this.df = df;
        this.lambda = lambda;
        xMin = 0;
        closedMin = true;
        xeps = 1e-5;
    }

    public double of (double y) {
        return Chi2.cdf (y, df, lambda);
    }
}

class Chi2Aux2 extends UniFunction {
    private double df, alpha;

    public Chi2Aux2(double df, double alpha) {
        this.df = df;
        this.alpha = alpha;
        xMin = 0;
        closedMin = true;
        xeps = 1e-5;
    }

    public double of (double lambda) {
        return Chi2.power (lambda, df, alpha);
    }
}

