#!/bin/bash

# Cursor AI IntelliJ Plugin Build Script
# This script provides common build operations for the plugin

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."

    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            print_success "Java $JAVA_VERSION found"
        else
            print_error "Java 17 or later required, found Java $JAVA_VERSION"
            exit 1
        fi
    else
        print_error "Java not found. Please install Java 17 or later"
        exit 1
    fi

    # Check Gradle wrapper
    if [ -f "./gradlew" ]; then
        print_success "Gradle wrapper found"
    else
        print_error "Gradle wrapper not found. Run from project root directory"
        exit 1
    fi
}

# Function to clean build artifacts
clean_build() {
    print_status "Cleaning build artifacts..."
    ./gradlew clean
    print_success "Build artifacts cleaned"
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    ./gradlew test
    print_success "All tests passed"

    # Show test report location
    if [ -f "build/reports/tests/test/index.html" ]; then
        print_status "Test report available at: build/reports/tests/test/index.html"
    fi
}

# Function to build the plugin
build_plugin() {
    print_status "Building plugin..."
    ./gradlew build
    print_success "Plugin built successfully"

    # Extract version from plugin.xml
    PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')
    
    # Show build artifacts
    if [ -f "build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}.jar" ]; then
        print_status "Plugin JAR: build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}.jar"
    fi
}

# Function to create distributable plugin
build_distribution() {
    print_status "Creating plugin distribution..."
    ./gradlew buildPlugin
    print_success "Plugin distribution created"

    # Extract version from plugin.xml
    PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')
    
    # Show distribution location
    if [ -f "build/distributions/cursor-intellij-plugin-${PLUGIN_VERSION}.zip" ]; then
        print_status "Distribution ZIP: build/distributions/cursor-intellij-plugin-${PLUGIN_VERSION}.zip"
    fi
}

# Function to run the plugin in development IDE
run_ide() {
    print_status "Starting development IDE..."
    print_warning "This will start IntelliJ IDEA with the plugin loaded"
    print_warning "Press Ctrl+C to stop the IDE"
    ./gradlew runIde
}

# Function to verify plugin
verify_plugin() {
    print_status "Verifying plugin..."
    ./gradlew verifyPlugin
    print_success "Plugin verification completed"
}

# Function to check for API key
check_api_key() {
    if [ -n "$CURSOR_API_KEY" ]; then
        print_success "CURSOR_API_KEY environment variable is set"
    elif [ -n "$(java -Dcursor.api.key=test -version 2>&1 | grep 'cursor.api.key')" ]; then
        print_success "cursor.api.key system property detected"
    else
        print_warning "No API key configured. Set CURSOR_API_KEY environment variable or cursor.api.key system property"
        print_status "Example: export CURSOR_API_KEY=your_api_key_here"
    fi
}

# Function to show usage
show_usage() {
    echo "Cursor AI IntelliJ Plugin Build Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  check       - Check prerequisites and configuration"
    echo "  clean       - Clean build artifacts"
    echo "  test        - Run all tests"
    echo "  build       - Build the plugin"
    echo "  dist        - Create plugin distribution"
    echo "  run         - Run plugin in development IDE"
    echo "  verify      - Verify plugin structure"
    echo "  all         - Run clean, test, build, and dist"
    echo "  help        - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all      - Complete build pipeline"
    echo "  $0 test     - Run tests only"
    echo "  $0 run      - Start development IDE"
}

# Main script logic
case "${1:-help}" in
    "check")
        check_prerequisites
        check_api_key
        ;;
    "clean")
        check_prerequisites
        clean_build
        ;;
    "test")
        check_prerequisites
        run_tests
        ;;
    "build")
        check_prerequisites
        build_plugin
        ;;
    "dist")
        check_prerequisites
        build_distribution
        ;;
    "run")
        check_prerequisites
        check_api_key
        run_ide
        ;;
    "verify")
        check_prerequisites
        verify_plugin
        ;;
    "all")
        check_prerequisites
        check_api_key
        clean_build
        run_tests
        build_plugin
        build_distribution
        print_success "Complete build pipeline finished successfully!"
        ;;
    "help"|*)
        show_usage
        ;;
esac
