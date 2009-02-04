package rvl.util;

/** class for sorting algorithms
 *  @author Russ Lenth
 *  @version 1.0
 */

public class Sort {

/** Quick-sort algorithm
 *  Sorts x[] in place
 */
    public static void qsort(double x[]) {
        qsort(x, 0, x.length-1);
    }

/** This does the real work of qsort(double x[]); it sorts a partition
 *  of x[] from x[bot] to x[top], making recursive calls as needed.
 */
    protected static void qsort(double x[], int bot, int top) {
        int lo = bot, hi = top;
        double test = x[ (lo + hi) / 2 ];
        while (lo <= hi) {
            while ( (lo < top) && (x[lo] < test) ) ++lo;
            while ( (hi > bot) && (x[hi] > test) ) --hi;
            if (lo <= hi) {
                double temp = x[lo];
                x[lo] = x[hi];
                x[hi] = temp;
                ++lo;
                --hi;
            }
        }
        if (bot < hi)  qsort(x, bot, hi);
        if (lo < top)  qsort(x, lo, top);
    }

/**
 *  @returns Indices by which x[] is ordered; i.e.,
 *           x[order(x)] is sorted.
 */
    public static int[] order(double x[]) {
        int ord[] = new int[x.length];
        for (int i=0; i<x.length; i++)
            ord[i] = i;
        order(ord, x, 0, x.length-1);
        return ord;
    }
    protected static void order(int ord[], double x[], int bot, int top) {
        int lo = bot, hi = top;
        double test = x[ ord[(lo + hi) / 2] ];
        while (lo <= hi) {
            while ( (lo < top) && (x[ord[lo]] < test) ) ++lo;
            while ( (hi > bot) && (x[ord[hi]] > test) ) --hi;
            if (lo <= hi) {
                int temp = ord[lo];
                ord[lo] = ord[hi];
                ord[hi] = temp;
                ++lo;
                --hi;
            }
        }
        if (bot < hi)  order(ord, x, bot, hi);
        if (lo < top)  order(ord, x, lo, top);
    }

/**
 *  @returns ranks of elements of x[] (not adj for ties)
 */
    public static int[] rank(double x[]) {
        int ord[] = order(x), n = x.length;
        int r[] = new int[n];
        for (int i=0; i<n; i++)  r[ord[i]] = i + 1;
        return r;
    }

/**
 *  @returns ranks of elements of x[], adjusting for ties
 */
    public static float[] rankTies(double x[]) {
        int ord[] = order(x), n = x.length, j;
        float r[] = new float[n];
        for (int i=0; i<n; ) {
            double z = x[ord[i]];
            for (j=i+1; j<n && x[ord[j]] == z; j++);
            for (int m=i; m<j; m++) r[ord[m]] = (float)0.5 * (i + j + 1);
            i = j;
        }
        return r;
    }


/***
// Test main ...
    public static void main(String argv[]) {
        RNG rng = new RNG();
        int n = 100;
        long start, finish;
        if (argv.length>0) n = rvl.util.Utility.strtoi(argv[0]);

        double x[] = new double[n];

        for (int i=0; i<n; i++) x[i] = Math.round(10*rng.unif());

    // Remove JIT compiler overhead
        int[] junk = order(new double[]{1,2,3});
        qsort(new double[]{1,2,3});

        start = System.currentTimeMillis();
        int ord[] = order(x);
        finish = System.currentTimeMillis();
        System.out.println("order() took " + (finish - start) + " msec.");

        start = System.currentTimeMillis();
        int rnk[] = rank(x);
        finish = System.currentTimeMillis();
        System.out.println("rank() took " + (finish - start) + " msec.");

        start = System.currentTimeMillis();
        float trnk[] = rankTies(x);
        finish = System.currentTimeMillis();
        System.out.println("rankTies() took " + (finish - start) + " msec.");

        start = System.currentTimeMillis();
        qsort(x);
        finish = System.currentTimeMillis();
        System.out.println("qsort() took " + (finish - start) + " msec.");
    }
    static void display(double x[], int ord[], int rnk[], float trnk[]) {
        int n = Math.min(24,x.length);
        for (int i=0; i<n; i++) {
            System.out.println(x[ord[i]] + "\t" + x[i] + "\t" + ord[i] + "\t"
                + rnk[i] + "\t" + trnk[i]);
        }
    }
****/
}