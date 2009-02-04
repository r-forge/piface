package rvl.stat.anova;

import rvl.stat.anova.*;


/**
 *  FactorSet class
 *   - defines a set of factors having the same number of levels.
 * @author Russ Lenth
 * @version 1.0 March 26, 1998
 * @see Factor
 */

public class FactorSet {

/**
 *  Construct a FactorSet involving n factors
 */
    public FactorSet(int n) {
        facset = new Factor[n];
    }

/**
 *  Set levels of all factors in FactorSet to n
 */
    public void setLevels(int n) {
        for (int i=0; i<facset.length; i++)
            facset[i].levels = n;
    }

/**
 *  @return number of levels in each factor
 */
    public int getLevels() {
        return facset[0].getLevels();
    }

    protected Factor facset[];
}