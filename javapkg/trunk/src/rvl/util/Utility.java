package rvl.util;

// Utility functions to make Russ's life easier!
// Often, these have the same names as C functions

import java.awt.*;
import java.io.*;
import java.util.*;

import rvl.util.*;
import rvl.awt.*;

public class Utility implements Closeable {

public Utility(){}

/****/  public static double
strtod (String s) {
    try {
        return Double.valueOf(s.trim()).doubleValue();
    }
    catch (NumberFormatException e) {
        return Double.NaN;
    }
}

/****/ public static float
strtof (String s) {
    try {
        return Float.valueOf(s.trim()).floatValue();
    }
    catch (NumberFormatException e) {
        return Float.NaN;
    }
}

/****/ public static int
strtoi (String s) {
    try {
        return Integer.valueOf(s.trim()).intValue();
    }
    catch (NumberFormatException e) {
        return Integer.MAX_VALUE;
    }
}

/****/ public static long
strtol (String s) {
    try {
        return Long.valueOf(s.trim()).longValue();
    }
    catch (NumberFormatException e) {
        return Long.MAX_VALUE;
    }
}


/**
 * Replacement for System.exit(status).
 * Does appropriate thing when running in a browser.
 */
public static void exit(int status) {
    try {
        System.exit(status);
    }
    catch (SecurityException se) {
        warning("You may need to close some windows manually");
    }
}

// This gets called by the modeless dialog generated in error()
public void close() {
    exit(1);
}

/**
 * Displays the message and returns Double.NaN.
 * Useful as an error exit for math routines
 */
public static double NaN(String msg) {
    warning(msg);
    return Double.NaN;
}


/**
 * Display a warning error in a text window
 */
    public static void warning(String msg) {
        if (!guiWarn) {
            System.err.println(msg);
            return;
        }
        if (msgWindow == null) {
            msgWindow = new ViewWindow("Errors and warnings", 25, 60);
            msgWindow.setClearButton(true);
        }
        msgWindow.append(msg + "\n");
        if (!msgWindow.isVisible()) {
            msgWindow.setVisible(true);
            msgWindow.show();
        }
    }

/**
 * Display the message from a Throwable object in a text window
 * the stack trace is also displayed if stackTrace is true
 */
    public static void warning(Throwable t, boolean stackTrace) {
        if (stackTrace) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(bos));
            warning(bos.toString());
        }
        else
            warning(t.toString());
    }

    public static void warning(Throwable t) {
        warning(t, false);
    }

/**
 * Display a fatal error message in a text window and bring up
 * a dialog to close it.  If app is not null,
 * its close() method is called.
 * Also attempts to shut down the JVM
 */
    public static void error(String msg, Closeable app) {
        warning(msg);
        if (guiWarn) {
//            Frame parent = null;
//            if (app != null && app instanceof Frame)
//                parent = (Frame)app;
//            GPDialog.msgBox(parent, "Fatal error", "See warning/error window");
            new ModelessMsgBox("Fatal error", "See warning/error window", new Utility());
            // clicking "OK" will call piAction with 2nd arg
        }
        else {
            if (app != null) app.close();
            exit(1);
        }
    }

/**
 * Display a fatal exception and its stack trace;
 * then shut down as in error(msg,app)
 */
    public static void error(Throwable t, Closeable app) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        error(bos.toString(), app);
    }
/**
 * Display a fatal error in a text window and bring up
 * a dialog to close it.
 * Also attempts to shut down the JVM
 */
    public static void error(String msg) {
        error(msg, null);
    }
/**
 * Display a fatal error in a text window and bring up
 * a dialog to close it.
 * Also attempts to shut down the JVM
 */
    public static void error(Throwable t) {
        error(t, null);
    }

/**
 * Set whether to display warnings/errors graphically or on console
 */
    public static void setGUIWarn(boolean option) {
        guiWarn = option;
    }


// variables used by warning() and error()
    private static ViewWindow msgWindow = null;
    private static boolean guiWarn = false;


/**
 * Return an array of double values parsed from a string
 * Any invalid entries are returned as Double.NaN
 */
    public static double[] parseDoubles(String str) {
        StringTokenizer st = new StringTokenizer(str);
        int n = st.countTokens();
        double x[] = new double[n];
        for (int i=0; i<n; i++)
            x[i] = strtod(st.nextToken());
        return x;
    }

/**
 * Return an array of nice numbers that contain the interval (a,b)
 * (if enclose==true) or that fall within [a,b] (if enclose==false)
 */
     public static double[]
     nice(double a, double b, int minTicks, boolean enclose) {
         if (a > b)     // (reversed)
             return nice(b, a, minTicks, enclose);
         if (b <= 0) {   // (both ends are negative)
             double x[] = nice(-b, -a, minTicks, enclose);
             for (int i=0; i<x.length; i++)
                 x[i] *= -1;
             return x;
         }
         if (a==b) {
             if (b==0)
                 return new double[] { -1, 1 };
             b *= 1.1;
         }

         double eps = .005*(b - a); // "fudge" the interval a little
         if (!enclose) {
             a -= eps;
             b += eps;
         }
         else {
             a += eps;
             b -= eps;
             minTicks -= 2;
         }
         eps *= 2;

         double main = 0;
         double pwr = Math.floor(Math.log(b) / Math.log(10));
         double incr = Math.pow(10, pwr);
         if (a * b > 0) {
             while (main + eps < a) {
                 main = incr * (int)(b / incr);
                 incr /= 10;
             }
         }
         incr *= 10;
         for (int j=0; nTicks(a,b,main,incr) < minTicks; j = (j+1)%3) {
             incr /= nice_d[j];
         }
         int n = nTicks(a,b,main,incr);
         while (main - incr >= a) main -= incr;
         if (enclose) {
             if (Math.abs(a - main) / incr > .05) {
                 main -= incr;
                 n++;
             }
             if (Math.abs(main + (n-1)*incr - b) / incr > .05)
                 n++;
         }
         double tick[] = new double[n];
         for (int j=0; j<n; j++)
             tick[j] = main + j*incr;
         return tick;
     }

    private static int nTicks(double a, double b, double start, double incr) {
        int n = 0;
        for (double t=start; t<=b; t+=incr) n++;
        for (double t=start-incr; t>=a; t -= incr) n++;
        return n;
    }

