package rvl.util;

/**
 * Interface for any class that has a close() method
 * This is used for handling of fatal errors,
 * e.g., rvl.Utility.error
 */

public interface Closeable {
    public void close();
}