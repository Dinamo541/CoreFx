/*
 * CoreFx - Librería utilitaria JavaFX
 * Autor: Dominique Mariano Q.C.
 * Fecha: 10 jun 2026
 * Paquete: cr.ac.una.corefx.ui
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.corefx.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Toggle;

/**
 * Utility class for binding JavaFX ToggleGroup selections to object properties.
 * Provides methods to synchronize a ToggleGroup's selected toggle with an ObjectProperty,
 * enabling two-way data binding between UI controls and model properties.
 * 
 * This is a utility class and should not be instantiated.
 * 
 * @author Carranza
 * @author Dominique
 * @version 2.2
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class BindingUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BindingUtils() {
    }

    /**
     * Binds a ToggleGroup to an ObjectProperty, creating a two-way binding.
     * The selected toggle's user data is synchronized with the property value.
     * 
     * Each toggle in the group must have non-null user data. If any toggle lacks
     * user data, an IllegalArgumentException is thrown. The listener is stored
     * internally to enable later unbinding via {@link #unbindToggleGroupToProperty(ToggleGroup, ObjectProperty)}.
     * 
     * @param <T> the type of the property value and toggle user data
     * @param toggleGroup the ToggleGroup to bind
     * @param property the ObjectProperty to bind to
     * @throws IllegalArgumentException if any toggle in the group has null user data
     * 
     * @see #unbindToggleGroupToProperty(ToggleGroup, ObjectProperty)
     */
    public static <T> void bindToggleGroupToProperty(final ToggleGroup toggleGroup, final ObjectProperty<T> property) {
        toggleGroup.getToggles().forEach(toggle -> {
            if (toggle.getUserData() == null) {
                throw new IllegalArgumentException("The ToggleGroup contains at least one Toggle without user data!");
            }
        });
        for (Toggle toggle : toggleGroup.getToggles()) {
            if (property.getValue() != null && property.getValue().equals(toggle.getUserData())) {
                toggleGroup.selectToggle(toggle);
                break;
            }
        }
        ChangeListener<Toggle> listener = (ObservableValue<? extends Toggle> observable, Toggle oldValue,
                Toggle newValue) -> {
            if (newValue != null) {
                T value = (T) newValue.getUserData();
                property.setValue(value);
            }
        };
        toggleGroup.getProperties().put("changeListener", listener);
        toggleGroup.selectedToggleProperty().addListener(listener);
    }

    /**
     * Unbinds a ToggleGroup from an ObjectProperty, removing the two-way binding.
     * Retrieves and removes the previously stored change listener from the ToggleGroup
     * and removes it from the selectedToggleProperty observable.
     * 
     * @param <T> the type of the property value and toggle user data
     * @param toggleGroup the ToggleGroup to unbind
     * @param property the ObjectProperty to unbind from
     * 
     * @see #bindToggleGroupToProperty(ToggleGroup, ObjectProperty)
     */
    public static <T> void unbindToggleGroupToProperty(final ToggleGroup toggleGroup,
            final ObjectProperty<T> property) {
        ChangeListener<Toggle> listener = (ChangeListener<Toggle>) toggleGroup.getProperties().remove("changeListener");
        if (listener != null) {
            toggleGroup.selectedToggleProperty().removeListener(listener);
        }
    }

    /**
     * Returns a string representation of this utility class.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "BindingUtils{}";
    }

    /**
     * Computes the hash code for this utility class.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash();
    }

    /**
     * Compares this utility class with another object for equality.
     * 
     * @param obj the object to compare with
     * @return true if the objects are of the same class, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return true;
    }

    /**
     * Prevents cloning of this utility class.
     * 
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of BindingUtils is not supported");
    }

}
