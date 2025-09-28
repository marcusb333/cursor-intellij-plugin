# Cursor AI IntelliJ Plugin

This plugin integrates Cursor's powerful AI chatbot directly into IntelliJ IDEA, providing intelligent code assistance, generation, and explanation capabilities.

## Features

- 🤖 **AI-Powered Code Suggestions**: Get intelligent code completions and suggestions
- 💬 **Natural Language Code Generation**: Generate code using natural language descriptions
- 🔍 **Context-Aware Responses**: AI responses based on your current codebase and context
- ⚡ **Real-Time Assistance**: Get help while coding without leaving your IDE
- 📝 **Code Explanation**: Understand complex code with AI-powered explanations
- 🐛 **Bug Detection**: Identify and get suggestions for fixing code issues

## Installation

### Prerequisites

- IntelliJ IDEA (any version - no version restrictions)
- Java 21 or later
- Cursor API key (get one from [Cursor](https://cursor.sh))

### Building from Source

1. Clone this repository:

   ```bash
   git clone https://github.com/your-username/cursor-intellij-plugin.git
   cd cursor-intellij-plugin
   ```

2. Set your Cursor API key as an environment variable:

   ```bash
   export CURSOR_API_KEY=your_api_key_here
   ```

3. Build the plugin using the build script (recommended):

   ```bash
   ./build.sh all
   ```

   Or use Gradle directly:

   ```bash
   ./gradlew buildPlugin
   ```

   To build for a specific IntelliJ platform:
   
   ```bash
   # Build for IntelliJ IDEA Community Edition
   ./gradlew buildPlugin -PplatformType=IC
   
   # Build for IntelliJ IDEA Ultimate Edition (default)
   ./gradlew buildPlugin -PplatformType=IU
   ```

4. Install the plugin:
   - Go to File → Settings → Plugins
   - Click the gear icon → Install Plugin from Disk
   - Select the generated `.zip` file from `build/distributions/`

### Build Script Commands

The project includes a comprehensive build script (`build.sh`) that provides the following commands:

```bash
# Check prerequisites and configuration
./build.sh check

# Clean build artifacts
./build.sh clean

# Run all tests
./build.sh test

# Build the plugin JAR
./build.sh build

# Create distributable plugin ZIP
./build.sh dist

# Run plugin in development IDE
./build.sh run

# Verify plugin structure and configuration
./build.sh verify

# Complete build pipeline (clean, test, build, dist)
./build.sh all

# Show help and available commands
./build.sh help
```

The build script provides colored output, error checking, and will verify that you have the correct Java version (21+) and Gradle wrapper before running any commands.

## Usage

### Opening the Cursor AI Panel

1. **Via Menu**: Tools → Open Cursor AI
2. **Via Keyboard Shortcut**: `Ctrl+Shift+C` (Windows/Linux) or `Cmd+Shift+C` (Mac)
3. **Via Tool Window**: View → Tool Windows → Cursor AI

### Using Context Menu Actions

- **Generate Code**: Right-click in editor → Generate Code with Cursor
- **Explain Code**: Select code → Right-click → Explain Code with Cursor

### Chat Interface

The Cursor AI panel provides a chat interface where you can:

- Ask questions about your code
- Request code generation
- Get explanations for complex logic
- Ask for bug fixes and optimizations

## Configuration

### API Key Setup

The plugin requires a Cursor API key to function. You can set it in one of these ways:

1. **Environment Variable** (recommended):

   ```bash
   export CURSOR_API_KEY=your_api_key_here
   ```

2. **Plugin Settings**:
   - File → Settings → Other Settings → Cursor AI
   - Enter your API key in the settings panel

## Development

### Release Management

This project includes a comprehensive release workflow system with automated version management, building, testing, signing, and publishing capabilities.

#### Quick Release Process

1. **Automated Release** (Recommended):
   ```bash
   # The system automatically detects changes and creates version bump PRs
   git push origin main
   # Review and merge the version bump PR
   # Create a tag: git tag v0.0.6 && git push origin v0.0.6
   # GitHub Actions automatically builds and creates the release
   ```

2. **Manual Release**:
   ```bash
   # Use the release script for manual control
   ./release.sh check          # Check prerequisites
   ./release.sh release 0.0.6  # Complete release process
   ./release.sh publish 0.0.6  # Publish to JetBrains Marketplace
   ```

#### Release Workflows

The project includes several GitHub Actions workflows:

- **Version Bump Workflow** - Automated version management and PR creation
- **Comprehensive Release Workflow** - Full release pipeline with multi-platform building
- **Marketplace Publishing Workflow** - JetBrains Marketplace publishing
- **Build and Test Workflow** - Continuous integration

See [RELEASE_WORKFLOWS.md](RELEASE_WORKFLOWS.md) for detailed documentation.

### Version Management

This project uses centralized version management. The version is defined in `src/main/resources/META-INF/plugin.xml` and automatically synchronized across all files using the `sync-version.sh` script.

To update the version:

1. Edit the version in `plugin.xml`
2. Run `./sync-version.sh`
3. Build and test with `./gradlew clean build`

See [VERSION_MANAGEMENT.md](VERSION_MANAGEMENT.md) for detailed information.

### Project Structure

```text
src/main/kotlin/com/cursor/plugin/
├── CursorPlugin.kt                    # Main plugin class
├── CompletionsChatAsyncService.kt     # AI service with coroutine support
├── ChatServiceInterface.kt            # Service interface definition
├── CursorToolWindowFactory.kt         # Tool window factory
├── CursorChatPanel.kt                 # Chat UI panel
├── CursorAIResponseCallback.kt        # Async response callback
├── OpenCursorAIAction.kt              # Action to open AI panel
├── GenerateCodeAction.kt              # Code generation action
└── ExplainCodeAction.kt               # Code explanation action

.github/
├── pull_request_template.md           # Streamlined PR template
└── workflows/
    └── auto-fill-pr-description.yml   # AI-powered PR template auto-fill
```

### Building and Testing

1. **Build the plugin**:

   ```bash
   ./gradlew build
   ```

2. **Run tests**:

   ```bash
   ./gradlew test
   ```

3. **Build distributable plugin**:

   ```bash
   ./gradlew buildPlugin
   ```

4. **Run in development IDE**:

   ```bash
   ./gradlew runIde
   ```

### Technology Stack

- **Language**: Kotlin 1.9.22 (JVM target 21)
- **Build Tool**: Gradle with IntelliJ Platform Plugin 2.0.0
- **IntelliJ Platform**: 2024.3.6
- **HTTP Client**: Java HttpClient (built-in)
- **JSON Processing**: Gson 2.10.1
- **Testing**: JUnit 5.10.1, Mockito 5.8.0, AssertJ 3.25.1

### Testing

The project includes comprehensive unit tests covering:

- AI service functionality
- Action implementations
- Error handling scenarios
- Mock server integration

Run the test suite with:

```bash
./gradlew test
```

For more detailed testing information, see [TESTING.md](TESTING.md).

## API Integration

The plugin communicates with OpenAI's API through Cursor's service using the following endpoint:

- **Base URL**: `https://api.openai.com/v1/chat/completions`
- **Authentication**: Bearer token (Cursor API key)
- **Request Format**: JSON with model, messages, and parameters

### Example API Request

```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "user",
      "content": "Explain this code"
    }
  ],
  "max_tokens": 1000,
  "temperature": 0.7
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Style

- Follow Kotlin conventions
- Use meaningful variable and method names
- Add KDoc comments for public methods
- Use companion objects for factory methods
- Maintain test coverage for new features

## Troubleshooting

### Common Issues

1. **"API key not configured" error**:
   - Ensure your API key is set as an environment variable or system property
   - Verify the API key is valid and has proper permissions

2. **Plugin not loading**:
   - Verify Java 21+ is installed and configured
   - Check that you're using a supported IntelliJ IDEA version

3. **Network connectivity issues**:
   - Check firewall settings
   - Verify proxy configuration if applicable

4. **Build script compilation errors**:
   - Ensure you're using Java 21+ (the build script checks this automatically)
   - Run `./build.sh check` to verify prerequisites
   - If you encounter Kotlin compilation errors, try `./build.sh clean` first

### Debug Mode

To enable debug logging, add this system property when starting IntelliJ:

```bash
-Dio.threethirtythree.plugin.debug=true
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- Create an issue on GitHub for bug reports
- Join our Discord community for discussions
- Check the [FAQ](https://github.com/your-username/cursor-intellij-plugin/wiki/FAQ) for common questions

## Changelog

### Version 0.5.0 (Current)

- ⚙️ **Settings Management**: Implemented comprehensive settings panel for API configuration
- 🔧 **Platform Upgrade**: Upgraded to latest IntelliJ Platform plugin for enhanced compatibility
- 🧪 **Enhanced Testing**: Improved test suite with better API connection testing and coverage
- 🚀 **Build System**: Updated build configuration for better platform compatibility
- 🔄 **Workflow Management**: Disabled auto-fill PR workflow for manual PR creation
- 🧹 **Code Refactoring**: Cleaned up and enhanced API connection handling

### Version 0.0.5

- 🚀 **Universal Compatibility**: Compatible with all IntelliJ IDEA versions (removed version restrictions)
- 🔧 **Build System Improvements**: Fixed dependency conflicts and enhanced build pipeline
- ⚡ **HTTP Client Migration**: Migrated to Java HttpClient for reliable HTTP communication
- 📈 **Enhanced Error Handling**: Improved error handling and interface design
- 🧪 **Comprehensive Testing**: All tests passing with full coverage
- 🎯 **Coroutine Optimization**: CompletionsChatAsyncService uses Dispatchers.Main with proper lifecycle
- 🔗 **OpenAI Integration**: Direct OpenAI API integration with proper authentication
- 🔄 **Resource Management**: Class-level coroutine scope with proper cleanup and disposal
- 📝 **GitHub Automation**: Streamlined PR template with AI-powered auto-fill workflow
- 🗑️ **Code Cleanup**: Removed deprecated GeneratePRDescriptionAction
- 🤖 **AI-Powered PRs**: GitHub Actions workflow intelligently populates PR templates using OpenAI
- 🔄 **Template Intelligence**: Workflow analyzes code changes, commit messages, and diffs to generate contextual PR descriptions

### Version 0.0.4

- ✅ **Build System Fixed**: Resolved Kotlin compilation errors and dependency conflicts
- 🔧 **HTTP Client Migration**: Initial migration from OpenAI client library
- ⚡ **Enhanced Compatibility**: GenerateCodeAction and ExplainCodeAction working
- 📈 **Improved Error Handling**: Better parameter validation and error messages
- 📦 **Full Build Pipeline**: Complete build, test, and distribution process
- 🧪 **Test Suite**: 16 tests passing with 100% success rate
- 🚀 **Main Dispatcher**: CompletionsChatAsyncService using Dispatchers.Main
- 🎯 **Coroutine Lifecycle**: Proper coroutine lifecycle management with disposal
- 🔄 **Resource Management**: Cleanup mechanism for async operations
