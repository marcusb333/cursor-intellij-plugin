# GitHub Workflows Documentation

This document describes the GitHub Actions workflows configured for the Cursor AI IntelliJ Plugin project.

## Workflows Overview

### 1. CI Workflow (`ci.yml`)
**Trigger**: Push to `main`/`develop`, Pull Requests to `main`
**Purpose**: Continuous Integration - runs tests and builds for all platforms

**Jobs**:
- `test`: Runs tests on both IC and IU platforms
- `build`: Creates build artifacts for both platforms

**Features**:
- Matrix strategy for testing both IntelliJ Community (IC) and Ultimate (IU)
- Java 21 with Temurin distribution
- Gradle 8.10.2
- Test result artifacts uploaded for debugging

### 2. Release Comprehensive (`release-comprehensive.yml`)
**Trigger**: Push tags (`v*`), Manual dispatch
**Purpose**: Complete release pipeline with validation, building, and publishing

**Jobs**:
- `validate-release`: Validates version consistency and creates tags
- `build-and-test`: Builds and tests for all platforms
- `sign-artifacts`: Signs artifacts (if signing keys available)
- `generate-release-notes`: Creates comprehensive release notes
- `create-release`: Creates GitHub release with artifacts
- `publish-packages`: Publishes to GitHub Packages (optional)
- `notify-completion`: Notifies completion status

**Features**:
- Version validation across all files
- Automatic changelog generation
- Artifact signing with GPG
- Comprehensive release notes with installation instructions

### 3. Publish Marketplace (`publish-marketplace.yml`)
**Trigger**: Manual dispatch, Release published
**Purpose**: Publishes plugin to JetBrains Marketplace

**Jobs**:
- `validate-marketplace`: Validates marketplace credentials and parameters
- `build-for-marketplace`: Builds plugin specifically for marketplace
- `publish-to-marketplace`: Publishes to JetBrains Marketplace
- `notify-marketplace`: Notifies publishing completion

**Features**:
- Credential validation with helpful error messages
- Dry-run support for testing
- Channel selection (default, beta, alpha)
- Comprehensive error handling

### 4. Version Bump (`version-bump.yml`)
**Trigger**: Push to `main`/`develop`, Manual dispatch
**Purpose**: Automated version bumping across all project files

**Jobs**:
- `check-version`: Determines if version bump is needed
- `bump-version`: Updates version in all files and creates PR
- `notify`: Notifies completion status

**Features**:
- Automatic detection of changes requiring version bump
- Manual version bump with type selection (major, minor, patch)
- Pull request creation for review
- Version synchronization across all files

### 5. Gradle Package (`gradle-publish.yml`)
**Trigger**: Push to `main`/cleanup/*/release/*, Pull Requests to `main`
**Purpose**: Builds and packages plugin (legacy workflow)

**Features**:
- Matrix strategy for IC and IU platforms
- Artifact upload to GitHub Packages (commented out)

### 6. Simple Release (`release.yml`)
**Trigger**: Push tags (`v*`)
**Purpose**: Simple release creation (legacy workflow)

**Features**:
- Basic release creation with changelog extraction
- Automatic release notes generation

## Required Secrets

### Marketplace Publishing
- `JETBRAINS_MARKETPLACE_TOKEN`: Your JetBrains Marketplace API token
- `JETBRAINS_MARKETPLACE_PLUGIN_ID`: Your plugin ID from the marketplace

### Artifact Signing (Optional)
- `SIGNING_KEY`: GPG private key for signing artifacts
- `SIGNING_KEY_PASSPHRASE`: Passphrase for the signing key

## Environment Variables

All workflows use consistent environment variables:
- `JAVA_VERSION`: '21'
- `GRADLE_VERSION`: '8.10.2'

## Platform Support

The workflows support both IntelliJ IDEA editions:
- **IC**: IntelliJ IDEA Community Edition
- **IU**: IntelliJ IDEA Ultimate Edition

## Usage Examples

### Creating a Release
1. Update version in `plugin.xml`
2. Run `./sync-version.sh` to sync across all files
3. Commit changes
4. Create and push tag: `git tag v0.6.1 && git push origin v0.6.1`
5. The comprehensive release workflow will automatically build and publish

### Publishing to Marketplace
1. Ensure marketplace secrets are configured
2. Go to Actions → "Publish to JetBrains Marketplace"
3. Click "Run workflow"
4. Enter version, select channel, choose dry-run if testing
5. Monitor the workflow execution

### Automated Version Bump
1. Make changes to source code
2. Push to main branch
3. If source files changed, version bump workflow will automatically create a PR
4. Review and merge the PR
5. Create release tag to trigger release workflow

## Troubleshooting

### Common Issues

1. **Build Failures**: Check Java version compatibility and Gradle wrapper
2. **Marketplace Publishing**: Verify token format and plugin ID
3. **Version Sync**: Ensure `sync-version.sh` is executable and all files are updated
4. **Test Failures**: Check platform-specific test configurations

### Debugging

- All workflows include `--stacktrace` and `--no-daemon` flags for better error reporting
- Test results are uploaded as artifacts for debugging
- Build artifacts are preserved for 30-90 days depending on workflow

## Workflow Status Badges

The README includes status badges for key workflows:
- CI status
- Release status  
- Marketplace publishing status

These badges provide quick visibility into the project's health and deployment status.