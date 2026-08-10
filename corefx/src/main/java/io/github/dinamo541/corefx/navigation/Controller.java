/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import java.util.Objects;

import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Abstract base class for controllers in the CoreFx framework.
 * Provides common functionality for managing stages, actions, and view names.
 * 
 * @author Dominique
 * @author Carranza
 * @version 1.3.0
 * @since 2026/06/10
 * @see FlowController
 */
public abstract class Controller {

    /** The stage associated with this controller. */
    private Stage stage;

    /** The current action associated with this controller. */
    private String action;

    /** The name of the view associated with this controller. */
    private String viewName;

    /**
     * Returns the current action associated with this controller.
     * 
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the current action for this controller.
     * 
     * @param action the action string to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Sets the stage associated with this controller.
     * 
     * @param stage the Stage to set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Returns the stage associated with this controller.
     * 
     * @return the Stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Returns the name of the view associated with this controller.
     * 
     * @return the view name
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Sets the name of the view associated with this controller.
     * 
     * @param viewName the view name to set
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Sends a TAB key event to the source control of the given KeyEvent.
     * This method consumes the original event and fires a new TAB key event.
     *
     * @param event the original KeyEvent to consume
     */
    public void sendTabEvent(KeyEvent event) {
        event.consume();
        KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, null, null, KeyCode.TAB, false, false, false, false);
        ((Control) event.getSource()).fireEvent(keyEvent);
    }

    /**
     * Initializes the controller. This method should be implemented by subclasses
     * to perform any necessary setup or initialization tasks.
     */
    public abstract void initialize();

    /**
     * Returns a string representation of this Controller.
     *
     * @return string representation including stage, action, and view name
     */
    @Override
    public String toString() {
        return "Controller{" +
                "stage=" + stage +
                ", action='" + action + '\'' +
                ", viewName='" + viewName + '\'' +
                '}';
    }

    /**
     * Computes the hash code for this Controller.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(stage, action, viewName);
    }

    /**
     * Compares this {@code Controller} with another object for equality.
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are of the same class
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Controller other = (Controller) obj;
        return Objects.equals(stage, other.stage) &&
                Objects.equals(action, other.action) &&
                Objects.equals(viewName, other.viewName);
    }

    /**
     * Creates and returns a copy of this Controller.
     * 
     * @return a clone of this Controller
     * @throws AssertionError if the Controller cannot be cloned
     */
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
