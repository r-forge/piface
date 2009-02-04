/**
 * Simple applet to launch a Piface application specified in
 * the applet tag APP
 */

package rvl.piface.apps;

import java.applet.*;
import rvl.piface.*;
import rvl.util.*;

public class PiLaunch extends Applet
{

    public void init() {
        try {
            String app = getParameter("app");
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

    public String getAppletInfo() {
        return
            "PiLaunch applet -- Russ Lenth, October, 2000\n"
          + "Launch a Piface application as if an applet";
    }


}
