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

- IntelliJ IDEA 2024.3 or later (compatible with all future versions)
- Java 17 or later
- Cursor API key (get one from [Cursor.com](https://cursor.com))

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
   Or set it as a system property:
   ```bash
   -Dcursor.api.key=your_api_key_here
   ```

3. Build the plugin using the build script (recommended):
   ```bash
   ./build.sh all
   ```
   
   Or use Gradle directly:
   ```bash
   ./gradlew buildPlugin
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

The build script provides colored output, error checking, and will verify that you have the correct Java version (17+) and Gradle wrapper before running any commands.

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

1. **Environment Variable**:
   ```bash
   export CURSOR_API_KEY=your_api_key_here
   ```

2. **System Property**:
   ```bash
   -Dcursor.api.key=your_api_key_here
   ```

3. **Plugin Settings** (coming soon):
   - File → Settings → Other Settings → Cursor AI
   - Enter your API key in the settings panel

## Development

### Version Management

This project uses centralized version management. The version is defined in `src/main/resources/META-INF/plugin.xml` and automatically synchronized across all files using the `sync-version.sh` script.

To update the version:
1. Edit the version in `plugin.xml`
2. Run `./sync-version.sh`
3. Build and test with `./gradlew clean build`

See [VERSION_MANAGEMENT.md](VERSION_MANAGEMENT.md) for detailed information.

### Project Structure

```
src/main/kotlin/com/cursor/plugin/
├── CursorPlugin.kt                # Main plugin class
├── CursorAIService.kt             # AI service integration
├── CursorToolWindowFactory.kt     # Tool window factory
├── CursorChatPanel.kt              # Chat UI panel
├── OpenCursorAIAction.kt           # Action to open AI panel
├── GenerateCodeAction.kt           # Code generation action
└── ExplainCodeAction.kt            # Code explanation action
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

- **Language**: Kotlin (JVM target)
- **Build Tool**: Gradle 8.14
- **IntelliJ Platform**: 2024.3
- **HTTP Client**: OkHttp 4.12.0
- **JSON Processing**: Gson 2.10.1
- **Testing**: JUnit 5, Mockito, AssertJ

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

The plugin communicates with Cursor's API using the following endpoint:
- **Base URL**: `https://api.cursor.com/v1/chat/completions`
- **Authentication**: Bearer token (API key)
- **Request Format**: JSON with model, prompt, context, and parameters

### Example API Request

```json
{
  "model": "gpt-4",
  "prompt": "Explain this code",
  "context": "// Selected code context",
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
   - Check that you're using a compatible IntelliJ IDEA version (2023.2+)
   - Verify Java 17+ is installed and configured

3. **Network connectivity issues**:
   - Check firewall settings
   - Verify proxy configuration if applicable

### Debug Mode

To enable debug logging, add this system property when starting IntelliJ:
```bash
-Dcom.cursor.plugin.debug=true
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- Create an issue on GitHub for bug reports
- Join our Discord community for discussions
- Check the [FAQ](https://github.com/your-username/cursor-intellij-plugin/wiki/FAQ) for common questions

## Changelog

### Version 1.0.3 (Current)
- 🚀 **Enhanced Future Compatibility**: Now works with all new IntelliJ IDEA versions
- 🔧 Removed version upper limits for unlimited forward compatibility
- ⚡ Improved stability and performance
- 📈 Better error handling and logging
- 🔄 Updated IntelliJ Gradle Plugin to 1.17.4
- 📦 Compatible with IntelliJ IDEA 2024.3 and all future releases

### Version 1.0.2
- Updated for IntelliJ IDEA 2024.3+ compatibility
- Improved Java 17 support
- Enhanced stability and performance

### Version 1.0.0
- Initial release with core AI integration
- Chat panel interface
- Code generation and explanation actions
- Context menu integration
- Comprehensive test suite

### Roadmap
- Settings panel for API key configuration
- Advanced code analysis features
- Custom prompts and templates
- Integration with more AI models
