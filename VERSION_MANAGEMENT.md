# Version Management

This project uses a centralized version management approach where `src/main/resources/META-INF/plugin.xml` serves as the single source of truth for the plugin version.

## How It Works

1. **Single Source of Truth**: The version is defined in `plugin.xml`:
   ```xml
   <version>1.0.2</version>
   ```

2. **Synchronization Script**: The `sync-version.sh` script automatically updates all version references across the project.

## Files That Get Updated

When you run `./sync-version.sh`, the following files are automatically updated:

- `build.gradle.kts` - Gradle build version
- `build.sh` - Build script artifact references
- `PROJECT_SUMMARY.md` - Documentation artifact paths
- `README.md` - Changelog current version

## Automated Version Management

The project includes GitHub Actions workflows for automated version management:

### Version Bump Workflow
- **Trigger**: Push to main/develop branches or manual dispatch
- **Features**: 
  - Automatic detection of changes requiring version bump
  - Manual version bump with type selection (major, minor, patch)
  - Pull request creation for review
  - Version synchronization across all files

### Release Workflow
- **Trigger**: Push tags (`v*`) or manual dispatch
- **Features**:
  - Version validation across all files
  - Automatic changelog generation
  - Comprehensive release notes
  - Artifact building and publishing

## How to Update the Version

1. **Edit the version in plugin.xml**:
   ```xml
   <version>1.0.3</version>
   ```

2. **Run the synchronization script**:
   ```bash
   ./sync-version.sh
   ```

3. **Verify the changes**:
   ```bash
   git diff
   ```

4. **Test the build**:
   ```bash
   ./gradlew clean build
   ```

## Benefits

- ✅ **Single source of truth**: No more version mismatches
- ✅ **Automated synchronization**: One command updates everything
- ✅ **Consistent versioning**: All files stay in sync
- ✅ **Easy maintenance**: Simple workflow for version updates
- ✅ **Build safety**: No broken builds due to version conflicts

## Script Features

- **Colored output**: Easy to read status messages
- **Backup creation**: Creates backups before making changes
- **Error handling**: Stops on errors and provides clear messages
- **Cleanup**: Automatically removes temporary files

## Example Workflow

```bash
# 1. Update version in plugin.xml
vim src/main/resources/META-INF/plugin.xml

# 2. Sync all files
./sync-version.sh

# 3. Build and test
./gradlew clean build

# 4. Commit changes
git add .
git commit -m "Bump version to 1.0.3"
```

This approach ensures that version management is simple, reliable, and consistent across the entire project.