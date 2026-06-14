/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Scene;

/**
 * Singleton, centralized controller for application themes and CSS stylesheets.
 *
 * <p>
 * A <em>theme</em> is a named set of stylesheet URLs. Applications register
 * their themes once, choose an active one, and let this manager apply it to any
 * number of scenes. Scenes registered via {@link #manage(Scene)} are tracked
 * with weak references and updated automatically whenever the active theme
 * changes, enabling live theme switching without leaking memory.
 * </p>
 *
 * <p>
 * Applying a theme only ever touches stylesheets that belong to a registered
 * theme: the manager removes the URLs of all known themes from a scene before
 * adding the active theme's URLs, so an application's own, non-theme stylesheets
 * are never disturbed.
 * </p>
 *
 * <p>
 * The class intentionally does <b>not</b> depend on
 * {@link io.github.dinamo541.corefx.navigation.FlowController}. To bridge the
 * two, pass {@link #asApplier()} to {@code FlowController.setThemeApplier(...)};
 * every scene the controller creates will then be tracked and themed:
 * </p>
 *
 * <pre>{@code
 * ThemeManager themes = ThemeManager.getInstance();
 * themes.registerTheme("dark", "/app/css/dark.css");
 * themes.registerTheme("light", "/app/css/light.css");
 * themes.setActiveTheme("dark");
 * FlowController.getInstance().setThemeApplier(themes.asApplier());
 * }</pre>
 *
 * <p>
 * <b>Thread safety.</b> The theme registry is backed by a
 * {@link ConcurrentHashMap}; every scene mutation is marshalled onto the JavaFX
 * Application Thread via {@link Platform#runLater(Runnable)} when necessary.
 * </p>
 *
 * @author Dominique
 * @author Sem
 * @version 1.0
 * @since 2026/06/10
 */
public final class ThemeManager {

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link #getInstance()} is first called.
     */
    private static final class ThemeManagerHolder {
        private static final ThemeManager INSTANCE = new ThemeManager();
    }

    /**
     * Registry of themes: theme name to an immutable list of stylesheet URLs.
     */
    private final ConcurrentHashMap<String, List<String>> themes = new ConcurrentHashMap<>();

    /**
     * Scenes tracked for automatic re-theming, held weakly so they can be garbage
     * collected once the application stops referencing them.
     */
    private final Set<Scene> managedScenes =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    /** The name of the currently active theme, or {@code null} if none. */
    private volatile String activeTheme;

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private ThemeManager() {
    }

    /**
     * Returns the singleton instance of {@code ThemeManager}.
     *
     * @return the single {@code ThemeManager} instance
     */
    public static ThemeManager getInstance() {
        return ThemeManagerHolder.INSTANCE;
    }

    // ---------------------------------------------------------------------
    // Theme registry
    // ---------------------------------------------------------------------

    /**
     * Registers (or replaces) a theme with the given stylesheet URLs.
     *
     * @param name           the theme name (must not be {@code null} or blank)
     * @param stylesheetUrls the stylesheet URLs, in application order (must not be
     *                       {@code null} and must contain no {@code null} entries)
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     * @throws NullPointerException     if {@code stylesheetUrls} or any entry is
     *                                  {@code null}
     */
    public void registerTheme(String name, String... stylesheetUrls) {
        validateName(name);
        Objects.requireNonNull(stylesheetUrls, "stylesheetUrls cannot be null");
        themes.put(name, List.of(stylesheetUrls));
    }

    /**
     * Registers (or replaces) a theme with the given stylesheet URLs.
     *
     * @param name           the theme name (must not be {@code null} or blank)
     * @param stylesheetUrls the stylesheet URLs, in application order (must not be
     *                       {@code null} and must contain no {@code null} entries)
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     * @throws NullPointerException     if {@code stylesheetUrls} or any entry is
     *                                  {@code null}
     */
    public void registerTheme(String name, List<String> stylesheetUrls) {
        validateName(name);
        Objects.requireNonNull(stylesheetUrls, "stylesheetUrls cannot be null");
        themes.put(name, List.copyOf(stylesheetUrls));
    }

    /**
     * Removes a theme from the registry. If it was the active theme, the active
     * selection is cleared.
     *
     * @param name the theme name (must not be {@code null} or blank)
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public void unregisterTheme(String name) {
        validateName(name);
        themes.remove(name);
        if (name.equals(activeTheme)) {
            activeTheme = null;
        }
    }

    /**
     * Returns whether a theme is registered under the given name.
     *
     * @param name the theme name (must not be {@code null} or blank)
     * @return {@code true} if the theme exists
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public boolean hasTheme(String name) {
        validateName(name);
        return themes.containsKey(name);
    }

    /**
     * Returns an unmodifiable snapshot of the registered theme names.
     *
     * @return an unmodifiable set of theme names (never {@code null})
     */
    public Set<String> getThemeNames() {
        return Set.copyOf(themes.keySet());
    }

