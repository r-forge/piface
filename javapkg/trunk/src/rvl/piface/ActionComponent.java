package rvl.piface;

/**
 * Interface for components such as buttons and menu items
 * that have no numerical value.  This interface has no
 * methods and is simply used by Piface to identify this
 * type of component.<br>
 * Use <tt>setLabel()</tt> and <tt>getLabel()</tt> to access the displayed
 * label. <br>
 * Use <tt>setName()</tt> and <tt>getName()</tt> to access the name of the
 * method that serves as the event handler for this component.
 * (The event handler must be <tt>public double</tt> with no arguments.)
 */

public interface ActionComponent extends PiComponent {
}
