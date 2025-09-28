#!/bin/bash

# Cursor AI IntelliJ Plugin Build Script
# This script provides common build operations for the plugin

set -Eeuo pipefail  # Strict mode: exit on error/undefined var, fail pipelines

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

# Wrapper for Gradle with common flags
gradle_cmd() {
    ./gradlew --stacktrace --no-daemon "$@"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."

    # Check if we're in the project root
    if [ ! -f "build.gradle.kts" ] || [ ! -f "src/main/resources/META-INF/plugin.xml" ]; then
        print_error "Not in project root directory. Please run from the cursor-intellij-plugin directory."
        exit 1
    fi

    # Check Java version (Gradle itself needs a JRE; toolchains will provision JDKs for compilation/tests)
    if command -v java &> /dev/null; then
        JAVA_VER_STR=$(java -version 2>&1 | head -n 1)
        # Extract major version number robustly (handles quoted/unquoted, dot or plus separated, etc.)
        JAVA_VERSION=$(echo "$JAVA_VER_STR" | sed -E 's/.*version "?([0-9]+(\.[0-9]+)*)"? .*/\1/' | cut -d'.' -f1)
        if [[ -z "${JAVA_VERSION:-}" ]] || ! [[ "$JAVA_VERSION" =~ ^[0-9]+$ ]]; then
            print_warning "Unable to parse Java version from: $JAVA_VER_STR"
        elif [ "$JAVA_VERSION" -ge 21 ]; then
            print_success "Java runtime detected: $JAVA_VER_STR"
        else
            print_error "Java 21+ required to run Gradle, found: $JAVA_VER_STR"
            exit 1
        fi
    else
        print_warning "Java runtime not found in PATH. If Gradle fails to start, install JDK 21 or later."
    fi

    # Check Gradle wrapper
    if [ -f "./gradlew" ]; then
        print_success "Gradle wrapper found"
        chmod +x ./gradlew || true
    else
        print_error "Gradle wrapper not found. Run from project root directory"
        exit 1
    fi

    # Check plugin.xml exists and is valid
    if [ -f "src/main/resources/META-INF/plugin.xml" ]; then
        if grep -q "<idea-plugin>" src/main/resources/META-INF/plugin.xml; then
            print_success "Plugin descriptor found"
        else
            print_error "Invalid plugin.xml file"
            exit 1
        fi
    else
        print_error "Plugin descriptor not found at src/main/resources/META-INF/plugin.xml"
        exit 1
    fi
}

# Function to clean build artifacts
clean_build() {
    print_status "Cleaning build artifacts..."
    gradle_cmd clean
    print_success "Build artifacts cleaned"
}

# Function to run tests
run_tests() {
    local test_filter=${1:-}
    print_status "Running tests..."

    # Ensure toolchain auto-download is enabled for CI/local
    export ORG_GRADLE_PROJECT_org__gradle__java__installations__auto_download=true

    # Clean previous results to avoid stale reports
    gradle_cmd cleanTest

    # Build arguments
    local args=(test)
    if [[ -n "$test_filter" ]]; then
        print_status "Filtering tests with pattern: $test_filter"
        args+=("--tests" "$test_filter")
    fi

    # Run tests
    if gradle_cmd "${args[@]}"; then
        print_success "All tests passed"
        local report_html="build/reports/tests/test/index.html"
        if [ -f "$report_html" ]; then
            print_status "Test report: $report_html"
        fi
    else
        print_error "Tests failed"
        local report_html="build/reports/tests/test/index.html"
        if [ -f "$report_html" ]; then
            print_status "Test report: $report_html"
        fi
        exit 1
    fi
}

# Function to build the plugin
build_plugin() {
    print_status "Building plugin..."
    gradle_cmd build
    print_success "Plugin built successfully"

    # Extract version from plugin.xml
    PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g' || true)

    # Show build artifacts
    if [ -n "${PLUGIN_VERSION:-}" ] && [ -f "build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}.jar" ]; then
        print_status "Plugin JAR: build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}.jar"
    fi
    
    # Show additional build artifacts
    if [ -f "build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}-all.jar" ]; then
        print_status "Fat JAR: build/libs/cursor-intellij-plugin-${PLUGIN_VERSION}-all.jar"
    fi
}

