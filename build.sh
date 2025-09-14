#!/bin/bash

# Build script for Cursor IntelliJ Plugin

echo "🚀 Building Cursor IntelliJ Plugin..."

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "Please install Java 11 or later"
    exit 1
fi

# Check if CURSOR_API_KEY is set
if [ -z "$CURSOR_API_KEY" ]; then
    echo "⚠️  Warning: CURSOR_API_KEY environment variable is not set"
    echo "You can set it with: export CURSOR_API_KEY=your_api_key_here"
    echo "Or pass it as a system property: -Dcursor.api.key=your_api_key_here"
fi

# Make gradlew executable
chmod +x gradlew

# Build the plugin
echo "📦 Building plugin..."
./gradlew buildPlugin

if [ $? -eq 0 ]; then
    echo "✅ Plugin built successfully!"
    echo "📁 Plugin file location: build/distributions/"
    echo ""
    echo "To install the plugin:"
    echo "1. Open IntelliJ IDEA"
    echo "2. Go to File → Settings → Plugins"
    echo "3. Click the gear icon → Install Plugin from Disk"
    echo "4. Select the .zip file from build/distributions/"
    echo ""
    echo "To run in development mode:"
    echo "./gradlew runIde"
else
    echo "❌ Build failed!"
    exit 1
fi