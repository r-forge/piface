package rvl.stat.dist;

import rvl.util.*;
import rvl.stat.dist.*;

/**
 * The distribution of R square
 * @author Russ Lenth -- based on Benton/Krishnamoorthy, CSDA 2003
 * @version 1.0 Septembel 12, 2006
 */

public class Rsquare {

/**
 * @return cdf at <tt>x</tt> of distribution of R-square
 * based on p parameters and sample size N, when the
 * population multiple correlation is r2.
 * Note: p is the TOTAL no. of variables in play;
 * i.e., p = 1 + #regressors
 */

    static double errtol = 1.0e-10;
    static int maxiter = 1000;

    public static double cdf(double x, double N, int p, double rho2) {
        double n, a, b, betaf, betab, xgamf, xgamb, pnegbf, pnegbb,
            remain, error, cum;
        int k;
        boolean done;

        if (p < 1 || N < p) {
            Utility.warning("Rsquare.cdf: illegal values of N or p");
            return Double.NaN;
        }
        if (rho2 < 0.0) {
            Utility.warning("Rsquare.cdf: rho2 must be nonnegative");
            return Double.NaN;
        }
        if (x <= 0) return 0;
        if (x >= 1.0) return 1;

        n = N - 1;
        k = (int) (n * rho2 / (2 * (1 - rho2)));
        a = (p - 1.0)/2 + k;
        b = (n - p + 1)/2;
        betaf = Beta.cdf(x, a, b);
    // For extremely small rho2, we've got the central dist
        if (rho2 < 1e-12) return betaf;
        betab = betaf;
        xgamf = Math.exp((a - 1) * Math.log(x) + b * Math.log(1 - x)
                + MoreMath.logGamma(a + b - 1) - MoreMath.logGamma(a)
                - MoreMath.logGamma(b));
        xgamb = xgamf * (a + b - 1) * x / a;
        pnegbf = Math.exp(MoreMath.logGamma(n/2 + k) - MoreMath.logGamma(k + 1)
                - MoreMath.logGamma(n/2)
                + k * Math.log(rho2) + (n/2) * Math.log(1 - rho2));
        pnegbb = pnegbf;
        remain = 1 - pnegbf;
        cum = pnegbf * betaf;
        done = false;
        for (int i=1; !done; i++) {
            xgamf *= (a + b + i - 2) * x / (a + i - 1);
            betaf -= xgamf;
            pnegbf = pnegbf * (n/2 + k + i - 1) * rho2 / (k + i);
            cum += pnegbf * betaf;
            error = remain * betaf;
            remain -= pnegbf;
            if (i > k) {
                if (error < errtol || i > maxiter) done = true;
            }
            else {
                xgamb *= (a - i + 1) / (x * (a + b - i));
                betab += xgamb;
                pnegbb *= (k - i + 1) / (rho2 * (n/2 + k - i));
                cum += pnegbb * betab;
                remain -= pnegbb;
                if (remain < errtol || i > maxiter) done = true;
            }
            if (i > maxiter) {
                Utility.warning("Convergence failure in Rsquare.cdf");
            }
        }
        return cum;
    }

/**
 * CDF of the central distribution of R-square
 */
    public static double cdf(double x, double N, int p) {
        return cdf(x, N, p, 0);
    }



/**
 * Quantile of the distribution of R-square
 */
    public static double quantile(double prob, double N, int p, double rho2) {
        if (p < 1 || N < p) {
            Utility.warning("Rsquare.quantile: illegal values of N or p");
            return Double.NaN;
        }
        if (rho2 < 0.0) {
            Utility.warning("Rsquare.quantile: rho2 must be nonnegative");
            return Double.NaN;
        }
        prob = prob < 0 ? 0 : (prob > 1 ? 1 : prob);
        if (prob*(1-prob) == 0)  return p;

        double theta = rho2 / (1 - rho2),
            start = Beta.quantile(prob, p, N-p-1, theta*(N-1));
        RsqAux raux = new RsqAux(N, p, rho2);
        return Solve.search(raux, prob, start, .01);
    }

/**
 * Quantile of central distribution of R square
 */
    public static double quantile (double prob, double N, int p) {
        return quantile (prob, N, p, 0);
    }


/* Test the fcn...
    public static void main (String[] args) {
        double x = rvl.util.Utility.strtod(args[0]);
        double rho2 = rvl.util.Utility.strtod(args[1]);
        int p = rvl.util.Utility.strtoi(args[2]);
        double N = rvl.util.Utility.strtod(args[3]);
        double prob = cdf(x,N,p,rho2);
        System.out.println("cdf = " + prob);
        double q = quantile(prob,N,p,rho2);
        System.out.println("quantile = " + q);
    }
*/
}


/**
 * Auxiliary class for use by quantile function
 */
class RsqAux extends UniFunction {
    private double N, rho2;
    private int p;
    public RsqAux(double N, int p, double rho2) {
        this.N = N;
        this.p = p;
        this.rho2 = rho2;
        xMin = 0.0;
        xMax = 1.0;
        closedMin = true;
        closedMax = true;
    }
    public double of(double x) {
        return Rsquare.cdf(x,N,p,rho2);
    }
}