/** Constants used by nice() **/
private static final double nice_d[] = new double[] { 2, 2.5, 2 };

/**
 * Find a string representation for a set of nice numbers (requires at least 2)
 * This will return unreliable results if numbers are not nice, especially
 * if the 1st 2 elements are equal
 */
public static String[] fmtNice(double x[]) {
    if (x.length < 2) return new String[] {""+x};
    double margin = 0.1 * Math.abs(x[1] - x[0]);
    if (x[0] == x[1]) margin = Math.max(1.0, Math.abs(x[0]));  // for safety
    String f[] = new String[x.length];
    for (int i=0; i<x.length; i++)
        f[i] = minFormat(x[i], margin);
    return f;
}
/**
 * @returns string representation of x to minimum digits required to
 * establish its value to within the given margin of error
 */
public static String minFormat (double x, double margin) {
    if (x == 0) return "0";
    for (int digits=1; true; digits++) {
        String xFmt = format (x, digits);
        if (Math.abs((new Double(xFmt)).doubleValue() - x) < margin)
            return xFmt;
    }
}


/****/ public static String
fixedFormat (double x, int decPlaces) {
    double mult = Math.pow(10.0, decPlaces);
    int xx =  Math.round((float)(x * mult));
    String s = "" + xx;
    if (decPlaces == 0) return s;
    if (decPlaces > 0) {
        for (int L = s.length(); L <= decPlaces; L++)
            s = "0" + s;
        int p = s.length() - decPlaces;
        return s.substring(0, p) + "." + s.substring(p);
    }
    // (else decPlaces < 0...)
    return "" + (int)(xx / mult + .01);
}

private static String sciFormat (String digStr, int exponent) {
    if (digStr.length() == 1)
        return digStr + "e" + exponent;
    else
        return digStr.substring(0,1) + "." + digStr.substring(1)
            + "e" + exponent;
}

/****/ public static String
format (double x, int digits) {
    return format (x, digits, true);
}

/****/ public static String
format (double x, int digits, boolean trim) {
    if (Double.isInfinite(x) || Double.isNaN(x))
        return "" + x;
    if (x < 0)
        return "-" + format(-x, digits, trim);
    if (digits <= 0)
        return fixedFormat(x, -digits);
    if (x == 0)
        return "0";
    digits = Math.min(digits,15);
    int logMult = (int) (digits - Math.floor(Math.log(x)/Math.log(10)) - 1);
    long xLong = (long) Math.floor(x * Math.pow(10,logMult) + .5);
    String xStg = (new Long(xLong)).toString();
    int sDigits = xStg.length(),
        decPos = sDigits - logMult;      // decimal position in xStg
    if (trim) { // strip off trailing 0s
        while (xStg.charAt(sDigits-1) == '0') {
            sDigits--;
            logMult--;
        }
        xStg = xStg.substring(0,sDigits);
    }
    if (logMult <= 0) { // append 0s to right as needed, and return
        if (logMult < -3)
            return sciFormat(xStg, decPos-1);
        StringBuffer sb = new StringBuffer(xStg);
        for (int i=0; i<-logMult; i++) sb.append("0");
        return new String(sb);
    }
    if (decPos >= 0)
        return xStg.substring(0,decPos) + "." + xStg.substring(decPos);
    else {  // append '.' and 0s to beginning of string
        if (decPos < -3) // if > 4 0s, use sci notation instead
            return sciFormat(xStg, decPos-1);
        StringBuffer sb = new StringBuffer(".");
        for (int i=0; i<-decPos; i++) sb.append("0");
        sb.append(xStg);
        return new String(sb);
    }
}


/****/ public static String
format (String s, int nChars) {
    if (s.length() > nChars)
        return s.substring(0,nChars-1);
    StringBuffer b = new StringBuffer(nChars+1);
    b.append(s);
    while (b.length()<nChars)
        b.append(" ");
    return b.toString();
}


//-------- Sorting, ordering, and ranking routines ------------------
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




private static void testTicks(double a, double b, int n, boolean encl) {
    System.out.println("\n a="+a +", b="+b +", minTicks="+n +", enclose="+encl);
    double x[] = nice(a,b,n,encl);
    String lab[] = fmtNice(x);
    for (int i=0; i<x.length; i++) System.out.println(lab[i]);
}
public static void main(String argv[]) {
    if (argv.length < 4) {
        testTicks(-7.99,3.99,4,true);
        testTicks(-799,399,4,true);
        testTicks(-.00799,.00399,4,true);
        testTicks(-7.99e10,3.99e10,4,true);
        testTicks(-7.99e-10,3.99e-10,4,true);
    }
    else {
        testTicks(strtod(argv[0]), strtod(argv[1]), strtoi(argv[2]),
            argv[3].equals("true"));
    }
}


} //=======================================================================
