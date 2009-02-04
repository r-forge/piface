package rvl.util;

import rvl.piface.*;
public class ModelessMsgBox extends Piface {

    private String msg;
    private Closeable listener;

/**
 * Set up the GUI
 */
    public void gui () {
        label(msg);
        button("ok", "OK");
    }

    public void click() {}  // not needed

    public void ok() {
        listener.close();
        dispose();
    }


/**
 * The following code makes it self-standing...
 */
    public ModelessMsgBox(String title, String msg, Closeable listener) {
        super(title, false);
        this.msg = msg;
        this.listener = listener;
        build();
    }

}
