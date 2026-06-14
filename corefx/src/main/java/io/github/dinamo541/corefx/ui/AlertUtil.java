/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * Singleton utility for displaying alert dialogs with fully self-contained,
 * dependency-free visual theming.
 *
 * <p>
 * Plain JavaFX {@link Alert}s always render with the platform's light styling,
 * which clashes with dark or branded user interfaces. {@code AlertUtil} solves
 * this without requiring the consumer to ship any CSS file: a
 * {@link AlertTheme}
 * is translated into a CSS stylesheet <em>in code</em> and injected into the
 * dialog through a base64 {@code data:} URI. Because this uses the real CSS
 * cascade, it consistently restyles the dialog background, content text,
 * header,
 * and buttons (including hover state).
 * </p>
 *
 * <p>
 * Three levels of customization are offered:
 * </p>
 * <ol>
 * <li><b>Built-in presets</b> — {@link AlertTheme#light()} and
 * {@link AlertTheme#dark()} cover the common cases with no configuration.</li>
 * <li><b>Custom colors</b> — {@link AlertTheme#builder()} lets callers set any
 * combination of colors, font family, and font size using native
 * {@link Color}s, so the dialog can match any palette.</li>
 * <li><b>Raw CSS</b> — {@link #showStyled(AlertType, String, String, String)}
 * accepts an arbitrary stylesheet string for total control.</li>
 * </ol>
 *
 * <p>
 * A default theme can be configured once via
 * {@link #setDefaultTheme(AlertTheme)}
 * so that every subsequent alert adopts it automatically — ideal for an app
 * that
 * is dark-themed throughout. Per-call overloads still allow overriding the
 * default for an individual dialog.
 * </p>
 *
 * <p>
 * <b>Note:</b> styling applies to the dialog's content area (the
 * {@link DialogPane}); the surrounding window title bar is drawn by the
 * operating system and is outside JavaFX's styling reach. Blocking dialogs
 * ({@code showModal}, {@code showConfirmation}, {@code askYesNo}) must be
 * invoked
 * on the JavaFX Application Thread, as required by JavaFX itself.
 * </p>
 *
 * @author Sem
 * @author Dominique
 * @version 1.2
 * @since 2026/06/10
 */
public final class AlertUtil {

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link #getInstance()} is first called.
     */
    private static final class AlertUtilHolder {
        private static final AlertUtil INSTANCE = new AlertUtil();
    }

    /**
     * Optional theme applied to alerts that do not specify one. {@code null}
     * leaves alerts with the native platform look. Declared {@code volatile} for
     * safe publication across threads.
     */
    private volatile AlertTheme defaultTheme;

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private AlertUtil() {
    }

    /**
     * Returns the singleton instance of {@code AlertUtil}.
     *
     * @return the single {@code AlertUtil} instance
     */
    public static AlertUtil getInstance() {
        return AlertUtilHolder.INSTANCE;
    }

    // ---------------------------------------------------------------------
    // Default theme configuration
    // ---------------------------------------------------------------------

    /**
     * Sets the theme applied to every alert that is shown without an explicit
     * theme. Pass {@link AlertTheme#dark()} once and all alerts become dark.
     *
     * @param theme the default theme, or {@code null} to use the native look
     */
    public void setDefaultTheme(AlertTheme theme) {
        this.defaultTheme = theme;
    }

    /**
     * Returns the currently configured default theme.
     *
     * @return the default theme, or {@code null} if none is set
     */
    public AlertTheme getDefaultTheme() {
        return defaultTheme;
    }

    /**
     * Clears the default theme, reverting un-themed alerts to the native look.
     */
    public void clearDefaultTheme() {
        this.defaultTheme = null;
    }

    // ---------------------------------------------------------------------
    // Non-blocking alerts
    // ---------------------------------------------------------------------

    /**
     * Shows a non-blocking alert using the default theme.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title
     * @param message the message content
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public void show(AlertType type, String title, String message) {
        show(type, title, message, defaultTheme);
    }

    /**
     * Shows a non-blocking alert using the given theme.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title
     * @param message the message content
     * @param theme   the theme to apply, or {@code null} for the native look
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public void show(AlertType type, String title, String message, AlertTheme theme) {
        Alert alert = buildAlert(type, title, message, theme);
        alert.show();
    }

    // ---------------------------------------------------------------------
    // Blocking (modal) alerts
    // ---------------------------------------------------------------------

    /**
     * Shows a modal alert owned by the given window and waits for it to close,
     * using the default theme.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public void showModal(AlertType type, String title, Window owner, String message) {
        showModal(type, title, owner, message, defaultTheme);
    }

    /**
     * Shows a modal alert owned by the given window and waits for it to close,
     * using the given theme.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @param theme   the theme to apply, or {@code null} for the native look
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public void showModal(AlertType type, String title, Window owner, String message, AlertTheme theme) {
        Alert alert = buildAlert(type, title, message, theme);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and reports whether the user accepted, using the
     * default theme.
     *
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @return {@code true} if the user clicked OK, {@code false} otherwise
     */
    public boolean showConfirmation(String title, Window owner, String message) {
        return showConfirmation(title, owner, message, defaultTheme);
    }

    /**
     * Shows a confirmation dialog and reports whether the user accepted, using the
     * given theme.
     *
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @param theme   the theme to apply, or {@code null} for the native look
     * @return {@code true} if the user clicked OK, {@code false} otherwise
     */
    public boolean showConfirmation(String title, Window owner, String message, AlertTheme theme) {
        Alert alert = buildAlert(AlertType.CONFIRMATION, title, message, theme);
        alert.initOwner(owner);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a yes/no dialog and reports the user's choice, using the default
     * theme. The buttons are labelled "Yes" and "No".
     *
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @return {@code true} if the user clicked Yes, {@code false} otherwise
     */
    public boolean askYesNo(String title, Window owner, String message) {
        return askYesNo(title, owner, message, defaultTheme);
    }

    /**
     * Shows a yes/no dialog and reports the user's choice, using the given theme.
     * The buttons are labelled "Yes" and "No".
     *
     * @param title   the window title
     * @param owner   the owning window (may be {@code null})
     * @param message the message content
     * @param theme   the theme to apply, or {@code null} for the native look
     * @return {@code true} if the user clicked Yes, {@code false} otherwise
     */
    public boolean askYesNo(String title, Window owner, String message, AlertTheme theme) {
        Alert alert = buildAlert(AlertType.CONFIRMATION, title, message, theme);
        alert.initOwner(owner);
        alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    // ---------------------------------------------------------------------
    // Raw-CSS escape hatch
    // ---------------------------------------------------------------------

    /**
     * Shows a non-blocking alert styled with an arbitrary CSS stylesheet string,
     * for callers who need full control beyond {@link AlertTheme}. The CSS is
     * injected via a base64 {@code data:} URI, so no stylesheet file is required.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title
     * @param message the message content
     * @param css     the raw CSS stylesheet (must not be {@code null} or blank)
     * @throws NullPointerException     if {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code css} is {@code null} or blank
     */
    public void showStyled(AlertType type, String title, String message, String css) {
        if (css == null || css.isBlank()) {
            throw new IllegalArgumentException("css cannot be null or blank");
        }
        Alert alert = buildAlert(type, title, message, null);
        applyCss(alert.getDialogPane(), css);
        alert.show();
    }

    // ---------------------------------------------------------------------
    // Internal construction
    // ---------------------------------------------------------------------

    /**
     * Builds and configures an alert: sets the title, removes the header, applies
     * smart content sizing, and applies the theme when present.
     *
     * @param type    the alert type (must not be {@code null})
     * @param title   the window title (may be {@code null})
     * @param message the message content (may be {@code null})
     * @param theme   the theme to apply, or {@code null} for the native look
     * @return the configured {@link Alert}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    private Alert buildAlert(AlertType type, String title, String message, AlertTheme theme) {
        Objects.requireNonNull(type, "alert type cannot be null");
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        configureContent(alert, message);
        if (theme != null) {
            applyCss(alert.getDialogPane(), theme.toCss());
        }
        return alert;
    }

    /**
     * Configures the dialog content with wrapping and automatic width, matching
     * the sizing behaviour used elsewhere in the library: width grows with the
     * longest line, clamped between 380 and 720 pixels.
     *
     * @param alert   the alert to configure
     * @param message the message text (may be {@code null})
     */
    private void configureContent(Alert alert, String message) {
        String text = message == null ? "" : message;

        Label content = new Label(text);
        content.setWrapText(true);
        content.setMaxWidth(Double.MAX_VALUE);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);

        int maxLineLength = 0;
        for (String line : text.split("\\R", -1)) {
            if (line != null && line.length() > maxLineLength) {
                maxLineLength = line.length();
            }
        }
        double prefWidth = Math.min(720, Math.max(380, maxLineLength * 7.0 + 160));
        dialogPane.setPrefWidth(prefWidth);
    }

    /**
     * Injects a CSS stylesheet into the dialog pane through a base64
     * {@code data:} URI. Base64 encoding sidesteps every character-escaping issue
     * a raw {@code data:text/css,...} URI would otherwise have.
     *
     * @param dialogPane the dialog pane to style
     * @param css        the CSS stylesheet to apply
     */
    private void applyCss(DialogPane dialogPane, String css) {
        String encoded = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        dialogPane.getStylesheets().add("data:text/css;base64," + encoded);
    }

    // ---------------------------------------------------------------------
    // Object overrides (singleton)
    // ---------------------------------------------------------------------

    /**
     * Returns a string representation of this {@code AlertUtil}.
     *
     * @return string representation including whether a default theme is set
     */
    @Override
    public String toString() {
        return "AlertUtil{hasDefaultTheme=" + (defaultTheme != null) + '}';
    }

    /**
     * Computes the hash code for this singleton class.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    /**
     * Compares this {@code AlertUtil} singleton with another object for equality.
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
        return true;
    }

    /**
     * Prevents cloning of this singleton class.
     *
     * @return never returns normally
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of AlertUtil is not supported");
    }

    // =====================================================================
    // AlertTheme
    // =====================================================================

    /**
     * Immutable visual theme for an {@link Alert}, expressed entirely with native
     * JavaFX {@link Color}s and rendered to CSS on demand.
     *
     * <p>
     * Instances are created with the {@link #builder()} or via the
     * {@link #light()} / {@link #dark()} presets. Every field has a sensible
     * default, so a builder only needs to set what it wants to change.
     * </p>
     */
    public static final class AlertTheme {

        private final Color background;
        private final Color text;
        private final Color headerBackground;
        private final Color headerText;
        private final Color border;
        private final Color buttonBackground;
        private final Color buttonText;
        private final Color buttonHover;
        private final double fontSize;
        private final String fontFamily;

        /**
         * Constructs a theme from a configured builder.
         *
         * @param builder the source builder
         */
        private AlertTheme(Builder builder) {
            this.background = builder.background;
            this.text = builder.text;
            this.headerBackground = builder.headerBackground;
            this.headerText = builder.headerText;
            this.border = builder.border;
            this.buttonBackground = builder.buttonBackground;
            this.buttonText = builder.buttonText;
            this.buttonHover = builder.buttonHover;
            this.fontSize = builder.fontSize;
            this.fontFamily = builder.fontFamily;
        }

        /**
         * Returns a new builder pre-populated with the light defaults.
         *
         * @return a new {@link Builder}
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Returns the built-in light theme, matching the conventional platform
         * appearance.
         *
         * @return a light {@link AlertTheme}
         */
        public static AlertTheme light() {
            return builder().build();
        }

        /**
         * Returns the built-in dark theme, suited to dark user interfaces.
         *
         * @return a dark {@link AlertTheme}
         */
        public static AlertTheme dark() {
            return builder()
                    .background(Color.web("#2b2b2b"))
                    .text(Color.web("#e6e6e6"))
                    .headerBackground(Color.web("#333333"))
                    .headerText(Color.web("#f5f5f5"))
                    .border(Color.web("#1e1e1e"))
                    .buttonBackground(Color.web("#3c3f41"))
                    .buttonText(Color.web("#e6e6e6"))
                    .buttonHover(Color.web("#4e5254"))
                    .build();
        }

        /**
         * Renders this theme to a CSS stylesheet string targeting the dialog
         * pane and its sub-controls.
         *
         * @return the CSS representation of this theme
         */
        private String toCss() {
            StringBuilder css = new StringBuilder(512);

            css.append(".dialog-pane {")
                    .append("-fx-background-color:").append(toCssColor(background)).append(';')
                    .append("-fx-border-color:").append(toCssColor(border)).append(';')
                    .append("-fx-border-width:1;");
            if (fontFamily != null && !fontFamily.isBlank()) {
                css.append("-fx-font-family:'").append(fontFamily).append("';");
            }
            css.append("-fx-font-size:").append(format(fontSize)).append("px;")
                    .append('}');

            css.append(".dialog-pane > .content, .dialog-pane .label {")
                    .append("-fx-text-fill:").append(toCssColor(text)).append(';')
                    .append('}');

            css.append(".dialog-pane > .header-panel {")
                    .append("-fx-background-color:").append(toCssColor(headerBackground)).append(';')
                    .append('}');

            css.append(".dialog-pane > .header-panel .label {")
                    .append("-fx-text-fill:").append(toCssColor(headerText)).append(';')
                    .append('}');

            css.append(".dialog-pane .button {")
                    .append("-fx-background-color:").append(toCssColor(buttonBackground)).append(';')
                    .append("-fx-text-fill:").append(toCssColor(buttonText)).append(';')
                    .append("-fx-background-radius:4;")
                    .append("-fx-cursor:hand;")
                    .append('}');

            css.append(".dialog-pane .button:hover {")
                    .append("-fx-background-color:").append(toCssColor(buttonHover)).append(';')
                    .append('}');

            return css.toString();
        }

        /**
         * Converts a JavaFX {@link Color} to an {@code rgba(...)} CSS value, which
         * (unlike {@code #rrggbb}) needs no escaping inside a data URI.
         *
         * @param color the color to convert
         * @return the CSS color string
         */
        private static String toCssColor(Color color) {
            return String.format(Locale.ROOT, "rgba(%d,%d,%d,%s)",
                    (int) Math.round(color.getRed() * 255),
                    (int) Math.round(color.getGreen() * 255),
                    (int) Math.round(color.getBlue() * 255),
                    format(color.getOpacity()));
        }

        /**
         * Formats a double for CSS using a dot decimal separator, independent of
         * the platform locale.
         *
         * @param value the value to format
         * @return the formatted value
         */
        private static String format(double value) {
            return String.format(Locale.ROOT, "%.3f", value);
        }

        /**
         * Builder for {@link AlertTheme}. All setters default to the light theme
         * and return {@code this} for chaining.
         */
        public static final class Builder {

            private Color background = Color.web("#ffffff");
            private Color text = Color.web("#202020");
            private Color headerBackground = Color.web("#f4f4f4");
            private Color headerText = Color.web("#202020");
            private Color border = Color.web("#cccccc");
            private Color buttonBackground = Color.web("#e8e8e8");
            private Color buttonText = Color.web("#202020");
            private Color buttonHover = Color.web("#d8d8d8");
            private double fontSize = 13.0;
            private String fontFamily = null;

            /**
             * Sets the dialog background color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder background(Color color) {
                this.background = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the content text color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder text(Color color) {
                this.text = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the header background color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder headerBackground(Color color) {
                this.headerBackground = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the header text color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder headerText(Color color) {
                this.headerText = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the dialog border color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder border(Color color) {
                this.border = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the button background color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder buttonBackground(Color color) {
                this.buttonBackground = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the button text color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder buttonText(Color color) {
                this.buttonText = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the button hover background color.
             *
             * @param color the color (must not be {@code null})
             * @return this builder
             * @throws NullPointerException if {@code color} is {@code null}
             */
            public Builder buttonHover(Color color) {
                this.buttonHover = Objects.requireNonNull(color, "color cannot be null");
                return this;
            }

            /**
             * Sets the font size in pixels.
             *
             * @param pixels the font size (must be positive)
             * @return this builder
             * @throws IllegalArgumentException if {@code pixels} is not positive
             */
            public Builder fontSize(double pixels) {
                if (!(pixels > 0)) {
                    throw new IllegalArgumentException("fontSize must be positive");
                }
                this.fontSize = pixels;
                return this;
            }

            /**
             * Sets the font family. A {@code null} or blank value leaves the
             * inherited font unchanged.
             *
             * @param family the font family name (may be {@code null})
             * @return this builder
             */
            public Builder fontFamily(String family) {
                this.fontFamily = family;
                return this;
            }

            /**
             * Builds an immutable {@link AlertTheme} from this builder's state.
             *
             * @return a new {@link AlertTheme}
             */
            public AlertTheme build() {
                return new AlertTheme(this);
            }
        }
    }

}
