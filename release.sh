#!/bin/bash

# Cursor AI IntelliJ Plugin Release Script
# This script helps with manual release management

set -Eeuo pipefail

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

# Function to show usage
show_usage() {
    cat <<EOF
Cursor AI IntelliJ Plugin Release Script

Usage: $0 [command] [options]

Commands:
  check                 Check release prerequisites
  bump [type]           Bump version (patch|minor|major)
  tag [version]         Create and push a version tag
  release [version]     Complete release process (bump + tag + push)
  publish [version]     Publish to JetBrains Marketplace
  status                Show current release status
  help                  Show this help message

Examples:
  $0 check                    # Check if ready for release
  $0 bump patch               # Bump patch version
  $0 tag 0.0.6                # Create tag v0.0.6
  $0 release 0.0.6            # Complete release process
  $0 publish 0.0.6            # Publish to marketplace
  $0 status                   # Show current status

Version Types:
  patch    - Bug fixes (0.0.4 → 0.0.5)
  minor    - New features (0.0.4 → 0.1.0)
  major    - Breaking changes (0.0.4 → 1.0.0)
EOF
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking release prerequisites..."
    
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi
    
    # Check if we're on main branch
    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "main" ]; then
        print_warning "Not on main branch (current: $CURRENT_BRANCH)"
        print_status "Consider switching to main branch for releases"
    fi
    
    # Check if working directory is clean
    if ! git diff-index --quiet HEAD --; then
        print_error "Working directory is not clean"
        print_status "Please commit or stash your changes before releasing"
        exit 1
    fi
    
    # Check if we have the build script
    if [ ! -f "./build.sh" ]; then
        print_error "Build script not found"
        exit 1
    fi
    
    # Check if we have the sync script
    if [ ! -f "./sync-version.sh" ]; then
        print_error "Version sync script not found"
        exit 1
    fi
    
    # Check if we can build
    print_status "Testing build process..."
    if ./build.sh check; then
        print_success "Build prerequisites OK"
    else
        print_error "Build prerequisites failed"
        exit 1
    fi
    
    print_success "All prerequisites met"
}

# Function to bump version
bump_version() {
    local bump_type="$1"
    
    print_status "Bumping $bump_type version..."
    
    # Extract current version from plugin.xml
    CURRENT_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')
    
    if [ -z "$CURRENT_VERSION" ]; then
        print_error "Could not extract current version from plugin.xml"
        exit 1
    fi
    
    print_status "Current version: $CURRENT_VERSION"
    
    # Calculate new version
    IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
    MAJOR=${VERSION_PARTS[0]}
    MINOR=${VERSION_PARTS[1]}
    PATCH=${VERSION_PARTS[2]}
    
    case $bump_type in
        major)
            NEW_VERSION="$((MAJOR + 1)).0.0"
            ;;
        minor)
            NEW_VERSION="$MAJOR.$((MINOR + 1)).0"
            ;;
        patch)
            NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))"
            ;;
        *)
            print_error "Invalid bump type: $bump_type"
            print_status "Valid types: patch, minor, major"
            exit 1
            ;;
    esac
    
    print_status "New version: $NEW_VERSION"
    
    # Update plugin.xml
    sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/" src/main/resources/META-INF/plugin.xml
    
    # Run sync script
    chmod +x ./sync-version.sh
    ./sync-version.sh
    
    # Verify version consistency
    PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')
    BUILD_VERSION=$(grep -o "version[[:space:]]*['\"][^'\"]*['\"]" build.gradle.kts | sed "s/version[[:space:]]*['\"]//" | sed "s/['\"]//")
    
    if [ "$PLUGIN_VERSION" != "$BUILD_VERSION" ]; then
        print_error "Version synchronization failed!"
        exit 1
    fi
    
    print_success "Version bumped to $NEW_VERSION"
    
    # Show what was updated
    echo ""
    print_status "Updated files:"
    git diff --name-only | while read file; do
        echo "  - $file"
    done
}

# Function to create and push tag
create_tag() {
    local version="$1"
    local tag_name="v$version"
    
    print_status "Creating tag $tag_name..."
    
    # Check if tag already exists
    if git tag -l | grep -q "^$tag_name$"; then
        print_error "Tag $tag_name already exists"
        exit 1
    fi
    
    # Create tag
    git tag -a "$tag_name" -m "Release $tag_name"
    
    # Push tag
    print_status "Pushing tag to remote..."
    git push origin "$tag_name"
    
    print_success "Tag $tag_name created and pushed"
}

