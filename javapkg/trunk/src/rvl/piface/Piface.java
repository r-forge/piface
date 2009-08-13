package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import rvl.awt.*;
import rvl.stat.dist.*;
import rvl.util.*;
import rvl.piface.*;

/**
 * Abstract class for simple construction a graphical interface.
 * Subclasses need the following methods:
 * <pre>
 *      public void gui()       // to construct components
 *      public void click()     // default event handler
 *      public void <i>var</i>_changed  // event handler for <i>var</i>
 * </pre>
 * For convenience, several component-adding methods are
 * provided for use in gui().  These methods include slider(),
 * bar(), interval(), checkbox(), and choice() -- each in several
 * versions.  The methods add listeners for the components and
 * arrange them in column(s) in the order that they are called.
 * To start a new column, call newColumn().
 * The current column is a PiPanel named "panel", which may be
 * manipulated directly in
 * gui() as needed.
 *
 * The interface works like this:
 * Whenever a variable <i>var</i> is changed by user action, the
 * value of that variable is set accordingly in this object.
 * If a method named <i>var</i>_changed exists, it is called;
 * otherwise, click() is called as the default event handler.
 * Finally, all graphical components are updated to reflect the
 * new values of all the variables.
 *
 * A large collection of mathematical functions
 * are provided in this class, so that subclasses need not
 * prefix them (e.g., we may call "log" rather than "Math.log".)
 */