# Function to create distributable plugin
build_distribution() {
    print_status "Creating plugin distribution..."
    gradle_cmd buildPlugin
    print_success "Plugin distribution created"

    # Extract version from plugin.xml
    PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g' || true)

    # Show distribution location
    if [ -n "${PLUGIN_VERSION:-}" ] && [ -f "build/distributions/cursor-intellij-plugin-${PLUGIN_VERSION}.zip" ]; then
        print_status "Distribution ZIP: build/distributions/cursor-intellij-plugin-${PLUGIN_VERSION}.zip"
    fi
}

# Function to run the plugin in development IDE
run_ide() {
    print_status "Starting development IDE..."
    print_warning "This will start IntelliJ IDEA with the plugin loaded"
    print_warning "Press Ctrl+C to stop the IDE"
    gradle_cmd runIde
}

# Function to verify plugin
verify_plugin() {
    print_status "Verifying plugin..."
    gradle_cmd verifyPlugin
    print_success "Plugin verification completed"
}

# Function to patch plugin.xml with version info
patch_plugin_xml() {
    print_status "Patching plugin.xml..."
    gradle_cmd patchPluginXml
    print_success "Plugin.xml patched successfully"
}

# Function to run plugin in sandbox IDE
run_sandbox() {
    print_status "Starting plugin in sandbox IDE..."
    print_warning "This will start IntelliJ IDEA Community Edition with the plugin loaded"
    print_warning "Press Ctrl+C to stop the IDE"
    gradle_cmd runIde
}

# Function to publish plugin to marketplace
publish_plugin() {
    print_status "Publishing plugin to JetBrains Marketplace..."
    
    if [ -z "${JETBRAINS_MARKETPLACE_TOKEN:-}" ]; then
        print_error "JETBRAINS_MARKETPLACE_TOKEN environment variable is not set"
        print_warning "Set your marketplace token: export JETBRAINS_MARKETPLACE_TOKEN=your_token"
        exit 1
    fi
    
    gradle_cmd publishPlugin
    print_success "Plugin published successfully"
}

# Function to check plugin compatibility
check_compatibility() {
    print_status "Checking plugin compatibility..."
    gradle_cmd verifyPlugin
    print_success "Plugin compatibility check completed"
}

# Function to check for API key
check_api_key() {
    if [ -n "${CURSOR_API_KEY:-}" ]; then
        print_success "CURSOR_API_KEY environment variable is set"
    else
        print_warning "CURSOR_API_KEY environment variable is not set. Please set it before running the build."
        print_warning "Get your Cursor API key from: https://cursor.com"
    fi
}

