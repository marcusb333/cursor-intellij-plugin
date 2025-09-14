# Testing Documentation

This document provides comprehensive information about the test suite for the Cursor IntelliJ Plugin.

## 🎯 Test Overview

The plugin includes a robust test suite with high-quality unit tests covering all major components. The tests are designed to ensure reliability, maintainability, and proper functionality of the plugin.

## 📊 Test Coverage

### Core Components Tested

- **✅ CursorAIService**: API integration, error handling, network failures, response parsing
- **✅ Action Classes**: User interactions, validation, error scenarios for all actions
- **✅ Plugin Lifecycle**: Service initialization and startup activities
- **✅ Build Configuration**: Complete testing framework setup

### Test Statistics

- **Total Test Classes**: 5
- **Total Test Methods**: 25+
- **Coverage Areas**: API Service, Actions, Plugin Lifecycle, Error Handling
- **Test Framework**: JUnit 5, Mockito, AssertJ, MockWebServer

## 🛠 Test Framework Stack

### Dependencies

```gradle
testImplementation 'org.junit.jupiter:junit-jupiter-api:5.9.2'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.9.2'
testImplementation 'org.junit.platform:junit-platform-suite-api:1.9.2'
testImplementation 'org.mockito:mockito-core:5.1.1'
testImplementation 'org.mockito:mockito-junit-jupiter:5.1.1'
testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
testImplementation 'org.assertj:assertj-core:3.24.2'
```

### Framework Features

- **JUnit 5**: Modern testing framework with Jupiter engine
- **Mockito**: Advanced mocking with strict stubbing validation
- **AssertJ**: Fluent assertions for readable test code
- **MockWebServer**: HTTP testing for API integration scenarios
- **Gradle Test Configuration**: Parallel execution, timeout management, JVM optimization

## 📁 Test Structure

```
src/test/java/com/cursor/plugin/
├── CursorAIServiceTest.java      # API service tests with MockWebServer
├── CursorPluginTest.java        # Plugin lifecycle tests
├── GenerateCodeActionTest.java  # Code generation action tests
├── ExplainCodeActionTest.java   # Code explanation action tests
└── OpenCursorAIActionTest.java  # Tool window action tests

src/test/resources/
└── test.properties              # Test configuration
```

## 🚀 Running Tests

### Basic Commands

```bash
# Run all tests
./gradlew test

# Run tests with verbose output
./gradlew test --info

# Run tests with debug output
./gradlew test --debug

# Run specific test class
./gradlew test --tests "CursorAIServiceTest"

# Run specific test method
./gradlew test --tests "CursorAIServiceTest.testSendMessageWithValidApiKey"
```

### Advanced Test Execution

```bash
# Run tests in parallel (configured in build.gradle)
./gradlew test --parallel

# Run tests with coverage report (if Jacoco is configured)
./gradlew test jacocoTestReport

# Run tests with custom JVM arguments
./gradlew test -Dtest.jvmArgs="-Xmx2g -XX:+UseG1GC"

# Run tests with specific system properties
./gradlew test -Dtest.api.key=test-key-12345
```

## 🧪 Test Categories

### 1. CursorAIService Tests

**File**: `CursorAIServiceTest.java`

**Coverage**:
- API key validation (missing, empty, valid)
- HTTP request/response handling
- Network error scenarios
- Response parsing and error handling
- Request payload structure validation
- Environment variable vs system property API key handling

**Key Test Methods**:
- `testSendMessageWithValidApiKey()`
- `testSendMessageWithMissingApiKey()`
- `testSendMessageWithApiError()`
- `testSendMessageWithNetworkError()`
- `testSendMessageWithMalformedResponse()`
- `testRequestPayloadStructure()`

### 2. Action Class Tests

**Files**: `GenerateCodeActionTest.java`, `ExplainCodeActionTest.java`, `OpenCursorAIActionTest.java`

**Coverage**:
- User interaction validation
- Input validation (null, empty, invalid inputs)
- Action enablement/disablement logic
- Error handling and user feedback
- Integration with CursorAIService

**Key Test Methods**:
- `testActionPerformedWithValidInput()`
- `testActionPerformedWithNullInput()`
- `testUpdateWithValidProject()`
- `testUpdateWithNullProject()`

### 3. Plugin Lifecycle Tests