public abstract class Piface extends Frame
    implements ActionListener, WindowListener, rvl.util.Closeable
{
    public static final String version = "1.72 - 13 August 2009";
    private Stack subpanels = new Stack();
    private Component master = null;
    private boolean standalone = false;      // if true, will do System.exit when closed
    public double javaVersion = 1.0;

    protected Vector
        panels = new Vector(),  // panels in the gui
        actors = new Vector(),  // action components
        listeners = new Vector();   // PiListeners

    protected PiPanel panel;      // current panel
    protected MenuBar menuBar = new MenuBar();      // Menu bar
    protected Menu optMenu = new Menu("Options"),
        helpMenu = new Menu("Help");   // Options menu

    protected Font boldFont = new Font("Serif", Font.BOLD, 12),
        bigFont = new Font("Serif", Font.BOLD, 14);

    protected String actionSource = "init";  // name of variable that caused an evant
    protected int sourceIndex = -1;      // index if actionSource is an array

/**
 * Piface constructor
 */
    public Piface() {
        this("Piface dialog");
    }

/**
 * Piface constructor - puts title on top bar
 */
    public Piface(String title) {
        super(title);
        build();
        Utility.setGUIWarn(true);   // Warnings will go into a window
    }

/**
 * Piface constructor - puts title on top bar
 * conditionally builds GUI
 */
    public Piface(String title, boolean build) {
        super(title);
        if (build) build();
    }

    public static String getVersion() {
        return version;
    }

/**
 * Build the GUI.  Subclass constructors should either simply
 * call Piface's constructor, or set stuff up and then call build().
 */
    public void build() {
    try {
        Class.forName("java.awt.event.ActionListener");
        javaVersion = 1.1;
    }
    catch (ClassNotFoundException cnfe) {
            errmsg("<init>","JVM version " + javaVersion + " is too old."
           + "  Need at least version 1.1", true);
    }
/***** Old test for version, replaced by above section
        String vs = System.getProperty("java.version");
        int pos = vs.indexOf("1.");
        javaVersion = Utility.strtod(vs.substring(pos, pos + 3));
        if (Double.isNaN(javaVersion) || javaVersion < 1.0999) {
            errmsg("<init>","JVM version " + javaVersion + " is too old."
                + "  Need at least version 1.1", true);
        }
*****/
      // Set up menu items
        MenuItem graphMI = new MenuItem("Graph..."),
            quitMI = new MenuItem("Quit");
        graphMI.addActionListener(this);
        quitMI.addActionListener(this);
        setMenuBar(menuBar);
        menuBar.add(optMenu);
        menuBar.setHelpMenu(helpMenu);
        setBackground(new Color(220,220,255)); // default color

        newColumn();            // prepare the first panel
        optMenu.add(graphMI);   // before gui(), optMenu has "Graph..."

        beforeSetup();

        gui();      // call the routine that sets up the GUI
        click();    // initialize values using default handler
        updateVars();

      // Assemble the panels
        PiPanel //butPan = new PiPanel(new FlowLayout(FlowLayout.RIGHT)),
            mainPan = new PiPanel(new RVLayout(panels.size(),0,0,false,true));
        for (int i=0; i<panels.size(); i++)
            mainPan.add((PiPanel)panels.elementAt(i));

        optMenu.addSeparator();
        menuItem("postHocRant", "Post hoc power...", optMenu);
        menuItem("cohenRant", "Cohen's effect sizes...", optMenu);

        optMenu.addSeparator();
        optMenu.add(quitMI);

        helpMenu.addSeparator();
        menuItem("guiHelp", "GUI help", helpMenu);
        menuItem("aboutPiface", "About Piface", helpMenu);

        addWindowListener(this);

      // Assemble all and display
        setLayout(new BorderLayout());
        add(mainPan,"Center");

        afterSetup();

        pack();
        show();

        afterShow();
    }

/**
 * Display an error message in the form methName: desc.
 * If only want one-part message, call with desc=null
 * May override this as needed.
 */
    public void errmsg(String methName, String desc, boolean fatal) {
        String msg = desc==null
            ? methName
            : methName + ": " + desc;
        if (fatal)
            Utility.error(msg, this);
        else
            Utility.warning(msg);
    }
/**
 * Non-fatal error message
 */
    public void errmsg(String methName, String desc) {
        errmsg(methName, desc, false);
    }
/**
 * Non-fatal error message
 */
    public void errmsg(String msg) {
        errmsg(msg, (String)null, false);
    }
/**
 * Fatal error with stack trace
 */
    public void stackTrace(Throwable t) {
        Utility.error(t, this);
    }

/**
 * Close this GUI
 */
    public void close() {
        Enumeration enm = ((Vector)listeners.clone()).elements();
        while (enm.hasMoreElements()) {
            PiListener pil = (PiListener) enm.nextElement();
            pil.close();
        }
        dispose();
        if (standalone)
            System.exit(0);
    }

/**
 * Add an action-generating component to the current panel,
 * and register it as an ActionComponent for event handling by named method.
 * @param methodName java.lang.String -- name of event handler for this component
 * @param label java.awt.Component -- label assigned to component
 *     (this does not show anywhere in the GUI, but <code>actionSource</code>
 *     is set to this label and so it may be used to identify the event).
 * @param comp java.awt.Component -- component to be added
 */
    public void addComponent(String methodName, String label, Component comp) {
        panel.add(comp);
        PiActionAdapter paa = new PiActionAdapter(methodName, label, comp);
        paa.addActionListener(this);
    }

/**
 * Add an action-generating component.
 * Same as <code>addComponent(methodName, methodName, comp)</code>
 */
    public void addComponent(String methodName, Component comp) {
    addComponent(methodName, methodName, comp);
    }
/**
 * Add an action-generating component to the current panel,
 * add an ActionListener for it, and register the variable
 * for event handling.
 * @param name java.lang.String -- name of a double variable
 * @param comp rvl.piface.DoubleComponent
 * @param value double
 */
    public void addVar(String name, DoubleComponent comp, double value) {
        panel.add((Component)comp);
        comp.addActionListener(this);
        setVar(name, value);
        actors.addElement(comp);
    }

/**
 * Add an action-generating component to the current panel,
 * add an ActionListener for it, and register the variable
 * for event handling.
 * @param name java.lang.String -- name of an int variable
 * @param comp rvl.piface.IntComponent
 * @param value double
 */
    public void addVar(String name, IntComponent comp, int value) {
        panel.add((Component)comp);
        comp.addActionListener(this);
        setVar(name, value);
        actors.addElement(comp);
    }

/**
 * Required method -- default event-handler
 */
    public void click() {
        errmsg("click()", "Method needed to handle events.", false);
    }

/**
 * Required method -- define the gui using
 * calls to methods like addVar()
 */
    public void gui() {
        errmsg("gui()", "Method needed to do anything useful.");
    }

/**
* Called before setting up the GUI.
* Override to initialize variables or to remove menu items that
* are automatically added before calling gui()
*/
    protected void beforeSetup() {
    }

/**
* Called after setting up the GUI and just before showing it.
* Override to permit last-minute changes (e.g., removing menu items that
* are automatically added after calling gui()
*/
    protected void afterSetup() {
    }

/**
* Called after the GUI is actually shown.
* Override, for example, to change the size of the window.
*/
    protected void afterShow() {
    }

/**
 * Separate a string of the form name[index] into
 * an Object array of {String name, Integer index}
 */
    Object[] parseArray(String s) {
        int b = s.indexOf("["), e = s.indexOf("]");
        String name = s.substring(0,b);
        Integer index = new Integer(s.substring(b+1,e));
        return new Object[] { name, index };
    }

/**
 * Set a double variable to a value using reflection
 * @param name java.lang.String
 * @param value double
 */
    protected void setVar(String name, double value) {
        if (name.endsWith("]")) {
            setVar(parseArray(name), value);
            return;
        }
        try {
            Field field = getClass().getField(name);
            field.setDouble(this, value);
        }
        catch (Exception e) {
            errmsg("setVar(String, double)",
                "Can't set value of " + name);
            stackTrace(e);
        }
    }

/**
 * Get the value of a named double variable using reflection
 * Also accesses an array element if the name ~ "var[index]"
 */
    protected double getDVar(String name) {
        if (name.endsWith("]"))
            return getDVar(parseArray(name));
        try {
            Field field = getClass().getField(name);
            return field.getDouble(this);
        }
        catch (Exception e) {
            errmsg("getDVar(String)",
                "Can't get value of " + name);
            stackTrace(e);
        }
        return Double.NaN;
    }

/**
 * Set an element of a double[] variable to a value using reflection
 * @param info[] contains {String name, Integer index}
 * @param value double
 */
    protected void setVar(Object info[], double value) {
        String name = (String)info[0];
        int index = ((Integer)info[1]).intValue();
        try {
            Object x = getClass().getField(name).get(this);
            java.lang.reflect.Array.setDouble(x, index, value);
        }
        catch (Exception e) {
            errmsg("setVar(Object[], double)",
                "Can't set value of " + name + "[" + index
                + "]\n");
            stackTrace(e);
        }
    }

/**
 * Get an element of a named double[] variable using reflection
 */
    protected double getDVar(Object info[]) {
        String name = (String)info[0];
        int index = ((Integer)info[1]).intValue();
        try {
            Object x = getClass().getField(name).get(this);
            return java.lang.reflect.Array.getDouble(x, index);
        }
        catch (Exception e) {
            errmsg("getDVar(Object[])",
                "Can't get value of " + name + "[" + index
                    + "]\n");
            stackTrace(e);
        }
        return Double.NaN;
    }

/**
 * Set an integer variable to a value using reflection
 * @param name java.lang.String
 * @param value int
 */
    protected void setVar(String name, int value) {
        if (name.endsWith("]")) {
            setVar(parseArray(name), value);
            return;
        }
        try {
            Field field = getClass().getField(name);
            field.setInt(this, value);
        }
        catch (Exception e) {
            errmsg("setVar(String, int)",
                "Can't set value of " + name);
            stackTrace(e);
        }
    }


/**
 * Get the value of a named int variable using reflection
 */
    protected int getIVar(String name) {
        if (name.endsWith("]"))
            return getIVar(parseArray(name));
        try {
            Field field = getClass().getField(name);
            return field.getInt(this);
        }
        catch (Exception e) {
            errmsg("getIVar(String)",
                "Can't get value of " + name);
            stackTrace(e);
        }
        return -9999;
    }

/**
 * Set an element of an int[] variable to a value using reflection
 * @param info[] contains {String name, Integer index}
 * @param value double
 */
    protected void setVar(Object info[], int value) {
        String name = (String)info[0];
        int index = ((Integer)info[1]).intValue();
        try {
            Object x = getClass().getField(name).get(this);
            java.lang.reflect.Array.setInt(x, index, value);
        }
        catch (Exception e) {
            errmsg("setVar(Object[], int)",
                "Can't set value of " + name + "[" + index + "]");
            stackTrace(e);
        }
    }

/**
 * Get an element of a named int[] variable using reflection
 */
    protected int getIVar(Object info[]) {
        String name = (String)info[0];
        int index = ((Integer)info[1]).intValue();
        try {
            Object x = getClass().getField(name).get(this);
            return java.lang.reflect.Array.getInt(x, index);
        }
        catch (Exception e) {
            errmsg("getIVar(Object[])",
                "Can't get value of " + name + "[" + index + "]");
            stackTrace(e);
        }
        return -9999;
    }


    /**
     * Set a local variable, if it exists, to the same value as it
     * has in the Piface object piface
     */
    protected void setVar(String varName, Piface piface) {
    if (varName.endsWith("]")) {
        Object[] array = parseArray(varName);
        String name = (String)(array[0]);
        try {
        Field fld = getClass().getField(name);
        Class typ = fld.getType();
        if (typ == double[].class)
            setVar(array, piface.getDVar(varName));
        else if (typ == int[].class)
            setVar(array, piface.getIVar(varName));
        return;
        }
        catch (Exception arrayex) {
        return;
        }
    }
    try {
        Field field = getClass().getField(varName);
        Class type = field.getType();
        if (type == double.class)
        setVar(varName, piface.getDVar(varName));
        else if (type == int.class)
        setVar(varName, piface.getIVar(varName));
        return;
    }
    catch (Exception e) {
        return;
    }
    }

/**
 * Calls public void <i>MethodName</i>()
 */
    public void callMethod(String methodName) {
        try {
            Method handler = getClass().getMethod(methodName, (Class<?>[])null);
            handler.invoke(this, (Object[])null);
        }
        catch (InvocationTargetException ite) {
            errmsg("callMethod(\"" + methodName + "\")", ite.toString(), false);
            ite.getTargetException().printStackTrace();
        }
        catch (Throwable e) {
            errmsg("callMethod(\"" + methodName + "\")", e.toString(), false);
            stackTrace(e);
        }
    }



/**
 * Calls method <name>_changed() if it exists,
 * otherwise calls click()
 */
    public void callMethodFor(String name) {
        if (name.endsWith("]")) {
            Object obj[] = parseArray(name);
            actionSource = (String)obj[0];
            sourceIndex = ((Integer)obj[1]).intValue();
        }
        else {
            actionSource = name;
            sourceIndex = -1;
        }
        try {
            Method handler = getClass().getMethod(actionSource + "_changed", (Class<?>[])null);
            handler.invoke(this, (Object[])null);
        }
        catch (NoSuchMethodException nsme) {
            click();
        }
        catch (InvocationTargetException ite) {
            errmsg("callMethodFor(\"" + name + "\")", ite.toString(), false);
            ite.getTargetException().printStackTrace();
        }
        catch (Exception e) {
            errmsg("callMethodFor(\"" + name + "\")", e.toString(), false);
            stackTrace(e);
        }
    }

/**
 * Update values of all components to current values
 */
    public void updateVars() {
        Enumeration enm = actors.elements();
        while (enm.hasMoreElements()) {
            Object elt = enm.nextElement();
            if (elt instanceof DoubleComponent) {
                DoubleComponent dc = (DoubleComponent) elt;
                dc.setValue(getDVar(dc.getName()));
            }
            else if (elt instanceof IntComponent) {
                IntComponent ic = (IntComponent) elt;
                ic.setValue(getIVar(ic.getName()));
            }
            else
                errmsg("updateVars()",
                    "Unsupported type: " + elt.getClass());
        }
    }



// ==== Layout methods for use in gui() ==== //

/**
 * Add a new panel to the gui
 */
    public void newColumn() {
        panel = new PiPanel(new RVLayout(1,false,true));
        panels.addElement(panel);
    }
    public void beginSubpanel(int columns) {
        subpanels.push(panel);
        panel = new PiPanel(new RVLayout(columns,false,true));
    }
    public void beginSubpanel(int columns, Color borderColor) {
        subpanels.push(panel);
        panel = new PiPanel(new RVLayout(columns,false,true));
        border(borderColor);
    }
    public void beginSubpanel(int columns, boolean raised) {
        subpanels.push(panel);
        panel = new PiPanel(new RVLayout(columns,false,true));
        set3D(raised);
    }
    public void endSubpanel() {
        if (subpanels.empty())
            errmsg("endSubpanel()", "Subpanel stack is empty", true);
        PiPanel p = panel;
        panel = (PiPanel) subpanels.pop();
        panel.add(p);
    }
    public void border(Color color) {
        panel.setBorderColor(color);
        panel.setBorderType(PiPanel.PLAIN_BORDER);
    }
    public void set3D(boolean raised) {
        panel.setBorderColor(null);
        panel.setBorderType(raised ? PiPanel.RAISED
            : PiPanel.LOWERED);
    }
    public void filler() {
        RVLayout layout = (RVLayout)panel.getLayout();
        layout.vertFill(panel);
        panel.setStretchable(true);
    }

    public void postHocRant() {
        // showText(Piface.class, "PostHocPower.txt",
        //     "Post hoc power", 25, 60);
        rvl.piface.apps.RetroPower dumbDialog = new rvl.piface.apps.RetroPower();
        dumbDialog.setMaster(this);
    }

    public void cohenRant() {
        showText(Piface.class, "Cohen.txt",
            "Cohen's effect sizes", 25, 60);
    }

    public void guiHelp() {
        showText(Piface.class, "PifaceHelp.txt",
            "Piface Help", 25, 50);
    }

    public void aboutPiface() {
        new AboutPiface();
    }

/**
* Display text in a separate window
*/
    public ViewWindow showText(String text, String title, int rows, int cols) {
        ViewWindow vw = new ViewWindow(title, rows, cols);
        vw.ta.setVisible(false);
        vw.setText(text);
        vw.setTop();
        vw.ta.setVisible(true);
        return vw;
    }

/**
* Display text from a file in a separate window
* The file should be in the same directory as clas
*/
    public ViewWindow showText(Class clas, String filename,
        String title, int rows, int cols)
    {
        ViewWindow vw = new ViewWindow(title,rows,cols);
        try {
            vw.ta.setVisible(false);
            InputStream in = clas.getResourceAsStream(filename);
            BufferedReader br = new BufferedReader
                (new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null)
                vw.append(line + "\n");
            in.close();
            vw.setTop();
            vw.ta.setVisible(true);
        }
        catch (Exception e) {
            errmsg("showText(Class,String,title,int,int)",
                "Can't display \"" + filename + "\"", false);
        }
        return vw;
    }

/**
 * @return the value for xName such that yName's value is
 * equal to target.  The starting values used by the numerical
 * method are xStart and xStart + xIncr.
 * This places no restrictions on the solution.
 * To place restrictions
 * or control on solutions, first create a PifaceAux object,
 * place the restrictions, and call solve(PifaceAux, ...)
 */
    public double solve(String xName, String yName, double target,
        double xStart, double xIncr)
    {
        PifaceAux pfa = new PifaceAux(xName, yName, this);
        return solve(pfa, target, xStart, xIncr);
    }

/**
 * This is exactly like solve(String, String, ...) except
 * it is used with a previously constructed PifaceAux object.
 */
    public double solve(PifaceAux pfa, double target,
        double xStart, double xIncr)
    {
        double saved[] = saveVars();
        double sol = Solve.search(pfa, target, xStart, xIncr);
        restoreVars(saved);
        return sol;
    }

/**
 * @return the Component associated with the named variable.
 * Note it's possible that more than one such component
 * exists; if so, we return the first one found.
 */
    public PiComponent getComponent(String name) {
        Enumeration enm = actors.elements();
        while (enm.hasMoreElements()) {
            PiComponent pic = (PiComponent)enm.nextElement();
            if (pic.getName().equals(name)) return pic;
        }
        return null;
    }

/**
 * Make the named component visible or invisible
 */
    public void setVisible(String name, boolean v) {
        ((Component)getComponent(name)).setVisible(v);
    }

/**
 * Re-label a component
 */
    public void relabel(String name, String newLabel) {
        PiComponent pic = getComponent(name);
        pic.setName(name, newLabel);
    }

// ==== Component-adding methods for use in gui() ==== //

/**
 * Labels
 */
    public void label(String label, Font font) {
        Label lbl = new Label(label);
        lbl.setFont(font);
        panel.add(lbl);
    }
    public void label(String label) {
        label(label, bigFont);
    }

/**
 * Sliders
 */
    public void slider(String name, String label, double value,
        double minVal, double maxVal, int digits,
        boolean hardMin, boolean hardMax, boolean editable)
    {
        PiSlider pisl = new PiSlider(name, label, value);
        pisl.setMinimum(minVal);
        pisl.setMaximum(maxVal);
        pisl.setMutable(!hardMin, !hardMax);
        pisl.setDigits(digits);
        pisl.setEditable(editable);
        addVar(name, pisl, value);
    }
    public void slider(String name, String label, double value, int digits) {
        PiSlider pisl = new PiSlider(name, label, value);
        pisl.setDigits(digits);
        addVar(name, pisl, value);
    }
    public void slider(String name, String label, double value) {
        slider(name, label, value, 4);
    }
    public void slider(String name, double value, int digits) {
        slider(name, name, value, digits);
    }
    public void slider(String name, double value) {
        slider(name, name, value);
    }
    public void bar(String name, String label, double value, int digits) {
        if (value > 0)
            slider(name,label,value, 0.0, 1.5*value, digits, true, false, true);
        else if (value < 0)
            slider(name,label,value, 1.5*value, 0.0, digits, false, true, true);
        else
            slider(name,label,value, 0.0, 1.0, digits, true, false, true);
    }
    public void bar(String name, String label, double value) {
        bar(name, label, value, 4);
    }
    public void bar(String name, double value) {
        bar(name, name, value);
    }
    public void bar(String name, double value, int digits) {
        bar(name, name, value, 4);
    }
    public void interval(String name, String label,
        double value, double minVal, double maxVal)
    {
        slider(name, label, value, minVal, maxVal, 4, true, true, true);
    }
    public void interval(String name, double value, double minVal, double maxVal) {
        interval(name, name, value, minVal, maxVal);
    }
  // output-only versions...
    public void oslider(String name, String label, double value, int digits) {
        PiSlider pisl = new PiSlider(name, label, value);
        pisl.setDigits(digits);
        pisl.setEditable(false);
        addVar(name, pisl, value);
    }
    public void oslider(String name, double value, int digits) {
        oslider(name, name, value, digits);
    }
    public void oslider(String name, String label, double value) {
        oslider(name, label, value, 4);
    }
    public void oslider(String name, double value) {
        oslider(name, name, value);
    }
    public void obar(String name, String label, double value, int digits) {
        if (value > 0)
            slider(name,label,value, 0.0, 1.5*value, digits, true, false, false);
        else if (value < 0)
            slider(name,label,value, 1.5*value, 0.0, digits, false, true, false);
        else
            slider(name,label,value, 0.0, 1.0, digits, true, false, false);
    }
    public void obar(String name, String label, double value) {
        obar(name, label, value, 4);
    }
    public void obar(String name, double value) {
        obar(name, name, value);
    }
    public void obar(String name, double value, int digits) {
        obar(name, name, value, digits);
    }
    public void ointerval(String name, String label,
        double value, double minVal, double maxVal)
    {
        slider(name, label, value, minVal, maxVal, 4, true, true, false);
    }
    public void ointerval(String name, double value, double minVal, double maxVal) {
        ointerval(name, name, value, minVal, maxVal);
    }

    /**
     * Value-entry fields
     */
    public void field(String name, String label, double value,
            int width, int digits)
    {
        PiDoubleField df = new PiDoubleField(name, label, value,
            width, digits);
        addVar(name, df, value);
    }
    public void field(String name, String label, double value) {
        PiDoubleField df = new PiDoubleField(name, label, value);
        addVar(name, df, value);
    }
    public void field(String name, double value) {
        field(name, name, value);
    }
    // output-only versions
    public void ofield(String name, String label, double value,
            int width, int digits)
    {
        PiDoubleField df = new PiDoubleField(name, label, value,
            width, digits);
        df.setEditable(false);
        addVar(name, df, value);
    }
    public void ofield(String name, String label, double value) {
        PiDoubleField df = new PiDoubleField(name, label, value);
        df.setEditable(false);
        addVar(name, df, value);
    }
    public void ofield(String name, double value) {
        ofield(name, name, value);
    }

    // output labels - similar to ofield() but look like labels
    public void otext(String name, String label, double value,
        int digits)
    {
        PiDoubleText dt = new PiDoubleText(name, label, value, digits);
        dt.setFont(boldFont);
        addVar(name, dt, value);
    }

    public void otext(String name, String label, double value) {
        otext(name, label, value, 4);
    }


/**
 * Array fields -- enter lists of numbers
 * This works like, e.g., a button.  You supply a method to
 * invoke when it is called.  That method is responsible for
 * retrieving the value using double [] getValue().
 */
    public PiArrayField arrayField
        (String method, String label, double value[], int width)
    {
        PiArrayField paf = new PiArrayField(method, label, value, width);
        paf.addActionListener(this);
        panel.add(paf);
        return paf;
    }
    public PiArrayField arrayField (String method, String label, double value[]) {
        return arrayField(method, label, value, 12);
    }

    /**
     * Dotplot field -- works like arrayField but uses a graphical style
     * of inputting data.  You supply a method to invoke when a dot is moved.
     * That method is responsible for retrieving the value using
     * double [] getValue().
     * Note: If label is "", the display takes up less vertical space.
     */
    public PiDotplot dotplot
        (String method, String label, double value[])
    {
    PiDotplot pd = new PiDotplot(method, label, value);
    pd.addActionListener(this);
    panel.add(pd);
    return pd;
    }


/**
 * Checkboxes
 */
    public void checkbox(String name, String label, int value) {
        value = (value == 0) ? 0 : 1;
        PiCheckbox cb = new PiCheckbox(name, label, value);
        addVar(name, cb, value);
    }
    public void checkbox(String name, int value) {
        checkbox(name, name, value);
    }

/**
 * Checkbox menu items
 */
    public void menuCheckbox(String name, String label, int value, Menu menu) {
        value = (value == 0) ? 0 : 1;
        PiMenuCheckbox mcb = new PiMenuCheckbox(name, label, value);
        menu.add(mcb);
        mcb.addActionListener(this);
        setVar(name, value);
        actors.addElement(mcb);
    }
    public void menuCheckbox(String name, String label, int value) {
        menuCheckbox(name, label, value, optMenu);
    }
    public void menuCheckbox(String name, int value, Menu menu) {
        menuCheckbox(name, name, value, menu);
    }
    public void menuCheckbox(String name, int value) {
        menuCheckbox(name, name, value, optMenu);
    }

/**
 * Choices (drop-down lists)
 * Note that these come in 2 varieties.  When the items[] argument
 * is String[], the associated variable is an integer equal to
 * the index (0-based) of the selected item.  When items[] is
 * double[], the associated variable is a double equal to the
 * selected value (provide the index of the desired starting value).
 * If the code attempts to set a double choice variable, the closest
 * one in items[] is used.
 */
    public void choice(String name, String label, String items[], int value) {
        PiChoice ch = new PiChoice(name, label, items, value);
        addVar(name, ch, value);
    }
    public void choice(String name, String items[], int value) {
        choice(name, name, items, value);
    }
    public void choice(String name, String label, double items[], int index) {
//        if (javaVersion < 1.2) {
            PiNumChoice ch = new PiNumChoice(name, label, items, index);
            addVar(name, ch, items[index]);
/**** Doesn't seem to work right all the time -- exclude for now
        }
        else {
            PiNumCombo ch = new PiNumCombo(name, label, items, index);
            addVar(name, ch, items[index]);
        }
*******/
    }
    public void choice(String name, double items[], int index) {
        choice(name, name, items, index);
    }

/**
 * Radio buttons - value is index (0-based) of selected button
 * Usually you'll want hradio() or vradio() for horizontal
 * or vertical arrangements; but you may use radio() to
 * specify something in between.  An empty string ("") makes
 * a blank position in the grid; for example, if items[] =
 * {"","choice 1","choice 2"} and columns = 2, the buttons will
 * be arranged in a row below the label.  SKIP all blank entries
 * when figuring the index of a button; in the above example,
 * "choice 1" corresponds to value 0.
 */
    public void radio(String name, String label, String items[],
        int value, int columns)
    {
        PiRadio r = new PiRadio(name, label, items, value, columns);
        addVar(name, r, value);
    }
    public void hradio(String name, String label, String items[], int value) {
        radio(name, label, items, value, 1 + items.length);
    }
    public void hradio(String name, String items[], int value) {
        hradio(name, name, items, value);
    }
    public void vradio(String name, String label, String items[], int value) {
        radio(name, label, items, value, 1);
    }
    public void vradio(String name, String items[], int value) {
        vradio(name, name, items, value);
    }


// Buttons and menu items
    public void button(String methodName, String label) {
        PiButton pib = new PiButton(methodName, label);
        pib.addActionListener(this);
        panel.add(pib);
    }
      public void menuItem(String methodName, String label, Menu menu) {
          PiMenuItem pimi = new PiMenuItem(methodName, label);
          pimi.addActionListener(this);
          menu.add(pimi);
    }
    public void menuItem(String methodName, String label) {
        menuItem(methodName, label, optMenu);
    }


// ====== METHODS TO ADD AN ARBITRARY COMPONENT ===== //

    /**
     * Identical to <code>addComponent</code>
     * @see #addComponent
     */
    public void component(String methodName, String label, Component comp) {
        addComponent(methodName, label, comp);
    }

    /**
     * Identical to <code>addComponent</code>
     * @see #addComponent
     */
    public void component(String methodName, Component comp) {
        addComponent(methodName, methodName, comp);
    }

// ===== METHODS PRIMARILY TO SUPPORT PiListeners ===== //
    public void addPiListener(PiListener pil) {
        listeners.addElement(pil);
    }
    public void removePiListener(PiListener pil) {
        listeners.removeElement(pil);
    }

/**
* Notify all the listeners of a value change
* Current variables are saved and restored before and after
* each call to keep things orderly
*/
    public synchronized void notifyListeners (String varName) {
        notifyListeners(varName, null);
    }

/**
* Notify all the listeners of a value change
* Current variables are saved and restored before and after
* each call to keep things orderly.
* If any registered PiListener is the same as caller, it is not notified
* (presumably the caller already knows!).
*/
    public synchronized void notifyListeners (String varName, PiListener caller) {
        Enumeration enm = listeners.elements();
        while (enm.hasMoreElements()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PiListener pil = (PiListener) enm.nextElement();
            if (!(pil == caller)) pil.piAction (varName);
            setCursor(Cursor.getDefaultCursor());
        }
    }


/**
* Returns all local variables
* -- so that we can later restore everything using restoreVars()
*/

    public synchronized double[] saveVars() {
        double x[] = new double[actors.size()];
        for (int i=0; i<actors.size(); i++) {
            PiComponent obj = (PiComponent) actors.elementAt(i);
            x[i] = (obj instanceof DoubleComponent)
                ? getDVar(obj.getName())
                : (double)getIVar(obj.getName());
        }
        return x;
    }

    public synchronized void restoreVars(double saved[]) {
        for (int i=0; i<actors.size(); i++) {
            PiComponent obj = (PiComponent)actors.elementAt(i);
            if (obj instanceof DoubleComponent) {
                setVar(obj.getName(), saved[i]);
                ((DoubleComponent)obj).setValue(saved[i]);
            }
            else {
                setVar(obj.getName(), (int)saved[i]);
                ((IntComponent)obj).setValue((int)saved[i]);
            }
        }
    }


/**
* @return value of variable named yName after processing with
* variable named xName set to xVal.  Normally, should call saveVars()
* before calling this, and call restoreVars() after finishing.
*/
    protected double eval (String yName, String xName, double xVal) {
        setVar(xName, xVal);
        callMethodFor(xName);
        return getDVar(yName);
    }

/**
* Set master to p.  Affects behavior on closing.
* If master is null, close() calls System.exit; otherwise,
* it is assumed that the master will take care of the problem
* if it's all that fatal.
*
* RVL 2-2-09:
* I think this setMaster and getMaster stuff is not used anymore,
* Instead I use a new boolean 'standalone' that is meant to
* be set true only if you actually *want* it to exit when closed
* but I'm leaving it in for now just
*/
    public void setMaster(Component p) {
        master = p;
    }
/**
* @return master component
*/
    public Component getMaster() {
        return master;
    }

/**
 * Make this a standalone app if <code>true</code>.
 * That means that when you quit, it will exit
 * using <code>System.exit(0)</code>.
 * If not called, we are not in standalone operation.
 */
    public void setStandalone(boolean flag) {
        standalone = flag;
    }

// ===== EVENT HANDLERS =====

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof DoubleComponent) {
            String name = ((DoubleComponent) src).getName();
            double x = ((DoubleComponent) src).getValue();
        if (feq(x,getDVar(name))) return;
        setVar(name, x);
            callMethodFor(name);
            updateVars();
            notifyListeners(name);
            return;
        }
        if (src instanceof IntComponent) {
            String name = ((IntComponent) src).getName();
        int ix = ((IntComponent) src).getValue();
            if (ix == getIVar(name)) return;
        setVar(name, ix);
            callMethodFor(name);
            updateVars();
            notifyListeners(name);
            return;
        }
        if (src instanceof ActionComponent) {
            String methodName = ((ActionComponent) src).getName();
            actionSource = ((ActionComponent) src).getLabel();
            callMethod(methodName);
            updateVars();
            notifyListeners(methodName);
            return;
        }
        String cmd = e.getActionCommand().toString();
        if (cmd.equals("Graph..."))
            new PiGraph(this);
        else if (cmd.equals("Quit"))
            close();
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowActivated(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }


