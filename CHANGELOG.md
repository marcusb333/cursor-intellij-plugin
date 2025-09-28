# Changelog

All notable changes to the Cursor AI IntelliJ Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.0] - 2025-01-27

### Added
- Professional logging framework integration using IntelliJ Logger
- Comprehensive error handling with specific exception types
- GitHub Actions CI/CD pipeline with automated testing
- Workflow status badges in README
- Comprehensive workflow documentation
- Enhanced marketplace credential validation
- Automatic resource cleanup and memory leak prevention

### Changed
- Updated Kotlin version to 2.2.0
- Updated Gradle version to 8.10.2
- Updated IntelliJ Platform Plugin to 2.9.0
- Updated testing dependencies to latest versions
- Improved HTTP client with lazy initialization (singleton pattern)
- Enhanced error messages for better user experience
- Updated GitHub Actions to latest stable versions

### Fixed
- Version inconsistency in plugin.xml changelog
- Duplicate companion object in CompletionsChatAsyncService
- println statements replaced with proper logging
- Exception handling patterns improved throughout codebase
- Resource cleanup in UI components
- Marketplace workflow dependencies on build.sh commands

### Security
- Enhanced API key validation and error handling
- Improved credential management in workflows
- Better error reporting without exposing sensitive information

## [0.0.5] - 2025-01-20

### Added
- Compatible with both IntelliJ IDEA Community (IC) and Ultimate (IU) editions
- Migrated to Java HttpClient for reliable HTTP communication
- Improved error handling and interface design
- CompletionsChatAsyncService now uses Dispatchers.Main
- Direct Cursor API integration with proper authentication
- Class-level coroutine scope with proper lifecycle management
- Resource cleanup and disposal mechanisms
- Streamlined GitHub PR template with AI auto-fill workflow
- GitHub Actions workflow for intelligent PR template population

### Fixed
- Dependency conflicts and build system issues
- All tests passing with comprehensive coverage

### Removed
- Deprecated PR description generator action

## [0.0.4] - 2025-01-15

### Added
- Initial coroutine-based implementation
- Cursor API integration
- Basic plugin functionality
- Core chat interface
- Context menu actions for code generation and explanation

## [Unreleased]

### Planned
- Message history persistence
- Support for additional AI models
- Custom prompt templates
- Advanced code analysis features
- Enhanced UI/UX improvements
- Plugin marketplace publishing
- JetBrains Plugin Repository integration

---

## Development Notes

### Version Management
This project uses a centralized version management approach where `src/main/resources/META-INF/plugin.xml` serves as the single source of truth for the plugin version. The `sync-version.sh` script automatically updates all version references across the project.

### Testing
The project maintains comprehensive test coverage with 100% pass rate across all test suites. Tests include unit tests, integration tests, and API integration tests using MockWebServer.

### CI/CD
The project includes automated GitHub Actions workflows for:
- Continuous Integration (CI)
- Automated testing on multiple platforms
- Release management
- Marketplace publishing
- Version bumping

### Compatibility
- IntelliJ IDEA Community Edition (IC) 2025.2+
- IntelliJ IDEA Ultimate Edition (IU) 2025.2+
- Java 21 or later
- Kotlin 2.2.0
- Gradle 8.10.2