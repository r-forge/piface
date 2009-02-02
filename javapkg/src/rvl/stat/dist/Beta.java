package rvl.stat.dist;

import rvl.util.*;
import rvl.stat.dist.*;

/**
 * The beta distribution (central and noncentral)
 * @author Russ Lenth
 * @version 1.1 July 26, 2007
 */

public class Beta {

/**
 * @return cdf at <tt>x</tt> of central Beta(<tt>a,b</tt>) distribution
 */
    public static double
    cdf (double x, double a, double b) {
        double x0, ix, oldix, evenc, oddc, ap2i,
               factor, numconst, den, denconst, eps_cdf=1e-8;
        int   i;
        int maxiter = 500;
        boolean argswap = false, decr_a = false, decr_b = false;

        if (a<=0 || b<=0) {
            Utility.warning("Beta.cdf: parameters must be positive");
            return Double.NaN;
        }

        x = (x < 0 ? 0 : (x > 1 ? 1 : x));

        if (x == 0 || x == 1) {
            return x;
        }

        factor = Math.pow(x,a) * Math.pow(1-x,b) * MoreMath.beta(a,b);

        if (a < 1.5) {
            decr_a = true;
            factor *= x * (a+b)/a;
            a++;
        }
        if (b < 1.5) {
            decr_b = true;
            factor *= (1-x) * (a+b)/b;
            b++;
        }
        if (x >= (a-1)/(a+b-2)) {
            argswap = true;
            x = 1 - x;
            x0 = a;  a = b;  b = x0;
        }

        ix = 1 / (1 - x * (a+b) / (a+1));
        numconst = ix;
        denconst = ix;
        i = 1;

        do {
            oldix = ix;
            ap2i = a+2*i;
            evenc = i * (b-i) * x / (ap2i * (ap2i-1));
            oddc  = -(a+i)*(a+b+i)*x / (ap2i*(ap2i+1));
            numconst = ix + evenc*numconst;
            ix = numconst + oddc*ix;
            denconst = 1 + evenc*denconst;
            den = denconst + oddc;
            numconst = numconst/den;
            denconst = denconst/den;
            ix /= den;
            i++;
        } while ( Math.abs(ix-oldix) >= eps_cdf*ix && i <= maxiter);

        if (i > maxiter) {
            Utility.warning("Convergence failure in beta.cdf"
                + " - error estimate = " + (ix-oldix)/ix);
        }
        ix=factor*ix/a;
        if (argswap) {
            x0 = a; a = b; b = x0;
            x = 1 - x;
            ix = 1 - ix;
        }
        if (decr_b) {
            b--;
            factor = b * factor / ((1-x)*(a+b));
            ix -= factor/b;
        }
        if (decr_a)  {
            a--;
            ix += factor / (x*(a+b));
        }
        return (ix);
    }


/**
 * @return cdf at <tt>x</tt> of noncentral
 * Beta(<tt>a, b, lambda</tt>) distribution
 * THIS IS THE OLD VERSION
 */
    public static double
    oldCdf (double x, double a, double b, double lambda) {
        double   ix, gam, q, p, sum, lnbeta, lnpdf, eps_cdf=1e-8;
        int     i;
        int maxiter = 500;

        if (a<=0 || b<=0 || lambda <0) {
            Utility.warning("Beta.cdf: parameters must be positive");
            return Double.NaN;
        }

        ix = cdf(x,a,b);    // central cdf for starters

        if (lambda==0)
            sum=ix;

        else {
            gam = Math.pow(x,a) * Math.pow(1-x,b) * MoreMath.beta(a,b) / a;
            lambda /= 2;
            q = Math.exp(-lambda);
            p = 1 - q;
            sum = q*ix;
            i=0;

            do {
            i++;
            ix -= gam;
            gam *= x * (a+b+i-1) / (a+i);
            q *= lambda / i;
            p -= q;
            sum += q*ix;
            } while ( (p*(ix-gam) >= eps_cdf) && (i <= maxiter) );

            if (i > maxiter) {
                Utility.warning("Convergence failure in Beta.cdf"
                    + " - error estimate = " + p*(ix-gam));
            }
            }
        return (sum);
    }



/**
 * @return cdf at <tt>x</tt> of noncentral
 * Beta(<tt>a, b, lambda</tt>) distribution
 * Uses Baharef/Kemeny algorithm (backward recursion between prespecified limits)
 */
    public static double
    cdf (double x, double a, double b, double lambda) {
        double   c, d, p, f, g;
        int     k, k1=0, k2;

        if (a<=0 || b<=0 || lambda <0) {
            Utility.warning("Beta.cdf: parameters must be positive");
            return Double.NaN;
        }

        lambda /= 2;          // Almost everything's in terms of lambda/2, so save work

        if (lambda < 1.0e-8)  // central cdf case
            return (cdf(x,a,b));

   // For large lambda, the gamma(lambda, 1) dist closely approximates the Poisson
        if (lambda > 15) {
            k1 = 1 + (int) Chi2.quantile(1e-8, 2*lambda) / 2;
            while (k1>0 && Poisson.cdf(k1-1, lambda) > 1e-8) k1--;
        }
        k2 = (int) Chi2.quantile(1 - 1e-8, 2*lambda) / 2;
        while (Poisson.cdf(k2, lambda) < 1 - 1e-8) k2++;

      // Initialize constants
        k = k2;
        c = cdf(x, a + k, b);
        d = MoreMath.logGamma(a+b+k-1) - MoreMath.logGamma(a+k-1) - MoreMath.logGamma(b)
            + (a + k - 1)*Math.log(x) + b * Math.log(1 - x);
        d = Math.exp(d) / (a + k - 1);
        p = Math.exp(-lambda + k * Math.log(lambda)
            - MoreMath.logGamma(k+1));
        f = p * c;
        p *= k / lambda;

        // Main loop
        for (k = k2 - 1; k >= k1; k--) {
            c += d;
            d *= (a + k) / x / (a + k + b - 1);
            f += p * c;
	    p *= k / lambda;
        }

        return (f);
    }




/**
 * @return <tt>p</tt>th quantile of noncentral
 * Beta(<tt>a, b, lambda</tt>) distribution
 */
    public static double
    quantile (double p, double a, double b, double lambda) {

        if (p*(1-p) == 0)
            return p;

        double z = 4.91*(Math.pow(p,.14) - Math.pow(1-p,.14)),
            // z is approx std. normal quantile
        al = a + lambda/2,
            // distn ~~ beta(a + lambda/2, b):
        start = al/(al+b) + z * Math.sqrt(al*b/Math.pow(al+b,3));
        start = Math.min(.99, Math.max(.01,start));

        BetaAux f = new BetaAux(a, b, lambda);
        return Solve.search(f, p, start, .01);
    }

/**
 * @return <tt>p</tt>th quantile of Beta(<tt>a,b</tt>) distribution
 */
    public static double quantile (double p, double a, double b) {
        return quantile (p,a,b,0);
    }


// Test routine
    public static void main(String[] args) {
        double x = rvl.util.Utility.strtod(args[0]),
               a = rvl.util.Utility.strtod(args[1]),
               b = rvl.util.Utility.strtod(args[2]),
               lambda = rvl.util.Utility.strtod(args[3]),
               cdf1, cdf2;
        cdf1 = oldCdf(x, a, b, lambda);
        cdf2 = cdf(x, a, b, lambda);
        System.out.println("Old cdf: " + cdf1);
        System.out.println("New cdf: " + cdf2);
    }
}

/**
 * Auxiliary class for Beta
 */
class BetaAux extends UniFunction {
    private double a, b, lambda;
    public BetaAux(double aa, double bb, double ll) {
        a = aa;
        b = bb;
        lambda = ll;
        xMin = 0.0d; closedMin = true;
        xMax = 1.0d; closedMax = true;
    }
    public double of(double x) {
        return Beta.cdf(x,a,b,lambda);
    }
}