# Function to complete release process
complete_release() {
    local version="$1"
    
    print_status "Starting complete release process for version $version..."
    
    # Check prerequisites
    check_prerequisites
    
    # Bump version
    bump_version "patch"  # Always use patch for manual releases
    
    # Create and push tag
    create_tag "$version"
    
    # Show next steps
    echo ""
    print_success "Release process completed!"
    print_status "Next steps:"
    echo "  1. Monitor GitHub Actions for build completion"
    echo "  2. Review the generated release on GitHub"
    echo "  3. Publish to JetBrains Marketplace if needed"
    echo ""
    print_status "Release URL: https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^.]*\).*/\1/')/releases/tag/v$version"
}

# Function to publish to marketplace
publish_marketplace() {
    local version="$1"
    
    print_status "Publishing version $version to JetBrains Marketplace..."
    
    # Check if marketplace token is available
    if [ -z "${JETBRAINS_MARKETPLACE_TOKEN:-}" ]; then
        print_error "JETBRAINS_MARKETPLACE_TOKEN environment variable not set"
        print_status "Set your marketplace token: export JETBRAINS_MARKETPLACE_TOKEN=your_token"
        exit 1
    fi
    
    # Check if plugin ID is available
    if [ -z "${JETBRAINS_MARKETPLACE_PLUGIN_ID:-}" ]; then
        print_error "JETBRAINS_MARKETPLACE_PLUGIN_ID environment variable not set"
        print_status "Set your plugin ID: export JETBRAINS_MARKETPLACE_PLUGIN_ID=your_plugin_id"
        exit 1
    fi
    
    # Build for marketplace
    print_status "Building plugin for marketplace..."
    ./build.sh all
    
    # Publish using Gradle
    print_status "Publishing to marketplace..."
    ./gradlew publishPlugin -PintellijPublishToken="$JETBRAINS_MARKETPLACE_TOKEN"
    
    print_success "Published to JetBrains Marketplace!"
    print_status "Marketplace URL: https://plugins.jetbrains.com/plugin/$JETBRAINS_MARKETPLACE_PLUGIN_ID"
}

# Function to show release status
show_status() {
    print_status "Release Status:"
    echo ""
    
    # Current version
    CURRENT_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')
    echo "Current version: $CURRENT_VERSION"
    
    # Git status
    CURRENT_BRANCH=$(git branch --show-current)
    echo "Current branch: $CURRENT_BRANCH"
    
    # Working directory status
    if git diff-index --quiet HEAD --; then
        echo "Working directory: Clean"
    else
        echo "Working directory: Modified"
    fi
    
    # Latest tag
    LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "No tags")
    echo "Latest tag: $LATEST_TAG"
    
    # Marketplace token status
    if [ -n "${JETBRAINS_MARKETPLACE_TOKEN:-}" ]; then
        echo "Marketplace token: Configured"
    else
        echo "Marketplace token: Not configured"
    fi
    
    # Plugin ID status
    if [ -n "${JETBRAINS_MARKETPLACE_PLUGIN_ID:-}" ]; then
        echo "Plugin ID: Configured"
    else
        echo "Plugin ID: Not configured"
    fi
    
    echo ""
    print_status "Ready for release: $(if git diff-index --quiet HEAD -- && [ "$CURRENT_BRANCH" = "main" ]; then echo "Yes"; else echo "No"; fi)"
}

# Main script logic
cmd="${1:-help}"
shift || true

case "$cmd" in
    check)
        check_prerequisites
        ;;
    bump)
        if [ -z "${1:-}" ]; then
            print_error "Bump type required (patch|minor|major)"
            exit 1
        fi
        bump_version "$1"
        ;;
    tag)
        if [ -z "${1:-}" ]; then
            print_error "Version required (e.g., 0.0.6)"
            exit 1
        fi
        create_tag "$1"
        ;;
    release)
        if [ -z "${1:-}" ]; then
            print_error "Version required (e.g., 0.0.6)"
            exit 1
        fi
        complete_release "$1"
        ;;
    publish)
        if [ -z "${1:-}" ]; then
            print_error "Version required (e.g., 0.0.6)"
            exit 1
        fi
        publish_marketplace "$1"
        ;;
    status)
        show_status
        ;;
    help|*)
        show_usage
        ;;
esac