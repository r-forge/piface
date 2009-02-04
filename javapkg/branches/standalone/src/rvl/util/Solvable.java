package rvl.util;

/**
 * Interface for classes that use SolveObject
 */
public interface Solvable {
/**
 * @param mode  user-defined mode number 
 * @param x     argument of univariate function of that mode
 */
    public double solveHook (int mode, double x);
}
