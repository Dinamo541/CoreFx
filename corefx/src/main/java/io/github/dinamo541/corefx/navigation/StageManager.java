/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Singleton manager for JavaFX {@link Stage} and window operations.
 *
 * <p>
 * Where {@link FlowController} concentrates on loading FXML views and swapping
 * scene content, {@code StageManager} concentrates on the windows themselves:
 * keeping a named registry of stages and providing a safe, uniform API for
 * their life cycle (show / hide / close), window state (maximize, iconify, full
 * screen, always-on-top, opacity, …), geometry (size, position, centering,
 * fitting to a screen), icons, and dragging of undecorated windows. The two
 * classes are complementary and can be used together.
 * </p>
 *
 * <p>
 * Implements the Singleton pattern using the initialization-on-demand holder
 * idiom, providing lazy, thread-safe access without synchronization overhead.
 * The registry is backed by a {@link ConcurrentHashMap}, so registration and
 * lookup are lock-free on the common path.
 * </p>
 *
 * <p>
 * <b>Thread safety.</b> Every method that mutates the UI delegates to
 * {@link #runOnFxThread(Runnable)}, which runs the action immediately when the
 * caller is already on the JavaFX Application Thread and otherwise defers it
 * via
 * {@link Platform#runLater(Runnable)}. This makes the manager safe to call from
 * background threads without the caller having to reason about threading, which
 * is essential for a reusable library consumed by arbitrary applications.
 * </p>
 *
 * <p>
 * <b>Null-safety and contracts.</b> Methods reject {@code null} stages and
 * {@code null}/blank keys with descriptive exceptions, mirroring the defensive
 * style of {@code FlowController}. Operations addressed by key throw
 * {@link IllegalArgumentException} when no stage is registered under that key,
 * surfacing the mistake instead of failing silently. Lookups
 * ({@link #getStage(String)}, {@link #hasStage(String)}) never throw.
 * </p>
 *
 * @author Dominique
 * @author Sem
 * @version 1.0
 * @since 2026/06/10
 */
public final class StageManager {

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link #getInstance()} is first called.
     */
    private static final class StageManagerHolder {
        private static final StageManager INSTANCE = new StageManager();
    }

    /**
     * Thread-safe registry of managed stages keyed by an application-chosen name.
     * {@link ConcurrentHashMap} allows lock-free reads and atomic updates without
     * an external lock.
     */
    private final ConcurrentHashMap<String, Stage> stages = new ConcurrentHashMap<>();

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private StageManager() {
    }

    /**
     * Returns the singleton instance of {@code StageManager}.
     *
     * @return the single {@code StageManager} instance
     */
    public static StageManager getInstance() {
        return StageManagerHolder.INSTANCE;
    }

    // ---------------------------------------------------------------------
    // Thread-safety helper
    // ---------------------------------------------------------------------

    /**
     * Runs the given action on the JavaFX Application Thread. If the caller is
     * already on that thread the action runs synchronously; otherwise it is
     * scheduled with {@link Platform#runLater(Runnable)} and runs asynchronously.
     *
     * @param action the action to execute (must not be {@code null})
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public void runOnFxThread(Runnable action) {
        Objects.requireNonNull(action, "action cannot be null");
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    // ---------------------------------------------------------------------
    // Registry
    // ---------------------------------------------------------------------

    /**
     * Registers a stage under the given key, replacing any stage previously
     * registered under the same key.
     *
     * @param key   the registry key (must not be {@code null} or blank)
     * @param stage the stage to register (must not be {@code null})
     * @return the previously registered stage for this key, or {@code null} if
     *         none
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     * @throws NullPointerException     if {@code stage} is {@code null}
     */
    public Stage register(String key, Stage stage) {
        validateKey(key);
        Objects.requireNonNull(stage, "stage cannot be null");
        return stages.put(key, stage);
    }

    /**
     * Removes the stage registered under the given key, if any. The stage itself
     * is not closed.
     *
     * @param key the registry key (must not be {@code null} or blank)
     * @return the removed stage, or {@code null} if no stage was registered
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public Stage unregister(String key) {
        validateKey(key);
        return stages.remove(key);
    }

    /**
     * Returns the stage registered under the given key.
     *
     * @param key the registry key (must not be {@code null} or blank)
     * @return the registered stage, or {@code null} if none
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public Stage getStage(String key) {
        validateKey(key);
        return stages.get(key);
    }

    /**
     * Returns whether a stage is registered under the given key.
     *
     * @param key the registry key (must not be {@code null} or blank)
     * @return {@code true} if a stage is registered under {@code key}
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public boolean hasStage(String key) {
        validateKey(key);
        return stages.containsKey(key);
    }

    /**
     * Returns an unmodifiable snapshot of the currently registered keys.
     *
     * @return an unmodifiable set of registry keys (never {@code null})
     */
    public Set<String> getKeys() {
        return Set.copyOf(stages.keySet());
    }

    /**
     * Removes all stages from the registry without closing them.
     */
    public void clear() {
        stages.clear();
    }

    /**
     * Resolves the stage registered under the given key or fails.
     *
     * @param key the registry key
     * @return the registered stage, guaranteed non-{@code null}
     * @throws IllegalArgumentException if {@code key} is {@code null}, blank, or
     *                                  not registered
     */
    private Stage require(String key) {
        validateKey(key);
        Stage stage = stages.get(key);
        if (stage == null) {
            throw new IllegalArgumentException("No stage is registered under key: " + key);
        }
        return stage;
    }

    /**
     * Validates a registry key.
     *
     * @param key the key to validate
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Stage key cannot be null or blank");
        }
    }

    // ---------------------------------------------------------------------
    // Life cycle
    // ---------------------------------------------------------------------

    /**
     * Shows the given stage and brings it to the front.
     *
     * @param stage the stage to show (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void show(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> {
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
        });
    }

    /**
     * Shows the stage registered under the given key.
     *
     * @param key the registry key
     * @throws IllegalArgumentException if no stage is registered under {@code key}
     */
    public void show(String key) {
        show(require(key));
    }

    /**
     * Hides the given stage without closing it.
     *
     * @param stage the stage to hide (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void hide(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(stage::hide);
    }

    /**
     * Hides the stage registered under the given key without closing it.
     *
     * @param key the registry key
     * @throws IllegalArgumentException if no stage is registered under {@code key}
     */
    public void hide(String key) {
        hide(require(key));
    }

    /**
     * Closes the given stage. The stage is not removed from the registry; use
     * {@link #close(String)} to close and unregister in one step.
     *
     * @param stage the stage to close (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void close(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(stage::close);
    }

    /**
     * Closes the stage registered under the given key and removes it from the
     * registry.
     *
     * @param key the registry key
     * @throws IllegalArgumentException if no stage is registered under {@code key}
     */
    public void close(String key) {
        Stage stage = require(key);
        stages.remove(key);
        close(stage);
    }

    /**
     * Closes every registered stage and clears the registry.
     */
    public void closeAll() {
        runOnFxThread(() -> {
            for (Stage stage : stages.values()) {
                stage.close();
            }
            stages.clear();
        });
    }

    /**
     * Hides every registered stage without closing them or clearing the registry.
     */
    public void hideAll() {
        runOnFxThread(() -> {
            for (Stage stage : stages.values()) {
                stage.hide();
            }
        });
    }

    // ---------------------------------------------------------------------
    // Window state
    // ---------------------------------------------------------------------

    /**
     * Sets whether the stage is maximized.
     *
     * @param stage     the target stage (must not be {@code null})
     * @param maximized {@code true} to maximize, {@code false} to restore
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setMaximized(Stage stage, boolean maximized) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setMaximized(maximized));
    }

    /**
     * Toggles the maximized state of the stage.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void toggleMaximized(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setMaximized(!stage.isMaximized()));
    }

    /**
     * Sets whether the stage is iconified (minimized).
     *
     * @param stage     the target stage (must not be {@code null})
     * @param iconified {@code true} to minimize, {@code false} to restore
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setIconified(Stage stage, boolean iconified) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setIconified(iconified));
    }

    /**
     * Sets whether the stage is shown in full screen.
     *
     * @param stage      the target stage (must not be {@code null})
     * @param fullScreen {@code true} for full screen, {@code false} for windowed
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setFullScreen(Stage stage, boolean fullScreen) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setFullScreen(fullScreen));
    }

    /**
     * Toggles the full screen state of the stage.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void toggleFullScreen(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setFullScreen(!stage.isFullScreen()));
    }

    /**
     * Sets whether the stage stays on top of other windows.
     *
     * @param stage       the target stage (must not be {@code null})
     * @param alwaysOnTop {@code true} to keep the window on top
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setAlwaysOnTop(Stage stage, boolean alwaysOnTop) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setAlwaysOnTop(alwaysOnTop));
    }

    /**
     * Sets whether the stage can be resized by the user.
     *
     * @param stage     the target stage (must not be {@code null})
     * @param resizable {@code true} to allow resizing
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setResizable(Stage stage, boolean resizable) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> stage.setResizable(resizable));
    }

    /**
     * Sets the stage opacity, clamping the value to the valid range
     * {@code [0.0, 1.0]} so out-of-range input can never produce an invalid
     * window.
     *
     * @param stage   the target stage (must not be {@code null})
     * @param opacity the desired opacity; values below {@code 0} or above
     *                {@code 1} are clamped, and {@code NaN} is treated as
     *                {@code 1.0}
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setOpacity(Stage stage, double opacity) {
        Objects.requireNonNull(stage, "stage cannot be null");
        double safe = Double.isNaN(opacity) ? 1.0 : Math.max(0.0, Math.min(1.0, opacity));
        runOnFxThread(() -> stage.setOpacity(safe));
    }

    /**
     * Sets the stage title. A {@code null} title is normalized to an empty
     * string.
     *
     * @param stage the target stage (must not be {@code null})
     * @param title the new title (may be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void setTitle(Stage stage, String title) {
        Objects.requireNonNull(stage, "stage cannot be null");
        String safe = title == null ? "" : title;
        runOnFxThread(() -> stage.setTitle(safe));
    }

    /**
     * Brings the stage to the front of the window stack, showing it first if
     * necessary.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void bringToFront(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> {
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
        });
    }

    // ---------------------------------------------------------------------
    // Icons
    // ---------------------------------------------------------------------

    /**
     * Replaces the stage icons with the single provided image.
     *
     * @param stage the target stage (must not be {@code null})
     * @param icon  the icon image (must not be {@code null})
     * @throws NullPointerException if {@code stage} or {@code icon} is
     *                              {@code null}
     */
    public void setIcon(Stage stage, Image icon) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(icon, "icon cannot be null");
        runOnFxThread(() -> {
            stage.getIcons().clear();
            stage.getIcons().add(icon);
        });
    }

    /**
     * Adds an icon image to the stage without removing existing ones.
     *
     * @param stage the target stage (must not be {@code null})
     * @param icon  the icon image to add (must not be {@code null})
     * @throws NullPointerException if {@code stage} or {@code icon} is
     *                              {@code null}
     */
    public void addIcon(Stage stage, Image icon) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(icon, "icon cannot be null");
        runOnFxThread(() -> stage.getIcons().add(icon));
    }

    // ---------------------------------------------------------------------
    // Geometry
    // ---------------------------------------------------------------------

    /**
     * Sets the size of the stage.
     *
     * @param stage  the target stage (must not be {@code null})
     * @param width  the new width in pixels (must be finite and non-negative)
     * @param height the new height in pixels (must be finite and non-negative)
     * @throws NullPointerException     if {@code stage} is {@code null}
     * @throws IllegalArgumentException if a dimension is negative, {@code NaN}, or
     *                                  infinite
     */
    public void setSize(Stage stage, double width, double height) {
        Objects.requireNonNull(stage, "stage cannot be null");
        validateDimension(width, "width");
        validateDimension(height, "height");
        runOnFxThread(() -> {
            stage.setWidth(width);
            stage.setHeight(height);
        });
    }

    /**
     * Sets the minimum size of the stage.
     *
     * @param stage  the target stage (must not be {@code null})
     * @param width  the minimum width in pixels (must be finite and non-negative)
     * @param height the minimum height in pixels (must be finite and non-negative)
     * @throws NullPointerException     if {@code stage} is {@code null}
     * @throws IllegalArgumentException if a dimension is negative, {@code NaN}, or
     *                                  infinite
     */
    public void setMinSize(Stage stage, double width, double height) {
        Objects.requireNonNull(stage, "stage cannot be null");
        validateDimension(width, "width");
        validateDimension(height, "height");
        runOnFxThread(() -> {
            stage.setMinWidth(width);
            stage.setMinHeight(height);
        });
    }

    /**
     * Sets the maximum size of the stage.
     *
     * @param stage  the target stage (must not be {@code null})
     * @param width  the maximum width in pixels (must be finite and non-negative)
     * @param height the maximum height in pixels (must be finite and non-negative)
     * @throws NullPointerException     if {@code stage} is {@code null}
     * @throws IllegalArgumentException if a dimension is negative, {@code NaN}, or
     *                                  infinite
     */
    public void setMaxSize(Stage stage, double width, double height) {
        Objects.requireNonNull(stage, "stage cannot be null");
        validateDimension(width, "width");
        validateDimension(height, "height");
        runOnFxThread(() -> {
            stage.setMaxWidth(width);
            stage.setMaxHeight(height);
        });
    }

    /**
     * Moves the stage to the given screen coordinates. Negative coordinates are
     * permitted, since they are valid on multi-monitor layouts.
     *
     * @param stage the target stage (must not be {@code null})
     * @param x     the new x coordinate (must be finite)
     * @param y     the new y coordinate (must be finite)
     * @throws NullPointerException     if {@code stage} is {@code null}
     * @throws IllegalArgumentException if a coordinate is {@code NaN} or infinite
     */
    public void setPosition(Stage stage, double x, double y) {
        Objects.requireNonNull(stage, "stage cannot be null");
        validateCoordinate(x, "x");
        validateCoordinate(y, "y");
        runOnFxThread(() -> {
            stage.setX(x);
            stage.setY(y);
        });
    }

    /**
     * Centers the stage on the screen using the platform's built-in centering.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void centerOnScreen(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(stage::centerOnScreen);
    }

    /**
     * Centers the stage within the visual bounds of the screen that currently
     * contains most of it, falling back to the primary screen. Unlike the
     * built-in centering, this accounts for the screen the window already lives
     * on, which is useful on multi-monitor setups.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void centerOnCurrentScreen(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> {
            Rectangle2D bounds = resolveScreenBounds(stage);
            double width = stage.getWidth();
            double height = stage.getHeight();
            if (Double.isNaN(width) || width <= 0) {
                width = bounds.getWidth() / 2.0;
            }
            if (Double.isNaN(height) || height <= 0) {
                height = bounds.getHeight() / 2.0;
            }
            stage.setX(bounds.getMinX() + (bounds.getWidth() - width) / 2.0);
            stage.setY(bounds.getMinY() + (bounds.getHeight() - height) / 2.0);
        });
    }

    /**
     * Resizes and positions the stage to fill the visual bounds of the screen it
     * currently occupies (excluding system areas such as the taskbar), without
     * entering the operating system's maximized or full-screen mode.
     *
     * @param stage the target stage (must not be {@code null})
     * @throws NullPointerException if {@code stage} is {@code null}
     */
    public void fitToScreen(Stage stage) {
        Objects.requireNonNull(stage, "stage cannot be null");
        runOnFxThread(() -> {
            Rectangle2D bounds = resolveScreenBounds(stage);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        });
    }

    /**
     * Resolves the visual bounds of the screen that best contains the stage,
     * defaulting to the primary screen when the stage has no usable geometry yet.
     * Must be called on the JavaFX Application Thread.
     *
     * @param stage the stage whose screen should be resolved
     * @return the visual bounds of the chosen screen
     */
    private Rectangle2D resolveScreenBounds(Stage stage) {
        double x = stage.getX();
        double y = stage.getY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(width) || Double.isNaN(height)
                || width <= 0 || height <= 0) {
            return Screen.getPrimary().getVisualBounds();
        }

        var screens = Screen.getScreensForRectangle(x, y, width, height);
        if (screens.isEmpty()) {
            return Screen.getPrimary().getVisualBounds();
        }
        return screens.get(0).getVisualBounds();
    }

    // ---------------------------------------------------------------------
    // Undecorated-window dragging
    // ---------------------------------------------------------------------

    /**
     * Makes an undecorated (or any) stage draggable by pressing and dragging the
     * given handle node — typically a custom title bar. Re-registering a handle
     * replaces the previous drag handlers on that node.
     *
     * <p>
     * The drag preserves the offset between the cursor and the window origin, so
     * the window follows the pointer naturally without jumping.
     * </p>
     *
     * @param stage  the stage to move (must not be {@code null})
     * @param handle the node that initiates the drag (must not be {@code null})
     * @throws NullPointerException if {@code stage} or {@code handle} is
     *                              {@code null}
     */
    public void makeDraggable(Stage stage, Node handle) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(handle, "handle cannot be null");
        final double[] offset = new double[2];
        runOnFxThread(() -> {
            handle.setOnMousePressed(event -> {
                offset[0] = stage.getX() - event.getScreenX();
                offset[1] = stage.getY() - event.getScreenY();
            });
            handle.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() + offset[0]);
                stage.setY(event.getScreenY() + offset[1]);
            });
        });
    }

    /**
     * Removes drag handlers previously installed by
     * {@link #makeDraggable(Stage, Node)} from the given handle node.
     *
     * @param handle the node to clear (must not be {@code null})
     * @throws NullPointerException if {@code handle} is {@code null}
     */
    public void removeDraggable(Node handle) {
        Objects.requireNonNull(handle, "handle cannot be null");
        runOnFxThread(() -> {
            handle.setOnMousePressed(null);
            handle.setOnMouseDragged(null);
        });
    }

    // ---------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------

    /**
     * Validates a size dimension.
     *
     * @param value the dimension to validate
     * @param name  the dimension name used in the exception message
     * @throws IllegalArgumentException if {@code value} is negative, {@code NaN},
     *                                  or infinite
     */
    private void validateDimension(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be a finite, non-negative number");
        }
    }

    /**
     * Validates a position coordinate.
     *
     * @param value the coordinate to validate
     * @param name  the coordinate name used in the exception message
     * @throws IllegalArgumentException if {@code value} is {@code NaN} or infinite
     */
    private void validateCoordinate(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be a finite number");
        }
    }

    // ---------------------------------------------------------------------
    // Object overrides (singleton)
    // ---------------------------------------------------------------------

    /**
     * Returns a string representation of this {@code StageManager}.
     *
     * @return string representation including the registered keys
     */
    @Override
    public String toString() {
        return "StageManager{" +
                "stages=" + stages.keySet() +
                '}';
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
     * Compares this {@code StageManager} singleton with another object for
     * equality.
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
        throw new CloneNotSupportedException("Cloning of StageManager is not supported");
    }

}
