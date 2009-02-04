package rvl.stat.power;
import rvl.util.*;

/**
 * generic class for statistical tests
 * @author Russ Lenth
 * @version 1.0 July 2, 1996
 */
abstract public class Test {

    abstract public double power();
    abstract public double solveFor(Param p, double goal);
    
    protected static double 
    genericSolve (Test t, Param p, 
      double targetPower, double start, double incr) {
        UniFunction f = new PwrFcn (t, p);
        f.xMin = p.min; 
        f.closedMin = p.closedMin;
        f.xMax = p.max; 
        f.closedMax = p.closedMax;
        return Solve.search (f, targetPower, start, incr);
    }

    public final String[] TAIL3 = {"Left","Two","Right"};
    public final String[] TAIL2 = {"One","Two"};
}

class PwrFcn extends UniFunction {
    Test thisTest;
    Param thisParam;
    
    PwrFcn (Test t, Param p) {
        thisTest = t;
        thisParam = p;
    }
    
    public double of (double x) {
        thisParam.value = x;
        return thisTest.power();
    }
}
