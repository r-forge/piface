package rvl.awt;

import java.awt.*;

/**
 *  User interface component intended for placing gridlines between components
 */
public class GridLine extends Canvas {
	int lineType;
	Color foreground = Color.black;
	
	public static final int
		HORIZ = 0,
		VERT = 1,
		CROSS = 2;
	
	public GridLine(int type) {
		lineType = type;
	}
	public GridLine(int type, Color color) {
		lineType = type;
		foreground = color;
	}

	public void paint(Graphics g) {
		int w = size().width, h = size().height;
		g.setColor(foreground);
		if (lineType == HORIZ || lineType == CROSS)
			g.drawLine(0, h/2, w, h/2);
		if (lineType == VERT || lineType == CROSS)
			g.drawLine(w/2, 0, w/2, h);
	}

	public Dimension preferredSize() {
		return new Dimension(5,5);
	}
}
