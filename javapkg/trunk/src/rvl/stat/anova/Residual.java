package rvl.stat.anova;

import rvl.stat.anova.*;

/**
 * The RESIDUAL term in an anova model.
 *
 * @author Russ Lenth
 * @version 1.1 March 26, 1998
 * @see Factor, Term, Model
 */
public    class Residual    extends Term
{
/**
 * Construct a RESIDUAL term for a model
 * @param model     the Model object to which this residual term applies
 */
    public Residual(Model model) {
        modl = model;
    }

/**
 *  @return N - 1 - sum of df for all other terms
 */
    public int df() {
        int df = span() - 1;
        for (int i=0; i<modl.nTerm(); i++) {
            Term t = modl.getTerm(i);
            if (! t.equals(this))
                df -= t.df();
        }
        return df;
    }
/**
 *  @return N = number of individual observations implied by the model
 */
    public int span() {
        return modl.getNobs();
    }

//----- Trivial overrides ----//
    public String getName() {
        return "RESIDUAL";
    }
    public boolean containsFactor(Factor f) {
        return true;
    }
    public boolean containsTerm(Term t) {
        return true;
    }
    public boolean overlaps(Factor f) {
        return true;
    }
    public boolean isRandom() {
        return true;
    }
    public int order() {    // shouldn't be called anyway
        return 1;
    }
    public Factor factor(int i) {   // shouldn't be called anyway
        return null;
    }
    protected void setNest(Factor n[]) {}

    private Model modl;
}