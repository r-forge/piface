package rvl.piface;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

import rvl.piface.*;

/**
 * The purpose of this class is to make it easy to write a Piface-style
 * event handler for any Component that creates an ActionEvent.
 */
public class PiActionAdapter
    implements ActionComponent, ActionListener
{

    private String methodName, label;
    private transient ActionListener actionListener = null;

    /**
     * Constructor
     * @param methodName The <code>Piface</code> method that is called
     *        when an action is generated
     * @param label  Available to even handler via <code>getLabel()</code>
     * @param component The component to register as an
     *        <code>ActionComponent</code>.
     * @see #getLabel
     */
    public PiActionAdapter(String methodName, String label,
			   Component component)
    {
        setName(methodName, label);
	// Would like to call component.addActionListener(this), but
	// Components aren't guaranteed to have this method.  So we
	// have to do it using reflection
		try {
			Method aal = component.getClass()
			.getMethod("addActionListener",
				   new Class[]{ActionListener.class});
			aal.invoke(component, new Object[]{this});
		}
		catch (Exception e) { // InvocationTargetE., NoSuchMethodE., IllegalAccessE.
			System.err.println("Can't register component:\n"
					   + component + "\n" + e);
		}
    }

    /**
     * Simplified constructor where the label is set equal to
     * <code>methodName</code>
     */
    public PiActionAdapter(String methodName, Component component) {
	    this (methodName, methodName, component);
    }

    public String getName() {
        return methodName;
    }

    public String getLabel() {
        return label;
    }

    public void setName(String methodName, String label) {
        this.methodName = methodName;
        this.label = label;
    }

    public void addActionListener(ActionListener al) {
        actionListener = AWTEventMulticaster.add(actionListener, al);
    }

    public void actionPerformed(ActionEvent ae) {
	    ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label);
        actionListener.actionPerformed(ae);
    }

}
