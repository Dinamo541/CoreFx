/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Utility class for loading and processing JavaFX images.
 *
 * <p>
 * The loader is deliberately forgiving about where an image lives: a location
 * containing a URI scheme (for example {@code https://}, {@code file:}) is used
 * verbatim, while any other value is first resolved against the classpath and
 * then, as a fallback, handed to JavaFX directly. This lets consumers reference
 * bundled resources ({@code "/img/logo.png"}), remote images, or local files
 * through a single, predictable API.
 * </p>
 *
 * <p>
 * Beyond loading, the class offers common view-shaping helpers (scaled, circular
 * and rounded {@link ImageView}s) and lightweight, pure-Java pixel processing
 * (grayscale conversion and node snapshots) that rely only on the native JavaFX
 * platform, so the library adds no external dependencies.
 * </p>
 *
 * <p>
 * This is a utility class and should not be instantiated.
 * </p>
 *
 * @author Dominique
 * @author Sem
 * @version 1.0
 * @since 2026/06/10
 */
public final class ImageUtil {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ImageUtil() {
    }

    // ---------------------------------------------------------------------
    // Loading
    // ---------------------------------------------------------------------

    /**
     * Loads an image from a classpath resource, a URL, or a local file path.
     *
     * @param location the image location (must not be {@code null} or blank)
     * @return the loaded {@link Image}
     * @throws IllegalArgumentException if {@code location} is {@code null}/blank
     *                                  or the image cannot be loaded
     */
    public static Image load(String location) {
        requireLocation(location);
        try {
            String resource = location.startsWith("/") ? location : "/" + location;
            InputStream stream = location.contains("://") ? null
                    : ImageUtil.class.getResourceAsStream(resource);
            if (stream != null) {
                return new Image(stream);
            }
            return new Image(resolveUrl(location));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to load image: " + location, ex);
        }
    }

    /**
     * Loads and rescales an image at load time, which is more memory-efficient
     * than loading at full size and scaling afterwards.
     *
     * @param location      the image location (must not be {@code null} or blank)
     * @param requestedWidth  the target width in pixels ({@code 0} to infer from
     *                        height while preserving ratio)
     * @param requestedHeight the target height in pixels ({@code 0} to infer from
     *                        width while preserving ratio)
     * @param preserveRatio whether to preserve the source aspect ratio
     * @param smooth        whether to apply higher-quality (slower) filtering
     * @return the loaded, rescaled {@link Image}
     * @throws IllegalArgumentException if {@code location} is {@code null}/blank
     *                                  or the image cannot be loaded
     */
    public static Image load(String location, double requestedWidth, double requestedHeight,
            boolean preserveRatio, boolean smooth) {
        requireLocation(location);
        try {
            return new Image(resolveUrl(location), requestedWidth, requestedHeight, preserveRatio, smooth);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to load image: " + location, ex);
        }
    }

    /**
     * Loads a classpath resource relative to the given anchor class. Useful for
     * images packaged next to a specific class rather than at the classpath root.
     *
     * @param anchor       the class used to resolve the resource (must not be
     *                     {@code null})
     * @param resourcePath the resource path relative to {@code anchor} (must not
     *                     be {@code null} or blank)
     * @return the loaded {@link Image}
     * @throws NullPointerException     if {@code anchor} is {@code null}
     * @throws IllegalArgumentException if the resource is missing or unreadable
     */
    public static Image load(Class<?> anchor, String resourcePath) {
        Objects.requireNonNull(anchor, "anchor cannot be null");
        requireLocation(resourcePath);
        try (InputStream stream = anchor.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException(
                        "Image resource not found: " + resourcePath + " (relative to " + anchor.getName() + ")");
            }
            return new Image(stream);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to load image resource: " + resourcePath, ex);
        }
    }

    /**
     * Loads an image, returning {@code null} instead of throwing when the image
     * is missing or unreadable. Suited to optional decorations where absence is
     * acceptable.
     *
     * @param location the image location (may be {@code null} or blank)
     * @return the loaded {@link Image}, or {@code null} if it cannot be loaded
     */
    public static Image loadOrNull(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        try {
            return load(location);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // View shaping
    // ---------------------------------------------------------------------

    /**
     * Wraps an image in an {@link ImageView} with smoothing enabled.
     *
     * @param image the image to wrap (must not be {@code null})
     * @return a new {@link ImageView}
     * @throws NullPointerException if {@code image} is {@code null}
     */
    public static ImageView view(Image image) {
        Objects.requireNonNull(image, "image cannot be null");
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        return view;
    }

    /**
     * Wraps an image in an {@link ImageView} scaled to fit the given box while
     * preserving its aspect ratio.
     *
     * @param image     the image to wrap (must not be {@code null})
     * @param fitWidth  the target width in pixels
     * @param fitHeight the target height in pixels
     * @return a new, scaled {@link ImageView}
     * @throws NullPointerException if {@code image} is {@code null}
     */
    public static ImageView view(Image image, double fitWidth, double fitHeight) {
        ImageView view = view(image);
        view.setPreserveRatio(true);
        view.setFitWidth(fitWidth);
        view.setFitHeight(fitHeight);
        return view;
    }

    /**
     * Creates a circular {@link ImageView}, clipping the image to a centered
     * circle of the given radius. Commonly used for avatars.
     *
     * @param image  the image to wrap (must not be {@code null})
     * @param radius the circle radius in pixels (must be positive)
     * @return a new, circularly-clipped {@link ImageView}
     * @throws NullPointerException     if {@code image} is {@code null}
     * @throws IllegalArgumentException if {@code radius} is not positive
     */
    public static ImageView circularView(Image image, double radius) {
        Objects.requireNonNull(image, "image cannot be null");
        if (!(radius > 0)) {
            throw new IllegalArgumentException("radius must be positive");
        }
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        view.setPreserveRatio(false);
        view.setFitWidth(radius * 2);
        view.setFitHeight(radius * 2);
        view.setClip(new Circle(radius, radius, radius));
        return view;
    }

    /**
     * Creates a rounded-rectangle {@link ImageView}, clipping the image to the
     * given size with rounded corners.
     *
     * @param image  the image to wrap (must not be {@code null})
     * @param width  the target width in pixels (must be positive)
     * @param height the target height in pixels (must be positive)
     * @param arc    the corner arc size in pixels (clamped to non-negative)
     * @return a new, rounded {@link ImageView}
     * @throws NullPointerException     if {@code image} is {@code null}
     * @throws IllegalArgumentException if {@code width} or {@code height} is not
     *                                  positive
     */
    public static ImageView roundedView(Image image, double width, double height, double arc) {
        Objects.requireNonNull(image, "image cannot be null");
        if (!(width > 0) || !(height > 0)) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        double safeArc = Math.max(0.0, arc);
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        view.setPreserveRatio(false);
        view.setFitWidth(width);
        view.setFitHeight(height);
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(safeArc);
        clip.setArcHeight(safeArc);
        view.setClip(clip);
        return view;
    }

    // ---------------------------------------------------------------------
    // Pixel processing
    // ---------------------------------------------------------------------

    /**
     * Produces a grayscale copy of the given image using the standard luminance
     * weights, leaving the original unchanged. If the image cannot be read it is
     * returned as-is.
     *
     * @param source the image to convert (must not be {@code null})
     * @return a grayscale {@link Image}, or {@code source} if it cannot be read
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public static Image toGrayscale(Image source) {
        Objects.requireNonNull(source, "source cannot be null");
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        PixelReader reader = source.getPixelReader();
        if (width <= 0 || height <= 0 || reader == null) {
            return source;
        }
        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double luminance = 0.299 * color.getRed()
                        + 0.587 * color.getGreen()
                        + 0.114 * color.getBlue();
                writer.setColor(x, y, new Color(luminance, luminance, luminance, color.getOpacity()));
            }
        }
        return output;
    }

    /**
     * Renders a snapshot of the given node into an image with a transparent
     * background. Must be called on the JavaFX Application Thread.
     *
     * @param node the node to capture (must not be {@code null})
     * @return a {@link WritableImage} containing the rendered node
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public static WritableImage snapshot(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return node.snapshot(parameters, null);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    /**
     * Resolves a location string to a URL usable by the {@link Image}
     * constructors. Classpath resources are converted to their external form;
     * everything else is returned unchanged for JavaFX to resolve.
     *
     * @param location the location to resolve
     * @return a URL string suitable for {@link Image}
     */
    private static String resolveUrl(String location) {
        if (location.contains("://")) {
            return location;
        }
        String resource = location.startsWith("/") ? location : "/" + location;
        URL url = ImageUtil.class.getResource(resource);
        return url != null ? url.toExternalForm() : location;
    }

    /**
     * Validates an image location argument.
     *
     * @param location the location to validate
     * @throws IllegalArgumentException if {@code location} is {@code null} or
     *                                  blank
     */
    private static void requireLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Image location cannot be null or blank");
        }
    }

    /**
     * Returns a string representation of this utility class.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "ImageUtil{}";
    }

    /**
     * Computes the hash code for this utility class.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    /**
     * Compares this utility class with another object for equality.
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
     * Prevents cloning of this utility class.
     *
     * @return never returns normally
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of ImageUtil is not supported");
    }

}
