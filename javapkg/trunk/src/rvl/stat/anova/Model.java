package rvl.stat.anova;

import rvl.stat.anova.*;
import rvl.stat.dist.F;
import java.util.*;
import rvl.util.LinAlg;
import rvl.util.Utility;

public class Model {
    Vector fac, term, fraction;
    int coef[];
    double leadCoef[], denom[], dendf[]; // for saving EMS stuff
    double EMSC[][], LU[][];    // save of ems coef matrix & its LU decomp
    int LUp[];                  // permutations for LU
//    Factor[] nullFactor = new Factor[] {new Factor("_null-",2,false)};

    public boolean recalcLU = true; // flag that LU, LUp needs recomputing

    public Model () {
        fac = new Vector(5,5);  // initial 5, grows linearly
        fraction = new Vector(5,5);  // initial 5, grows linearly
        term = new Vector();    // initial 10, grows exponentially
    }

/**
 *  Construct a Model by parsing a string s
 */
    public Model (String s) {
        fac = new Vector(5,5);  // initial 5, grows linearly
        fraction = new Vector(5,5);  // initial 5, grows linearly
        term = new Vector();    // initial 10, grows exponentially

        StringTokenizer modST = new StringTokenizer(s,"+");
        while (modST.hasMoreTokens()) {
            StringTokenizer expST = new StringTokenizer (modST.nextToken(),"|");
            int expandPos = nTerm();
            while (expST.hasMoreTokens()) {
                addTerm(expST.nextToken(), expandPos);
            }
        }
    }

/**
 *  @return ith factor in model
 */
    public Factor getFac(int i) {
        return (Factor)fac.elementAt(i);
    }
/**
 *  @return factor named s in the model (case is ignored)
 *  Note: Nesting factors are ignored, and  s  is assumed
 *  not to have any parenthesized expressions.
 */
    public Factor getFac(String s) {
        for (int i=0; i<nFac(); i++)
            if (s.equalsIgnoreCase(getFac(i).name))
                return getFac(i);
        Utility.warning("Warning: Factor named '" + s + "' not found");
        return null;
    }

    public Term getTerm(int i) {
        return (Term)term.elementAt(i);
    }

    public int nFac() {
        return fac.size();
    }

    public int nTerm() {
        return term.size();
    }

    public void addFactor(Factor f) {
        fac.addElement(f);
        term.addElement(f);
    }

    public void addFactor(Factor f, boolean expand) {
        int nt = term.size();
        fac.addElement(f);
        term.addElement(f);
        if (!expand) return;

        for (int i=0; i<nt; i++) {
            Term t = getTerm(i);
            if (!(t.overlaps(f))) {
                term.addElement(new Term(t,f));
            }
        }
    }

/**
 *  Add a term to the model
 */
    public void addTerm(Term t) {
        term.addElement(t);
    }

/**
 *  Parse  t  and create a new factor or term as appropriate.
 *  expandStart is the index in  term  at which to start expanding
 *  with interactions of new factors added
 */
    private void addTerm (String t, int expandStart) {
        String  delim = "\n\t\r ()*",
                facName[] = new String[20],
                nesName[] = new String[20];
        int nfac=0, nnes=0, nestDepth=0;
        StringTokenizer st = new StringTokenizer(t, delim, true);

        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            int ch = tok.charAt(0);     // 1st character
            if (delim.indexOf(ch) > -1) {   // it is a delimiter
                if ( ch == '(' ) nestDepth++;
                else if ( ch == ')' ) nestDepth--;
            }
            else {                          // it is a name
                if (nestDepth == 0) facName[nfac++] = tok;
                else if (nestDepth == 1) nesName[nnes++] = tok;
            }
        }

        if (nfac==0)        // empty term
            return;

        if (nfac == 1) {    // 1st-order term
            Factor f = new Factor(facName[0], 2, false);
            if (nnes > 0) {
                f.nestedIn = new Factor[nnes];
                for (int i=0; i<nnes; i++)
                    f.nestedIn[i] = getFac(nesName[i]);
                f.setName(facName[0]);
            }

            int expandStop = term.size();
            addFactor(f);
            for (int i=expandStart; i<expandStop; i++) {
                Term trm = getTerm(i);
                if (!(trm.overlaps(f)))
                    term.addElement(new Term(trm,f));
            }
            return;
        }

        // (else) it's a high-order term ...
        Factor ff[] = new Factor[nfac];
        for (int i=0; i<nfac; i++)
            ff[i] = getFac(facName[i]);
        Term trm = new Term(ff);
        addTerm(trm);
    }



    public void removeTerm(int i) {
        term.removeElementAt(i);
    }

