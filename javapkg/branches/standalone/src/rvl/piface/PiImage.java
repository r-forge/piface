package rvl.piface;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class PiImage extends Component {

    private Image image = null;
    private int w, h;

    /**
     * Constructor - sets preferred size to image size
     */
    public PiImage (String filename, Class clas) {
        this(filename, clas, 0, 0);
    }

    /**
     * Constructor - sets preferred size to specified width and height
     */
    public PiImage (String filename, Class clas, int width, int height) {
       try {
            InputStream in = clas.getResourceAsStream(filename);
            int size = in.available(), bytesRead=0;
            byte buf[] = new byte[size];
            while (bytesRead < size)
				bytesRead += in.read(buf, bytesRead, size-bytesRead);
            in.close();
            image = Toolkit.getDefaultToolkit().createImage(buf,0,size);
            if (width*height > 0) {
                w = width;
                h = height;
            }
            else {
                MediaTracker mt = new MediaTracker(this);
                mt.addImage(image,0);
                mt.waitForID(0);
	            w = image.getWidth(this);
                h = image.getHeight(this);
            }
        }
        catch (IOException e) {
            System.err.println("PiImage(\"" + filename + "\",Class)\n" +
            e.toString());
        }
        catch (InterruptedException ie) {
            System.err.println("PiImage(\"" + filename + "\",Class)\n" +
            ie.toString());
 	}
    }


    public void paint(Graphics g) {
        if (image != null) {
            Dimension ps = getPreferredSize(),
                s = getSize();
            int hoff = (s.width - ps.width) / 2,
                voff = (s.height - ps.height) / 2;
            g.drawImage(image, hoff, voff, this);
        }
    }

    public Dimension getPreferredSize() {
        if (image == null)
            return new Dimension(0,0);
        return new Dimension(w,h);
	    //(image.getWidth(null), image.getHeight(null));
    }

    public Dimension preferredSize() {
        return getPreferredSize();
    }

}
