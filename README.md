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

- IntelliJ IDEA 2023.2 or later
- Java 11 or later
- Cursor API key (get one from [Cursor.com](https://cursor.com))

### Building from Source

1. Clone this repository
2. Set your Cursor API key as an environment variable:
   ```bash
   export CURSOR_API_KEY=your_api_key_here
   ```
   Or set it as a system property:
   ```bash
   -Dcursor.api.key=your_api_key_here
   ```

3. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

4. Install the plugin:
   - Go to File → Settings → Plugins
   - Click the gear icon → Install Plugin from Disk
   - Select the generated `.zip` file from `build/distributions/`

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

### Project Structure

```
src/main/java/com/cursor/plugin/
├── CursorPlugin.java              # Main plugin class
├── CursorAIService.java           # AI service integration
├── CursorToolWindowFactory.java   # Tool window factory
├── CursorChatPanel.java           # Chat UI panel
├── OpenCursorAIAction.java        # Action to open AI panel
├── GenerateCodeAction.java        # Code generation action
└── ExplainCodeAction.java         # Code explanation action
```

### Building

```bash
# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run plugin in development mode
./gradlew runIde
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please use the GitHub Issues page.

## Acknowledgments

- Built with IntelliJ Platform SDK
- Powered by Cursor AI
- Uses OkHttp for HTTP requests
- Uses Gson for JSON parsing