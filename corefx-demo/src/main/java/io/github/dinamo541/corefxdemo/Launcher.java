/*
 * CoreFx Demo - local, runnable showcase for the CoreFx library.
 * This module is for development/testing only and is never published.
 */
package io.github.dinamo541.corefxdemo;

import javafx.application.Application;

/**
 * Plain entry point for the demo application.
 *
 * <p>
 * When a JavaFX {@link Application} subclass is launched directly from the
 * classpath (as opposed to the module path), the JavaFX runtime aborts with
 * <em>"JavaFX runtime components are missing"</em>. Routing startup through a
 * separate launcher class whose {@code main} is <b>not</b> an
 * {@link Application} subclass sidesteps that check, so the demo runs both from
 * an IDE "Run" button and from {@code mvn javafx:run}.
 * </p>
 */
public final class Launcher {

    /** Utility launcher; not meant to be instantiated. */
    private Launcher() {
    }

    /**
     * Launches the {@link DemoApp}.
     *
     * @param args standard command-line arguments, forwarded to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(DemoApp.class, args);
    }
}
