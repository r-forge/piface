package rvl.piface.apps;

import rvl.piface.*;
import rvl.awt.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class AnovaPicker extends Piface {

    private static String
        title = "Select an ANOVA model"; // title of the dialog

    public int isRep, preDef;
    public double reps; // not actually used...
    protected TextField ttlFld, modFld, levFld, randFld;
    protected Label noteLbl;
    protected PiDoubleField repFld;
    protected Vector builtIns;

    protected String[] setup() {
        Properties prop = new Properties();
        try {
            ttlFld = new TextField(50);
            modFld = new TextField();
            levFld = new TextField();
            randFld = new TextField();
            repFld = new PiDoubleField("reps","Observations per factor combination",1.0);
            noteLbl = new Label("");
            noteLbl.setFont(new Font("SanSerif",Font.PLAIN,10));

            // Setup choices from property file "AnovaPicker.txt"
            builtIns = new Vector();
            // "roll your own" entry...
            builtIns.addElement(new String[] {"","","","","",""});
            prop.load(AnovaPicker.class.getResourceAsStream("AnovaPicker.txt"));
            String keylist = prop.getProperty("keys");
            StringTokenizer st = new StringTokenizer(keylist,",");
            while (st.hasMoreTokens()) {
                String key = st.nextToken();
                builtIns.addElement( new String[] {
                    prop.getProperty(key),
                    prop.getProperty(key+".model",""),
                    prop.getProperty(key+".levels",""),
                    prop.getProperty(key+".random",""),
                    prop.getProperty(key+".reps",""),
                    prop.getProperty(key+".note","")
                } );
            }
            String choices[] = new String[builtIns.size()];
            choices[0] = "(Define your own)";
            for (int i=1; i<builtIns.size(); i++)
                choices[i] = ((String[])builtIns.elementAt(i))[0];
            return choices;
        }
        catch (IOException io) {
            errmsg("AnovaPicker","Can't load resource file",true);
            return null;
        }
    }


/**
 * Set up the GUI
 */
    public void gui () {
        setBackground(new Color(215,235,235));
        String choices[] = setup();
        choice("preDef", "Built-in models", choices, 1);
        beginSubpanel(2);
            panel.setLayout(new BorderLayout());
            panel.add(new Label("     "),"West");
            panel.add(noteLbl,"Center");
        endSubpanel();
        beginSubpanel(2,false);
            label("Title");  panel.add(ttlFld);
            label("Model");  panel.add(modFld);
            label("Levels");  panel.add(levFld);
            label("Random factors");  panel.add(randFld);
            checkbox("isRep","Replicated",0); panel.add(repFld);
        endSubpanel();
        beginSubpanel(1);
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            label("Study the power of...");
            button("compDialog","Differences/Contrasts");
            button("fDialog","F tests");
        endSubpanel();
        repFld.addActionListener(this);
        menuItem("localHelp", "How to use this dialog", helpMenu);
	preDef_changed();
    }

    protected void afterSetup() {
        optMenu.remove(0); // graph...
        optMenu.remove(0); // separator
        helpMenu.remove(2); // GUI help
    }


/**
 * Default event handler- does nothing
 */
    public void click() {
    }

    public void preDef_changed() {
        String pick[] = (String[])builtIns.elementAt(preDef);
        ttlFld.setText(pick[0]);
        modFld.setText(pick[1]);
        levFld.setText(pick[2]);
        randFld.setText(pick[3]);
        noteLbl.setText(pick[5]);
        isRep = pick[4].equals("")? 0 : 1;
        if (isRep != 0)
            repFld.setValue((new Integer(pick[4])).doubleValue());
        isRep_changed();
    }

    public void isRep_changed() {
        if (isRep==0) {
            repFld.setVisible(false);
        }
        else
            repFld.setVisible(true);
    }

    // ignore repFld events
    public void rep_changed() {
    }

    public void fDialog() {
        int reps = (isRep == 1) ? (int) repFld.getValue() : 0;
        AnovaGUI ag = new AnovaGUI(ttlFld.getText(),modFld.getText(),
            reps, levFld.getText(), randFld.getText());
        ag.setMaster(this);
    }

    public void compDialog() {
        int reps = (isRep == 1) ? (int) repFld.getValue() : 0;
        AnovaCompGUI ag = new AnovaCompGUI(ttlFld.getText(),modFld.getText(),
            reps, levFld.getText(), randFld.getText());
        ag.setMaster(this);
    }

    public void localHelp() {
        showText(AnovaPicker.class, "AnovaPickerHelp.txt",
            "Specifying an ANOVA scenario", 25, 60);
    }

/**
 * The following code makes it self-standing...
 */
    public AnovaPicker() {
        super(title);
    }
    public static void main(String argv[]) {
        new AnovaPicker();
    }

/**
* Needed to handle (actually, ignore) key event from repFld
*/
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
    }

}