    // ---------------------------------------------------------------------
    // Active theme
    // ---------------------------------------------------------------------

    /**
     * Activates the named theme and immediately re-applies it to every managed
     * scene.
     *
     * @param name the theme name (must be registered)
     * @throws IllegalArgumentException if {@code name} is {@code null}, blank, or
     *                                  not registered
     */
    public void setActiveTheme(String name) {
        validateName(name);
        if (!themes.containsKey(name)) {
            throw new IllegalArgumentException("No theme is registered under name: " + name);
        }
        this.activeTheme = name;
        reapplyToManagedScenes();
    }

    /**
     * Returns the name of the active theme.
     *
     * @return the active theme name, or {@code null} if none is active
     */
    public String getActiveTheme() {
        return activeTheme;
    }

    /**
     * Clears the active theme and removes all theme stylesheets from managed
     * scenes, returning them to their unstyled state.
     */
    public void clearActiveTheme() {
        this.activeTheme = null;
        reapplyToManagedScenes();
    }

    // ---------------------------------------------------------------------
    // Scene application
    // ---------------------------------------------------------------------

    /**
     * Applies the active theme to a single scene without tracking it for future
     * updates. Existing theme stylesheets are removed first; the application's own
     * stylesheets are preserved.
     *
     * @param scene the scene to style (must not be {@code null})
     * @throws NullPointerException if {@code scene} is {@code null}
     */
    public void applyTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene cannot be null");
        runOnFxThread(() -> applyActiveThemeNow(scene));
    }

    /**
     * Registers a scene for automatic theming. The active theme is applied
     * immediately and re-applied whenever it changes. The scene is held weakly, so
     * managing it does not prevent garbage collection.
     *
     * @param scene the scene to manage (must not be {@code null})
     * @throws NullPointerException if {@code scene} is {@code null}
     */
    public void manage(Scene scene) {
        Objects.requireNonNull(scene, "scene cannot be null");
        managedScenes.add(scene);
        runOnFxThread(() -> applyActiveThemeNow(scene));
    }

    /**
     * Stops tracking a scene for automatic theming. Its current stylesheets are
     * left untouched.
     *
     * @param scene the scene to forget (must not be {@code null})
     * @throws NullPointerException if {@code scene} is {@code null}
     */
    public void forget(Scene scene) {
        Objects.requireNonNull(scene, "scene cannot be null");
        managedScenes.remove(scene);
    }

    /**
     * Returns a {@link Consumer} that manages each scene it receives, suitable for
     * wiring into {@code FlowController.setThemeApplier(...)}. Scenes passed to the
     * returned consumer are tracked and themed exactly as with
     * {@link #manage(Scene)}.
     *
     * @return a scene-consuming theme applier
     */
    public Consumer<Scene> asApplier() {
        return this::manage;
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    /**
     * Re-applies the active theme to every managed scene, iterating over a
     * snapshot to avoid concurrent-modification issues.
     */
    private void reapplyToManagedScenes() {
        final List<Scene> snapshot;
        synchronized (managedScenes) {
            snapshot = List.copyOf(managedScenes);
        }
        runOnFxThread(() -> snapshot.forEach(this::applyActiveThemeNow));
    }

    /**
     * Applies the active theme to a scene on the calling (FX) thread: removes the
     * URLs of every registered theme, then adds the active theme's URLs.
     *
     * @param scene the scene to update
     */
    private void applyActiveThemeNow(Scene scene) {
        Set<String> allThemeUrls = new HashSet<>();
        themes.values().forEach(allThemeUrls::addAll);
        scene.getStylesheets().removeAll(allThemeUrls);

        String active = activeTheme;
        if (active != null) {
            List<String> urls = themes.get(active);
            if (urls != null && !urls.isEmpty()) {
                scene.getStylesheets().addAll(urls);
            }
        }
    }

    /**
     * Runs an action on the JavaFX Application Thread, immediately if already on
     * it, otherwise via {@link Platform#runLater(Runnable)}.
     *
     * @param action the action to run
     */
    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Validates a theme name argument.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Theme name cannot be null or blank");
        }
    }

    /**
     * Returns a string representation of this {@code ThemeManager}.
     *
     * @return string representation including registered themes and the active one
     */
    @Override
    public String toString() {
        return "ThemeManager{" +
                "themes=" + themes.keySet() +
                ", activeTheme=" + activeTheme +
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
     * Compares this {@code ThemeManager} singleton with another object for
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
        throw new CloneNotSupportedException("Cloning of ThemeManager is not supported");
    }

}
