package rvl.util;

/** Linear algebra functions
 * @author Russ Lenth
 * @version 1.0 July 8, 1996
 */
public class LinAlg {

/**
 * in-place LU decomposition of A
 * uses scaled-column pivoting
 * @param A square matrix (n x n)
 * @param row integer array of length n to store row permutations
 * @return true if A is nonsingular, false if singular<br>
 */
    public static boolean
    LUInPlace (double A[][], int row[]) {
        int n = A.length;
        double rowMax[] = new double[n];
        //--- set up row indices and row maxima ---
        for (int i=0; i<n; i++) {
            row[i] = i;
            rowMax[i] = A[i][0];
            for (int j=1; j<n; j++)
                if (A[i][j] > rowMax[i]) rowMax[i] = A[i][j];
        }
        //--- Gaussian elimination ---
        for (int i=0; i<n; i++) {
            //--- determine pivot ---
            int p = i;
            double maxPivot = A[row[p]][i]/rowMax[row[p]];
            for (int j=i+1; j<n; j++)
                if (A[row[j]][i]/rowMax[row[j]] > maxPivot) {
                    p = j;
                    maxPivot = A[row[p]][i]/rowMax[row[p]];
                }
            if (p != i) {   // swap row pointers if needed
                int q = row[i];
                row[i] = row[p];
                row[p] = q;
            }
            //--- if maxPivot is small, we're S.O.L. ---
            if (isZero(maxPivot)) return false;
            //--- this little loop does the elimination itself ---
            for (int j=i+1; j<n; j++) {
                double m = A[row[j]][i]/A[row[i]][i];
                for (int k=i; k<n; k++) 
                    A[row[j]][k] -= m*A[row[i]][k];
                A[row[j]][i] = m;   // store L in lower triangle
            }
        }
        return true;
    }

/**
 * Solve the system <tt>LUx = b</tt> (in-place; alters <tt>b</tt>)
 * @param LU LU decomposition (result of <tt>LU</tt> 
 *   or <tt>LUInPlace</tt>)
 * @param row permutation array
 * @param b rhs of system (altered by process)
 * @return the solution
 */
    public static double[] 
    LUSolveInPlace(double LU[][], int row[], double b[]) {
        int n = row.length;
        for (int i=0; i<n; i++) 
            for (int j=0; j<i; j++)
                b[row[i]] -= LU[row[i]][j]*b[row[j]];
        double x[] = new double[n];
        for (int i=n-1; i>=0; i--) {
            x[i] = b[row[i]];
            for (int j=i+1; j<n; j++) 
                x[i] -= LU[row[i]][j] * x[j];
            x[i] /= LU[row[i]][i];
        }
        return x;
    }

/**
 * @return LU decomposition of square matrix <tt>A</tt>
 * (Does not alter contents of A)
 * @see LUInPlace
 */
    public static double[][] 
    LU(double A[][], int row[]) {
        double AA[][] = new double[A.length][];
        for (int i=0; i<A.length; i++) 
            AA[i] = copy(A[i]);
        if(LUInPlace(AA, row)) return AA;
        return null;
    }
/**
 * @return solution <tt>x</tt> to <tt>LUx = b</tt>
 */
    public static double[] 
    LUSolve(double LU[][], int row[], double b[]) {
        double bb[] = copy(b);
        return LUSolveInPlace(LU, row, bb);
    }

/**
 * @return a newly allocated copy of <tt>x</tt>
 */
    public static double[] copy(double x[]) {
        double y[] = new double[x.length];
        for (int i=0; i<x.length; i++) y[i]=x[i];
        return y;
    }

/** transpose a matrix */
    public static double[][] transpose(double A[][]) {
        int nr = A.length, nc = A[0].length;
        double At[][] = new double[nc][];
        for (int j=0; j<nc; j++) {
            At[j] = new double[nr];
            for (int i=0; i<nr; i++) At[j][i] = A[i][j];
        }
        return At;
    }

/**
 * fuzzy test for being close to zero
 */
    private static boolean isZero(double x) {
        return (Math.abs(x) < 1e-8); 
    }

/**
 * create a constant vector of given value val, of length n
 */
    public static double[] constant(double val, int n) {
        double x[] = new double[n];
        for (int i=0; i<n; i++) x[i] = val;
        return x;
    }

/**
 * print a vector
 */
    public static void print(double x[]) {
        for (int i=0; i<x.length; i++)
            System.out.print(" " + x[i]);
    }

/**
 * print a vector, then a newline
 */
    public static void println(double x[]) {
        print(x);
        System.out.println();
    }

/**
 * print a matrix and a newline
 */
    public static void println(double x[][]) {
        for (int i=0; i<x.length; i++)
            println(x[i]);
    }

}
