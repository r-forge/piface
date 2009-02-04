package rvl.piface;

import java.awt.*;
import rvl.awt.*;

/**
 * Simple extension of Panel that adds ability to
 * add a border around it.
 */

public class PiPanel extends Panel {

  // Border types
    public static final int
        NO_BORDER = 0,
        PLAIN_BORDER = 1,
        RAISED = 2,
        LOWERED = 3;

    private int borderType = NO_BORDER;
    private Color borderColor = null;
    protected boolean isStretchable = false;

  // identical constructors
    public PiPanel() {
        super();
    }
    public PiPanel(LayoutManager layout) {
        super(layout);
    }

/**
 * Set the border type
 */
    public void setBorderType (int borderType) {
        this.borderType = borderType;
    }

/**
 * Set the border color
 */
    public void setBorderColor (Color borderColor) {
        this.borderColor = borderColor;
    }

/**
 * Make (or unmake) this panel and any PiPanels that
 * contain it vertically stretchable (assuming
 * the RVLayout manager is used)
 */
    public void setStretchable (boolean st) {
        isStretchable = st;
        LayoutManager lay = getLayout();
        if (lay instanceof RVLayout)
            ((RVLayout)lay).setStretchable(st,true);
        Container cont = getParent();
        if (cont != null)
            if (cont instanceof PiPanel)
            ((PiPanel)cont).setStretchable(st);
    }

    public Component add(Component comp) {
        if (comp instanceof PiPanel)
            if (((PiPanel)comp).isStretchable)
            setStretchable(true);
        return super.add(comp);
    }


/**
 * Override the paint method -- draw the border
 * before the rest of the panel is painted.
 */
    public void paint (Graphics g) {
        int w = getSize().width - 1, h = getSize().height - 1;
        switch (borderType) {
            case NO_BORDER:
                break;
            case PLAIN_BORDER:
                if (borderColor == null)
                    setBorderColor(getForeground());
                g.setColor(borderColor);
                g.drawRect(0, 0, w, h);
                break;
            case RAISED:
                if (borderColor == null)
                    setBorderColor(getBackground());
                g.setColor(borderColor);
                g.draw3DRect(0, 0, w, h, true);
                g.setColor(borderColor.brighter());
                g.draw3DRect(1, 1, w-2, h-2, true);
                break;
            case LOWERED:
                if (borderColor == null)
                    setBorderColor(getBackground());
                g.setColor(borderColor);
                g.draw3DRect(0, 0, w, h, false);
                g.setColor(borderColor.darker());
                g.draw3DRect(1, 1, w-2, h-2, false);
                break;
        }
        super.paint(g);
    }
}
