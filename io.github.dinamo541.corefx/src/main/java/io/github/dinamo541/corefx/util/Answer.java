/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.util
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.dinamo541.corefx.util;

import java.util.HashMap;

/**
 * Generic response object for application operations.
 * Contains information about the operation status, messages,
 * and result data in a hash map.
 * 
 * @author Carranza
 * @author Dominique
 * @version 2.1
 * @since 2026/06/10
 */
public class Answer implements java.io.Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;
    
    private Boolean state;
    private String message;
    private String internalMessage;
    private HashMap<String, Object> result;

    /**
     * No-argument constructor. Initializes the result map as empty.
     */
    public Answer() {
        this.result = new HashMap<>();
    }

    /**
     * Constructor with state and messages.
     * 
     * @param state operation state
     * @param message message for the user
     * @param internalMessage internal message for logs/debugging
     */
    public Answer(Boolean state, String message, String internalMessage) {
        this.state = state;
        this.message = message;
        this.internalMessage = internalMessage;
        this.result = new HashMap<>();
    }
    
    /**
     * Constructor with state, messages, and an initial result.
     * 
     * @param state operation state
     * @param message message for the user
     * @param internalMessage internal message for logs/debugging
     * @param name result key name
     * @param result result value
     */
    public Answer(Boolean state, String message, String internalMessage, String name, Object result) {
        this.state = state;
        this.message = message;
        this.internalMessage = internalMessage;
        this.result = new HashMap<>();
        this.result.put(name, result);
    }
    
    /**
     * Gets the operation state.
     * 
     * @return true if operation was successful, false otherwise
     */
    public Boolean getState() {
        return state;
    }

    /**
     * Sets the operation state.
     * 
     * @param state new state
     */
    public void setState(Boolean state) {
        this.state = state;
    }

    /**
     * Gets the message for the user.
     * 
     * @return descriptive message of the operation
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message for the user.
     * 
     * @param message new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the internal message for logs or debugging.
     * 
     * @return internal message with technical details
     */
    public String getInternalMessage() {
        return internalMessage;
    }

    /**
     * Sets the internal message.
     * 
     * @param internalMessage new internal message
     */
    public void setInternalMessage(String internalMessage) {
        this.internalMessage = internalMessage;
    }
    
    /**
     * Gets a result from the map by key name.
     * 
     * @param name key name
     * @return the stored object, or null if not found
     */
    public Object getResult(String name) {
        return result.get(name);
    }

    /**
     * Adds or updates a result in the map.
     * 
     * @param name key name
     * @param result value to store
     */
    public void setResult(String name, Object result) {
        this.result.put(name, result);
    }

    /**
     * Gets all stored results.
     * 
     * @return map with all results
     */
    public HashMap<String, Object> getResults() {
        return result;
    }

    /**
     * Returns a string representation of the Answer object.
     * 
     * @return string representation of the Answer
     */
    @Override
    public String toString() {
        return "Answer{" +
                "state=" + state +
                ", message='" + message + '\'' +
                ", internalMessage='" + internalMessage + '\'' +
                ", result=" + result +
                '}';
    }

    /**
     * Overrides hashCode for proper hashing based on fields.
     * 
     * @return hash code of the object
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(state, message, internalMessage, result);
    }

    /**
     * Overrides equals to compare Answer objects based on their fields.
     * 
     * @param obj the object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Answer other = (Answer) obj;
        return java.util.Objects.equals(state, other.state) &&
                java.util.Objects.equals(message, other.message) &&
                java.util.Objects.equals(internalMessage, other.internalMessage) &&
                java.util.Objects.equals(result, other.result);
    }

    /**
     * Creates a shallow copy of this Answer object.
     * Attempts to clone the object using the superclass clone method.
     * 
     * @return a clone of this Answer instance
     * @throws AssertionError if cloning is not supported (should never occur)
     */
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning of Answer is not supported");
        }
    }

}
