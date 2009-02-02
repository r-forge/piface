/**
 * Simple applet to launch a Piface application specified in
 * the applet tag APP
 */

package rvl.piface.apps;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import rvl.piface.*;
import rvl.util.*;

public class PiLaunchButton extends Applet 
    implements ActionListener
{

    String app;
    Button button;

    public void init() {
	setLayout(new GridLayout(1,1));
	app = getParameter("app");
	button = new Button("Launch");
	button.addActionListener(this);
	add(button);
    }

    public void runApp() {
        try {
            Object obj = (Class.forName(app)).newInstance();
            ((Piface)obj).setMaster(this);
        }
        catch(Exception exc) {
            Utility.warning(exc,true);
        }
    }

// Call this, then return from init() to quit early.
    public void quit(String msg) {
        destroy();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
	    System.out.println("Running app: " + app);
	    runApp();
	}
    }

    public String getAppletInfo() {
        return
            "PiLaunchButton applet -- Russ Lenth, Spetember, 2001\n"
          + "Launch a Piface application from an applet button click";
    }


}
