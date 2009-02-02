package rvl.stat.anova;

import rvl.stat.anova.*;
import rvl.util.*;


/**
 * Factor class
 * @author Russ Lenth
 * @version 1.0 July 3, 1996
 * @see FactorSet, Term, WithinCells
 */
public class Factor extends Term {

    protected String name;
    protected int         levels;
    protected boolean     random;
    protected FactorSet   siblings;
// Following are inherited from Term ...
//  protected StringBuffer fullName;
//  protected Factor  nestedIn[];

    public Factor() {}      // Trivial constructor

/**
 * Construct a Factor object
 * @param Name name of factor
 * @param Levels number of levels
 * @param Random true if levels are random, false if levels are fixed
 */
    public Factor (String Name, int Levels, boolean Random) {
        setName(Name);
        setLevels(Levels);
        setRandom(Random);
    }

/**
 * Construct a Factor nested in one or more other factors.
 * (<i>Note:</i> Object is initialized as a random factor.)
 * @param Name name of factor
 * @param Levels number of levels
 * @param Nest a <tt>Term</tt> object containing the nesting factors
 */
    public Factor (String Name, int Levels, Term Nest) {
        setLevels(Levels);
        nestedIn = new Factor[Nest.order()];
        for (int i=0; i<Nest.order(); i++)
            nestedIn[i] = Nest.factor(i);
        setName(Name);  // AFTER setting up nest so nest names are included
        setRandom(true);
    }

/**
 * Construct a Factor nested in one or more other factors.
 * (<i>Note:</i> Object is initialized as a random factor.)
 * @param Name name of factor
 * @param Levels number of levels
 * @param nNest number of factors in its nest
 */
    public Factor (String Name, int Levels, int nNest) {
        setLevels(Levels);
        nestedIn = new Factor[nNest];
        name = Name;
        setRandom(true);
    }

// --- Accessors for class variables ---------------------------------------

    public void setName (String s) {
        s = s.trim();
        if (s.length() > 0) {
            name = new String (s);
            fullName = new StringBuffer(s);
            if (nestedIn != null) {
                fullName.append("(");
                for (int i=0; i<nestedIn.length; ) {
                    fullName.append(nestedIn[i].getName());
                    if (++i < nestedIn.length) fullName.append(" ");
                }
                fullName.append(")");
            }
        }
        else
            Utility.error("Factor name cannot be null string");
    }

    public String getName() {
        return fullName.toString();
    }

    public String getShortName() {
        return name;
    }

    public void setLevels (int k) {
        if (k>1) {
            if (siblings == null)
                levels = k;
            else
                siblings.setLevels(k);     // coordinate levels with siblings
        }
        else
            Utility.warning("Factor must have at least two levels");
    }

    public int getLevels() {
        return levels;
    }

/** Overrides <tt>Term</tt> method */
    public int order() {
        return 1;
    }

/** Overrides <tt>Term</tt> method */
    public Factor factor(int i) {
        if (i==0) return this;
        return null;
    }

    public void setRandom(boolean r) {
        random = r;
        if (nestedIn != null && !r) // check nesting factors
            for (int i=0; i<nestedIn.length; i++)
                if (nestedIn[i].isRandom()) {
                    random = true;
                    Utility.warning("Warning: fixed factor nested in a random factor:");
                    Utility.warning("\t" + fullName + " was made random.");
                }
    }

/** Overrides <tt>Term</tt> method */
    public boolean isRandom() {
        return random;
    }

/**
 * @return true if <tt>f</tt> is equal to this factor or is
 * in or contained in this factor's nest
 */
    public boolean contains (Factor f) {
        if (equals(f)) return true;
        if (nestedIn == null) return false;
        for (int i=0; i<nestedIn.length; i++)
            if (nestedIn[i].contains(f)) return true;
        return false;
    }


/**
 * @return degrees of freedom for this Factor
    public int df() {
        int d = getLevels() - 1;
        if (nestedIn != null)
            for (int i=0; i<nestedIn.length; i++)
                d *= nestedIn[i].getLevels();
        return d;
    }
 */

/**
 * @return total number of levels of this factor INCLUDING those in
 * other nests of this factor.
 * If there are N observations in the experiment, the number of observations
 * at each level of the factor is N / factor.span().
     public int span() {
        int d = getLevels();
        if (nestedIn != null)
            for (int i=0; i<nestedIn.length; i++)
                d *= nestedIn[i].getLevels();
        return d;
     }
 */
}
