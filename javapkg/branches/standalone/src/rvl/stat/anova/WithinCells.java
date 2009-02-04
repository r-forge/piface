package rvl.stat.anova;

import rvl.stat.anova.*;

/**
 * The Within-Cells term in an anova model.
 *
 * @author Russ Lenth
 * @version 1.2 Feb 14, 2000
 * @see Factor, Term, Model
 */
public    class WithinCells    extends Factor
{
/**
 * Construct a Within-Cells factor for a model
 * @param model     the Model object to which this residual term applies
 */
    public WithinCells(Model model, int reps) {
        modl = model;
        /* Not part of model YET
            -- so df()==0 signals model is saturated. */
        if (df()<=0) setName("Within");
        else setName("Residual");
        setLevels(reps);
        setRandom(true);
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
    public void setLevels(int lev) {
        levels = (lev < 1) ? 1 : lev;
    }
    public int order() {    // shouldn't be called anyway
        return 1;
    }
    public Factor factor(int i) {   // shouldn't be called anyway
        return null;
    }

    private Model modl;
}