package rvl.piface;

import rvl.util.UniFunction;


/**
 * Auxiliary class used by Piface.solve()
 */
 
public class PifaceAux extends UniFunction {

    Piface piface;
    String yName, xName;

    public PifaceAux(String xName, String yName, Piface piface) {
        this.piface = piface;
        this.xName = xName;
        this.yName = yName;
    }

    public double of(double x) {
        return piface.eval(yName, xName, x);
    }
    
}
