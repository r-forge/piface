package rvl.stat.dist;

import rvl.stat.dist.*;
import rvl.util.*;

/**
 *  The binomial distribution
 *  @author Russ Lenth
 *  @version 1.1 February 24, 2000
 */

public class Binomial {

    public static double cdf (int x, int n, double p) {
        if (p < 0 || p > 1) {
            Utility.warning("Binomial.cdf: p must be in [0,1]");
            return Integer.MAX_VALUE;
        }
        if (x < 0) return 0.0;
        if (x >= n) return 1.0;
        return Beta.cdf (1.0 - p, 0.0 + n - x, x + 1.0);
    }

    public static int quantile (double alpha, int n, double p) {
        if (alpha < 0 || alpha > 1) {
            Utility.warning("Binomial.quantile: alpha must be in [0,1]");
            return Integer.MAX_VALUE;
        }
        if (p < 0 || p > 1) {
            Utility.warning("Binomial.quantile: p must be in [0,1]");
            return Integer.MAX_VALUE;
        }
        int x = (int) (n * p + Normal.quantile(alpha) * Math.sqrt(n * p * (1 - p)));
        x = Math.max(-1, Math.min(n, x));
        double prob = cdf(x, n, p);
        if (prob < alpha) {
            do {
                prob = cdf (++x, n, p);
            } while (prob < alpha - 1.0e-6);
            x--;
        }
        else if (prob > alpha) {
            do {
                prob = cdf (--x, n, p);
            } while (prob > alpha + 1.0e-6);
        }
        return x;
    }

/**
 * @return an array, { power, size } at p = p1 of a test of
 * H0: p = p0 vs. an alternative H1 determined by <tt>tail</tt>.
 * n is the sample size, and<tt>alpha</tt> is the significance
 * level.
 */
    public static double[] power (double p0, double p1, int n, int tail, double alpha)
    {
        double size, power;
        int cv;
        if (tail < 0) { // p < p0
            cv = quantile(alpha, n, p0);
            size = cdf(cv, n, p0);
            power = cdf(cv, n, p1);
        }
        else if (tail == 0) {  // p != p0
            cv = quantile(alpha/2, n, p0);
            size = cdf(cv, n, p0);
            power = cdf(cv, n, p1);
            cv = 1 + quantile(1 - alpha/2, n, p0);
            size += 1 - cdf(cv, n, p0);
            power += 1 - cdf(cv, n, p1);
        }
        else {  // p > p0
            cv = 1 + quantile(1 - alpha, n, p0);
            size = 1 - cdf(cv, n, p0);
            power = 1 - cdf(cv, n, p1);
        }
        return new double[] {power, size};
    }


}