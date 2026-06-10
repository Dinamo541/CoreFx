# VS Code Configuration for CoreFx Project

This directory contains Visual Studio Code workspace settings configured for the CoreFx Java/JavaFX Maven project.

## Files Included

### `settings.json`
- Java runtime configuration (JDK 21)
- Maven integration settings
- JavaFX SDK path configuration
- Editor preferences (tabs, formatting, etc.)
- File associations for FXML files
- Search exclusions for build artifacts

### `launch.json`
Debug configurations:
- **Launch Current File**: Runs the currently selected Java file
- **Launch AppContext**: Launches the main navigation class
- **Launch JavaFX Application**: Configures VM arguments for JavaFX modules

### `tasks.json`
Maven tasks accessible via Terminal → Run Task:
- `maven: compile` - Compile the core module
- `maven: test` - Run tests
- `maven: package` - Package the application
- `maven: clean` - Clean build directory
- `maven: clean package` - Full rebuild
- `maven: run` - Execute the JavaFX application

### `extensions.json`
Recommended extensions for this workspace:
- Language Support for Java™ by Red Hat
- Java Debugger (vscjava.vscode-java-debug)
- Java Test Runner (vscjava.vscode-java-test)
- Maven for Java (vscjava.vscode-maven)
- GitHub Pull Requests
- Prettier - Code formatter
- EditorConfig

### `snippets/`
Code snippets for faster development:
- `javafx.code-snippets`: JavaFX FXML controllers, event handlers, etc.
- `java.code-snippets`: Common Java constructs (main method, loops, getters/setters, etc.)
- `xml.code-snippets`: FXML templates and controls

## Usage Instructions

1. **Update Paths**: Before using, update the following paths to match your local installation:
   - In `settings.jsn`: 
     - `"java.home"` - Path to your JDK 21 installation
     - `"javafx.runtimePath"` - Path to your JavaFX SDK lib directory
   - In `launch.json`: 
     - Update the `--module-path` argument to point to your JavaFX SDK lib directory

2. **Install Recommendations**: When opening the workspace, VS Code will prompt to install recommended extensions.

3. **Using Tasks**: Access Maven tasks via:
   - Terminal → Run Task...
   - Or use the command palette (Ctrl+Shift+P) → "Tasks: Run Task"

4. **Debugging**: Use the debug configurations in `launch.json` to start debugging sessions.

5. **Snippets**: Trigger snippets by typing their prefix and pressing Tab:
   - JavaFX: `fxcontroller`, `fxevent`, `fxinject`, etc.
   - Java: `main`, `sout`, `fori`, `foreach`, `try`, `get`, `set`, `ctor`
   - XML/FXML: `fxroot`, `fxbutton`, `fxlabel`, `fxtextfield`, `fxvbox`, `fxhbox`

## Notes

- This configuration assumes a standard Maven project structure with the core module in the `core/` directory.
- JavaFX SDK must be installed separately and the path configured in the settings.
- The configurations are workspace-specific and will not affect your global VS Code settings.