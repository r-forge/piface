package rvl.stat;
import rvl.util.*;

/**
 * Basic statistics calculations
 * @author Russ Lenth
 * @version 1.0 January 5, 2001
 */

public class Stat {

    /**
     * @return the mean of x[]
     */
    public static double mean (double x[]) {
	int n = x.length;
	double sum = x[0];
	for (int i=1; i<n; i++)
	    sum += x[i];
	return sum / n;
    }

    /**
     * @return the SD of x[], given the mean is already calculated
     */
    public static double sd(double x[], double mean) {
	int n = x.length;
	double sum = 0;
	for (int i=0; i<n; i++) {
	    double d = x[i] - mean;
	    sum += d * d;
	}
	return Math.sqrt(sum / (n - 1.0));
    }

    /**
     * @return sd of x[]
     */
    public static double sd (double x[]) {
	return meanSD(x)[1];
    }

    /**
     * @return an array of two elements with the mean and SD of x[]
     */
    public static double[] meanSD (double x[]) {
	double s[] = new double[] {x[0], 0};
	int n = x.length;
	for (int i=1; i<n; i++) {
	    s[0] += x[i];
	    double d = x[i] - x[0];
	    s[1] += d * d;
	}
	s[0] /= n;
	double d = x[0] - s[0];
	s[1] = Math.sqrt((s[1] - n * d * d) / (n - 1));
	return s;
    }
}
