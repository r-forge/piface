package rvl.stat.dist;
import rvl.util.*;

/**
 * The F distribution (central and noncentral)
 * @author Russ Lenth
 * @version 1.2 April 17, 2000
 */

public class F {

/**
 * @return cdf of the central <i>F</i> distribution at <tt>f</tt>
 *   with d.f. <tt>(df1, df2)</tt>
 */
    public static double
    cdf (double f, double df1, double df2) {
        if (df1 <= 0 || df2 <= 0) {
            Utility.warning("F.cdf: must use positive df");
            return Double.NaN;
        }
        return cdf(f,df1,df2,0);
    }

/**
 * @return cdf of noncentral <i>F</i> distribution at <tt>f</tt>
 *   with d.f. <tt>(df1, df2)</tt> and noncentrality <tt>lambda</tt>
 */
    public static double
    cdf (double f, double df1, double df2, double lambda) {
        if (df1 <= 0 || df2 <= 0 || lambda < 0) {
            Utility.warning("F.cdf: must use positive df and lambda");
            return Double.NaN;
        }
        f = Math.max(f,0);
        return Beta.cdf(1 - df2 / (df1 * f + df2), df1/2, df2/2, lambda);
    }

/**
 * @return <tt>p</tt>th quantile of the noncentral <i>F</i>
 *   distribution with d.f. <tt>(df1, df2)</tt> and
 *   noncentrality <tt>lambda</tt>
 */
    public static double
    quantile(double p, double df1, double df2, double lambda) {
        if (df1 <= 0 || df2 <= 0 || lambda < 0) {
            Utility.warning("F.quantile: must use positive df and lambda");
            return Double.NaN;
        }
        if (p < 0 || p >= 1) {
            Utility.warning("F.quantile: p is out of bounds");
            return Double.NaN;
        }
        double x = Beta.quantile(p, df1/2, df2/2, lambda);
        return  (df2 / df1) * x / (1 - x);
    }

/**
 * @return <tt>p</tt>th quantile of the central <i>F</i>
 *   distribution with d.f. <tt>(df1, df2)</tt> and
 *   noncentrality <tt>lambda</tt>
 */
    public static double
    quantile(double p, double df1, double df2) {
        return quantile (p,df1,df2,0);
    }


/**
 * @return power of fixed-effects <i>F</i> test with
 * size <tt>alpha</tt> and d.f. <tt>(df1, df2)</tt>
 * when the effect size (noncentrality) if <tt>lambda</tt>
 */
    public static double
    power (double lambda, double df1, double df2, double alpha) {
        if (lambda < 0 || df1 < 0 || df2 < 0 || alpha < 0 || alpha > 1) {
            Utility.warning("F.power: illegal argument");
            return Double.NaN;
        }
        double critval = quantile(1-alpha, df1, df2);
        return 1 - cdf (critval, df1, df2, lambda);
    }

/**
 * @return power of right-tailed <i>F</i> test with
 * size <tt>alpha</tt> and d.f. <tt>(df1, df2)</tt>
 * when the effect size is <tt>effSize</tt>.
 * <tt>random</tt> flags whether it is a fixed- or random-effects test.
 * In an ANOVA context, effSize = {EMS(num) - EMS(denom)} / EMS(denom),
 * i.e., EMS(numerator) / EMS(denom) = 1 + effSize.
 */
    public static double
    power (double effSize, double df1, double df2, double alpha, boolean random) {
        double critval = quantile(1-alpha, df1, df2);
        if (random)
            return 1 - cdf(critval/(1+effSize), df1, df2, 0d);
        else
            return 1 - cdf (critval, df1, df2, df1*effSize);
    }

    public static double
    lambda (double pwr, double df1, double df2, double alpha) {
        if (pwr < alpha || pwr>1 || df1 < 0 || df2 < 0 || alpha < 0 || alpha > 1) {
            Utility.warning("F.lambda: illegal argument");
            return Double.NaN;
        }
        FAux fcn = new FAux(df1, df2, alpha);
        double start = quantile(1-alpha,df1,df2) + quantile(pwr,df1,df2);
        return Solve.search(fcn, pwr, start, .1);
    }

/**
 * @return area under the ROC curve with ncp
 * <tt>lambda</tt> and d.f. <tt>df1, df2</tt>
 * This is the integral of <tt>power(..., alpha)</tt>
 * over <tt>alpha</tt>.
 * @param eps Bound on the error of numerical integration
 */
    public static double
    rocArea(double lambda, double df1, double df2, double eps) {
        saveLambda = lambda;
        saveDf1 = df1;
        saveDf2 = df2;
        return NumAnal.integral(rvl.stat.dist.F.class,
            "power", 0, 1, eps, false, 0, 1);
    }
/**
 * @return rocArea(lambda, df1, df2, 1e-4)
 */
    public static double rocArea(double lambda, double df1, double df2) {
        return rocArea(lambda, df1, df2, 1e-4);
    }

/**
 * @return area under the ROC curve with effect size
 * <tt>effSize</tt> and d.f. <tt>df1, df2</tt>
 * This is the integral of <tt>power(..., alpha)</tt>
 * over <tt>alpha</tt>.
 * @param eps Bound on the error of numerical integration
 */
    public static double
    rocArea(double effSize, double df1, double df2, boolean random, double eps) {
        saveEffSize = effSize;
        saveDf1 = df1;
        saveDf2 = df2;
        saveRandom = random;
        return NumAnal.integral(rvl.stat.dist.F.class,
            "powerr", 0, 1, eps, false, 0, 1);
    }
/**
 * @return rocArea(lambda, df1, df2, 1e-4)
 */
    public static double rocArea(double effSize, double df1, double df2, boolean random) {
        return rocArea(effSize, df1, df2, random, 1e-4);
    }
/**
 * @return power at alpha for internally stored parameters.
 * This is an auxiliary function used by rocArea, not
 * intended for other uses
 */
    public static double power(double alpha) {
        return power(saveLambda, saveDf1, saveDf2, alpha);
    }

/**
 * @return power at alpha for internally stored parameters.
 * This is an auxiliary function used by rocArea, not
 * intended for other uses
 */
    public static double powerr(double alpha) {
        return power(saveEffSize, saveDf1, saveDf2, alpha, saveRandom);
    }

// Parameters used by rocArea
    private static double saveLambda = 0, saveEffSize = 1,
        saveDf1 = 1, saveDf2 = 1;
    private static boolean saveRandom = false;

}

/** Auxiliary class used by F.lambda() */
class FAux extends UniFunction {
    private double df1, df2, alpha;
    public FAux (double d1, double d2, double a) {
        alpha = a;
        df1 = d1;
        df2 = d2;
        xMin = 0; closedMin = true;
        xeps = 1e-5;
    }
    public double of (double lambda) {
        return F.power(lambda, df1, df2, alpha);
    }
}
