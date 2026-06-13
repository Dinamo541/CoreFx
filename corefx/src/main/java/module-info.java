module io.github.dinamo541.corefx {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;

    exports io.github.dinamo541.corefx.navigation;
    exports io.github.dinamo541.corefx.persistence;
    exports io.github.dinamo541.corefx.ui;
    exports io.github.dinamo541.corefx.util;
}