/**
 *  Set numbers of levels based on a string with format
 *      name #levels   name #levels    ...        (space-delimited)
 *  Extensions:
 *      1. Factors can be locked to have common numbers of levels
 *         by specifying name1=name2=... instead of a single name.
 *      2. A factor's levels may be used to define a fractional
 *         experiment by preceding its name with a "/"
 *      Example (3-period crossover design): Pass the string
 *         /seq=per=drug 3   SUBJ 5
 */
    public void setLevels(String s) {
        Factor fac;
        FactorSet fs;

        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            StringTokenizer ft = new StringTokenizer(st.nextToken(),"=");
            int nLev = Integer.parseInt(st.nextToken());
        int nf = ft.countTokens(), index = 0;
            fs = new FactorSet(nf);
            while (ft.hasMoreTokens()) {
                String facName = ft.nextToken();
                if (facName.charAt(0) == '/') {
                    fac = getFac(facName.substring(1));
                    fractionBy(fac);
                }
                else
                    fac = getFac(facName);
                fac.levels = nLev;
                if (nf > 1) {
                    fs.facset[index++] = fac;
                    fac.siblings = fs;
                }
            }
        }
    }

/**
 *  Set specified factors to be random
 */
    public void setRandom(String s) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            getFac(st.nextToken()).setRandom(true);
        }
    }

/**
 *  Set specified factors to be fixed
 */
    public void setFixed(String s) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            getFac(st.nextToken()).setRandom(false);
        }
    }

/**
 *  Add factor to the fraction list
 */
    private void fractionBy(Factor f) {
        fraction.addElement(f);
    }

/**
 *  @return the total number of observations in the model
 */
    public int getNobs() {
        int N = 1;
        for (int i=0; i<fac.size(); i++)
            N *= ((Factor)(fac.elementAt(i))).getLevels();
        for (int i=0; i<fraction.size(); i++)
            N /= ((Factor)(fraction.elementAt(i))).getLevels();
        return N;
    }

/**
 *  Figure out coefficients of model terms used in EMS algorithm
 */
    private void getCoefs() {
        int n = term.size(),
            N = getNobs();
        if (coef==null) {
            coef = new int[n];
            leadCoef = new double[n];
            denom = new double[n];
            dendf = new double[n];
        }
        for (int i=0; i<n; i++) {
            Term t = getTerm(i);
            coef[i] = (int)( N / t.span() + .1 );
        }
    }

/**
 * @return true iff EMS(<tt>thisTerm</tt>) includes a variance
 * component for <tt>thatTerm</tt>.
 */
    private boolean
    include (Term thisTerm, Term thatTerm) {
        if (thatTerm.isRandom())
            return (thatTerm.containsTerm(thisTerm));   // unrestricted model
        else
            return false;
    }

/**
 * @return matrix of coefficients of the EMSs
 * The ith row contains the coefficients of the variance components.
 * This linear combination is the EMS of the ith term
 */
    public double[][] EMSCoefs() {
        int n = term.size();
        double C[][] = new double[n][];
        getCoefs();
        for (int i=0; i<n; i++) {
            Term t = getTerm(i);
            C[i] = new double[n];
            for (int j=0; j<n; j++)
                C[i][j] = include(t, getTerm(j)) ? coef[j] : 0;
            C[i][i] = coef[i];
        }
        return C;
    }

/**
 * @return matrix of coefficients of the mean squares comprising
 * the error terms for each term
 */
    public double[][] getErrorTerms(double C[][]) {
        int n = term.size(), perm[] = new int[n];
        double Ct[][] = LinAlg.transpose(C),
            ErrCoef[][] = new double[n][];
        if (!(LinAlg.LUInPlace(Ct,perm))) {
            Utility.warning("Variance components are not all estimable");
            return null;
        }
        for (int i=0; i<n; i++) {
            ErrCoef[i] = LinAlg.copy(C[i]);
            ErrCoef[i][i] = 0;
            ErrCoef[i] = LinAlg.LUSolveInPlace(Ct,perm,ErrCoef[i]);
        }
        return ErrCoef;
    }

    public double[][] getErrorTerms() {
        return getErrorTerms(EMSCoefs());
    }