# Function to show build information
show_build_info() {
    print_status "Build Information:"
    
    # Check for xmllint
    if ! command -v xmllint &> /dev/null; then
        print_warning "xmllint is not installed. Please install libxml2-utils to enable robust XML parsing."
        PLUGIN_VERSION="Unknown"
        PLUGIN_ID="Unknown"
        PLUGIN_NAME="Unknown"
    else
        PLUGIN_VERSION=$(xmllint --xpath 'string(//version)' src/main/resources/META-INF/plugin.xml 2>/dev/null || echo "")
        PLUGIN_ID=$(xmllint --xpath 'string(//id)' src/main/resources/META-INF/plugin.xml 2>/dev/null || echo "")
        PLUGIN_NAME=$(xmllint --xpath 'string(//name)' src/main/resources/META-INF/plugin.xml 2>/dev/null || echo "")
        # Error handling for empty values
        if [ -z "$PLUGIN_VERSION" ]; then
            print_warning "Could not extract <version> from plugin.xml"
            PLUGIN_VERSION="Unknown"
        fi
        if [ -z "$PLUGIN_ID" ]; then
            print_warning "Could not extract <id> from plugin.xml"
            PLUGIN_ID="Unknown"
        fi
        if [ -z "$PLUGIN_NAME" ]; then
            print_warning "Could not extract <name> from plugin.xml"
            PLUGIN_NAME="Unknown"
        fi
    fi
    
    echo "  Plugin ID: ${PLUGIN_ID:-Unknown}"
    echo "  Plugin Name: ${PLUGIN_NAME:-Unknown}"
    echo "  Version: ${PLUGIN_VERSION:-Unknown}"
    
    # Show IntelliJ Platform version
    if [ -f "gradle.properties" ]; then
        PLATFORM_VERSION=$(grep "platformVersion=" gradle.properties | cut -d'=' -f2 || true)
        PLATFORM_TYPE=$(grep "platformType=" gradle.properties | cut -d'=' -f2 || true)
        echo "  IntelliJ Platform: ${PLATFORM_TYPE:-IC} ${PLATFORM_VERSION:-2024.2}"
    fi
    
    # Show Java version
    if command -v java &> /dev/null; then
        JAVA_VER_STR=$(java -version 2>&1 | head -n 1)
        echo "  Java Runtime: $JAVA_VER_STR"
    fi
    
    # Show Gradle version
    if [ -f "./gradlew" ]; then
        GRADLE_VERSION=$(./gradlew --version 2>/dev/null | grep "Gradle" | head -n 1 || true)
        echo "  Gradle: $GRADLE_VERSION"
    fi
}

# Function to show usage
show_usage() {
    cat <<EOF
Cursor AI IntelliJ Plugin Build Script

Usage: $0 [command] [options]

Commands:
  check                 Check prerequisites and configuration
  info                  Show build information and plugin details
  clean                 Clean build artifacts
  test [pattern]        Run tests (optionally filter with --tests pattern)
  build                 Build the plugin
  dist                  Create plugin distribution
  run                   Run plugin in development IDE (sandbox)
  verify                Verify plugin structure and compatibility
  patch                 Patch plugin.xml with version info
  publish               Publish plugin to JetBrains Marketplace
  compatibility         Check plugin compatibility with IntelliJ Platform
  all                   Run clean, test, build, and dist
  help                  Show this help message

Examples:
  $0 check                    # Check prerequisites and API key
  $0 info                     # Show build information and plugin details
  $0 clean                    # Clean build artifacts
  $0 test                     # Run all tests
  $0 test io.threethirtythree.**      # Run tests matching pattern (recursively)
  $0 build                    # Build the plugin
  $0 dist                     # Create plugin distribution
  $0 run                      # Run plugin in development IDE
  $0 verify                   # Verify plugin structure
  $0 patch                    # Patch plugin.xml with version info
  $0 publish                  # Publish to JetBrains Marketplace (requires token)
  $0 compatibility            # Check plugin compatibility
  $0 all                      # Complete build pipeline (clean, test, build, dist)

Environment Variables:
  CURSOR_API_KEY              # Required for plugin functionality
  JETBRAINS_MARKETPLACE_TOKEN # Required for publishing to marketplace
EOF
}

# Main script logic
cmd="${1:-help}"
shift || true
case "$cmd" in
    check)
        check_prerequisites
        check_api_key
        ;;
    info)
        check_prerequisites
        show_build_info
        ;;
    clean)
        check_prerequisites
        clean_build
        ;;
    test)
        check_prerequisites
        run_tests "${1:-}"
        ;;
    build)
        check_prerequisites
        build_plugin
        ;;
    dist)
        check_prerequisites
        build_distribution
        ;;
    run)
        check_prerequisites
        check_api_key
        run_sandbox
        ;;
    verify)
        check_prerequisites
        verify_plugin
        ;;
    patch)
        check_prerequisites
        patch_plugin_xml
        ;;
    publish)
        check_prerequisites
        check_api_key
        publish_plugin
        ;;
    compatibility)
        check_prerequisites
        check_compatibility
        ;;
    all)
        check_prerequisites
        check_api_key
        clean_build
        run_tests
        build_plugin
        build_distribution
        print_success "Complete build pipeline finished successfully!"
        ;;
    help|*)
        show_usage
        ;;
esac
