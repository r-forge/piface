package rvl.util;

/** Useful math functions
 * @author Russ Lenth
 * @version 1.0 June 29, 1996
 */

public class MoreMath {

/**
 * Natural logarithm of the gamma function at x
 */
    public static double logGamma (double x) {
        if (x <= 0.0d) {
            Utility.warning("logGamma: argument must be > 0");
            return Double.NaN;
        }
        if (x < 15.0d)
            return logGamma (x + 1.0d) - Math.log(x);

        double z = 1.0d / (x*x);
        return (
            (x - 0.5) * Math.log(x) - x + 0.91893853320467267 +
            (1 - z * (1 - z * (1 - 0.75 * z) / 3.5) / 30) / 12 / x );
    }

/**
 * Gamma function at x
 */
    public static double gamma (double x) {
        return Math.exp (logGamma(x));
    }

/**
 * Beta function beta(a,b) = gamma(a)gamma(b)/gamma(a+b)
 */
    public static double beta (double a, double b) {
        return Math.exp (logGamma(a) + logGamma(b) - logGamma(a+b));
    }

}
