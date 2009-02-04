package rvl.stat.dist;

import rvl.stat.dist.*;
import rvl.util.*;

/**
 *  The Poisson distribution
 *  @author Russ Lenth
 *  @version 1.0 September, 1998
 */

public class Poisson {

    public static double cdf (int x, double lambda) {
        if (x < 0) return 0.0;
        if (lambda <= 0) {
            Utility.warning("Poisson.cdf: lambda must be positive");
            return Double.NaN;
        }
        return 1 - Chi2.cdf (2 * lambda,   2 * (x + 1.0));
    }

    public static int quantile (double alpha, double lambda) {
        if (lambda <= 0) {
            Utility.warning("Poisson.quantile: lambda must be positive");
            return Integer.MAX_VALUE;
        }
        if (alpha < 0 || alpha >= 1) {
            Utility.warning("Poisson.quantile: alpha must be in [0,1)");
            return Integer.MAX_VALUE;
        }
        if (alpha == 0)
            return -1;

    int x = (int) (lambda + Normal.quantile(alpha) * Math.sqrt(lambda));
        x = Math.max(-1, x);
        double prob = cdf(x, lambda);
        if (prob < alpha) {
            do {
                prob = cdf (++x, lambda);
            } while (prob < alpha - 1.0e-6);
            x--;
        }
        else if (prob > alpha) {
            do {
                prob = cdf (--x, lambda);
            } while (prob > alpha + 1.0e-6);
        }
        return x;
    }

}