/**
 * @return powers of tests, given the
 * SD components in <tt>sd[]</tt> and test size <tt>alpha</tt><br>
 * <i>Note:</i> If test is invalid, a negative power is returned:
 * A "power" of -1 means no error term;
 * -2 means denominator df are too low;
 * -3 means an arithmetic exception occurred.
 */
    public double[] power(double sd[], double alpha) {
        int n = term.size();
        double num[] = new double[n],
//            denom[] = new double[n],
//            dendf,
            ems[] = new double[n],
            pwr[] = new double[n],
            emsCoef[][] = EMSCoefs(),
            errCoef[][] = getErrorTerms(emsCoef),
            d;
        for (int i=0; i<n; i++) {
            double c[] = emsCoef[i];
            num[i] = c[i] * sd[i]*sd[i];
            ems[i] = 0d;
            for (int j=0; j<n; j++)
                ems[i] += c[j] * sd[j]*sd[j];
            denom[i] = ems[i] - num[i];
            leadCoef[i] = c[i];
        }
        for (int i=0; i<n; i++) {   // get error terms
            try {
                Term t = getTerm(i);
                double a=0, b=0, m;
                boolean noError = true;
                for (int j=0; j<n; j++) {
                    if ((d=errCoef[i][j]) > 1e-6) {
                        m = d*ems[j];
                        a += m;
                        b += m*m/getTerm(j).df();
                        noError = false;
                    }
                }
                if (noError) {
                    pwr[i] = -1.0;
                    dendf[i] = Double.NaN;
                }
                else {
                    dendf[i] = a*a/b;
                    pwr[i] = (dendf[i] > 0.1)
                        ? F.power(num[i]/denom[i],t.df(),
                            dendf[i],alpha,t.isRandom())
                        : -2.0;
                }
            }
            catch (ArithmeticException evt) {
                pwr[i] = -3;
            }
        }
        return pwr;
    }

/**
 * @return basic information for use in power computation
 * for the i-th term:
 * { leading coef, EMS(denom), numdf, dendf }<br>
 * These values are stored in the last call to power(),
 * so you should call power() first if it hasn't benn
 * called previously, or if anything has changed.
 */
    public double[] getPowerInfo(int i) {
        return new double[] {
            leadCoef[i], denom[i], getTerm(i).df(), dendf[i]
        };
    }


    public void printEMS() {
        System.out.print(EMSString());
    }

    public String EMSString() {
        StringBuffer b = new StringBuffer();
        int n = term.size();
        double C[][] = EMSCoefs(),
            Err[][] = getErrorTerms();

        b.append("Expected mean squares\n");
        for (int i=0; i<n; i++) {
            Term t = getTerm(i);
            b.append("\n" + t + "\n");
            b.append("  EMS =");
            boolean first = true;
            for (int j=0; j<n; j++) if (C[i][j] != 0) {
                b.append(first ? " " : " + ");
                first = false;
                if (C[i][j] != 1.0)
                    b.append(C[i][j] + "*Var{"
                        + getTerm(j).getName() + "}");
                else
                    b.append("Var{" + getTerm(j).getName() + "}");
            }
            b.append("\n  Denom =");
            first = true;
            for (int j=0; j<n; j++) if (Err[i][j] != 0) {
                b.append(first ? " " : " + ");
                first = false;
                if (Err[i][j] != 1.0)
                    b.append(Err[i][j] + "*MS{"
                        + getTerm(j).getName() + "}");
                else
                    b.append("MS{" + getTerm(j).getName() + "}");
            }
            b.append("\n");
        }
        return b.toString();
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        for (int i=0; i<term.size(); i++) {
            if (i>0) b.append("\n");
//            b.append(getTerm(i).getName() + "\t<" + getTerm(i).df() + " df>");
            b.append(getTerm(i).toString());
        }
        return b.toString();
    }


// ---- Support for multiple comparisons ----

/**
 * @return Vector of fac[] arrays corresponding to
 * restrictions on comparisons of levels or factor
 * combinations in compTerm.  Each restriction represents
 * a combination of factors that, if held fixed in a
 * comparison, will have a smaller variance than just any
 * comparison. Note: The first element is always null,
 * for an unrestricted comparison.<br>
 * compTerm should be fixed -- otherwise null is returned
 */
    public Vector getCompRestr(Term compTerm) {
        if (compTerm.isRandom()) return null;
        Vector vec = new Vector();
        vec.addElement(null);
        if (compTerm.order() > 1) for (int i=0; i<nTerm(); i++) {
            Term u = getTerm(i);
            if (u.isRandom() && u.overlaps(compTerm)
                    && !u.containsTerm(compTerm)) {
                Vector v = new Vector();
                for (int j=0; j<compTerm.order(); j++)
                    if (u.containsFactor(compTerm.factor(j)))
                        v.addElement(compTerm.factor(j));
                Factor f[] = new Factor[v.size()];
                for (int j=0; j<v.size(); j++)
                    f[j] = (Factor)v.elementAt(j);
                vec.addElement(f);
            }
        }
        return vec;
    }

