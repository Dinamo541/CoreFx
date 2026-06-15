/**
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Singleton controller for managing JavaFX view navigation and stage
 * management.
 * Provides centralized control over scene creation, view loading, window
 * creation,
 * and navigation between different UI states in the application.
 *
 * <p>
 * Implements the Singleton pattern using the initialization-on-demand holder
 * idiom,
 * which provides lazy, thread-safe initialization without synchronization
 * overhead.
 * </p>
 *
 * <p>
 * <b>Usage:</b> {@link #initialize} must be called exactly once before any
 * navigation
 * or loading method. All public methods enforce this contract via
 * {@link #checkInitialized()}.
 * </p>
 *
 * <p>
 * Supports multiple navigation patterns:
 * </p>
 * <ul>
 * <li>Main stage navigation (full-scene replacement)</li>
 * <li>Independent windows (non-blocking top-level stages)</li>
 * <li>Modal dialogs (owner-blocking stages)</li>
 * <li>Container-based navigation (replacing children in {@link Pane} or
 * {@link Group})</li>
 * <li>BorderPane region management (Center, Top, Bottom, Left, Right)</li>
 * </ul>
 *
 * @author Dominique
 * @version 2.7
 * @since 2026-06-25
 */
public final class FlowController {

    // =========================================================================
    // SECTION 1: SINGLETON & FIELDS
    // =========================================================================

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly
     * once, only when {@link FlowController#getInstance()} is first called.
     */
    private static final class FlowControllerHolder {
        private static final FlowController INSTANCE = new FlowController();
    }

    /**
     * Thread-safe cache of {@link FXMLLoader} instances keyed by view name.
     * {@link ConcurrentHashMap} allows lock-free reads on cache hits, while
     * {@link ConcurrentHashMap#computeIfAbsent} guarantees atomic creation on
     * cache misses, eliminating the need for an external lock around the loader
     * map.
     */
    private final ConcurrentHashMap<String, FXMLLoader> loaders = new ConcurrentHashMap<>();

    /**
     * Guard for the {@link #initialize} block against concurrent first-time calls.
     * Used together with the {@code volatile} flag {@link #initialized} to
     * implement
     * double-checked locking.
     */
    private final Object initLock = new Object();

    /**
     * Whether {@link #initialize} has completed successfully.
     * Declared {@code volatile} to guarantee visibility of the write across threads
     * without requiring synchronization on every subsequent read.
     */
    private volatile boolean initialized = false;

    /** Primary JavaFX {@link Stage} managed by this controller. */
    private volatile Stage mainStage;

    /**
     * Optional {@link ResourceBundle} injected into every {@link FXMLLoader}
     * created by this controller, enabling internationalized FXML — {@code %key}
     * references in the markup resolve against this bundle. Declared
     * {@code volatile} so its registration is visible across threads.
     * Set via {@link #setIdioma(ResourceBundle)}; {@code null} loads views without
     * a bundle. Changing it clears the loader cache so subsequent loads pick up
     * the new locale.
     */
    private volatile ResourceBundle idioma;

    /** Application display name used as the window title. */
    private volatile String appName;

    /**
     * Classpath-rooted path to the FXML view directory
     * (e.g. {@code /io/github/dinamo541/corefx/view/}).
     */
    private volatile String baseViewPath;

    /**
     * Classpath-rooted path to the resource directory
     * (e.g. {@code /io/github/dinamo541/corefx/resources/}).
     */
    private volatile String baseResourcePath;

    /** Classpath path to the application icon file ({@code .png}). */
    private volatile String appIconPath;

    /**
     * Application class used to resolve classpath resources
     * via {@link Class#getResource(String)}.
     */
    private volatile Class<?> appClass;

    /**
     * Optional callback applied to every {@link Scene} created by this controller.
     * Allows consuming applications to inject CSS or a theming library without
     * coupling {@code FlowController} to any specific styling framework.
     * Set via {@link #setThemeApplier(Consumer)}; {@code null} leaves scenes
     * unstyled.
     */
    private volatile Consumer<Scene> themeApplier = null;

    /**
     * Arbitrary value handed from one view's controller to the next across a
     * navigation, letting controllers share data without referencing one another.
     * Declared {@code volatile} to guarantee visibility of writes across threads.
     * Set via {@link #setTransferValue(Object)} and consumed via
     * {@link #getTransferValue()} or {@link #getTransferValue(Class)};
     * {@code null} means no value is pending.
     */
    private volatile Object transferValue;

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     * All initialization logic is deferred to {@link #initialize} to allow
     * flexible setup after the singleton is first referenced.
     */
    private FlowController() {
    }

    /**
     * Returns the singleton instance of {@code FlowController}.
     *
     * <p>
     * <strong>Note:</strong> {@link #initialize} must be called before invoking
     * any navigation or loading method.
     * </p>
     *
     * @return the single {@code FlowController} instance
     */
    public static FlowController getInstance() {
        return FlowControllerHolder.INSTANCE;
    }

    // =========================================================================
    // SECTION 2: INITIALIZATION
    // =========================================================================

    /**
     * Initializes the controller using only the application class.
     * A new {@link Stage} is created automatically.
     * Use this overload when no existing stage is available at startup.
     *
     * @param appClass the main application class, used to derive paths and load
     *                 resources
     * @throws NullPointerException  if {@code appClass} is {@code null}
     * @throws IllegalStateException if the controller has already been initialized
     */
    public void initialize(Class<?> appClass) {
        Objects.requireNonNull(appClass, "appClass cannot be null");
        initialize(new Stage(), appClass);
    }

    /**
     * Initializes the controller with a stage and application class.
     * The application name and base paths are derived automatically from the
     * package name.
     *
     * @param stage    the primary JavaFX stage
     * @param appClass the main application class, used to derive paths and load
     *                 resources
     * @throws NullPointerException  if {@code stage} or {@code appClass} is
     *                               {@code null}
     * @throws IllegalStateException if the controller has already been initialized
     */
    public void initialize(Stage stage, Class<?> appClass) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(appClass, "appClass cannot be null");

        String packageName = appClass.getPackageName();
        String derivedName = packageName.substring(packageName.lastIndexOf('.') + 1);
        initialize(stage, derivedName, appClass);
    }

    /**
     * Initializes the controller with a stage, a custom application name,
     * and an application class. Base paths are derived automatically.
     *
     * @param stage    the primary JavaFX stage
     * @param appName  the display name used as the window title
     * @param appClass the main application class, used to derive paths and load
     *                 resources
     * @throws NullPointerException     if {@code stage} or {@code appClass} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code appName} is {@code null} or blank
     * @throws IllegalStateException    if the controller has already been
     *                                  initialized
     */
    public void initialize(Stage stage, String appName, Class<?> appClass) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(appClass, "appClass cannot be null");
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("appName cannot be null or blank");
        }
        String basePath = "/" + appClass.getPackageName().replace('.', '/') + "/";
        initialize(stage, appName, basePath, appClass);
    }

    /**
     * Initializes the controller with a stage, application name, a shared base
     * path,
     * and an application class. View and resource paths are derived from
     * {@code basePath}.
     *
     * @param stage    the primary JavaFX stage
     * @param appName  the display name used as the window title
     * @param basePath the root classpath path for the application
     *                 (e.g. {@code /io/github/dinamo541/soulward/})
     * @param appClass the main application class, used to load resources
     * @throws NullPointerException     if {@code stage} or {@code appClass} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code appName} or {@code basePath} is
     *                                  {@code null} or blank
     * @throws IllegalStateException    if the controller has already been
     *                                  initialized
     */
    public void initialize(Stage stage, String appName, String basePath, Class<?> appClass) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(appClass, "appClass cannot be null");
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("appName cannot be null or blank");
        }
        if (basePath == null || basePath.isBlank()) {
            throw new IllegalArgumentException("basePath cannot be null or blank");
        }
        String baseViewPath = basePath + "view/";
        String baseResourcePath = basePath + "resources/";
        String appIconPath = baseResourcePath + appName + ".png";
        initialize(stage, appName, baseViewPath, baseResourcePath, appIconPath, appClass);
    }

    /**
     * Initializes the controller with full parameters plus a theme applier.
     * The theme applier is registered only after the core initialization succeeds,
     * preventing partial state if initialization fails (e.g. called twice).
     *
     * @param stage            the primary JavaFX stage
     * @param appName          the display name used as the window title
     * @param baseViewPath     classpath path to the view folder
     * @param baseResourcePath classpath path to the resources folder
     * @param appIconPath      classpath path to the application icon
     * @param appClass         the main application class, used to load resources
     * @param themeApplier     optional callback to apply theming to created scenes;
     *                         pass {@code null} to leave scenes unstyled
     * @throws NullPointerException     if {@code stage} or {@code appClass} is
     *                                  {@code null}
     * @throws IllegalArgumentException if any {@code String} parameter is
     *                                  {@code null} or blank
     * @throws IllegalStateException    if the controller has already been
     *                                  initialized
     */
    public void initialize(Stage stage, String appName, String baseViewPath,
            String baseResourcePath, String appIconPath,
            Class<?> appClass, Consumer<Scene> themeApplier) {
        initialize(stage, appName, baseViewPath, baseResourcePath, appIconPath, appClass);
        setThemeApplier(themeApplier);
    }

    /**
     * Initializes the controller with full parameters plus a theme applier and a
     * localization bundle. Both the theme applier and the bundle are registered
     * only after the core initialization succeeds, preventing partial state if
     * initialization fails (e.g. called twice).
     *
     * <p>
     * Unlike the {@code String} parameters, {@code idioma} is fully optional:
     * passing {@code null} simply loads views without a {@link ResourceBundle},
     * which is harmless for FXML that contains no {@code %key} references. See
     * {@link #setIdioma(ResourceBundle)}.
     * </p>
     *
     * @param stage            the primary JavaFX stage
     * @param appName          the display name used as the window title
     * @param baseViewPath     classpath path to the view folder
     * @param baseResourcePath classpath path to the resources folder
     * @param appIconPath      classpath path to the application icon
     * @param appClass         the main application class, used to load resources
     * @param themeApplier     optional callback to apply theming to created scenes;
     *                         pass {@code null} to leave scenes unstyled
     * @param idioma           optional resource bundle for localizing views;
     *                         pass {@code null} to load views without one
     * @throws NullPointerException     if {@code stage} or {@code appClass} is
     *                                  {@code null}
     * @throws IllegalArgumentException if any {@code String} parameter is
     *                                  {@code null} or blank
     * @throws IllegalStateException    if the controller has already been
     *                                  initialized
     */
    public void initialize(Stage stage, String appName, String baseViewPath,
            String baseResourcePath, String appIconPath,
            Class<?> appClass, Consumer<Scene> themeApplier, ResourceBundle idioma) {
        initialize(stage, appName, baseViewPath, baseResourcePath, appIconPath, appClass);
        setThemeApplier(themeApplier);
        setIdioma(idioma);
    }

    /**
     * Core initialization method. All other overloads ultimately delegate here.
     * Uses double-checked locking on {@link #initLock} to guarantee that the
     * controller
     * is configured exactly once, even under concurrent access.
     *
     * @param stage            the primary JavaFX stage
     * @param appName          the display name used as the window title
     * @param baseViewPath     classpath path to the view folder
     * @param baseResourcePath classpath path to the resources folder
     * @param appIconPath      classpath path to the application icon
     * @param appClass         the main application class, used to load resources
     * @throws NullPointerException     if {@code stage} or {@code appClass} is
     *                                  {@code null}
     * @throws IllegalArgumentException if any {@code String} parameter is
     *                                  {@code null} or blank
     * @throws IllegalStateException    if the controller has already been
     *                                  initialized
     */
    public void initialize(Stage stage, String appName, String baseViewPath,
            String baseResourcePath, String appIconPath, Class<?> appClass) {
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(appClass, "appClass cannot be null");

        if (appName == null || appName.isBlank())
            throw new IllegalArgumentException("appName cannot be null or blank");
        if (baseViewPath == null || baseViewPath.isBlank())
            throw new IllegalArgumentException("baseViewPath cannot be null or blank");
        if (baseResourcePath == null || baseResourcePath.isBlank())
            throw new IllegalArgumentException("baseResourcePath cannot be null or blank");
        if (appIconPath == null || appIconPath.isBlank())
            throw new IllegalArgumentException("appIconPath cannot be null or blank");

        if (initialized) {
            throw new IllegalStateException(
                    "FlowController is already initialized. Call initialize() only once.");
        }

        synchronized (initLock) {
            if (initialized) {
                throw new IllegalStateException(
                        "FlowController is already initialized. Call initialize() only once.");
            }
            this.mainStage = stage;
            this.appName = appName;
            this.baseViewPath = baseViewPath;
            this.baseResourcePath = baseResourcePath;
            this.appIconPath = appIconPath;
            this.appClass = appClass;
            mainStage.setTitle(appName);
            this.initialized = true;
        }
    }

    /**
     * Internal guard invoked at the start of every public navigation and loading
     * method.
     * Ensures the controller has been fully initialized before any operation is
     * attempted.
     *
     * @throws IllegalStateException if {@link #initialize} has not been called yet
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "FlowController is not initialized. Call initialize() before using navigation methods.");
        }
    }

    // =========================================================================
    // SECTION 3: FXML LOADER MANAGEMENT
    // =========================================================================

    /**
     * Creates and returns a new, fully loaded {@link FXMLLoader} for the named
     * view.
     * {@link FXMLLoader#load()} is called immediately so that both the root node
     * and
     * its controller are accessible on the returned instance.
     *
     * <p>
     * This method always creates a fresh loader and does not interact with the
     * cache.
     * Use {@link #getLoader(String)} to obtain a cached instance instead.
     * </p>
     *
     * <p>
     * The currently registered {@link ResourceBundle} (see
     * {@link #setIdioma(ResourceBundle)}) is passed to the loader, so
     * internationalized FXML resolves against it; a {@code null} bundle loads the
     * view without one.
     * </p>
     *
     * @param name the view name, without the {@code .fxml} extension
     * @return a loaded {@link FXMLLoader} for the specified view
     * @throws IllegalStateException if the controller has not been initialized
     * @throws IOException           if the FXML resource cannot be found or parsed
     */
    public FXMLLoader createLoaderInstance(String name) throws IOException {
        checkInitialized();
        try {
            FXMLLoader loader = new FXMLLoader(appClass.getResource(baseViewPath + name + ".fxml"), idioma);
            loader.load();
            return loader;
        } catch (IOException | RuntimeException ex) {
            throw new IOException("Error creating loader: [" + name + "].", ex);
        }
    }

    /**
     * Returns a cached FXMLLoader for the specified view, creating one if
     * necessary.
     * Uses synchronization to ensure thread safety during loader caching.
     *
     * @param viewName the name of the view (without .fxml extension)
     * @return the cached or newly created FXMLLoader
     * @throws RuntimeException if the loader cannot be initialized
     *
     * @see #createLoaderInstance(String)
     */
    public FXMLLoader getLoader(String viewName) {
        checkInitialized();
        try {
            synchronized (initLock) { // ← initLock instead of FlowController.class
                FXMLLoader loader = loaders.get(viewName);
                if (loader == null) {
                    loader = createLoaderInstance(viewName);
                    loaders.put(viewName, loader);
                }
                return loader;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    /**
     * Returns the controller associated with the specified view.
     * Casts the controller to the desired type.
     *
     * @param <T>      the type of the controller
     * @param viewName the name of the view whose controller to retrieve
     * @return the controller, or null if not found
     */
    public <T> T getController(String viewName) {
        checkInitialized();
        return getLoader(viewName).getController();
    }

    /**
     * Removes a view loader from the cache.
     * This forces the view to be reloaded from disk on next access.
     *
     * @param view the view name to remove from cache
     */
    public void removeLoader(String view) {
        checkInitialized();
        loaders.remove(view);
    }

    /**
     * Clears all cached view loaders.
     * Forces all views to be reloaded from disk on next access.
     */
    public void clearLoadersMap() {
        checkInitialized();
        loaders.clear();
    }

    // =========================================================================
    // SECTION 4: RESOURCES & SCENE CREATION
    // =========================================================================

    /**
     * Loads the application icon image from the resource path.
     *
     * @return the application icon as an Image
     * @throws RuntimeException if the icon resource cannot be found or loaded
     */
    public Image loadAppIcon() {
        checkInitialized();
        try {
            var stream = FlowController.class.getResourceAsStream(appIconPath);
            if (stream == null)
                return null;
            return new Image(stream);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a Scene with the given root node and applies the configured theme.
     * If no theme applier has been set via setThemeApplier(), the scene is returned
     * unstyled.
     *
     * @param root the root node of the scene
     * @return a Scene with the optional theme applied
     */
    public Scene createScene(Parent view) {
        AnchorPane root = new AnchorPane(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
        Scene scene = new Scene(root);
        if (themeApplier != null) {
            themeApplier.accept(scene);
        }
        return scene;
    }

    /**
     * Prepares a stage with the given scene.
     * Sets the scene, title, and application icon.
     *
     * @param stage the stage to prepare
     * @param scene the scene to set on the stage
     */
    private void prepareStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setTitle(appName);
        Image icon = loadAppIcon();
        if (icon != null) {
            stage.getIcons().add(icon);
        }
    }

    // =========================================================================
    // SECTION 5: NODE & CONTAINER UTILITIES
    // =========================================================================

    /**
     * Resets the visual properties of a node to default values.
     * Sets opacity, visibility, scale, and translation to default states.
     *
     * @param node the node to normalize (may be null)
     */
    private void normalizeViewNode(Node node) {
        if (node == null) {
            return;
        }
        node.setOpacity(1.0);
        node.setVisible(true);
        node.setMouseTransparent(false);
        node.setScaleX(1.0);
        node.setScaleY(1.0);
        node.setTranslateX(0.0);
        node.setTranslateY(0.0);
    }

    /**
     * Replaces the contents of a container (Pane or Group) with a new node.
     * Clears existing children and adds the new node.
     * Falls back to a custom setter if the container type is not supported.
     *
     * @param container      the container node (Pane, Group, or null)
     * @param node           the new node to place in the container
     * @param fallbackSetter callback for unsupported container types
     */
    public void replaceNodeInContainer(Node container, Parent node, Consumer<Node> fallbackSetter) {
        switch (container) {
            case null -> fallbackSetter.accept(node);
            case Pane pane -> {
                normalizeViewNode(node);
                pane.getChildren().clear();
                pane.getChildren().add(node);
            }
            case Group group -> {
                normalizeViewNode(node);
                group.getChildren().clear();
                group.getChildren().add(node);
            }
            default -> {
            }
        }
    }

    /**
     * Adds a node to a container (Pane or Group) without clearing existing
     * children.
     * Falls back to a custom setter if the container type is not supported.
     *
     * @param currentNode    the container node (Pane, Group, or null)
     * @param node           the node to add
     * @param fallbackSetter callback for unsupported container types
     */
    public void placeNodeInContainer(Node currentNode, Parent node, Consumer<Node> fallbackSetter) {
        switch (currentNode) {
            case null -> fallbackSetter.accept(node);
            case Pane pane -> pane.getChildren().add(node);
            case Group group -> group.getChildren().add(node);
            default -> {
            }
        }
    }

    /**
     * Clears all child nodes from a container (Pane or Group).
     *
     * @param node the container to clear
     * @throws IllegalArgumentException if node type is not supported
     */
    public void clearContainer(Node node) {
        switch (node) {
            case null -> {
            }
            case Pane pane -> pane.getChildren().clear();
            case Group group -> group.getChildren().clear();
            default -> {
                throw new IllegalArgumentException("Unsupported container type: " + node.getClass().getName());
            }
        }
    }

    // =========================================================================
    // SECTION 6: CONTAINER NAVIGATION
    // =========================================================================

    /**
     * Replaces the contents of a container with a view loaded from FXML.
     * Uses an empty fallback setter by default.
     *
     * @param viewName    the name of the view to load
     * @param currentNode the container to replace contents in
     *
     * @see #replaceViewInContainer(String, Node, Consumer)
     */
    public void replaceViewInContainer(String viewName, Node currentNode) {
        checkInitialized();
        replaceViewInContainer(viewName, currentNode, (fallbackNode) -> {
        });
    }

    /**
     * Replaces the contents of a container with a view loaded from FXML.
     * The container is cleared and the new view is added to it.
     *
     * @param viewName       the name of the view to load
     * @param currentNode    the container to replace contents in
     * @param fallbackSetter callback for handling null containers
     * @throws IllegalArgumentException if fallbackSetter is null
     * @throws RuntimeException         if the view cannot be loaded
     */
    public void replaceViewInContainer(String viewName, Node currentNode, Consumer<Node> fallbackSetter) {
        checkInitialized();
        if (fallbackSetter == null)
            throw new IllegalArgumentException("Fallback setter cannot be null");
        try {
            Parent root = getLoader(viewName).getRoot();
            replaceNodeInContainer(currentNode, root, fallbackSetter);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Adds a view loaded from FXML to a container without clearing existing
     * children.
     * Uses an empty fallback setter by default.
     *
     * @param viewName    the name of the view to load
     * @param currentNode the container to add the view to
     *
     * @see #placeViewInContainer(String, Node, Consumer)
     */
    public void placeViewInContainer(String viewName, Node currentNode) {
        checkInitialized();
        placeViewInContainer(viewName, currentNode, (fallbackNode) -> {
        });
    }

    /**
     * Adds a view loaded from FXML to a container without clearing existing
     * children.
     *
     * @param viewName       the name of the view to load
     * @param currentNode    the container to add the view to
     * @param fallbackSetter callback for handling null containers
     * @throws RuntimeException if the view cannot be loaded
     */
    public void placeViewInContainer(String viewName, Node currentNode, Consumer<Node> fallbackSetter) {
        checkInitialized();
        try {
            Parent root = getLoader(viewName).getRoot();
            placeNodeInContainer(currentNode, root, fallbackSetter);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    // =========================================================================
    // SECTION 7: MAIN STAGE NAVIGATION
    // =========================================================================

    /**
     * Navigates to the specified view in the main stage.
     * Creates a new scene if none exists, or replaces the scene root.
     * Shows the stage if not currently visible.
     *
     * @param viewName the name of the view to load
     * @throws IllegalArgumentException if viewName is null or empty
     * @throws IllegalStateException    if main stage is not initialized
     * @throws RuntimeException         if the view cannot be loaded
     */
    public void goViewMain(String viewName) {
        checkInitialized();
        try {
            if (viewName == null || viewName.isBlank()) {
                throw new IllegalArgumentException("View name is null or empty");
            }

            Parent root = getLoader(viewName).getRoot();
            Scene scene = mainStage.getScene();

            if (scene == null) {
                prepareStage(mainStage, createScene(root));
            } else if (scene.getRoot() != root) {
                scene.setRoot(root);
            }

            if (!mainStage.isShowing()) {
                mainStage.show();
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Changes the view displayed in the main stage.
     * Equivalent to {@link #goViewMain(String)}.
     *
     * @param viewName the name of the view to load
     * @throws IllegalArgumentException if viewName is null or empty
     * @throws IllegalStateException    if main stage is not initialized
     * @throws RuntimeException         if the view cannot be loaded
     */
    public void changeViewInMain(String viewName) {
        checkInitialized();
        try {
            if (viewName == null || viewName.isBlank()) {
                throw new IllegalArgumentException("View name is null or empty");
            }

            Parent view = getLoader(viewName).getRoot();
            Scene scene = mainStage.getScene();

            if (scene == null) {
                prepareStage(mainStage, createScene(view));
            } else if (scene.getRoot() != view) {
                replaceNodeInContainer(scene.getRoot(), view, (fallbackNode) -> {
                    scene.setRoot(view);
                });
            }

            if (!mainStage.isShowing()) {
                mainStage.show();
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    // =========================================================================
    // SECTION 8: WINDOW NAVIGATION
    // =========================================================================

    /**
     * Opens a view in a new resizable window.
     * Uses default resizable setting (true).
     *
     * @param viewName the name of the view to load
     *
     * @see #goViewInWindow(String, Boolean)
     */
    public void goViewInWindow(String viewName) {
        checkInitialized();
        goViewInWindow(viewName, true);
    }

    /**
     * Opens a view in a new window.
     * The window is independent and can be closed without affecting the main stage.
     *
     * @param viewName  the name of the view to load
     * @param resizable whether the window can be resized by the user
     * @throws RuntimeException if the view cannot be loaded
     */
    public void goViewInWindow(String viewName, Boolean resizable) {
        checkInitialized();
        try {
            FXMLLoader loader = getLoader(viewName);
            Stage stage = new Stage();
            stage.setResizable(resizable);
            stage.setOnHidden((WindowEvent event) -> {
                stage.getScene().setRoot(new Pane());
            });
            prepareStage(stage, createScene(loader.getRoot()));
            stage.show();
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    // =========================================================================
    // SECTION 9: MODAL NAVIGATION
    // =========================================================================

    /**
     * Opens a view in a modal dialog owned by the main stage.
     * Uses default resizable setting (true).
     *
     * @param viewName the name of the view to load
     *
     * @see #goViewInModal(String, Stage, Boolean)
     */
    public void goViewInModal(String viewName) {
        checkInitialized();
        goViewInModal(viewName, mainStage, true);
    }

    /**
     * Opens a view in a modal dialog owned by the specified stage.
     * Uses default resizable setting (true).
     *
     * @param viewName the name of the view to load
     * @param owner    the owner stage for the modal dialog
     *
     * @see #goViewInModal(String, Stage, Boolean)
     */
    public void goViewInModal(String viewName, Stage owner) {
        checkInitialized();
        goViewInModal(viewName, owner, true);
    }

    /**
     * Opens a view in a modal dialog owned by the main stage.
     *
     * @param viewName  the name of the view to load
     * @param resizable whether the dialog can be resized by the user
     *
     * @see #goViewInModal(String, Stage, Boolean)
     */
    public void goViewInModal(String viewName, Boolean resizable) {
        checkInitialized();
        goViewInModal(viewName, mainStage, resizable);
    }

    /**
     * Opens a view in a modal dialog.
     * The dialog blocks interaction with its owner window until closed.
     * Automatically centers on screen and applies application styling.
     *
     * @param viewName  the name of the view to load
     * @param owner     the owner stage for the modal dialog
     * @param resizable whether the dialog can be resized by the user
     * @throws RuntimeException if the view cannot be loaded
     */
    public void goViewInModal(String viewName, Stage owner, Boolean resizable) {
        checkInitialized();
        try {
            FXMLLoader loader = getLoader(viewName);
            Stage stage = new Stage();

            stage.setResizable(resizable);
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden((WindowEvent event) -> {
                stage.getScene().setRoot(new Pane());
            });
            prepareStage(stage, createScene(loader.getRoot()));
            stage.centerOnScreen();
            stage.show();
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Opens a view in a modal dialog owned by the main stage and blocks until it
     * is closed. Uses default resizable setting (true).
     *
     * @param viewName the name of the view to load
     *
     * @see #goViewInModalAndWait(String, Stage, Boolean)
     */
    public void goViewInModalAndWait(String viewName) {
        checkInitialized();
        goViewInModalAndWait(viewName, mainStage, true);
    }

    /**
     * Opens a view in a modal dialog owned by the specified stage and blocks
     * until it is closed. Uses default resizable setting (true).
     *
     * @param viewName the name of the view to load
     * @param owner    the owner stage for the modal dialog
     *
     * @see #goViewInModalAndWait(String, Stage, Boolean)
     */
    public void goViewInModalAndWait(String viewName, Stage owner) {
        checkInitialized();
        goViewInModalAndWait(viewName, owner, true);
    }

    /**
     * Opens a view in a modal dialog owned by the main stage and blocks until it
     * is closed.
     *
     * @param viewName  the name of the view to load
     * @param resizable whether the dialog can be resized by the user
     *
     * @see #goViewInModalAndWait(String, Stage, Boolean)
     */
    public void goViewInModalAndWait(String viewName, Boolean resizable) {
        checkInitialized();
        goViewInModalAndWait(viewName, mainStage, resizable);
    }

    /**
     * Opens a view in a modal dialog and blocks the calling thread until the
     * dialog is closed.
     *
     * <p>
     * This is the blocking counterpart of
     * {@link #goViewInModal(String, Stage, Boolean)}: it uses
     * {@link Stage#showAndWait()} so the call does not return until the user
     * dismisses the dialog, which is useful for confirmation flows where the
     * result must be read immediately after navigation (for example via
     * {@link #getTransferValue()}). It must be invoked on the JavaFX Application
     * Thread.
     * </p>
     *
     * @param viewName  the name of the view to load
     * @param owner     the owner stage for the modal dialog
     * @param resizable whether the dialog can be resized by the user
     * @throws RuntimeException if the view cannot be loaded
     */
    public void goViewInModalAndWait(String viewName, Stage owner, Boolean resizable) {
        checkInitialized();
        try {
            FXMLLoader loader = getLoader(viewName);
            Stage stage = new Stage();

            stage.setResizable(resizable);
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden((WindowEvent event) -> {
                stage.getScene().setRoot(new Pane());
            });
            prepareStage(stage, createScene(loader.getRoot()));
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    // =========================================================================
    // SECTION 10: SCENE & STAGE NAVIGATION
    // =========================================================================

    /**
     * Changes the root scene of the specified stage to the given view.
     *
     * @param viewName the name of the view to load
     * @param stage    the stage whose scene should be changed
     * @throws NullPointerException if {@code stage} is {@code null}
     * @throws RuntimeException     if the view cannot be loaded
     */
    public void changeViewInStage(String viewName, Stage stage) {
        checkInitialized();
        Objects.requireNonNull(stage, "stage cannot be null");
        try {
            FXMLLoader loader = getLoader(viewName);
            prepareStage(stage, createScene(loader.getRoot()));
            if (!stage.isShowing()) {
                stage.show();
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Changes the root node of the main stage's scene to the specified view.
     * The scene must already be set on the main stage.
     *
     * @param viewName the name of the view to load
     * @throws RuntimeException if main stage's scene is null or view cannot be
     *                          loaded
     */
    public void changeViewInScene(String viewName) {
        checkInitialized();
        changeViewInScene(viewName, mainStage.getScene());
    }

    /**
     * Changes the root node of the scene to the specified view.
     * The scene must already be set on a stage.
     *
     * @param viewName the name of the view to load
     * @param scene    the scene whose root should be changed
     * @throws NullPointerException if {@code scene} is {@code null}
     * @throws RuntimeException     if the view cannot be loaded
     */
    public void changeViewInScene(String viewName, Scene scene) {
        checkInitialized();
        Objects.requireNonNull(scene, "scene cannot be null");
        try {
            FXMLLoader loader = getLoader(viewName);
            Node root = scene.getRoot();

            replaceNodeInContainer(root, loader.getRoot(), (fallbackNode) -> {
                scene.setRoot(loader.getRoot());
            });
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    // =========================================================================
    // SECTION 11: BORDERPANE NAVIGATION
    // =========================================================================

    /**
     * Changes a region within a BorderPane to display the specified view.
     * If no BorderPane is provided, attempts to use the root of the main stage's
     * scene.
     * Uses "Center" as the default region.
     *
     * @param viewName   the name of the view to load
     * @param borderPane the BorderPane to update (may be null to use main stage's
     *                   root)
     *
     * @see #changeViewInBorderPane(String, BorderPane, String)
     */
    public void changeViewInBorderPane(String viewName, BorderPane borderPane) {
        checkInitialized();
        if (borderPane == null) {
            try {
                borderPane = (BorderPane) mainStage.getScene().getRoot();
            } catch (RuntimeException ex) {
                throw new RuntimeException(ex);
            }
        }
        changeViewInBorderPane(viewName, borderPane, "Center");
    }

    /**
     * Changes a region in the BorderPane root of the main stage to display the
     * specified view.
     * Uses "Center" as the default region.
     *
     * @param viewName the name of the view to load
     *
     * @see #changeViewInBorderPane(String, Stage, String)
     */
    public void changeViewInBorderPane(String viewName) {
        checkInitialized();
        changeViewInBorderPane(viewName, mainStage, "Center");
    }

    /**
     * Changes a region in the BorderPane root of the specified stage to display the
     * specified view.
     * Uses "Center" as the default region.
     *
     * @param viewName the name of the view to load
     * @param stage    the stage whose BorderPane root should be updated
     *
     * @see #changeViewInBorderPane(String, Stage, String)
     */
    public void changeViewInBorderPane(String viewName, Stage stage) {
        checkInitialized();
        changeViewInBorderPane(viewName, stage, "Center");
    }

    /**
     * Changes the specified region in the BorderPane root of the main stage.
     *
     * @param viewName the name of the view to load
     * @param region   the BorderPane region to update (default: "Center")
     *
     * @see #changeViewInBorderPane(String, Stage, String)
     */
    public void changeViewInBorderPane(String viewName, String region) {
        checkInitialized();
        changeViewInBorderPane(viewName, mainStage, region);
    }

    /**
     * Changes the specified region in the BorderPane root of the given stage.
     * Uses "Center" if the region is null or empty.
     *
     * @param viewName the name of the view to load
     * @param stage    the stage whose BorderPane root should be updated
     * @param region   the BorderPane region ("Center", "Top", "Bottom", "Left",
     *                 "Right")
     * @throws RuntimeException if the stage's scene root is not a BorderPane
     *
     * @see #changeViewInBorderPane(String, BorderPane, String)
     */
    public void changeViewInBorderPane(String viewName, Stage stage, String region) {
        checkInitialized();
        try {
            BorderPane borderPane = (BorderPane) stage.getScene().getRoot();
            changeViewInBorderPane(viewName, borderPane, region);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Changes the specified region in a BorderPane to display the given view.
     * Valid regions are: "Center", "Top", "Bottom", "Left", "Right".
     * Uses "Center" as default if region is null or empty.
     *
     * @param viewName   the name of the view to load
     * @param borderPane the BorderPane to update
     * @param region     the BorderPane region to change
     * @throws IllegalArgumentException if borderPane is null or region is invalid
     * @throws RuntimeException         if the view cannot be loaded
     */
    public void changeViewInBorderPane(String viewName, BorderPane borderPane, String region) {
        checkInitialized();
        if (borderPane == null) {
            throw new IllegalArgumentException("BorderPane is null");
        }
        if (region == null || region.isBlank()) {
            region = "Center";
        }
        try {
            FXMLLoader loader = getLoader(viewName);
            Parent node = loader.getRoot();
            switch (region) {
                case "Center" -> replaceNodeInContainer(borderPane.getCenter(), node, borderPane::setCenter);
                case "Top" -> replaceNodeInContainer(borderPane.getTop(), node, borderPane::setTop);
                case "Bottom" -> replaceNodeInContainer(borderPane.getBottom(), node, borderPane::setBottom);
                case "Right" -> replaceNodeInContainer(borderPane.getRight(), node, borderPane::setRight);
                case "Left" -> replaceNodeInContainer(borderPane.getLeft(), node, borderPane::setLeft);
                default -> {
                    throw new IllegalArgumentException("Invalid region: " + region);
                }
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Clears all child nodes from a specific region in a BorderPane.
     * Valid regions are: "Center", "Top", "Bottom", "Left", "Right".
     *
     * @param region     the BorderPane region to clear
     * @param borderPane the BorderPane containing the region
     * @throws IllegalArgumentException if region is invalid
     */
    public void clearRegion(String region, BorderPane borderPane) {
        checkInitialized();
        switch (region) {
            case "Center" ->
                borderPane.setCenter(null);
            case "Top" ->
                borderPane.setTop(null);
            case "Bottom" ->
                borderPane.setBottom(null);
            case "Right" ->
                borderPane.setRight(null);
            case "Left" ->
                borderPane.setLeft(null);
            default ->
                throw new IllegalArgumentException("Invalid region: " + region);
        }
    }

    // =========================================================================
    // SECTION 12: STAGE CONFIGURATION
    // =========================================================================

    /**
     * Sets the minimum size for the main stage.
     *
     * @param width  the minimum width in pixels
     * @param height the minimum height in pixels
     * @throws IllegalStateException if main stage is not initialized
     */
    public void setStageMinSize(double width, double height) {
        checkInitialized();
        mainStage.setMinWidth(width);
        mainStage.setMinHeight(height);
    }

    /**
     * Sets the minimum size for the specified stage.
     * Falls back to main stage if the provided stage is null.
     *
     * @param stage  the stage to configure (may be null to use main stage)
     * @param width  the minimum width in pixels
     * @param height the minimum height in pixels
     * @throws IllegalStateException if main stage is not initialized and no stage
     *                               is provided
     */
    public void setStageMinSize(Stage stage, double width, double height) {
        checkInitialized();
        if (stage == null) {
            stage = mainStage;
        }
        stage.setMinWidth(width);
        stage.setMinHeight(height);
    }

    /**
     * Toggles the full screen state of the main stage.
     * Switches between full screen and windowed modes.
     *
     * @throws IllegalStateException if main stage is not initialized
     */
    public void toggleFullScreen() {
        checkInitialized();
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    /**
     * Toggles the full screen state of the specified stage.
     *
     * @param stage the stage to toggle
     * @throws IllegalStateException if main stage is not initialized
     */
    public void toggleFullScreen(Stage stage) {
        checkInitialized();
        stage.setFullScreen(!stage.isFullScreen());
    }

    /**
     * Sets the full screen state of the specified stage.
     *
     * @param stage      the stage to configure
     * @param fullScreen true for full screen, false for windowed
     * @throws IllegalStateException if main stage is not initialized
     */
    public void setFullScreen(Stage stage, boolean fullScreen) {
        checkInitialized();
        stage.setFullScreen(fullScreen);
    }

    /**
     * Closes the main application stage.
     * This typically terminates the application if it is the primary stage.
     */
    public void closeMainStage() {
        checkInitialized();
        mainStage.close();
    }

    // =========================================================================
    // SECTION 13: THEME
    // =========================================================================

    /**
     * Sets an optional theme applier that will be called on every new Scene created
     * by this FlowController. This allows the consuming application to apply any
     * CSS theme or styling framework without coupling CoreFx to a specific library.
     *
     * <p>
     * Example with MaterialFX:
     * </p>
     *
     * <pre>{@code
     * FlowController.getInstance()
     *         .setThemeApplier(scene -> MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY));
     * }</pre>
     *
     * <p>
     * Example with plain CSS:
     * </p>
     *
     * <pre>{@code
     * FlowController.getInstance()
     *         .setThemeApplier(scene -> scene.getStylesheets().add("/io/github/dinamo541/myapp/view/style.css"));
     * }</pre>
     *
     * @param themeApplier a Consumer that receives each new Scene for styling,
     *                     or null to disable theming
     */
    public void setThemeApplier(Consumer<Scene> themeApplier) {
        this.themeApplier = themeApplier;
    }

    // =========================================================================
    // SECTION 14: INTERNATIONALIZATION
    // =========================================================================

    /**
     * Registers the {@link ResourceBundle} used to localize every view loaded
     * afterwards. The bundle is passed to each {@link FXMLLoader}, so {@code %key}
     * references in the FXML resolve against it.
     *
     * <p>
     * Because cached loaders capture the bundle at creation time, this method
     * clears the loader cache; subsequent loads rebuild their views against the
     * new bundle, which makes runtime language switching work. Pass {@code null}
     * to load views without any bundle.
     * </p>
     *
     * <p>
     * Example:
     * </p>
     *
     * <pre>{@code
     * FlowController.getInstance()
     *         .setIdioma(ResourceBundle.getBundle("i18n.messages", new Locale("es")));
     * }</pre>
     *
     * @param idioma the resource bundle to apply, or {@code null} to disable
     *               localization
     */
    public void setIdioma(ResourceBundle idioma) {
        this.idioma = idioma;
        loaders.clear();
    }

    /**
     * Returns the currently registered localization bundle.
     *
     * @return the active {@link ResourceBundle}, or {@code null} if none is set
     */
    public ResourceBundle getIdioma() {
        return idioma;
    }

    // =========================================================================
    // SECTION 15: DATA TRANSFER
    // =========================================================================

    /**
     * Stores a value to be handed to the next view, replacing any value
     * previously stored. This lets a controller pass data to the controller it
     * navigates to without the two referencing each other directly.
     *
     * @param value the value to transfer; pass {@code null} to clear any pending
     *              value
     */
    public void setTransferValue(Object value) {
        this.transferValue = value;
    }

    /**
     * Returns the pending transfer value as an opaque {@link Object}.
     *
     * <p>
     * The value is not consumed by this call; it remains available until
     * overwritten or cleared via {@link #setTransferValue(Object)}. Prefer the
     * type-safe {@link #getTransferValue(Class)} when the expected type is known.
     * </p>
     *
     * @return the pending transfer value, or {@code null} if none is set
     */
    public Object getTransferValue() {
        return transferValue;
    }

    /**
     * Returns the pending transfer value cast to the requested type.
     *
     * @param <T>  the expected type of the transfer value
     * @param type the class object of the expected type; must not be {@code null}
     * @return the transfer value cast to {@code T}, or {@code null} if none is set
     * @throws NullPointerException  if {@code type} is {@code null}
     * @throws IllegalStateException if the stored value is not an instance of
     *                               {@code type}
     */
    public <T> T getTransferValue(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        Object current = transferValue;
        if (current == null) {
            return null;
        }
        if (!type.isInstance(current)) {
            throw new IllegalStateException(
                    "The transfer value is of type " + current.getClass().getName()
                            + " and cannot be viewed as " + type.getName() + ".");
        }
        return type.cast(current);
    }

    // =========================================================================
    // SECTION 16: STATE & ACCESSORS
    // =========================================================================

    /**
     * Returns whether the FlowController has been successfully initialized.
     *
     * @return true if initialize() has been called successfully, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns a reference to the main application stage.
     *
     * @return the main stage
     * @throws IllegalStateException if main stage is not initialized
     */
    public Stage getMainStage() {
        checkInitialized();
        return mainStage;
    }

    /**
     * Gets the application name constant.
     *
     * @return the application name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Gets the base path for FXML view files.
     *
     * @return the base view path
     */
    public String getBaseViewPath() {
        return baseViewPath;
    }

    /**
     * Gets the base path for application resources.
     *
     * @return the base resource path
     */
    public String getResourcePath() {
        return baseResourcePath;
    }

    /**
     * Gets the path to the application icon image.
     *
     * @return the application icon path
     */
    public String getAppIconPath() {
        return appIconPath;
    }

    // =========================================================================
    // SECTION 17: OBJECT CONTRACT
    // =========================================================================

    /**
     * Returns a string representation of this FlowController.
     *
     * @return string representation including main stage and cached loader keys
     */
    @Override
    public String toString() {
        return "FlowController{" +
                "mainStage=" + mainStage +
                ", loaders=" + loaders.keySet() +
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
     * Compares this {@code FlowController} singleton with another object for
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
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of FlowController is not supported");
    }

}
