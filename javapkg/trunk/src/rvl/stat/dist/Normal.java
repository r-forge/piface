package rvl.stat.dist;
import rvl.util.*;

/**
 * The normal distribution
 * @author Russ Lenth
 * @version 1.2 April 17, 2000
 */
public class Normal {

// Fields used by rocArea and power(alpha)
    private static double saveMu1 = 0;
    private static int saveTail = 0;

/**
 * @return standard normal cdf at <tt>z</tt>
 */
    public static double
    cdf (double z) {
        double a  = 0.2316419, b1 = 0.319381530, b2 = -0.356563782,
            b3 = 1.781477937, b4 = -1.821255978, b5 = 1.330274429;
        double t;
        if (z > 0.0d)
            return 1.0d - cdf(-z);
        if (z > -5.0d) {
            t = 1.0 / (1.0 - a * z);
            t = pdf(z) * t * (b1 + t * (b2 + t * (b3 + t * (b4 + t * b5))));
        }
        else {
            t = z*z;
            t = pdf(z) * (1 - (1 - 3 * (1 - 5 * (1 - 7 / t) / t) / t) / t) / (-z);
        }
        return t;
    }

/**
 * @return density at <tt>z</tt> of standard normal distribution
 */
    public static double
    pdf(double z) {
        return 0.398942280 * Math.exp(-0.5 * z * z);
    }

/**
 * @return standard normal quantile at <tt>p</tt>
 */
    public static double
    quantile (double p) {
        if (p <= 0 || p >= 1)
            return Utility.NaN("Normal.quantile: p must be in (0,1)");
        NormalAux f = new NormalAux();
        double x = 4.91 * ( Math.pow(p,.14) - Math.pow(1-p,.14) );
        return Solve.search(f, p, x-.0025, .005);
    }

// extensions to arbitrary mu and sigma...
/**
 * @return cdf at <tt>x</tt> of <i>N(</i><tt>mu,sigma</tt><i>)</i>
 *     distribution
 */
    public static double
    cdf (double x, double mu, double sigma) {
        return cdf( (x - mu)/sigma );
    }
/**
 * @return <tt>p</tt>th quantile of <i>N(</i><tt>mu,sigma</tt><i>)</i>
 *     distribution
 */
    public static double
    quantile (double p, double mu, double sigma) {
        return mu + sigma * quantile(p);
    }


/**
 * @return power of normal-theory test of H<sub>0</sub>: mu = 0
 *   where sigma0 = 1 and the distribution under H<sub>1</sub> is
 *   <i>N(</i><tt>mu1, sigma1</tt><i>)</i>
 */
    public static double
    power (double mu1, int tail, double alpha, double sigma1) {
        if (sigma1 <= 0)
            return Utility.NaN("Normal.power: sigma1 <= 0");
        if (alpha <= 0 || alpha >= 1)
            return Utility.NaN("Normal.power: alpha not in (0,1)");
        double critval;
        if (tail < 0)
            return power (-mu1, 1, alpha, sigma1);
        if (tail == 0) {
            critval = quantile (alpha/2);
            return cdf (critval, mu1, sigma1)
                + 1.0d - cdf (-critval, mu1, sigma1);
        }
        critval = -quantile(alpha);
        return 1.0d - cdf(critval, mu1, sigma1);
    }

/**
 * Power for homogeneous-variance case
 * @return <tt>power(mu1, tail, alpha, 1.0)</tt>
 */
    public static double
    power (double mu1, int tail, double alpha) {
        return power (mu1, tail, alpha, 1.0d);
    }

/**
 * Effect size of a test based on the normal distribution
 * @return value of <tt>mu1</tt> such that
 *   <tt>power(mu1, tail, alpha) == goal</tt>
 * @see Normal#power
 */
 public static double
 effectSize (double goal, int tail, double alpha) {
     if (goal <= 0 || goal >= 1)
        return Utility.NaN("Normal.effectSize: goal not in (0,1)");
     if (alpha <= 0 || alpha >= 1)
        return Utility.NaN("Normal.effectSize: alpha not in (0,1)");
     if (tail > 0)
         return quantile(goal) - quantile(alpha);
     else if (tail < 0)
         return -effectSize (goal, 1, alpha);
     else {
         double start = effectSize(goal, 1, alpha/2);
         NormalAux2 f = new NormalAux2(alpha);
         return Solve.search(f, goal, start, -.05);
     }
 }


/**
 * @return area under the ROC curve with effect size
 * <tt>mu1</tt>.
 * This is the integral of <tt>power(mu1, tail, alpha)</tt>
 * over <tt>alpha</tt>.
 * @param eps Bound on the error of numerical integration
 */
    public static double
    rocArea(double mu1, int tail, double eps) {
        saveMu1 = mu1;
        saveTail = tail;
        return NumAnal.integral(rvl.stat.dist.Normal.class,
            "power", 0, 1, eps, false, 0, 1);
    }
/**
 * @return rocArea(mu1, tail, 1e-4)
 */
    public static double rocArea(double mu1, int tail) {
        return rocArea(mu1, tail, 1e-4);
    }

/**
 * @return power at alpha for internally stored parameters.
 * This is an auxiliary function used by rocArea, not
 * intended for other uses
 */
    public static double power(double alpha) {
        return power(saveMu1, saveTail, alpha);
    }

}


class NormalAux extends UniFunction {
    public double of(double z) {
        return Normal.cdf(z);
    }
}

class NormalAux2 extends UniFunction {
    private double alpha;
    public NormalAux2(double a) {
        alpha = a;
        xeps = 1e-5;
    }
    public double of (double mu) {
        return Normal.power (mu, 0, alpha);
    }
}
