package rvl.util;

import java.awt.*;
import java.io.*;

/**
 *  This class is used to set up a TextArea to receive output.
 *  e.g., to redirect System.out, you'd do:<code>
 *      TextAreaOutputStream taos = new TextAreaOutputStream (txtArea);
 *      PrintStream ps = new PrintStream (taos, true);
 *      System.setIn (ps);</code>
 */
public class TextAreaOutputStream extends ByteArrayOutputStream {

    private TextArea textArea;

/**
 *  Construct a TextAreaOutputStream with the given TextArea
 */
    public TextAreaOutputStream (TextArea ta) {
        textArea = ta;
    }
    
    public void flush () {
        textArea.append (toString());
        reset();
    }
    
}
