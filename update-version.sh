#!/bin/bash

# Script to update version references in documentation files
# This script extracts the version from plugin.xml and updates documentation

# Extract version from plugin.xml
PLUGIN_VERSION=$(grep -o '<version>[^<]*</version>' src/main/resources/META-INF/plugin.xml | sed 's/<[^>]*>//g')

echo "Current plugin version: $PLUGIN_VERSION"

# Update PROJECT_SUMMARY.md
if [ -f "PROJECT_SUMMARY.md" ]; then
    sed -i.bak "s/cursor-intellij-plugin-[0-9]\+\.[0-9]\+\.[0-9]\+/cursor-intellij-plugin-${PLUGIN_VERSION}/g" PROJECT_SUMMARY.md
    echo "Updated PROJECT_SUMMARY.md with version $PLUGIN_VERSION"
fi

# Update README.md changelog section
if [ -f "README.md" ]; then
    # Update the current version line in changelog
    sed -i.bak "s/### Version [0-9]\+\.[0-9]\+\.[0-9]\+ (Current)/### Version ${PLUGIN_VERSION} (Current)/g" README.md
    echo "Updated README.md with version $PLUGIN_VERSION"
fi

# Clean up backup files
rm -f *.bak

echo "Version update complete!"