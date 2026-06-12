# Contributing to CoreFx

Thank you for your interest in contributing to CoreFx! We welcome contributions from everyone.

## Project Overview

CoreFx is a base framework designed to simplify the development of JavaFX applications, providing standardized utilities for navigation, persistence, UI management, and theming.

## Getting Started

### Prerequisites

Before contributing, ensure you have the following installed:
- **JDK 25** (Required)
- **Apache Maven** (Latest stable version)
- **JavaFX 25 SDK**

### Development Environment

We recommend using **Visual Studio Code** with the following extensions:
- Language Support for Java™ by Red Hat
- Java Debugger
- Maven for Java
- Java Test Runner

Refer to the `README.md` in the root directory for detailed VS Code configuration instructions.

## Contribution Guidelines

### 1. Code Quality
- **Standards**: Follow standard Java naming conventions (PascalCase for classes, camelCase for methods/variables).
- **Java Version**: Use Java 25 features where appropriate.
- **Consistency**: Maintain the existing project structure. New utilities should be placed in the `io.github.dinamo541.corefx` package hierarchy under their respective categories (`ui`, `navigation`, `util`, `persistence`).
- **Documentation**: Document public methods and classes using Javadoc.

### 2. Workflow
1. **Fork the Repository**: Create your own fork of the project.
2. **Create a Branch**: Use a descriptive name for your branch (e.g., `feat/new-ui-util` or `fix/navigation-bug`).
3. **Implement Changes**: Write your code and ensure it adheres to the project standards.
4. **Test Your Changes**: Ensure that your modifications do not break existing functionality.
5. **Submit a Pull Request**: Provide a clear description of the changes and link any related issues.

### 3. Commit Messages
We encourage clear and concise commit messages. For example:
- `feat: add input validation utility to Validator class`
- `fix: resolve memory leak in StageManager`
- `docs: update contributing guidelines`

## Project Structure

- `io.github.dinamo541.corefx/`: The main module containing all framework logic.
    - `src/main/java/io/github/dinamo541/corefx/`:
        - `navigation/`: Navigation and flow management.
        - `persistence/`: Database and entity management utilities.
        - `ui/`: UI components, themes, and view utilities.
        - `util/`: General-purpose utility classes.

---

By contributing to CoreFx, you agree that your contributions will be licensed under the project's open-source license.
