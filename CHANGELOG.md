# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-06-14

### Added
- **Distribution**: Published to **Maven Central** under
  `io.github.dinamo541:corefx`. Releases are signed with GPG and uploaded through
  the Sonatype Central Portal via a `release` Maven profile.
- **CI**: Added a GitHub Actions workflow (`.github/workflows/build.yml`) that
  builds the project and runs the test suite on every push and pull request to `main`.
- **Tests**: Added a JUnit 5 unit-test suite (47 tests) covering the
  runtime-free classes: `Validator`, `Answer`, `AppContext`, and the non-UI
  surface of `Format`.
- **Build**: Stamped an `Automatic-Module-Name` (`io.github.dinamo541.corefx`)
  into the jar manifest so JPMS consumers get a stable module name without a
  full `module-info.java`.
- **API — `FlowController`**: Blocking modals (`goViewInModalAndWait`), a typed
  data-transfer slot (`setTransferValue` / `getTransferValue`), and
  internationalization via an injectable `ResourceBundle` (`setIdioma`, plus an
  `initialize` overload that accepts a locale bundle).
- **API — `AppContext`**: `putIfAbsent`, `getOrDefault`, `isEmpty`, `size`, and a
  generic, type-inferring `get`.
- **API — `Answer`**: Static factories (`ok`, `success`, `failure`), a fluent
  `with` builder, type-safe `getResult(key, type)`, and `copy()`.
- **API — `Validator`**: Full suite of null-safe predicates and throwing
  contract validators, including `requireInRange(double, ...)`.
- **Docs**: Added a Maven Central installation section (Maven + Gradle) to `README.md`.

### Changed
- **Docs**: Moved `CONTRIBUTING.md` into `.github/` to consolidate the
  GitHub community files, and updated the `README.md` link accordingly.
- **Environment**: Upgraded to **Java 25** and **JavaFX 25**.
- **Group/Package**: Reorganized folder structure and migrated packages to `io.github.dinamo541.corefx` from `cr.ac.una.corefx`. The Maven `groupId` remains `io.github.dinamo541`.
- **Build**: Added JUnit 5 (Jupiter) test dependencies and `maven-surefire-plugin` configuration.
- **Build**: Added Maven publication metadata (`<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`, `<scm>`) required by Maven Central.
- **Hardening (all packages)**: Sealed utility/singleton classes as `final`,
  enforced null-safety contracts with `Objects.requireNonNull`, pre-compiled
  regular expressions into reusable constants, and made `equals`/`hashCode`
  consistent with singleton identity.
- **`Format`**: `lettersFormat` is now Unicode-aware (accepts accented names such
  as *María*, *Ñoño*); `getDecimalFormat()` returns a defensive copy; primitive
  `int` parameters replace boxed `Integer` to remove auto-unboxing NPEs.
- **`Message`**: Converted from a singleton to a pure static utility class,
  consistent with the other `ui` helpers.
- **`Answer` / `AppContext`**: Made key and value null-handling consistent across
  the entire map API surface.
- **Docs**: Added `LICENSE` (MIT) and refreshed `.gitignore` / `licenseheader.txt`.

### Fixed
- **`FlowController`**: Fixed an `NullPointerException` in `prepareStage` when no
  application icon is present, added `checkInitialized` guards to every public
  navigation method, and corrected `equals`/`hashCode` to singleton identity.
- **`Message`**: `loadIcon` no longer throws an opaque `NullPointerException`
  when a resource is missing; it now delegates to `ImageUtil` and reports a clear
  `IllegalArgumentException`.
- **`Answer`**: Fixed inconsistent null-key handling where read methods silently
  accepted `null` keys while writes rejected them.

## [1.0.2] - 2026-06-10

### Added
- **Core Framework Foundation**: Initialized the project structure and base architecture for CoreFx.
- **Navigation System**:
    - `AppContext`: Central context for application state and shared data.
    - `FlowController`: Logic for managing transitions between different views/flows.
    - `StageManager`: Management of JavaFX stages and windowing operations.
- **Persistence Layer**:
    - `EntityManagerHelper`: Utility for managing JPA/Hibernate entity managers.
- **UI Utilities**:
    - `AlertUtil`: Simplified creation and display of alert dialogs.
    - `BindingUtils`: Helpers for JavaFX property binding.
    - `Format`: Utility for data and text formatting.
    - `ImageUtil`: Helpers for image loading and processing.
    - `Message`: System for managing application messages and notifications.
    - `TableUtils`: Utilities for configuring and managing JavaFX Tables.
- **Theming**:
    - `ThemeManager`: Centralized control for application themes and styles.
- **General Utilities**:
    - `Answer`: Standardized response wrapper for utility methods.
    - `Validator`: Suite of validation tools for data integrity.
- **Environment**:
    - Full support for **Java 21**.
    - Integrated **JavaFX 21.0.2**.
    - Maven multi-module project structure.

### Fixed
- Initial project configuration and dependency management in `pom.xml`.

---
[unreleased]: #unreleased
[1.2.0]: #120---2026-06-14
