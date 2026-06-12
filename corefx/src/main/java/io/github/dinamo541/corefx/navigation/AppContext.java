/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import java.util.HashMap;

/**
 * Application context singleton for managing global application state.
 * Provides a thread-safe, lazy-initialized singleton instance that stores
 * key-value pairs accessible throughout the application lifecycle.
 * 
 * This class implements the Singleton pattern with double-checked locking
 * to ensure thread safety during initialization.
 * 
 * @author Carranza
 * @author Dominique
 * @version 2.3
 * @since 2026/06/10
 */
public class AppContext {

    private static class AppContextHolder {
        private static final AppContext INSTANCE = new AppContext();
    }

    private static final HashMap<String, Object> context = new HashMap<>();

    /**
     * Private constructor to prevent instantiation.
     * Use {@link #getInstance()} to obtain the singleton instance.
     */
    private AppContext() {
    }

    /**
     * Returns the singleton instance of AppContext.
     * Uses lazy initialization with double-checked locking for thread safety.
     * 
     * @return the singleton instance of AppContext
     */
    public static AppContext getInstance() {
        return AppContextHolder.INSTANCE;
    }

    /**
     * Stores a key-value pair in the application context.
     * 
     * @param key the key under which the value is stored
     * @param value the object to store
     */
    public void put(String key, Object value) {
        context.put(key, value);
    }

    /**
     * Retrieves a value from the application context by key.
     * 
     * @param key the key of the value to retrieve
     * @return the value associated with the key, or null if not found
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * Removes a key-value pair from the application context.
     * 
     * @param key the key to remove
     */
    public void remove(String key) {
        context.remove(key);
    }

    /**
     * Clears all key-value pairs from the application context.
     * 
     */
    public void clear() {
        context.clear();
    }

    /**
     * Checks if a key exists in the application context.
     * 
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public Boolean contains(String key) {
        return context.containsKey(key);
    }

    /**
     * Returns a string representation of this AppContext.
     * 
     * @return string representation including the context map contents
     */
    @Override
    public String toString() {
        return "AppContext{" +
                "context=" + context +
                '}';
    }

    /**
     * Computes the hash code for this AppContext.
     * 
     * @return hash code based on the context map
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(context);
    }

    /**
     * Compares this AppContext with another object for equality.
     * Two AppContext instances are equal if their context maps are equal.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AppContext other = (AppContext) obj;
        return java.util.Objects.equals(context, other.context);
    }

    /**
     * Prevents cloning of this singleton instance.
     * 
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of AppContext is not supported");
    }

}
