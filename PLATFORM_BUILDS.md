# Building for Different IntelliJ Platforms

The Cursor AI plugin supports building for both IntelliJ IDEA Community Edition (IC) and Ultimate Edition (IU).

## Platform Configuration

The plugin uses Gradle properties to determine which platform to build for:

- `platformType`: Either `IC` for Community Edition or `IU` for Ultimate Edition (default: `IU`)
- `platformVersion`: The IntelliJ IDEA version to build against (default: `2025.2`)

## Building for Specific Platforms

### Command Line

You can specify the platform type when building:

```bash
# Build for IntelliJ IDEA Ultimate (default)
./gradlew buildPlugin

# Build for IntelliJ IDEA Community
./gradlew buildPlugin -PplatformType=IC

# Build for a specific version
./gradlew buildPlugin -PplatformType=IU -PplatformVersion=2025.2
```

### Configuration File

You can also set these properties in `gradle.properties`:

```properties
platformType=IU
platformVersion=2025.2
```

### Environment Variables

For CI/CD pipelines, you can use environment variables:

```bash
export ORG_GRADLE_PROJECT_platformType=IC
export ORG_GRADLE_PROJECT_platformVersion=2025.2
./gradlew buildPlugin
```

## Important Notes

1. **Version Compatibility**: Starting with IntelliJ IDEA 2025.3, the Community Edition (IC) is no longer available as a separate build target. For versions 2025.3 and later, you must use IU.

2. **Plugin Compatibility**: The plugin is designed to work with both editions. The same plugin binary can be installed in either Community or Ultimate editions, as long as the version requirements are met.

3. **Testing**: Our CI/CD pipeline automatically tests both IC and IU builds to ensure compatibility.

## CI/CD Matrix Builds

The GitHub Actions workflow is configured to build and test for both platforms:

```yaml
strategy:
  matrix:
    platform: [IC, IU]
```

This ensures that every commit is tested against both editions.

## Troubleshooting

### Deprecation Warnings

You may see warnings about deprecated methods when building:
```
'fun intellijIdeaCommunity(...)' is deprecated
```

These are expected for versions approaching 2025.3 and don't affect the build.

### Build Failures

If you encounter build failures related to platform type:

1. Ensure you're using a supported platform type (`IC` or `IU`)
2. For versions 2025.3+, use `IU` only
3. Check that the specified version is available in the IntelliJ Platform repositories

### Local Development

For local development, we recommend using the default `IU` platform type unless you specifically need to test Community Edition features.