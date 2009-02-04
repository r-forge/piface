/**
 * Simple applet to choose and run a class selected from a list
 * The list of classes is given in a standard property file
 */

package rvl.piface.apps;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import rvl.piface.*;
import rvl.util.*;

public class PiPicker extends Applet implements ActionListener
{

    java.awt.List list;
    Button runBut;
    Vector classes;
    Properties pl;
    String pkg;     // class package name to append to class names


    public void init() {
        String appList = getParameter("rcFile");
        if (appList==null) appList = "PiPicker.txt";
        setLayout(new BorderLayout());
        pl = new Properties();
        list = new java.awt.List();
        runBut = new Button("Run selection");
        Panel butPan = new Panel(new FlowLayout(FlowLayout.RIGHT));
        butPan.add(runBut);
        classes = new Vector();
        try {
            InputStream in = getClass().getResourceAsStream(appList);
            pl.load(in);
            pkg = pl.getProperty("package","rvl.piface.apps");
            String keys = pl.getProperty("keys");
            StringTokenizer st = new StringTokenizer(keys,",");
            while (st.hasMoreTokens()) {
                String cls = st.nextToken();
                classes.addElement(cls);
                list.add(pl.getProperty(cls,"???"));
            }
            showStatus("");
        }
        catch (IOException ioe) {
            Utility.error(ioe);
        }
	catch(Exception e) {
	    Utility.error(e);
	}
        add(list,"Center");
        add(butPan,"South");
        runBut.addActionListener(this);
        list.addActionListener(this);

        resize(250,150);
    }

// Call this, then return from init() to quit early.
    public void quit(String msg) {
        add(new Label(msg),"Center");
        resize(400,300);
        destroy();
    }

    public String getAppletInfo() {
        return
            "PiPicker applet -- Russ Lenth, March, 2000\n"
          + "Package Piface applications into a selection list";
    }




    public void actionPerformed (ActionEvent e) {
        Object src = e.getSource();
        if (!src.equals(runBut) && !src.equals(list)) return;
        int i = list.getSelectedIndex();
        if (i < 0) return;
        String cname = (String)classes.elementAt(i);
        try {
            Object obj = (Class.forName(pkg + "." + cname)).newInstance();
            ((Piface)obj).setMaster(this);
        }
        catch(Exception exc) {
            Utility.warning(exc,true);
        }
    }

}
