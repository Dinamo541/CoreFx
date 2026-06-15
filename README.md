<div align="center">
   <img width=100% src=https://capsule-render.vercel.app/api?type=waving&height=100&color=gradient&reversal=true />

# CoreFx

### The foundation layer for JavaFX applications

_Navigation, persistence, theming, i18n, and UI utilities — wired together so you can ship screens, not boilerplate._

<br/>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dinamo541/corefx?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/io.github.dinamo541/corefx)
[![Build](https://github.com/Dinamo541/CoreFx/actions/workflows/build.yml/badge.svg)](https://github.com/Dinamo541/CoreFx/actions/workflows/build.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-25-1f9bcf?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-3fb950)

</div>

---

## 📖 Table of Contents

- [What is CoreFx?](#-what-is-corefx)
- [Installation](#-installation) ⭐
  - [Maven](#maven)
  - [Gradle (Groovy DSL)](#gradle-groovy-dsl)
  - [Gradle (Kotlin DSL)](#gradle-kotlin-dsl)
  - [⚠️ The JavaFX requirement](#️-the-javafx-requirement)
- [Quick Start](#-quick-start)
- [The Modules](#-the-modules)
  - [Navigation & Flow](#️-navigation--flow)
  - [UI Utilities](#-ui-utilities)
  - [General Utilities](#️-general-utilities)
  - [Persistence](#-persistence)
- [Recipes](#-recipes)
- [Requirements](#-requirements)
- [Project Structure](#-project-structure)
- [Building from Source](#-building-from-source)
- [Contributing](#-contributing)
- [License](#-license)

---

## 💡 What is CoreFx?

Every JavaFX application re-implements the same plumbing: a singleton to switch
screens, a place to stash the logged-in user, helpers to wire up tables, a way to
theme scenes, and a validation toolkit. **CoreFx is that plumbing, done once and
done well.**

It is a small, dependency-free library (JavaFX aside) that gives you:

|                                 |                                                                                                                                                                               |
| ------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 🗺️ **A navigation engine**      | One `FlowController` to load FXML views, swap scenes, open modals (blocking or not), and manage windows — with a cached `FXMLLoader` per view so controllers are reachable.   |
| 🧠 **Shared application state** | A thread-safe `AppContext` key-value store for the current user, selected records, and feature flags — without coupling unrelated screens.                                    |
| 🎨 **Live theming**             | A `ThemeManager` that applies named CSS theme sets to any scene and re-applies them on the fly, tracking scenes with weak references so nothing leaks.                        |
| 🌍 **Internationalization**     | Inject a `ResourceBundle` once and every FXML view loads localized — switch locale at runtime.                                                                                |
| 🧰 **Battle-tested utilities**  | Null-safe `Validator`, a standardized `Answer` response wrapper, type-safe `TableUtils`, `Format` input filters (Unicode-aware), alerts, image loading, and property binding. |

Each class is `final`, null-safe by contract, and documented. The runtime-free
classes ship with a **47-test JUnit suite**.

---

## 📦 Installation

CoreFx is published to **Maven Central**, so it works out of the box — no extra
repositories to configure. Use the latest version: **`1.2.0`**.

### Maven

```xml
<dependency>
  <groupId>io.github.dinamo541</groupId>
  <artifactId>corefx</artifactId>
  <version>1.2.0</version>
</dependency>
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.dinamo541:corefx:1.2.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.dinamo541:corefx:1.2.0")
}
```

### ⚠️ The JavaFX requirement

> CoreFx declares JavaFX with **`provided`** scope — it does **not** bundle a
> JavaFX runtime. This is deliberate: your application controls which JavaFX
> version (and OS classifier) it ships with. **You must add JavaFX yourself**, or
> you will hit `NoClassDefFoundError` / `ClassNotFoundException` at runtime.
>
> Target the same major version CoreFx is built against: **JavaFX 25**.

<details>
<summary><b>Maven — adding JavaFX</b></summary>

```xml
<dependencies>
  <dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>25</version>
  </dependency>
  <dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>25</version>
  </dependency>
</dependencies>
```

</details>

<details>
<summary><b>Gradle — adding JavaFX (via the JavaFX plugin)</b></summary>

```kotlin
plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
```

</details>

> **Module path users:** CoreFx ships an `Automatic-Module-Name` of
> `io.github.dinamo541.corefx`, so it works on the JPMS module path without a
> `module-info.java`.

---

## 🚀 Quick Start

A complete, minimal JavaFX application backed by CoreFx:

```java
import io.github.dinamo541.corefx.navigation.FlowController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MyApp extends Application {

    @Override
    public void start(Stage stage) {
        FlowController flow = FlowController.getInstance();

        flow.initialize(
            stage,                         // the primary stage
            "My Application",              // window title
            "/com/example/views/",        // folder holding your .fxml files
            "/com/example/resources/",    // folder holding other resources
            "/com/example/icon.png",      // application icon
            MyApp.class                    // class used to resolve resources
        );

        // Loads /com/example/views/Home.fxml, builds the scene,
        // and shows the stage automatically.
        flow.goViewMain("Home");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

From any controller, navigate without touching `FXMLLoader` again:

```java
FlowController flow = FlowController.getInstance();

flow.goViewMain("Dashboard");        // swap the main scene's content
flow.goViewInModal("EditUser");      // open a non-blocking modal
flow.goViewInModalAndWait("Confirm");// open a blocking modal, wait for it to close
```

---

## 🧱 The Modules

CoreFx is organized into four packages under `io.github.dinamo541.corefx`.

### 🗺️ Navigation & Flow

> `io.github.dinamo541.corefx.navigation`

| Class                | Responsibility                                                                                                                                                                                                                       |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`FlowController`** | The heart of the library. Loads & caches FXML views, swaps scenes, opens windows and modals (blocking & non-blocking), swaps regions of a `BorderPane`, manages full-screen and min-size, and carries i18n + a typed transfer value. |
| **`AppContext`**     | Thread-safe, process-wide key-value store for shared application state. Backed by `ConcurrentHashMap`; keys must be non-blank, values non-null.                                                                                      |
| **`StageManager`**   | Helpers for creating, configuring, and controlling JavaFX stages and windows.                                                                                                                                                        |

### 🎨 UI Utilities

> `io.github.dinamo541.corefx.ui`

| Class              | Responsibility                                                                                                                           |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------- |
| **`ThemeManager`** | Register named CSS theme sets, activate one, and apply it to scenes. Live-switches all managed scenes; tracks them with weak references. |
| **`AlertUtil`**    | Themed alert/confirmation dialogs (recommended for new code).                                                                            |
| **`Message`**      | Lightweight, theme-free alert/confirmation dialogs — a static utility.                                                                   |
| **`TableUtils`**   | Type-safe `TableView` setup: lambda-based columns, items, selection, placeholders, and a ready-made live search filter.                  |
| **`Format`**       | `TextFormatter` input filters (integers, 2-decimal, IDs, Unicode-aware letters, max-length) plus shared date/decimal formatters.         |
| **`BindingUtils`** | Two-way binding between a `ToggleGroup` and an `ObjectProperty`.                                                                         |
| **`ImageUtil`**    | Robust image loading from classpath resources, URLs, and local files.                                                                    |

### 🛠️ General Utilities

> `io.github.dinamo541.corefx.util`

| Class           | Responsibility                                                                                                                                                                        |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`Validator`** | A null-safe predicate suite (`isBlank`, `isEmail`, `isInRange`, …) **and** throwing contract validators (`requireNotBlank`, `requireInRange`, …). Regex pre-compiled and linear-time. |
| **`Answer`**    | An immutable-friendly response wrapper: success/failure state, user + internal messages, and a keyed result payload with a fluent builder.                                            |

### 💾 Persistence

> `io.github.dinamo541.corefx.persistence`

| Class                     | Responsibility                                                    |
| ------------------------- | ----------------------------------------------------------------- |
| **`EntityManagerHelper`** | Simplified management of JPA/Hibernate `EntityManager` instances. |

---

## 🍳 Recipes

<details open>
<summary><b>Share state across screens with <code>AppContext</code></b></summary>

```java
AppContext ctx = AppContext.getInstance();

// After login:
ctx.put("currentUser", user);

// Anywhere else — the type is inferred:
User current = ctx.get("currentUser");
String role  = ctx.getOrDefault("role", "guest");
```

</details>

<details>
<summary><b>Apply and live-switch themes</b></summary>

```java
ThemeManager themes = ThemeManager.getInstance();
themes.registerTheme("dark",  "/app/css/dark.css");
themes.registerTheme("light", "/app/css/light.css");
themes.setActiveTheme("dark");

// Let every scene FlowController builds be themed automatically:
FlowController.getInstance().setThemeApplier(themes.asApplier());

// Flip the theme at runtime — all managed scenes update instantly:
themes.setActiveTheme("light");
```

</details>

<details>
<summary><b>Localize your views (i18n)</b></summary>

```java
ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("es"));

// Pass it at init time...
flow.initialize(stage, "Mi App", "/views/", "/res/", "/icon.png",
                MyApp.class, themes.asApplier(), bundle);

// ...or switch the language later (clears the view cache for you):
flow.setIdioma(ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH));
```

FXML `%key` references resolve automatically.

</details>

<details>
<summary><b>Pass a value between views</b></summary>

```java
// Sending screen:
flow.setTransferValue(selectedInvoice);
flow.goViewInModalAndWait("InvoiceDetail");

// Receiving controller:
Invoice invoice = flow.getTransferValue(Invoice.class);
```

</details>

<details>
<summary><b>Validate input and return a structured result</b></summary>

```java
Validator v = Validator.getInstance();

if (!v.isEmail(emailField.getText())) {
    return Answer.failure("Please enter a valid e-mail address");
}

return Answer.success("Account created")
             .with("userId", newUser.getId())
             .with("user", newUser);
```

</details>

<details>
<summary><b>Wire up a searchable table</b></summary>

```java
TableUtils.addColumn(table, "Name",  Person::getName);
TableUtils.addColumn(table, "Email", Person::getEmail);

ObservableList<Person> data = TableUtils.setItems(table, people);
TableUtils.installFilter(table, data, searchField,
        (person, query) -> person.getName().toLowerCase().contains(query.toLowerCase()));
```

</details>

<details>
<summary><b>Restrict a text field to Unicode letters</b></summary>

```java
// Accepts "María", "Ñoño", "José" — rejects digits & symbols, max 50 chars:
nameField.setTextFormatter(Format.getInstance().lettersFormat(50));
```

</details>

---

## 🧪 Requirements

| Tool           | Version                             |
| -------------- | ----------------------------------- |
| **JDK**        | 25                                  |
| **JavaFX**     | 25 _(provided by your application)_ |
| **Build tool** | Maven or Gradle                     |

---

## 📂 Project Structure

This repository is a Maven multi-module project. The published artifact is the
`corefx` core module.

```
CoreFx/
├── pom.xml                       # Parent POM — versions, plugins, metadata
├── .github/
│   ├── workflows/build.yml       # CI: build + test on every push / PR
│   └── CONTRIBUTING.md
├── CHANGELOG.md
├── LICENSE                       # MIT
└── corefx/                       # ← the published module
    ├── pom.xml
    └── src/
        ├── main/java/io/github/dinamo541/corefx/
        │   ├── navigation/       # FlowController, AppContext, StageManager
        │   ├── persistence/      # EntityManagerHelper
        │   ├── ui/               # ThemeManager, AlertUtil, Message, TableUtils, …
        │   └── util/             # Validator, Answer
        └── test/java/…           # JUnit 5 suite (runtime-free classes)
```

---

## 🔨 Building from Source

```bash
# Clone
git clone https://github.com/Dinamo541/CoreFx.git
cd CoreFx

# Build, run tests, and install to your local Maven repo
mvn install

# Run just the tests
mvn -pl corefx test
```

The CI workflow runs `mvn -B verify` on Temurin JDK 25 for every push and pull
request to `main`.

---

## 🤝 Contributing

Contributions are welcome! Please read the contribution guide first:
👉 [CONTRIBUTING.md](./.github/CONTRIBUTING.md)

For the full version history, see the
📝 [CHANGELOG.md](./CHANGELOG.md).

---

## 📜 License

Released under the **MIT License**. See [LICENSE](./LICENSE) for details.

<div align="center">
<br/>
Built with care by <a href="https://github.com/Dinamo541">Dominique</a>
</div>
