package rvl.stat.anova;

import rvl.stat.anova.*;
import java.util.*;

/**
 * One term in a balanced anova model.  A <tt>Term</tt> is simply the cross of
 * two or more <tt>Factor</tt>s.  Any nesting information is contained in
 * the included <tt>Factor</tt>s.  Note also that <tt>Factor</tt> is a subclass
 * of <tt>Term</tt>, obviating the extra work of constructing <tt>Term</tt>s
 * for main effects (or purely nested effects, for that matter).
 *
 * @author Russ Lenth
 * @version 1.1 March 18, 2000
 * @see Factor
 */
public class Term {

    protected Factor fac[];
    protected StringBuffer fullName;
    protected Factor  nestedIn[];

/** DON'T USE THIS.  IT EXISTS ONLY SO THAT <tt>Factor</tt> CAN BE A SUBCLASS */
    public Term(){}

/**
 *  Construct a new Term defined as the interaction of factors f[].
 */
    public Term(Factor f[]) {
        fac = new Factor[f.length];
        fullName = new StringBuffer();
        int n=0;
        Vector nes = new Vector(5,5);
        for (int i=0; i<f.length; i++) {
            fac[i] = f[i];
            if (i>0) fullName.append("*");
            fullName.append(f[i].name);
            Factor thisNest[] = f[i].getNest();
            if (thisNest!=null) for (int j=0; j<thisNest.length; j++) {
                boolean isNew=true;
                Factor thisFac = thisNest[j];
                for (int k=0; k<n; k++)
                    if (thisFac.equals((Factor)nes.elementAt(k)))
                        isNew = false;
                if (isNew) {
                    nes.addElement(thisFac);
                    n++;
                }
            }
        }
        if (n>0) {
            nestedIn = new Factor[n];
            String sep = "(";
            for (int i=0; i<n; i++) {
                nestedIn[i] = (Factor)nes.elementAt(i);
                fullName.append(sep + nestedIn[i].name);
                sep = " ";
            }
                fullName.append(")");
        }
    }

/**
 * Construct a new Term defined as the interaction of <tt>t</tt> and <tt>f</tt>
 */
    public Term (Term t, Factor f) {
        fac = new Factor[t.order()+1];
        fullName = new StringBuffer();
        for (int i=0; i<t.order(); i++) {
            fac[i] = t.factor(i);
            if (i>0) fullName.append("*");
            fullName.append(t.factor(i).name);
        }
        fullName.append("*" + f.name);
        fac[t.order()] = f;
        Factor tnest[] = t.getNest(), fnest[] = f.getNest();
        if (tnest != null) {
            if (fnest == null) setNest(tnest);
            else {  // merge the two nests, omitting duplicates
                int n = tnest.length;
                for (int i=0; i<fnest.length; i++)
                    if (!t.containsFactor(fnest[i])) n++;
                nestedIn = new Factor[n];
                n = tnest.length;
                for (int i=0; i<tnest.length; i++)
                    nestedIn[i] = tnest[i];
                for (int i=0; i<fnest.length; i++)
                    if (!t.containsFactor(fnest[i])) nestedIn[n++] = fnest[i];
            }
        }
        else setNest(fnest);

        if (nestedIn != null) {
            fullName.append("(" + nestedIn[0].getName());
            for (int i=1; i<nestedIn.length; i++) {
                fullName.append(" " + nestedIn[i].getName());
            }
            fullName.append(")");
        }
    }

    protected void setNest (Factor[] n) {
        if (n == null) return;
        nestedIn = new Factor[n.length];
        for (int i=0; i<n.length; i++)
            nestedIn[i] = n[i];
    }

    public Factor[] getNest() {
        return nestedIn;
    }

/**
 * @return the number of interacting factors (<i>not</i> counting nests)
 */
    public int order() {
        return fac.length;
    }

/**
 * @return the factor at index <tt>i</tt>
 */
    public Factor factor(int i) {
        return fac[i];
    }

/**
 * @return the factors in this term
 */
    public Factor[] getFactors() {
        return fac;
    }

/**
 * @return the full name of the term
 */
    public String getName() {
        return fullName.toString();
    }

/**
 * @return whether or not the term contains random factors
 */
    public boolean isRandom() {
        for (int i=0; i<fac.length; i++)
            if ((fac[i].isRandom()))
                return true;
        return false;    // (we get here only if everything was fixed)
    }

/**
 * @return whether this term contains all the factors in <tt>t</tt>
 */
    public boolean containsTerm(Term t) {
        for (int i=0; i<t.order(); i++)
            if (!containsFactor(t.factor(i))) return false;
        return true;
    }

/**
 * @return whether this term contains <tt>f</tt>
 */
    public boolean containsFactor(Factor f) {
        for (int i=0; i<order(); i++)
            if (factor(i).contains(f)) return true;
        return false;
    }

/**
 * @return whether there is any overlap in the factors in this term and <tt>f</tt>
 */
    public boolean overlaps(Factor f) {
        Factor fn[] = f.getNest();
        for (int i=0; i<order(); i++) {
            Factor fi = factor(i);
            if (fi.equals(f)) return true;
            if (fn != null) for (int j=0; j<fn.length; j++)
                if (fn[j].overlaps(fi)) return true;
        }
        if (nestedIn != null) for (int i=0; i<nestedIn.length; i++) {
            if (nestedIn[i].equals(f)) return true;
            if (fn != null) for (int j=0; j<fn.length; j++)
                if (fn[j].overlaps(nestedIn[i])) return true;
        }
        return false;
    }

/**
 * @return whether there is any overlap in the factors in this term and <tt>f</tt>
 */
    public boolean overlaps(Term t) {
        if (t instanceof Factor)
            return containsFactor((Factor)t);
        Factor f[] = t.fac;
        for (int i=0; i<f.length; i++) {
            if (this.overlaps(f[i])) return true;
        }
        return false;
    }

/**
 * @return this term with the factors removed
 */
    public Term minus(Factor f[]) {
        if (f == null) return this;
        if (f.length == 0) return this;
        Factor ff[] = new Factor[fac.length - f.length];
        Term r = new Term(f);
        int i, n;
        for (i=n=0; i<fac.length; i++)
            if (!r.containsFactor(fac[i])) ff[n++] = fac[i];
        return new Term(ff);
    }

    public int df() {
        int d = 1;
        for (int i=0; i<order(); i++)
            d *= factor(i).getLevels() - 1;
        if (nestedIn != null)
            for (int i=0; i<nestedIn.length; i++)
                d *= nestedIn[i].span();
        return d;
    }

    public int span() {
        int d = 1;
        for (int i=0; i<order(); i++)
            d *= factor(i).getLevels();
        if (nestedIn != null)
            for (int i=0; i<nestedIn.length; i++)
                d *= nestedIn[i].span();
        return d;
    }

    public String toString() {
        StringBuffer b = new StringBuffer(getName());
        b.append(" <" + (isRandom() ? "random" : "fixed") + "> ");
        if (this instanceof Factor)
            b.append("" + ((Factor)this).getLevels() + " levels, ");
        b.append("" + df() + " df");   //=== skip: , span = " + span());
        return b.toString();
    }
}
