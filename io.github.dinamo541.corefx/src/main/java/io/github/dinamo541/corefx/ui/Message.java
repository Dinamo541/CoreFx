/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.dinamo541.corefx.ui;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/**
 * Utility class for displaying alert dialogs and user messages in JavaFX.
 * Provides methods for showing different types of alerts (error, warning,
 * confirmation, information)
 * with customizable titles and messages. Automatically configures dialog
 * appearance,
 * content wrapping, and dimensions based on message length.
 * 
 * Supports both modal and non-modal alert displays, as well as confirmation
 * dialogs
 * with boolean return values for user response handling.
 * 
 * @author Carranza
 * @author Dominique
 * @version 2.3
 * @since 2024-06-10
 */
public class Message {

    /**
     * Displays a non-modal alert dialog of the specified type.
     * The dialog is shown without blocking the application thread.
     * 
     * @param alertType the type of alert (ERROR, WARNING, CONFIRMATION,
     *                  INFORMATION)
     * @param title     the window title of the alert
     * @param message   the message content to display
     * 
     * @see AlertType
     */
    public void show(AlertType alertType, String title, String message) {
        Alert alert = predefinedSets(alertType, title, message);
        alert.show();
    }

    /**
     * Displays a modal alert dialog that blocks user interaction with the specified
     * parent window.
     * Waits for the user to respond before continuing.
     * 
     * @param alertType the type of alert (ERROR, WARNING, CONFIRMATION,
     *                  INFORMATION)
     * @param title     the window title of the alert
     * @param parent    the parent window that owns this alert
     * @param message   the message content to display
     * 
     * @see AlertType
     */
    public void showModal(AlertType alertType, String title, Window parent, String message) {
        Alert alert = predefinedSets(alertType, title, message);
        alert.initOwner(parent);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog and returns whether the user clicked OK.
     * The dialog blocks user interaction with the parent window.
     * 
     * @param title   the window title of the alert
     * @param parent  the parent window that owns this alert
     * @param message the message content to display
     * @return true if the user clicked OK, false if they clicked Cancel or closed
     *         the dialog
     */
    public Boolean showConfirmation(String title, Window parent, String message) {
        Alert alert = predefinedSets(AlertType.CONFIRMATION, title, message);
        alert.initOwner(parent);
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Displays a yes/no confirmation dialog and returns the user's choice.
     * The dialog blocks user interaction with the parent window.
     * Button options are "NO" and "YES" instead of "Cancel" and "OK".
     * 
     * @param title   the window title of the alert
     * @param parent  the parent window that owns this alert
     * @param message the message content to display
     * @return true if the user clicked YES, false if they clicked NO or closed the
     *         dialog
     */
    public Boolean askYesOrNoBoolean(String title, Window parent, String message) {
        Alert alert = predefinedSets(AlertType.CONFIRMATION, title, message);
        alert.initOwner(parent);
        alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Configures a new Alert with predefined settings.
     * Sets title, removes header text, and configures message content.
     * Does not apply stylesheets or custom graphics (commented out for optional
     * use).
     * 
     * @param alertType the type of alert to create
     * @param title     the window title
     * @param message   the message content
     * @return a configured Alert instance
     */
    private Alert predefinedSets(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        configureContent(alert, message);
        // addStyleSheets(alert);
        // alert.setOnShown(e -> applyButtonsStyles(alert));
        return alert;
    }

    /**
     * Configures the dialog content with text wrapping and automatic sizing.
     * Calculates the optimal dialog width based on message length (min 380px, max
     * 720px).
     * Each character is approximated to be 7 pixels wide, plus 160 pixels for
     * padding.
     * 
     * @param alert   the Alert whose content should be configured
     * @param message the message text to display
     */
    private void configureContent(Alert alert, String message) {
        String text = message == null ? "" : message;

        Label content = new Label(text);
        content.setWrapText(true);
        content.setMaxWidth(Double.MAX_VALUE);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(content);

        dialogPane.setMinHeight(Region.USE_PREF_SIZE);

        int maxLineLen = 0;
        for (String line : text.split("\\R", -1)) {
            if (line != null && line.length() > maxLineLen) {
                maxLineLen = line.length();
            }
        }
        double prefWidth = Math.min(720, Math.max(380, maxLineLen * 7.0 + 160));
        dialogPane.setPrefWidth(prefWidth);
    }

    /**
     * Loads an icon image from the specified resource path.
     * Resizes the icon to 32x32 pixels for display in alerts.
     * 
     * @param path the resource path to the icon image file
     * @return an ImageView containing the loaded and scaled icon
     */
    public ImageView loadIcon(String path) {
        ImageView icon = new ImageView(
                new Image(getClass().getResource(path).toExternalForm()));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        return icon;
    }

    /**
     * Applies custom CSS classes to alert buttons based on their text content.
     * Maps "OK" and "Aceptar" buttons to "btn-accept" style class.
     * Maps "Cancel" and "Cancelar" buttons to "btn-cancel" style class.
     * 
     * @param alert the Alert whose buttons should be styled
     */
    public void applyButtonsStyles(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.lookupAll(".button").forEach(node -> {
            if (node instanceof Button button) {
                String buttonText = button.getText();

                if (buttonText != null) {
                    if (buttonText.equals("Aceptar") || buttonText.equals("OK")) {
                        button.getStyleClass().add("btn-accept");
                    } else if (buttonText.equals("Cancelar") || buttonText.equals("Cancel")) {
                        button.getStyleClass().add("btn-cancel");
                    }
                }
            }
        });
    }

    /**
     * Returns a string representation of this Message utility class.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "Message{}";
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
     * Compares this Message utility class with another object for equality.
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
        throw new CloneNotSupportedException("Cloning of Message is not supported");
    }

}