**File**: `CursorPluginTest.java`

**Coverage**:
- Service initialization
- Plugin startup activity
- Service registration and retrieval
- Plugin configuration validation

**Key Test Methods**:
- `testRunActivityWithValidProject()`
- `testRunActivityInitializesAiService()`
- `testPluginImplementsStartupActivity()`

## 🔧 Test Configuration

### Gradle Test Configuration

```gradle
test {
    useJUnitPlatform()
    
    // Test configuration
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = true
    }
    
    // Set test timeout
    timeout = Duration.ofMinutes(5)
    
    // Enable parallel execution
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    
    // Test system properties
    systemProperty 'test.api.key', 'test-api-key-12345'
    systemProperty 'test.timeout.seconds', '30'
    
    // JVM arguments for tests
    jvmArgs '-Xmx1g', '-XX:+UseG1GC'
}
```

### Test Properties

**File**: `src/test/resources/test.properties`

```properties
# Test configuration properties
test.api.key=test-api-key-12345
test.timeout.seconds=30
test.mock.server.port=8080

# Test data
test.sample.code=public class TestClass { public void testMethod() { } }
test.sample.response=This is a test response from the AI service
```

## 🐛 Common Test Issues and Solutions

### Mockito Strictness Warnings

**Issue**: `UnnecessaryStubbingException` when mocks are set up but not used.

**Solution**: Use `@MockitoSettings(strictness = Strictness.LENIENT)` or remove unused stubs.

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyTest {
    // Test implementation
}
```

### MockWebServer IOException

**Issue**: `IOException` when shutting down MockWebServer.

**Solution**: Wrap shutdown calls in try-catch blocks.

```java
try {
    mockServer.shutdown();
} catch (IOException e) {
    // Ignore shutdown errors in tests
}
```

### IntelliJ Platform Dependencies

**Issue**: IntelliJ Platform test framework dependencies not available.

**Solution**: Use standard testing frameworks (JUnit 5, Mockito) instead of IntelliJ-specific test frameworks.

## 📈 Test Quality Metrics

### Code Coverage Goals

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 90%

### Test Quality Standards

- **Test Isolation**: Each test is independent and can run in any order
- **Test Clarity**: Tests are readable and self-documenting
- **Test Reliability**: Tests produce consistent results
- **Test Performance**: Tests complete within reasonable time limits

## 🔄 Continuous Integration

### CI/CD Integration

The test suite is designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: ./gradlew test
  
- name: Generate Test Report
  run: ./gradlew test jacocoTestReport
```

### Test Reporting

- **Gradle Test Reports**: Available in `build/reports/tests/test/`
- **JUnit XML Reports**: Compatible with CI/CD systems
- **Coverage Reports**: Jacoco reports for code coverage analysis

## 🚀 Future Enhancements

### Planned Test Improvements

1. **Integration Tests**: End-to-end testing with real IntelliJ instances
2. **Performance Tests**: Load testing for API calls and UI responsiveness
3. **UI Tests**: Automated testing of the chat panel and user interactions
4. **Coverage Reports**: Jacoco integration for detailed coverage analysis
5. **Test Data Management**: Centralized test data and fixtures

### Test Automation

- **Automated Test Execution**: Scheduled test runs
- **Test Result Notifications**: Automated reporting of test failures
- **Performance Monitoring**: Track test execution times and performance trends

## 📚 Best Practices

### Writing Tests

1. **Arrange-Act-Assert**: Follow the AAA pattern for test structure
2. **Descriptive Names**: Use clear, descriptive test method names
3. **Single Responsibility**: Each test should verify one specific behavior
4. **Mock External Dependencies**: Use mocks for external services and APIs
5. **Test Edge Cases**: Include tests for error conditions and edge cases

### Maintaining Tests

1. **Keep Tests Updated**: Update tests when code changes
2. **Refactor Tests**: Improve test code quality over time
3. **Remove Dead Tests**: Delete tests for removed functionality
4. **Document Complex Tests**: Add comments for complex test logic

## 🎯 Conclusion

The comprehensive test suite ensures the reliability and maintainability of the Cursor IntelliJ Plugin. The tests cover all major components and provide confidence in the plugin's functionality across various scenarios and edge cases.

For questions or contributions to the test suite, please refer to the main project documentation or create an issue in the repository.