/**
 * @return all possible restrictions that could be
 * placed on a comparison.  This is just like
 * getCompRestr(), except it does not confine itself
 * to the restrictions that make a difference in the
 * variance of a comparison or contrast.
 */
    public Vector getAllCompRestr(Term compTerm) {
        if (compTerm.isRandom()) return null;
        Vector vec = new Vector(5);
        vec.addElement(null);
        if (compTerm.order() > 1) for (int i=0; i<nTerm(); i++) {
            Term u = getTerm(i);
            if (u != compTerm && !u.isRandom()
                && compTerm.containsTerm(u)) {
                    if (u instanceof Factor)
                        vec.addElement(new Factor[]{(Factor)u});
                    else vec.addElement(u.fac);
            }
        }
        return vec;
    }

/**
 * @return ["base variance", d.f.] of a comparison of the levels
 * of compTerm, when it is restricted to the same levels of
 * factors in restr, given the SDs in effSD (note - only
 * the components of effSD that correspond to random
 * terms are used.<br>
 * Multiply this result by the sum of squares of the contrast
 * coefficients to get the actual variance.
 */
    public double[] getCompVariance
        (Term compTerm, Factor restr[], double effSD[])
    {
        double coef[] = getCompCoefs(compTerm, restr),
            c[] = getCompErrorTerms(coef),
            ms[] = LinAlg.constant(0, nTerm()),
            v = 0, denom = 0, df;
        for (int i=0; i<nTerm(); i++) if (coef[i] > 0) {
            v += coef[i] * effSD[i] * effSD[i];
            for (int j=0; j<nTerm(); j++) if (EMSC[i][j] > 0)
                ms[i] += EMSC[i][j] * effSD[j] * effSD[j];
        }
        for (int i=0; i<nTerm(); i++) if (c[i] != 0) {
            double z = c[i] * ms[i];
            denom += z * z / getTerm(i).df();
        }
        df = v * v / denom;
        return new double[]{v, df};
    }

/**
 * @return the coefficients of the variance components
 * to obtain the "base variance" described above
 */
    public double[] getCompCoefs(Term compTerm, Factor restr[]) {
        double coef[] = new double[nTerm()];
        Term ct = compTerm;
        if (restr != null) ct = compTerm.minus(restr);
        for (int i=0; i<nTerm(); i++) {
            Term u = getTerm(i);
            if (u.isRandom() && u.overlaps(ct)) {
                coef[i] = 1.0 / u.span();   // 1 / # distinct levels
                for (int j=0; j<compTerm.order(); j++)
                    if (u.containsFactor(compTerm.factor(j)))
                        coef[i] *= compTerm.factor(j).span();
            }
            else
                coef[i] = 0;
        }
        return coef;
    }

/**
 * @return lin. comb. of MSs that estimates the given lin. comb.
 * of variances in coef[].<br>
 * If model has changed since last call, set recalcLU to true
 * before calling
 */
    public double[] getCompErrorTerms(double[] coef) {
        if (recalcLU) {
            recalcLU = false;
            EMSC = EMSCoefs();
            LU = LinAlg.transpose(EMSC);
            LUp = new int[nTerm()];
            if (!(LinAlg.LUInPlace(LU,LUp))) {
                Utility.warning("Variance components are not all estimable");
                return null;
            }
        }
        double c[] = LinAlg.copy(coef);
        c = LinAlg.LUSolveInPlace(LU,LUp,c);
        return c;
    }

/**
 * @return strings that describes the variance of a comparison.<br>
 * Element 0 is the combination of variances (to be multiplied
 * by sum of squares of contrast coefs);<br>
 * Element 1 is the combination of mean squares to estimate
 * this variance.<p>
 * Should set recalcLU to true if model has
 * changed since last call.
 */
    public String[] getCompVarString(Term compTerm, Factor restr[]) {
        double coef[] = getCompCoefs(compTerm, restr),
            c[] = getCompErrorTerms(coef);
        boolean isFirstV = true, isFirstM = true;
        StringBuffer sbV = new StringBuffer(""), sbM = new StringBuffer("");
        for (int i=0; i<nTerm(); i++) {
            if (coef[i] > 0) {
                if (!isFirstV) sbV.append(" + ");
                isFirstV = false;
                sbV.append("Var{" + getTerm(i).getName() + "}");
                sbV.append("/" + Utility.format(1/coef[i],3));
            }

            if (c[i] > 0) {
                if (!isFirstM) sbM.append(" + ");
                isFirstM = false;
                sbM.append(Utility.format(c[i],3) + "*");
                sbM.append("MS{" + getTerm(i).getName() + "}");
            }
        }
        return new String[] {sbV.toString(), sbM.toString()};
    }

}
