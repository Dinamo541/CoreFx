/*
 * CoreFx Demo - local, runnable showcase for the CoreFx library.
 * This module is for development/testing only and is never published.
 */
package io.github.dinamo541.corefxdemo;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

import io.github.dinamo541.corefx.navigation.AppContext;
import io.github.dinamo541.corefx.navigation.FlowController;
import io.github.dinamo541.corefx.navigation.StageManager;
import io.github.dinamo541.corefx.persistence.EntityManagerHelper;
import io.github.dinamo541.corefx.ui.AlertUtil;
import io.github.dinamo541.corefx.ui.AlertUtil.AlertTheme;
import io.github.dinamo541.corefx.ui.BindingUtils;
import io.github.dinamo541.corefx.ui.Format;
import io.github.dinamo541.corefx.ui.ImageUtil;
import io.github.dinamo541.corefx.ui.Message;
import io.github.dinamo541.corefx.ui.TableUtils;
import io.github.dinamo541.corefx.ui.ThemeManager;
import io.github.dinamo541.corefx.util.Answer;
import io.github.dinamo541.corefx.util.Validator;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Single-window showcase that exercises every functional CoreFx class against
 * its real public API, so the library can be evaluated visually and in practice
 * exactly as a consumer application would use it.
 *
 * <p>
 * Each tab targets one class. The code intentionally mirrors how a real consumer
 * would call the library: singletons via {@code getInstance()}, utility classes
 * via static methods, and so on. Tabs are built defensively — a failure while
 * building one tab is shown inside that tab instead of bringing the whole
 * application down.
 * </p>
 */
public class DemoApp extends Application {

    /** Light theme stylesheet, injected as a data URI (no CSS file needed). */
    private static final String LIGHT_CSS =
            ".root { -fx-base: #ececec; -fx-background: #f4f4f4; }";

    /** Dark theme stylesheet, injected as a data URI (no CSS file needed). */
    private static final String DARK_CSS =
            ".root { -fx-base: #2b2b2b; -fx-background: #1e1e1e;"
            + " -fx-text-background-color: #e6e6e6; -fx-accent: #4e8cff; }";

    @Override
    public void start(Stage primaryStage) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // One tab per class. Each builder is isolated so a single failure cannot
        // crash the showcase.
        addTab(tabs, "Overview", this::buildOverviewTab);
        addTab(tabs, "Answer", () -> buildAnswerTab());
        addTab(tabs, "Validator", () -> buildValidatorTab());
        addTab(tabs, "AppContext", () -> buildAppContextTab());
        addTab(tabs, "Format", () -> buildFormatTab());
        addTab(tabs, "BindingUtils", () -> buildBindingUtilsTab());
        addTab(tabs, "Message", () -> buildMessageTab(primaryStage));
        addTab(tabs, "AlertUtil", () -> buildAlertUtilTab(primaryStage));
        addTab(tabs, "ImageUtil", () -> buildImageUtilTab());
        addTab(tabs, "TableUtils", () -> buildTableUtilsTab());
        addTab(tabs, "ThemeManager", () -> buildThemeManagerTab());
        addTab(tabs, "StageManager", () -> buildStageManagerTab());
        addTab(tabs, "FlowController", () -> buildFlowControllerTab());
        addTab(tabs, "EntityManager", () -> buildEntityManagerTab());

        Scene scene = new Scene(tabs, 920, 640);

        // ThemeManager drives the whole window's light/dark switching live.
        ThemeManager themes = ThemeManager.getInstance();
        themes.registerTheme("light", dataCss(LIGHT_CSS));
        themes.registerTheme("dark", dataCss(DARK_CSS));
        themes.manage(scene);

