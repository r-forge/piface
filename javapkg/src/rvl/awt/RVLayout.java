package rvl.awt;

import java.awt.*;
import java.util.*;

public class RVLayout implements LayoutManager, java.io.Serializable {

    private int rows, cols,
        hGap = 6, vGap = 6,
        rowHgt[], colWid[],
        nvg = 0, nhg = 0,       // numbers of glue joints
        vGlue[] = new int[20], hGlue[] = new int[20];   // places where glue is inserted

    private boolean sizesSet = false,
        stretchRows = false, stretchCols = false;

    private Vector extras = new Vector();

/** Construct a new RVLayout with default of 2 columns */
    public RVLayout() {
        this(2);
    }
/** Construct a new RVLayout with <t>ncols</t> columns */
    public RVLayout(int ncols) {
        cols = ncols;
    }
/** Construct a new RVLayout with <t>ncols</t> columns
 *  and specified gaps between components
 */
    public RVLayout(int ncols, int hgap, int vgap) {
        cols = ncols;
        hGap = hgap;
        vGap = vgap;
    }
/** Construct a new RVLayout with <t>ncols</t> columns
 *   and specified enabling of stretching of components
 */
    public RVLayout(int ncols, boolean stretchrows, boolean stretchcols) {
        cols = ncols;
        stretchRows = stretchrows;
        stretchCols = stretchcols;
    }
/** Construct a new RVLayout with <t>ncols</t> columns,
 *  specified gaps between components,
 *  and specified enabling of stretching of components
 */
    public RVLayout(int ncols, int hgap, int vgap,
                    boolean stretchrows, boolean stretchcols) {
        cols = ncols;
        hGap = hgap;
        vGap = vgap;
        stretchRows = stretchrows;
        stretchCols = stretchcols;
    }

    public void setHgap(int gap) { hGap = gap; }

    public void setVgap(int gap) { vGap = gap; }

    public int getHgap() { return hGap; }

    public int getVgap() { return vGap; }

    public void setStretchable(boolean rows, boolean cols) {
        stretchRows = rows;
        stretchCols = cols;
    }

    public boolean[] isStretchable() {
        return new boolean[] {stretchRows, stretchCols};
    }

// The following 2 functions need to save information in extras to be used
// later in setSizes(parent).  Since 2 numbers are needed, a Dimension
// object is convenient.  The .width element is the index; its sign
// indicates whether it is a row (+) or a column (-).  The .height
// element saves the height or width of the row or column.
/**
 *  Set the width of a particular column
 */
    public void setColWidth(int column, int width) {
        extras.addElement(new Dimension(-column, width));
        sizesSet = false;
    }
/**
 *  Set the height of a particular row
 */
    public void setRowHeight(int row, int height) {
        extras.addElement(new Dimension(row, height));
        sizesSet = false;
    }

    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.insets();
        if (!sizesSet) setSizes(parent);
        int wid = insets.left + insets.right + cols*hGap,
            hgt = insets.top + insets.bottom + rows*vGap;
        for (int i=0; i<rows; hgt += rowHgt[i++]);
        for (int j=0; j<cols; wid += colWid[j++]);
            return new Dimension(wid, hgt);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        int x, y;
        if (!sizesSet) setSizes(parent);
        Dimension d = preferredLayoutSize(parent);

        int cw[] = new int[cols], hf[] = new int[cols];
        double fac = stretchCols
            ? (0.0 + parent.size().width - cols*hGap) / (d.width - cols*hGap)
            : 1.0;
        for (int j=0; j<cols; j++) {
            cw[j] = (int) (fac * colWid[j]);
            hf[j] = 0;
        }

        int rh[] = new int[rows], vf[] = new int[rows];
        fac = stretchRows
            ? (0.0 + parent.size().height - rows*vGap) / (d.height - rows*vGap)
            : 1.0;
        for (int i=0; i<rows; i++) {
            rh[i] = (int) (fac * rowHgt[i]);
            vf[i] = 0;
        }

        Dimension pls = preferredLayoutSize(parent);
        if (nhg > 0 && !stretchCols) {
            int hFill = (parent.size().width - pls.width) / nhg;
            if (hFill > 0) for (int i=0; i<nhg; i++)
                if (hGlue[i] < cols) hf[hGlue[i]] = hFill;
        }
        if (nvg > 0 && !stretchRows) {
            int vFill = (parent.size().height - pls.height) / nvg;
            if (vFill > 0) for (int i=0; i<nvg; i++)
                if (vGlue[i] < rows) vf[vGlue[i]] = vFill;
        }

        Insets insets = parent.insets();
        y = insets.top + vGap / 2;

        int n = parent.countComponents();
        loop: for (int r=0, i=0; r<rows; r++) {
            y += vf[r];
            x = insets.left + hGap / 2;
            for (int c=0; c<cols; c++) {
                x += hf[c];
                Component comp = parent.getComponent(i);
                    comp.reshape(x, y, cw[c], rh[r]);
                x += cw[c] + hGap;
                if (++i >= n) break loop;
            }
            y += rh[r] + vGap;
        }
    }

    private void setSizes(Container parent) {
        int n = parent.countComponents();
        rows = (n + cols - 1) / cols;
        colWid = new int[cols];
        rowHgt = new int[rows];
        for (int j=0; j<cols; colWid[j++]=0);
        for (int i=0; i<rows; rowHgt[i++]=0);

        for (int i=0, row=0, col=0; i<n; i++) {
            Dimension d = parent.getComponent(i).preferredSize();
            colWid[col] = Math.max(colWid[col], d.width);
            rowHgt[row] = Math.max(rowHgt[row], d.height);
            if(++col >= cols) {
                ++row;
                col = 0;
            }
        }

        /*** Deal with special cases ***/
        for (int i=0; i<extras.size(); i++) {
            Dimension d = (Dimension) extras.elementAt(i);
            if (d.width > 0) rowHgt[d.width] = d.height;
            else colWid[-d.width] = d.height;
        }

 //!!!!       sizesSet = true;
    }

/**
* Reserve a place for a stretchable horizontal gap (glue)
* in the layout.  Applicable only when stretchRows = false
*/
    public void horzFill(Container parent) {
        hGlue[nhg++] = parent.countComponents() % cols;
    }
/**
* Reserve a place for a stretchable vertical gap (glue)
* in the layout.  Applicable only when stretchCols = false
*/
    public void vertFill(Container parent) {
        vGlue[nvg++] = parent.countComponents() / cols;
    }

}
