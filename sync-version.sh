#!/bin/bash

# Script to synchronize version numbers across all files
# This script uses plugin.xml as the single source of truth for version

set -e

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

# Extract version from plugin.xml
PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')

if [ -z "$PLUGIN_VERSION" ]; then
    print_error "Could not extract version from plugin.xml"
    exit 1
fi

print_status "Current plugin version: $PLUGIN_VERSION"

# Function to update version in a file
update_version_in_file() {
    local file="$1"
    local pattern="$2"
    local replacement="$3"
    
    if [ -f "$file" ]; then
        # Create backup
        cp "$file" "$file.bak"
        
        # Update the file (portable sed -i)
        if [[ "$(uname)" == "Darwin" ]]; then
            sed -i '' "$pattern" "$file"
        else
            sed -i "$pattern" "$file"
        fi
        
        print_success "Updated $file"
    else
        print_warning "File $file not found, skipping"
    fi
}

# Update build.gradle (only the project version, not plugin versions)
print_status "Updating build.gradle..."
update_version_in_file "build.gradle" "s/^[[:space:]]*version[[:space:]]*['\"][^'\"]*['\"][[:space:]]*$/version '$PLUGIN_VERSION'/g"

# Update build.sh script
print_status "Updating build.sh..."
update_version_in_file "build.sh" "s/cursor-intellij-plugin-[0-9]\+\.[0-9]\+\.[0-9]\+/cursor-intellij-plugin-$PLUGIN_VERSION/g"

# Update PROJECT_SUMMARY.md
print_status "Updating PROJECT_SUMMARY.md..."
update_version_in_file "PROJECT_SUMMARY.md" "s/cursor-intellij-plugin-[0-9]\+\.[0-9]\+\.[0-9]\+/cursor-intellij-plugin-$PLUGIN_VERSION/g"

# Update README.md changelog
print_status "Updating README.md..."
update_version_in_file "README.md" "s/### Version [0-9]\+\.[0-9]\+\.[0-9]\+ (Current)/### Version $PLUGIN_VERSION (Current)/g"

# Clean up backup files
print_status "Cleaning up backup files..."
rm -f *.bak

print_success "Version synchronization complete!"
print_status "All files now reference version: $PLUGIN_VERSION"

# Show what was updated
echo ""
print_status "Updated files:"
echo "  - build.gradle"
echo "  - build.sh"
echo "  - PROJECT_SUMMARY.md"
echo "  - README.md"
echo ""
print_status "To update the version, edit src/main/resources/META-INF/plugin.xml and run this script again."