        primaryStage.setTitle("CoreFx — Live Showcase");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(520);
        primaryStage.show();
    }

    // =====================================================================
    // Overview
    // =====================================================================

    private Region buildOverviewTab() {
        Label title = h1("CoreFx — Live Showcase");
        Label intro = body(
                "Every tab below drives one CoreFx class through its real public API, "
                + "the same way a project depending on the library would. Interact with "
                + "the controls to see the behaviour in practice.");

        Label implementedTitle = h2("Demonstrated classes");
        VBox implemented = new VBox(4,
                bullet("Answer — generic operation result wrapper (util)"),
                bullet("Validator — null-safe predicates & throwing contract checks (util)"),
                bullet("AppContext — global key/value application state (navigation)"),
                bullet("Format — live text-input formatters & locale formatting (ui)"),
                bullet("BindingUtils — bind a ToggleGroup to a property (ui)"),
                bullet("Message — classic, theme-free alert helper (ui)"),
                bullet("AlertUtil — self-themed alerts, dark/light/custom/raw CSS (ui)"),
                bullet("ImageUtil — load, shape and process images (ui)"),
                bullet("TableUtils — typed columns + live search filter (ui)"),
                bullet("ThemeManager — live, app-wide theme switching (ui)"),
                bullet("StageManager — window registry & life-cycle control (navigation)"),
                bullet("FlowController — scene creation + theme applier bridge (navigation)"),
                bullet("EntityManagerHelper — provider-agnostic persistence holder (persistence)"));

        VBox box = section(title, intro, implementedTitle, implemented);
        return scroll(box);
    }

    // =====================================================================
    // Answer (util)
    // =====================================================================

    private Region buildAnswerTab() {
        Label title = h1("Answer");
        Label desc = body("A generic result object carrying a state, a user message, an "
                + "internal message, and an arbitrary result map. Build one and inspect it.");

        TextArea output = readOnlyArea(8);

        Button success = new Button("Build a SUCCESS answer");
        success.setOnAction(e -> {
            // Uses the static factories + fluent with(...) builder, exactly as a
            // consumer would in real code.
            Answer answer = Answer.success("User saved successfully", "INSERT ok, id=42")
                    .with("id", 42)
                    .with("username", "sem")
                    .with("createdAt", LocalDate.now().toString());
            output.setText(render(answer));
        });

        Button failure = new Button("Build a FAILURE answer");
        failure.setOnAction(e -> {
            Answer answer = Answer.failure("The email is already in use",
                    "UNIQUE constraint violated on column 'email'")
                    .with("field", "email");
            output.setText(render(answer));
        });

        return scroll(section(title, desc, new HBox(10, success, failure), h2("Result"), output));
    }

    private String render(Answer answer) {
        StringBuilder sb = new StringBuilder();
        sb.append("state           = ").append(answer.getState()).append('\n');
        sb.append("isSuccess()     = ").append(answer.isSuccess()).append('\n');
        sb.append("message         = ").append(answer.getMessage()).append('\n');
        sb.append("internalMessage = ").append(answer.getInternalMessage()).append('\n');
        sb.append("results:\n");
        for (Map.Entry<String, Object> entry : answer.getResults().entrySet()) {
            sb.append("    ").append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
        }
        sb.append('\n').append("toString(): ").append(answer);
        return sb.toString();
    }

    // =====================================================================
    // Validator (util)
    // =====================================================================

    private Region buildValidatorTab() {
        Label title = h1("Validator");
        Label desc = body("A null-safe predicate suite plus throwing contract validators. "
                + "Type below to see every predicate evaluate live; the predicates never "
                + "throw, returning false for input that cannot satisfy them.");

        Validator v = Validator.getInstance();

        TextField input = field("Type anything: an e-mail, a number, a name…");
        TextArea predicates = readOnlyArea(9);
        Runnable evaluate = () -> {
            String s = input.getText();
            StringBuilder sb = new StringBuilder();
            sb.append("isBlank             = ").append(v.isBlank(s)).append('\n');
            sb.append("isNotBlank          = ").append(v.isNotBlank(s)).append('\n');
            sb.append("isNumeric           = ").append(v.isNumeric(s)).append('\n');
            sb.append("isInteger           = ").append(v.isInteger(s)).append('\n');
            sb.append("isDecimal           = ").append(v.isDecimal(s)).append('\n');
            sb.append("isAlphabetic        = ").append(v.isAlphabetic(s)).append('\n');
            sb.append("isAlphabeticWSpaces = ").append(v.isAlphabeticWithSpaces(s)).append('\n');
            sb.append("isAlphanumeric      = ").append(v.isAlphanumeric(s)).append('\n');
            sb.append("isEmail             = ").append(v.isEmail(s)).append('\n');
            sb.append("hasLengthBetween3-15= ").append(v.hasLengthBetween(s, 3, 15));
            predicates.setText(sb.toString());
        };
        input.textProperty().addListener((obs, old, now) -> evaluate.run());
        evaluate.run();

        // Contract validators: succeed silently or throw a descriptive exception,
        // which the demo catches and reports rather than crashing.
        Label contractResult = new Label();
        TextField ageField = field("Age 0–120 (try 200 to see it fail)");
        ageField.setTextFormatter(Format.getInstance().integerFormat());

        Button requireBlank = new Button("requireNotBlank(input, \"field\")");
        requireBlank.setOnAction(e -> contractResult.setText(run(() -> {
            v.requireNotBlank(input.getText(), "field");
            return "OK — \"" + input.getText() + "\" is not blank";
        })));

        Button requireRange = new Button("requireInRange(age, 0, 120)");
        requireRange.setOnAction(e -> contractResult.setText(run(() -> {
            long age = ageField.getText().isBlank() ? -1 : Long.parseLong(ageField.getText());
            v.requireInRange(age, 0, 120, "age");
            return "OK — age " + age + " is in range";
        })));

        return scroll(section(title, desc,
                labeled("Live input", input),
                h2("Predicates"), predicates,
                h2("Contract validators (throw on violation)"),
                labeled("Age", ageField),
                new HBox(10, requireBlank, requireRange),
                contractResult));
    }

    /** Runs a throwing action, returning either its success text or the caught error. */
    private String run(ThrowingSupplier action) {
        try {
            return action.get();
        } catch (RuntimeException ex) {
            return "Threw " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }

    /** A supplier whose body may throw a runtime exception. */
    @FunctionalInterface
    private interface ThrowingSupplier {
        String get();
    }

    // =====================================================================
    // AppContext (navigation)
    // =====================================================================

    private Region buildAppContextTab() {
        Label title = h1("AppContext");
        Label desc = body("A process-wide key/value store (singleton). Put, read and remove "
                + "entries; the live snapshot below reflects the shared context.");

        TextField key = new TextField();
        key.setPromptText("key");
        TextField value = new TextField();
        value.setPromptText("value");

        TextArea snapshot = readOnlyArea(6);
        AppContext ctx = AppContext.getInstance();
        Runnable refresh = () -> snapshot.setText(ctx.toString()
                + (key.getText().isBlank() ? "" :
                "\nget(\"" + key.getText().trim() + "\") = " + ctx.getOrDefault(key.getText().trim(), "<absent>")));

        Button put = new Button("put");
        put.setOnAction(e -> {
            if (!key.getText().isBlank()) {
                ctx.put(key.getText().trim(), value.getText());
                refresh.run();
            }
        });
        Button remove = new Button("remove");
        remove.setOnAction(e -> {
            if (!key.getText().isBlank()) {
                ctx.remove(key.getText().trim());
                refresh.run();
            }
        });
        Button clear = new Button("clear");
        clear.setOnAction(e -> {
            ctx.clear();
            refresh.run();
        });

        Label contains = new Label();
        Button check = new Button("contains?");
        check.setOnAction(e -> {
            if (!key.getText().isBlank()) {
                contains.setText("contains(\"" + key.getText().trim() + "\") = "
                        + ctx.contains(key.getText().trim()) + "   |   size = " + ctx.size());
            }
        });

        refresh.run();
        return scroll(section(title, desc,
                new HBox(10, key, value),
                new HBox(10, put, remove, clear, check),
                contains,
                h2("Live snapshot"), snapshot));
    }

    // =====================================================================
    // Format (ui)
    // =====================================================================

    private Region buildFormatTab() {
        Label title = h1("Format");
        Label desc = body("Pre-built TextFormatters that validate input as you type, plus "
                + "locale-aware date and decimal formatting. Try typing invalid characters — "
                + "they are simply rejected.");

        Format fmt = Format.getInstance();

        TextField integers = field("Digits only (e.g. 12345)");
        integers.setTextFormatter(fmt.integerFormat());

        TextField letters = field("Letters & single spaces, max 20");
        letters.setTextFormatter(fmt.lettersFormat(20));

        TextField id = field("National ID, alphanumeric + '-', max 15");
        id.setTextFormatter(fmt.idFormat(15));

        TextField decimals = field("Up to 2 decimals (e.g. 1,234.56)");
        decimals.setTextFormatter(fmt.twoDecimalFormat());

        TextField capped = field("Anything, hard max length 10");
        capped.setTextFormatter(fmt.maxLengthFormat(10));

        Label formatted = body(
                "Today (medium): " + LocalDate.now().format(fmt.formatDateMedium) + "\n"
                + "Today (short):  " + LocalDate.now().format(fmt.formatDateShort) + "\n"
                + "Decimal format: " + fmt.getDecimalFormat().format(1234567.89));

        return scroll(section(title, desc,
                labeled("Integer", integers),
                labeled("Letters", letters),
                labeled("ID / Cédula", id),
                labeled("Two decimals", decimals),
                labeled("Max length", capped),
                h2("Locale formatting"), formatted));
    }

    // =====================================================================
    // BindingUtils (ui)
    // =====================================================================

    private Region buildBindingUtilsTab() {
        Label title = h1("BindingUtils");
        Label desc = body("Binds a ToggleGroup to a property: selecting a radio updates the "
                + "bound value through each toggle's user data. The label below tracks the "
                + "property in real time.");

        ToggleGroup group = new ToggleGroup();
        RadioButton admin = radio("Administrator", "ADMIN", group);
        RadioButton editor = radio("Editor", "EDITOR", group);
        RadioButton viewer = radio("Viewer", "VIEWER", group);

        // Bound property; starts at EDITOR so the binding selects that toggle on bind.
        ObjectProperty<String> role = new SimpleObjectProperty<>("EDITOR");
        BindingUtils.bindToggleGroupToProperty(group, role);

        Label bound = h2("");
        bound.textProperty().bind(role.asString("Bound role = %s"));

        return scroll(section(title, desc, new VBox(6, admin, editor, viewer), bound));
    }

    // =====================================================================
    // Message (ui) — static utility
    // =====================================================================

    private Region buildMessageTab(Stage owner) {
        Label title = h1("Message");
        Label desc = body("The classic, theme-free alert helper, used purely through its "
                + "static methods: information, warning, error, and blocking confirmation / "
                + "yes-no dialogs. Results are reported below.");

        Label result = new Label();

        Button info = new Button("Information");
        info.setOnAction(e -> Message.show(AlertType.INFORMATION, "Information", "A non-blocking notice."));

        Button warn = new Button("Warning");
        warn.setOnAction(e -> Message.show(AlertType.WARNING, "Warning", "Something needs attention."));

        Button error = new Button("Error");
        error.setOnAction(e -> Message.show(AlertType.ERROR, "Error", "An operation failed."));

        Button confirm = new Button("Confirmation");
        confirm.setOnAction(e -> result.setText("showConfirmation returned: "
                + Message.showConfirmation("Confirm", owner, "Proceed with the action?")));

        Button yesNo = new Button("Yes / No");
        yesNo.setOnAction(e -> result.setText("askYesOrNoBoolean returned: "
                + Message.askYesOrNoBoolean("Question", owner, "Do you like CoreFx?")));

        return scroll(section(title, desc,
                new HBox(10, info, warn, error),
                new HBox(10, confirm, yesNo),
                result));
    }

    // =====================================================================
    // AlertUtil (ui)
    // =====================================================================

    private Region buildAlertUtilTab(Stage owner) {
        Label title = h1("AlertUtil");
        Label desc = body("Alerts that style themselves with no CSS file: the theme is "
                + "generated from colors in code and injected as a base64 data URI. Compare "
                + "the native look against the dark, custom, and raw-CSS variants.");

        AlertUtil alerts = AlertUtil.getInstance();
        Label result = new Label();

        Button plain = new Button("Info (native)");
        plain.setOnAction(e -> alerts.show(AlertType.INFORMATION, "Native", "Default platform styling."));

        Button dark = new Button("Info (dark)");
        dark.setOnAction(e -> alerts.show(AlertType.INFORMATION, "Dark", "Dark theme via data URI.",
                AlertTheme.dark()));

        Button light = new Button("Info (light)");
        light.setOnAction(e -> alerts.show(AlertType.INFORMATION, "Light", "Light preset theme.",
                AlertTheme.light()));

        Button custom = new Button("Info (custom)");
        custom.setOnAction(e -> alerts.show(AlertType.INFORMATION, "Custom",
                "Indigo background, white text, rounded buttons.",
                AlertTheme.builder()
                        .background(Color.web("#1e1b4b"))
                        .text(Color.web("#e0e7ff"))
                        .headerBackground(Color.web("#312e81"))
                        .headerText(Color.web("#ffffff"))
                        .border(Color.web("#4338ca"))
                        .buttonBackground(Color.web("#4338ca"))
                        .buttonText(Color.web("#ffffff"))
                        .buttonHover(Color.web("#6366f1"))
                        .fontSize(14.0)
                        .build()));

        Button rawCss = new Button("Raw CSS");
        rawCss.setOnAction(e -> alerts.showStyled(AlertType.INFORMATION, "Raw CSS",
                "Styled with an arbitrary stylesheet string.",
                ".dialog-pane { -fx-background-color: #3b0764; }"
                + " .dialog-pane .label { -fx-text-fill: #f5d0fe; }"
                + " .dialog-pane .button { -fx-background-color: #a21caf; -fx-text-fill: white; }"));

        Button confirmDark = new Button("Confirm (dark)");
        confirmDark.setOnAction(e -> result.setText("showConfirmation: "
                + alerts.showConfirmation("Confirm", owner, "Delete this record?", AlertTheme.dark())));

        Button yesNoDark = new Button("Yes/No (dark)");
        yesNoDark.setOnAction(e -> result.setText("askYesNo: "
                + alerts.askYesNo("Question", owner, "Apply changes now?", AlertTheme.dark())));

        CheckBox defaultDark = new CheckBox("Set dark as the default theme (then 'Info (native)' turns dark)");
        defaultDark.setOnAction(e -> {
            if (defaultDark.isSelected()) {
                alerts.setDefaultTheme(AlertTheme.dark());
            } else {
                alerts.clearDefaultTheme();
            }
        });

        return scroll(section(title, desc,
                new HBox(10, plain, dark, light, custom, rawCss),
                new HBox(10, confirmDark, yesNoDark),
                defaultDark,
                result));
    }

    // =====================================================================
    // ImageUtil (ui)
    // =====================================================================

    private Region buildImageUtilTab() {
        Label title = h1("ImageUtil");
        Label desc = body("Loading, shaping and pixel processing. Here a source image is "
                + "produced from a node snapshot, then shown plain, circular, rounded and "
                + "grayscale — all with native JavaFX only.");

        // A colourful node to act as the source image (snapshot needs valid bounds,
        // which it gets once this tab is laid out on screen).
        StackPane source = new StackPane();
        source.setPrefSize(160, 160);
        source.setMinSize(160, 160);
        source.setBackground(gradientBackground());
        Label glyph = new Label("Fx");
        glyph.setTextFill(Color.WHITE);
        glyph.setFont(Font.font("System", FontWeight.BOLD, 56));
        source.getChildren().add(glyph);

        HBox gallery = new HBox(16);
        gallery.setAlignment(Pos.CENTER_LEFT);

        Button generate = new Button("Snapshot the source → generate variants");
        generate.setOnAction(e -> {
            Image src = ImageUtil.snapshot(source);
            gallery.getChildren().setAll(
                    captioned("plain", ImageUtil.view(src, 120, 120)),
                    captioned("circular", ImageUtil.circularView(src, 60)),
                    captioned("rounded", ImageUtil.roundedView(src, 120, 120, 28)),
                    captioned("grayscale", ImageUtil.view(ImageUtil.toGrayscale(src), 120, 120)));
        });

        return scroll(section(title, desc,
                h2("Source node"), source,
                generate,
                h2("Variants"), gallery));
    }

    // =====================================================================
    // TableUtils (ui)
    // =====================================================================

    private Region buildTableUtilsTab() {
        Label title = h1("TableUtils");
        Label desc = body("Typed columns built from extractor functions, plus a live search "
                + "filter wired to a text field that coexists with column sorting. Type to "
                + "filter; click a header to sort.");

        TableView<Person> table = new TableView<>();
        TableUtils.addColumn(table, "Name", Person::name);
        TableUtils.addColumn(table, "Role", Person::role);
        TableUtils.addColumn(table, "Age", p -> p.age());
        TableUtils.setPlaceholder(table, "No matching people");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        ObservableList<Person> source = FXCollections.observableArrayList(
                new Person("Ada Lovelace", "Administrator", 36),
                new Person("Alan Turing", "Editor", 41),
                new Person("Grace Hopper", "Administrator", 85),
                new Person("Linus Torvalds", "Editor", 54),
                new Person("Margaret Hamilton", "Viewer", 88),
                new Person("Dennis Ritchie", "Viewer", 70));

        TextField search = field("Search by name or role…");
        TableUtils.installFilter(table, source, search,
                (person, query) -> {
                    String q = query.toLowerCase();
                    return person.name().toLowerCase().contains(q)
                            || person.role().toLowerCase().contains(q);
                });

        VBox box = section(title, desc, search, table);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        return scroll(box);
    }

    // =====================================================================
    // ThemeManager (ui)
    // =====================================================================

    private Region buildThemeManagerTab() {
        Label title = h1("ThemeManager");
        Label desc = body("Named themes applied across managed scenes with weak references. "
                + "This window's scene is managed — switch the active theme and the entire "
                + "application restyles live. Only registered theme stylesheets are touched.");

        ThemeManager themes = ThemeManager.getInstance();
        Label status = new Label();
        Runnable refresh = () -> status.setText("active = " + themes.getActiveTheme()
                + "   |   registered = " + themes.getThemeNames());

        Button light = new Button("Light");
        light.setOnAction(e -> {
            themes.setActiveTheme("light");
            refresh.run();
        });
        Button dark = new Button("Dark");
        dark.setOnAction(e -> {
            themes.setActiveTheme("dark");
            refresh.run();
        });
        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            themes.clearActiveTheme();
            refresh.run();
        });

        refresh.run();
        return scroll(section(title, desc, new HBox(10, light, dark, clear), status));
    }

    // =====================================================================
    // StageManager (navigation)
    // =====================================================================

    private Region buildStageManagerTab() {
        Label title = h1("StageManager");
        Label desc = body("A named registry of stages plus a thread-safe, uniform API for "
                + "their life cycle and window state. Open a managed window below, then drive "
                + "it through the manager — every call is marshalled onto the FX thread for you.");

        StageManager stages = StageManager.getInstance();
        final String key = "demo-secondary";

        Label status = new Label();
        Runnable refresh = () -> {
            Stage managed = stages.getStage(key);
            status.setText("registered keys = " + stages.getKeys()
                    + "   |   hasStage(\"" + key + "\") = " + stages.hasStage(key)
                    + (managed != null ? "   |   showing = " + managed.isShowing() : ""));
        };

        Button open = new Button("Register & show a window");
        open.setOnAction(e -> {
            if (!stages.hasStage(key)) {
                Stage secondary = new Stage();
                secondary.setTitle("Managed window");
                Label content = h2("I am a StageManager-managed window");
                content.setWrapText(true);
                VBox root = new VBox(content);
                root.setPadding(new Insets(24));
                root.setAlignment(Pos.CENTER);
                secondary.setScene(new Scene(root, 360, 200));
                stages.register(key, secondary);
            }
            stages.show(key);
            refresh.run();
        });

        Button center = new Button("Center on screen");
        center.setOnAction(e -> withManaged(stages, key, status, s -> stages.centerOnScreen(s)));

        Button maximize = new Button("Toggle maximize");
        maximize.setOnAction(e -> withManaged(stages, key, status, stages::toggleMaximized));

        Button onTop = new Button("Always-on-top ON");
        onTop.setOnAction(e -> withManaged(stages, key, status, s -> stages.setAlwaysOnTop(s, true)));

        Button half = new Button("Opacity 60%");
        half.setOnAction(e -> withManaged(stages, key, status, s -> stages.setOpacity(s, 0.6)));

        Button resize = new Button("Resize 500×320");
        resize.setOnAction(e -> withManaged(stages, key, status, s -> stages.setSize(s, 500, 320)));

        Button close = new Button("Close & unregister");
        close.setOnAction(e -> {
            if (stages.hasStage(key)) {
                stages.close(key);
            }
            refresh.run();
        });

        refresh.run();
        return scroll(section(title, desc,
                new HBox(10, open, center, maximize),
                new HBox(10, onTop, half, resize),
                close,
                h2("Manager state"), status));
    }

    /** Applies an action to the managed stage if present, then refreshes the status line. */
    private void withManaged(StageManager stages, String key, Label status,
            java.util.function.Consumer<Stage> action) {
        Stage stage = stages.getStage(key);
        if (stage == null) {
            status.setText("Open the managed window first.");
            return;
        }
        action.accept(stage);
        status.setText("registered keys = " + stages.getKeys()
                + "   |   showing = " + stage.isShowing());
    }

    // =====================================================================
    // FlowController (navigation)
    // =====================================================================

    private Region buildFlowControllerTab() {
        Label title = h1("FlowController");
        Label desc = body("The navigation core. Initialised once with its own throwaway "
                + "stage, wired with a theme applier, and used here to build a themed Scene "
                + "on demand. Full FXML navigation (goViewMain / goViewInWindow / "
                + "goViewInModal) additionally requires FXML view files and an app icon, "
                + "which a real consumer application provides.");

        FlowController flow = FlowController.getInstance();
        // initialize() must run exactly once; guard against repeated entry.
        if (!flow.isInitialized()) {
            flow.initialize(new Stage(), "CoreFxDemo", DemoApp.class);
            // Every scene FlowController creates is themed dark via a data URI.
            flow.setThemeApplier(scene -> scene.getStylesheets().add(dataCss(DARK_CSS)));
        }

        Label info = body(
                "isInitialized = " + flow.isInitialized() + "\n"
                + "appName       = " + flow.getAppName() + "\n"
                + "baseViewPath  = " + flow.getBaseViewPath() + "\n"
                + "resourcePath  = " + flow.getResourcePath());

        Button open = new Button("Open a window built with FlowController.createScene(...)");
        open.setOnAction(e -> {
            VBox content = new VBox(12,
                    h1("Created via FlowController"),
                    body("This window's Scene was produced by createScene(root); the "
                            + "registered theme applier styled it dark automatically."),
                    new Button("Close"));
            content.setPadding(new Insets(24));
            content.setAlignment(Pos.CENTER);
            ((Button) content.getChildren().get(2)).setOnAction(c -> ((Stage) content.getScene().getWindow()).close());

            Scene themed = flow.createScene(content);
            Stage popup = new Stage();
            popup.setTitle("FlowController.createScene");
            popup.setScene(themed);
            popup.setWidth(460);
            popup.setHeight(280);
            popup.show();
        });

        return scroll(section(title, desc, h2("Controller state"), info, open));
    }

    // =====================================================================
    // EntityManagerHelper (persistence)
    // =====================================================================

    private Region buildEntityManagerTab() {
        Label title = h1("EntityManagerHelper");
        Label desc = body("A provider-agnostic holder for a shared persistence context. The "
                + "consuming app supplies a factory; CoreFx stores, lazily creates, caches and "
                + "hands back the manager as an opaque object — so the library never depends on "
                + "JPA. Here the supplier produces a tiny fake \"manager\" so the lifecycle can "
                + "be exercised without a database.");

        EntityManagerHelper helper = EntityManagerHelper.getInstance();
        Label status = new Label();
        Runnable refresh = () -> status.setText(
                "isInitialized = " + helper.isInitialized()
                + "   |   hasManager = " + helper.hasManager());

        // initialize() may run only once for the whole process; guard it.
        Button init = new Button("initialize(supplier)");
        init.setOnAction(e -> {
            if (helper.isInitialized()) {
                status.setText("Already initialized — initialize() only runs once per process.");
                return;
            }
            helper.initialize(FakeManager::new);
            refresh.run();
        });

        TextArea output = readOnlyArea(5);

        Button get = new Button("getManager(FakeManager.class)");
        get.setOnAction(e -> output.setText(run(() -> {
            FakeManager em = helper.getManager(FakeManager.class);
            return "Got manager: " + em + "\n(repeated calls return the same cached instance)";
        })));

        Button reset = new Button("resetManager()");
        reset.setOnAction(e -> {
            helper.resetManager();
            output.setText("Cached manager discarded; next getManager() rebuilds a fresh one.");
            refresh.run();
        });

        Button close = new Button("close()");
        close.setOnAction(e -> {
            helper.close();
            output.setText("close() invoked AutoCloseable.close() on the manager and cleared it.");
            refresh.run();
        });

        refresh.run();
        return scroll(section(title, desc,
                new HBox(10, init, get),
                new HBox(10, reset, close),
                h2("Helper state"), status,
                h2("Output"), output));
    }

    /**
     * A stand-in for a real persistence manager (e.g. a JPA {@code EntityManager}).
     * It implements {@link AutoCloseable} so {@link EntityManagerHelper#close()} can
     * demonstrate best-effort shutdown without pulling in a persistence provider.
     */
    private static final class FakeManager implements AutoCloseable {
        private static int counter = 0;
        private final int id = ++counter;

        @Override
        public void close() {
            // No-op: a real EntityManager would release its connection here.
        }

        @Override
        public String toString() {
            return "FakeManager#" + id;
        }
    }

    // =====================================================================
    // Small UI helpers
    // =====================================================================

    /** A simple immutable row model for the TableUtils demo. */
    private record Person(String name, String role, int age) {
    }

    /**
     * Builds a tab from a supplier, isolating failures so one broken tab cannot
     * take down the whole showcase.
     */
    private void addTab(TabPane tabs, String name, java.util.function.Supplier<Region> builder) {
        Tab tab = new Tab(name);
        try {
            tab.setContent(builder.get());
        } catch (RuntimeException ex) {
            TextArea error = readOnlyArea(12);
            error.setText("Failed to build the '" + name + "' tab:\n\n" + ex);
            tab.setContent(error);
        }
        tabs.getTabs().add(tab);
    }

    private static String dataCss(String css) {
        String encoded = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        return "data:text/css;base64," + encoded;
    }

    private static VBox section(javafx.scene.Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(20));
        box.setFillWidth(true);
        return box;
    }

    private static ScrollPane scroll(Region content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        return pane;
    }

    private static Label h1(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 22));
        return label;
    }

    private static Label h2(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 15));
        return label;
    }

    private static Label body(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }

    private static Label bullet(String text) {
        return body("✓  " + text);
    }

    private static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    private static VBox labeled(String label, Region control) {
        return new VBox(3, new Label(label), control);
    }

    private static TextArea readOnlyArea(int rows) {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefRowCount(rows);
        area.setWrapText(true);
        area.setStyle("-fx-font-family: 'monospace';");
        return area;
    }

    private static RadioButton radio(String text, String userData, ToggleGroup group) {
        RadioButton button = new RadioButton(text);
        button.setUserData(userData);
        button.setToggleGroup(group);
        return button;
    }

    private static VBox captioned(String caption, ImageView view) {
        VBox box = new VBox(6, view, new Label(caption));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private static javafx.scene.layout.Background gradientBackground() {
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7c3aed")),
                new Stop(1, Color.web("#2563eb")));
        return new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(gradient, null, null));
    }
}