// ==== "intrinsic" functions available to subclasses ===== //
    private static final double rlog10 = 1 / Math.log(10);

    public static double sin(double x) { return Math.sin(x); }
    public static double cos(double x) { return Math.cos(x); }
    public static double tan(double x) { return Math.tan(x); }
    public static double asin(double x) { return Math.asin(x); }
    public static double acos(double x) { return Math.acos(x); }
    public static double atan(double x) { return Math.atan(x); }
    public static double atan(double x, double y) { return Math.atan2(x,y); }
    public static double atan2(double x, double y) { return Math.atan2(x,y); }
    public static double log(double x) { return Math.log(x); }
    public static double log10(double x) { return rlog10 * Math.log(x); }
    public static double exp(double x) { return Math.exp(x); }
    public static double sqrt(double x) { return Math.sqrt(x); }
    public static long round(double x) { return Math.round(x); }
    public static double round(double x, int places) {
        double m = Math.pow(10,places);
        return Math.round(x * m) / m;
    }
    public static double pow(double x, double e) { return Math.pow(x,e); }
    public static int abs(int x) { return Math.abs(x); }
    public static double abs(double x) { return Math.abs(x); }
    public static double floor(double x) { return Math.floor(x); }
    public static double ceil(double x) { return Math.ceil(x); }
    public static double max(double x, double y) { return Math.max(x,y); }
    public static double min(double x, double y) { return Math.min(x,y); }
    public static int max(int x, int y) { return Math.max(x,y); }
    public static int min(int x, int y) { return Math.min(x,y); }
    public static int sign(double x) { return (x == 0) ? 0 : (x < 0) ? -1 : 1; }
    public static boolean feq(double x, double y, double eps) {
    return (abs(x-y) < eps);
    }
    public static boolean feq(double x, double y) {
    return feq(x,y,1e-12);
    }

    public static double time() { return System.currentTimeMillis()/1000.0; }

// Random numbers
    private static double theSeed = -1,
                          theMult = 63069,
                          theTerm = .84763521;
    private static boolean flag = true;
    private static double u1, u2, r;

/**
 ** @return a U(0,1) random number
 **/
    public static double random () {
        if (theSeed < 0) seed();      // Initialize using current time
        double  x = theMult*theSeed + theTerm;
        theSeed = x - Math.floor(x);
        return theSeed;
    }
/**
 ** @return a N(0,1) random number
 **/
    public synchronized static double nrand() {
        flag = !flag;
        if (flag)
            return r*u2;

        do {
            u1 = 2*random() - 1;
            u2 = 2*random() - 1;
            r = u1*u1 + u2*u2;
        } while (r >= 1.0);

        r = Math.sqrt(-2 * Math.log(r) / r);
        return r*u1;
    }
/**
 *  Initialize with given seed
 */
    public static double seed (double s) {
        theSeed = s;
        return s;
    }
/**
 *  Initialize with system clock
 */
    public static double seed () {
        theSeed = (System.currentTimeMillis() & 0xFFFFL) / 65536.0;
        return theSeed;
    }

}
