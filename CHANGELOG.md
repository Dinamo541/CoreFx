# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- **Environment**: Upgraded to **Java 25** and **JavaFX 25**.
- **Group/Package**: Reorganized folder structure and migrated packages to `io.github.dinamo541.corefx` from `cr.ac.una.corefx`. The Maven `groupId` remains `io.github.dinamo541`.
- **Build**: Added JUnit 5 (Jupiter) test dependencies and `maven-surefire-plugin` configuration.
- **Build**: Added Maven publication metadata (`<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`, `<scm>`) for Maven Central / JitPack compatibility.
- **Docs**: Added `LICENSE` (MIT) and refreshed `.gitignore` / `licenseheader.txt`.

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
