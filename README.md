# CoreFx

CoreFx is a base framework designed to simplify the development of JavaFX applications, providing standardized utilities for navigation, persistence, UI management, and theming.

## 🚀 Features

The project is divided into several utility modules to accelerate the development of robust graphical interfaces:

### 🗺️ Navigation and Flow

- **AppContext**: Central context for application state and shared data.
- **FlowController**: Logic for managing transitions between different views and workflows.
- **StageManager**: Management of JavaFX stages and window operations.

### 💾 Persistence

- **EntityManagerHelper**: Utility for simplified management of JPA/Hibernate `EntityManagers`.

### 🎨 UI Utilities

- **ThemeManager**: Centralized control of application themes and CSS styles.
- **AlertUtil**: Simplified creation and display of alert dialogs.
- **BindingUtils**: Helpers for JavaFX property binding.
- **Format**: Utilities for formatting data and text.
- **ImageUtil**: Helpers for loading and processing images.
- **Message**: System for managing application messages and system notifications.
- **TableUtils**: Utilities for configuring and managing JavaFX `TableView` controls.

### 🛠️ General Utilities

- **Validator**: Validation tools to ensure data integrity.
- **Answer**: Standardized wrapper for utility method responses.

## 📦 Installation

CoreFx is distributed through [JitPack](https://jitpack.io). Add the JitPack
repository and the dependency to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.Dinamo541.CoreFx</groupId>
    <artifactId>io.github.dinamo541.corefx</artifactId>
    <version>v1.1.0</version>
  </dependency>
</dependencies>
```

> Replace `v1.1.0` with the [release tag](https://github.com/Dinamo541/CoreFx/releases)
> you want to use.

> **Note:** JavaFX is declared with `provided` scope, so your application must
> supply its own JavaFX runtime (the same version CoreFx targets, **JavaFX 25**).

## 🛠️ Technical Requirements

To compile and run CoreFx, you need:

- **Java**: JDK 25
- **JavaFX**: 25
- **Dependency Manager**: Apache Maven

## 📂 Project Structure

The project follows a Maven multi-module structure:

- `io.github.dinamo541.corefx/`: Main module containing all framework logic.
  - `src/main/java/io/github/dinamo541/corefx/`:
    - `navigation/`: Navigation and flow management.
    - `persistence/`: Database and entity management utilities.
    - `ui/`: UI components, themes, and view utilities.
    - `util/`: General-purpose utility classes.

## 🤝 Contributions

If you wish to contribute to CoreFx, please check our contribution guide:
👉 [CONTRIBUTING.md](./.github/CONTRIBUTING.md)

## 📜 Changelog

To check the latest updates and versions, view the changelog:
👉 [CHANGELOG.md](./CHANGELOG.md)

---

Developed by [Dominique](https://github.com/Dinamo541) under the [MIT License](./LICENSE).
