package rvl.piface;

/**
* Basic interface for any accessories that might be added to
* Piface dialogs.  Implementations should call
* piface.addListener(this) where piface is the Piface object we
* want to get messages from.
*/

public interface PiListener
{

/**
* This is called when the value of the variable "varName"
* is changed in the Piface
*/
    void piAction(String varName);

/**
* Host routine should call this if it wants to close it
*/
    void close();
}