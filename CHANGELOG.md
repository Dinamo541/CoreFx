# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.4.0] - 2026-08-07

### Added
- **Navigation — `FlowController`**: `hideMainStage()` and `showMainStage()` for
  hiding the primary window without destroying it and bringing it back later.
  Unlike `closeMainStage()` — which typically terminates the application — a
  hidden stage keeps its scene, size and position, so tray-style apps and
  splash/login flows can toggle the main window at will. Both validate that the
  controller has been initialized first.
- **Utilities — `Answer`**: four new shorthands that remove boilerplate from the
  most common cases.
  - `Answer(Boolean state)` — constructor for a bare state with no messages.
  - `Answer.notOk()` — the failure counterpart to the existing `ok()` ;).
  - `Answer.success(message, internalMessage, key, result)` and
    `Answer.failure(message, internalMessage, key, result)` — build a fully
    populated answer, including its first result entry, in a single call
    instead of chaining `.with(...)` afterwards. As with `setResult`, `key`
    must not be `null`.

### Changed
- **Utilities — `Answer`**: `ok()` now delegates to the new single-argument
  constructor. Behaviour is unchanged.
- **Utilities — `Answer` (source compatibility)**: because `Answer(Boolean)` and
  the copy constructor `Answer(Answer)` both accept a bare `null`, the call
  `new Answer(null)` is now ambiguous and no longer compiles. Binary
  compatibility is unaffected — already-compiled code keeps working — but source
  that used that form must disambiguate with a cast, e.g.
  `new Answer((Answer) null)`.
- **Javadoc**: normalized `@version` tags across all thirteen public classes to
  a three-part `X.Y.Z` form, so class versions read consistently with the
  project's semantic versioning.

### Documentation
- **Docs site**: documented the new `FlowController` and `Answer` members, and
  bumped the install snippets to `1.4.0`.

## [1.3.1] - 2026-08-06

### Fixed
- **Release pipeline**: Bumped `central-publishing-maven-plugin` from `0.6.0` to
  `0.11.0`. The Sonatype Central API started returning a `warnings` field that the
  old plugin could not deserialize, aborting the deploy goal with
  `UnrecognizedPropertyException: Unrecognized field "warnings"` even though the
  artifacts had already been uploaded. Publishing now completes cleanly.
- **Pages deployment**: The docs site is now deployed by an explicit
  `.github/workflows/pages.yml` instead of the auto-generated
  `pages-build-deployment` workflow. Three problems are addressed:
  - Deployments were queued for *every* push to `main`, including commits that
    never touched the site; a `paths: docs/**` filter stops that, which also
    removes the window in which one deployment cancels another
    (`Error: Deployment cancelled`).
  - Concurrent runs now queue (`cancel-in-progress: false`) rather than
    cancelling each other.
  - GitHub's Pages status API repeatedly failed to report a terminal state for
    this repo, leaving `actions/deploy-pages` polling until its hard 10-minute
    cap and aborting with `Timeout reached, aborting!` — despite the content
    publishing correctly every time. The workflow now stamps each build with the
    commit SHA (`deploy-id.txt`) and verifies the live site is serving it, so the
    run's pass/fail reflects the deployed site rather than the status API. Note
    the action's `timeout` input is capped at 600000 ms, so raising it is a no-op.

### Documentation
- **Docs site — `Controller`**: New page documenting the base class added in
  1.3.0: the re-runnable `initialize()` contract, the injected `stage`,
  `viewName` and `action`, `sendTabEvent`, and a table of the `FlowController`
  hooks that wire it up. Registered in the sidebar, the navigation package card
  grid, and the prev/next chain.
- **Docs site — `FlowController`**: Added a "Controller integration" section
  explaining why cached loaders make JavaFX call `initialize()` only once, and
  how extending `Controller` restores per-visit refresh. The stale-state callout
  now points at `Controller` as the built-in fix.
- **README**: Added a documentation-site badge linking to
  <https://dinamo541.github.io/CoreFx/>, and a `EntityManagerHelper` quick-start
  snippet covering supplier registration, typed retrieval and shutdown.

## [1.3.0] - 2026-08-06

### Added 
- **Navigation — `Controller`**: New abstract base class for FXML controllers. Exposes
  `stage`, `action`, and `viewName` metadata through getters/setters, and declares an
  abstract `initialize()` hook that subclasses implement to react every time their view
  is shown again.
- **API — `FlowController`**: Controllers that extend `Controller` are now wired up
  automatically. `viewName` is set as soon as a loader is created, the current `Stage`
  is (re-)injected on every navigation call (`goViewMain`, `changeViewInMain`,
  `goViewInWindow`, `goViewInModal`/`goViewInModalAndWait`, `changeViewInStage`,
  `changeViewInScene`, and `changeViewInBorderPane`), and `initialize()` is invoked
  again whenever a cached loader is reused — so controllers can refresh their state
  without any manual wiring.

## [1.2.1] - 2026-06-27

### Fixed
- **POM**: Renamed parent artifact from `CoreFx` to `CoreFx-parent` to eliminate a
  false Maven cycle error when consuming `corefx` as a dependency
  (`The parents form a cycle: …corefx → CoreFx → CoreFx`).

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
[1.2.1]: #121---2026-06-27
[1.2.0]: #120---2026-06-14
