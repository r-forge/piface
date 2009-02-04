package rvl.awt;

import java.awt.*;
import java.awt.event.*;
import rvl.awt.*;

/**************************************************************************
This class provides some general-purpose dialogs...
    msgBox - display a message and wait for "OK"
    okCancelDialog - ask a question, return true if OK, false if Cancel
    stringDialog - prompt for a string
    intDialog - prompt for an int
    doubleDialog - prompt for a double
    dialog - general dialog that consists of labels paired with
        TextFields or TextAreas
***************************************************************************/

public class GPDialog extends Dialog 
    implements ActionListener {

    protected Component component[];
    protected int cols=30, rows=4;
    protected boolean ok = false;

    public GPDialog(Frame parent, String title, String item[][]) {
        super(parent, title, true);
        setLayout(new RVLayout(1));
        
        component = new Component[item.length];
        for (int i=0; i<item.length; i++) {
            add(new Label(item[i][0]));
            if (item[i][1].equals("TextField")) {
                TextField tf = new TextField(cols);
                add(tf);
            	component[i] = tf;
            }
            else {
                TextArea ta = new TextArea(rows,cols);
                add(ta);
                component[i] = ta;
            }
        }
        
        Panel butPan = new Panel();
        butPan.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Button addButton = new Button("OK");
        Button canButton = new Button("Cancel");
        butPan.add(addButton);
        butPan.add(canButton);
        addButton.addActionListener(this);
        canButton.addActionListener(this);
        add(butPan);
        
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ok = false;
                dispose();
            }
        } );
        
        Point p = parent.getLocation();
        p.x += 50;   p.y += 50;
        setLocation(p);
        
        pack();
    }
    
    public GPDialog(Frame parent, String title, 
        String label[], Component component[], boolean hasCancelButton) 
    {
        super(parent, title, true);
        setLayout(new RVLayout(1));
        
        this.component = component;
        for (int i=0; i<label.length; i++) {
            add(new Label(label[i]));
            add(component[i]);
        }
        
        Panel butPan = new Panel();
        butPan.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Button addButton = new Button("OK");
        butPan.add(addButton);
        addButton.addActionListener(this);
        if (hasCancelButton) {
            Button canButton = new Button("Cancel");
            canButton.addActionListener(this);
            butPan.add(canButton);
        }
        add(butPan);
        
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ok = false;
                dispose();
            }
        } );
        
        Point p = parent.getLocation();
        p.x += 50;   p.y += 50;
        setLocation(p);
        
        pack();
    }
    
    public GPDialog(Frame parent, String title, 
        String label[], Component component[]) 
    {
        this(parent, title, label, component, true);
    }
    
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd.equals("OK")) {
            ok = true;
        }
        else if (cmd.equals("Cancel")) {
            ok = false;
        }
        dispose();
    }
    
    public static String[] dialog(Frame parent, String title, String item[][]) {
        if (parent == null) parent = new Frame();
        GPDialog gpd = new GPDialog(parent, title, item);
        gpd.show();
        if (gpd.ok) {
            String rtn[] = new String[item.length];
            for (int i=0; i<item.length; i++) {
                Component comp = gpd.component[i];
                if (comp instanceof TextField)
                    rtn[i] = ((TextField)comp).getText();
                else if (comp instanceof TextArea)
                    rtn[i] = ((TextArea)comp).getText();
            }
            return rtn;
        }
        else
            return null;
    }

    public static String[] dialog(Frame parent, String title, 
        String label[], Component component[]) 
    {
        if (parent == null) parent = new Frame();
        GPDialog gpd = new GPDialog(parent, title, label, component);
        gpd.show();
        if (component.length > 0) 
            component[0].requestFocus();
        if (gpd.ok) {
            String rtn[] = new String[label.length];
            for (int i=0; i<label.length; i++) {
                Component comp = gpd.component[i];
                if (comp instanceof TextComponent)
                    rtn[i] = ((TextComponent)comp).getText();
                else 
                    rtn[i] = (String)null;
            }
            return rtn;
        }
        else
            return null;
    }
    
    public static void msgBox(Frame parent, String title, String msg) {
        if (parent == null) parent = new Frame();
        GPDialog gpd = new GPDialog(parent, title, 
            new String[] { msg }, new Component[] { new GridLine(GridLine.HORIZ) }, false);
        gpd.show();
    }
    
    public static boolean okCancelDialog(Frame parent, String title, String msg) {
        if (parent == null) parent = new Frame();
        GPDialog gpd = new GPDialog(parent, title, 
            new String[] { msg }, new Component[] { new Label("") });
        gpd.show();
        return gpd.ok;
    }
    
    public static String stringDialog(Frame parent, String title,
        String prompt, String preset, int width)
    {
        if (parent == null) parent = new Frame();
        TextField tf = new TextField(preset, width);
        GPDialog gpd = new GPDialog(parent, title, 
            new String[] { prompt }, new Component[] { tf });
        tf.requestFocus();
        gpd.show();
        if (gpd.ok)
            return tf.getText();
        else 
            return (String)null;
    }
    
    public static String stringDialog(Frame parent, String title, String prompt) {
        return stringDialog(parent, title, prompt, "", 30);
    }
    
    public static String stringDialog(Frame parent, String title, 
        String prompt, String preset) 
    {
        return stringDialog(parent, title, prompt, preset, 30);
    }
    
    public static int intDialog(Frame parent, String title, 
        String prompt, String preset) 
    {
        String s = stringDialog(parent, title, prompt, preset, 5);
        if (s == null) return Integer.MIN_VALUE;
        try {
            return (new Integer(s)).intValue();
        }
        catch(NumberFormatException e) {
            msgBox(parent, title, "Invalid integer: " + s + " - try again");
            return intDialog(parent, title, prompt, preset);
        }
    }

    public static int intDialog(Frame parent, String title, 
        String prompt, int preset)
    {
        return intDialog(parent, title, prompt, "" + preset);
    }
    
    public static int intDialog(Frame parent, String title, String prompt) {
        return intDialog(parent, title, prompt, "");
    }


    public static double doubleDialog(Frame parent, String title, 
        String prompt, String preset) 
    {
        String s = stringDialog(parent, title, prompt, preset, 15);
        if (s == null) return Double.NaN;
        try {
            return (new Double(s)).doubleValue();
        }
        catch(NumberFormatException e) {
            msgBox(parent, title, "Invalid number: " + s + " - try again");
            return doubleDialog(parent, title, prompt, preset);
        }
    }

    public static double doubleDialog(Frame parent, String title, 
        String prompt, double preset) 
    {
        return doubleDialog(parent, title, prompt, "" + preset);
    }
    
    public static double doubleDialog(Frame parent, String title, 
        String prompt) 
    {
        return doubleDialog(parent, title, prompt, "");
    }
    
}
