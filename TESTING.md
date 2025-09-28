# Testing Documentation

## Overview

This document describes the testing strategy, test structure, and how to run tests for the Cursor AI IntelliJ Plugin. The project maintains comprehensive test coverage with 100% pass rate across all test suites.

## Test Architecture

### Testing Framework Stack

- **JUnit 5.11.0** (Jupiter) - Core testing framework
- **Mockito 5.11.0** - Mocking framework for unit tests
- **AssertJ 3.25.1** - Fluent assertion library
- **MockWebServer** - HTTP server mocking for API integration tests

### Test Structure

```
src/test/kotlin/com/cursor/plugin/
├── CompletionsChatAsyncServiceIntegrationTest.kt # Service integration tests (8 tests)
├── ExplainCodeActionTest.kt       # Code explanation action tests (4 tests)
├── GenerateCodeActionTest.kt      # Code generation action tests (4 tests)
└── OpenCursorAIActionTest.kt      # Panel opening action tests (4 tests)
```

## Test Categories

### 1. Unit Tests

**Action Tests** - UI action functionality
- ✅ Context menu action registration
- ✅ Action execution and error handling
- ✅ User interaction scenarios
- ✅ Project and editor state validation
- ✅ Error handling for invalid inputs
- ✅ Action availability based on context

### 2. Integration Tests

- **Action Integration**: Tests actions within IntelliJ's action system
- **UI Integration**: Validates proper integration with IntelliJ's UI components
- **Context Integration**: Tests actions in various editor contexts


## Running Tests

### Command Line

```bash
# Using the build script (recommended)
./build.sh test          # Run all tests with colored output
./build.sh all           # Complete pipeline including tests

# Using Gradle directly
./gradlew test           # Run all tests
./gradlew test --tests ExplainCodeActionTest    # Run specific test class
./gradlew test --info    # Run tests with detailed output
./gradlew test jacocoTestReport    # Run tests and generate coverage report
```

### IDE Integration

Tests can be run directly from IntelliJ IDEA:
1. Right-click on test files or test methods
2. Select "Run Test" or "Debug Test"
3. View results in the Test Runner window

## Test Configuration

### Environment Setup

Tests automatically handle:
- API key configuration via Mockito spies (no environment variable manipulation)
- Mock server initialization and cleanup
- Resource management (HTTP clients, connections)
- Proper test isolation between test cases

### Test Properties

Located in `src/test/resources/test.properties`:
- Test timeout configurations
- Mock server settings
- Debug logging levels

## Detailed Test Descriptions

### CompletionsChatAsyncServiceIntegrationTest

#### testGetInstance()
- **Purpose**: Verify service instance retrieval from IntelliJ's service container
- **Method**: Uses Mockito to mock Project service
- **Assertions**: Service instance is not null and correct type

#### testSendMessageWithValidApiKey()
- **Purpose**: Test successful API communication with Cursor API
- **Setup**: 
  - Sets API key via system property
  - Configures mock server with valid JSON response
  - Uses CountDownLatch for async testing
- **Assertions**: 
  - Response received within timeout
  - Content matches expected value
  - HTTP headers are correct

#### testSendMessageWithMissingApiKey()
- **Purpose**: Validate error handling when API key is not configured
- **Method**: 
  - Clears system properties
  - Uses custom service override to simulate missing key scenario
- **Assertions**: Error callback triggered with appropriate message

#### testSendMessageWithApiError()
- **Purpose**: Test handling of HTTP error responses (401, 500, etc.)
- **Setup**: Mock server returns 401 Unauthorized
- **Assertions**: Error callback contains API error details

#### testSendMessageWithMalformedResponse()
- **Purpose**: Validate parsing error handling
- **Setup**: Mock server returns invalid JSON
- **Assertions**: Parse error is caught and reported to user

#### testSendMessageWithEmptyApiKey()
- **Purpose**: Test handling of empty string API keys
- **Setup**: Mock getApiKey() to return empty string
- **Assertions**: Error callback triggered with appropriate message

#### testSendMessageWithEmptyResponse()
- **Purpose**: Validate handling of empty response content
- **Setup**: Mock server returns valid JSON with empty content
- **Assertions**: Empty response is handled gracefully

#### testSendMessageWithServerError()
- **Purpose**: Test handling of server errors (500, etc.)
- **Setup**: Mock server returns 500 Internal Server Error
- **Assertions**: Server error is caught and reported to user

### Action Tests

#### Context Menu Integration
- Verify actions are properly registered
- Test action availability based on context
- Validate proper editor integration

#### Error Handling
- Network connectivity issues
- API authentication failures
- Invalid code selections

## Improved Testing Approach

### API Key Testing Strategy

The tests now use Mockito spies instead of environment variable manipulation to avoid `UnsupportedOperationException` when trying to modify `System.getenv()`:

```kotlin
@BeforeEach
fun setUp() {
    mockServer = MockWebServer()
    mockServer.start()
    aiService = CompletionsChatAsyncService.createForTesting(mockProject, mockServer.url("/").toString())
    spyService = spy(aiService)  // Create spy for mocking
}

@Test
fun testSendMessageWithValidApiKey() {
    // Given
    `when`(spyService.getApiKey()).thenReturn(TEST_API_KEY)
    // ... rest of test
}
```

### Benefits of Spy Approach
- **No Environment Variable Manipulation**: Avoids Java's unmodifiable environment map
- **Better Test Isolation**: Each test can independently control API key behavior
- **Cleaner Setup**: No need to restore environment variables in tearDown
- **More Reliable**: Tests don't depend on external environment state

## Mock Server Testing

### Setup
```kotlin
val mockServer = MockWebServer()
mockServer.start()
val baseUrl = mockServer.url("/").toString()
```

### Response Mocking
```kotlin
mockServer.enqueue(MockResponse()
    .setResponseCode(200)
    .setBody(jsonResponse.toString())
    .addHeader("Content-Type", "application/json"))
```

### Request Validation
```kotlin
val request = mockServer.takeRequest()
assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-api-key")
```

## Asynchronous Testing

### CountDownLatch Pattern
```kotlin
val latch = CountDownLatch(1)
val result = AtomicReference<String>()

val callback = object : CursorAIResponseCallback {
    override fun onSuccess(response: String) {
        result.set(response)
        latch.countDown()
    }
    
    override fun onError(error: String) {
        // Handle error
    }
}

service.sendMessage("test", "context", callback)
assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
```

## Test Data Management

### API Response Simulation
```json
{
  "choices": [
    {
      "message": {
        "content": "AI-generated response"
      }
    }
  ]
}
```

### Error Response Simulation
- HTTP status codes (401, 500, 503)
- Malformed JSON responses
- Network timeout scenarios

## Continuous Integration

### GitHub Actions CI/CD
```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        platform: [IC, IU]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v5
        with:
          java-version: '21'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'
      - run: ./gradlew test --stacktrace --no-daemon --continue
        env:
          ORG_GRADLE_PROJECT_platformType: ${{ matrix.platform }}
```

## Test Maintenance

### Adding New Tests

1. **Create test class** following naming convention: `*Test.java`
2. **Use appropriate annotations**: `@ExtendWith(MockitoExtension.class)`
3. **Follow AAA pattern**: Arrange, Act, Assert
4. **Include setup/teardown**: `@BeforeEach`, `@AfterEach`

### Test Best Practices

- **Descriptive test names**: Use clear, descriptive method names
- **Independent tests**: Each test should be isolated and not depend on others
- **Mock external dependencies**: Use MockWebServer for HTTP, Mockito for services
- **Assert meaningful outcomes**: Test behavior, not implementation
- **Clean up resources**: Properly close mock servers and HTTP clients

### Example Test Template

```kotlin
@ExtendWith(MockitoExtension::class)
class NewFeatureTest {
    
    @Mock
    private lateinit var mockProject: Project
    
    private lateinit var mockServer: MockWebServer
    private lateinit var service: ServiceUnderTest
    
    @BeforeEach
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        service = ServiceUnderTest.createForTesting(mockProject, mockServer.url("/").toString())
    }
    
    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
    }
    
    @Test
    fun shouldHandleSuccessfulOperation() {
        // Arrange
        mockServer.enqueue(MockResponse().setResponseCode(200))
        
        // Act
        val result = service.performOperation()
        
        // Assert
        assertThat(result).isNotNull()
        assertThat(result.isSuccess()).isTrue()
    }
}
```

## Debugging Tests

### Common Issues

1. **Timeout failures**: Increase timeout values or check async handling
2. **Mock server issues**: Verify server startup and proper URL usage
3. **Resource leaks**: Ensure proper cleanup in tearDown methods

### Debug Techniques

```kotlin
// Enable debug logging
System.setProperty("io.threethirtythree.plugin.debug", "true")

// Add debug output
println("Request: ${mockServer.takeRequest()}")

// Use breakpoints in IDE
// Set conditional breakpoints for specific scenarios
```

## Performance Testing

### Load Testing
- Multiple concurrent requests
- Large response handling
- Memory usage validation

### Timeout Testing
- Network delay simulation
- Connection timeout scenarios
- Read/write timeout validation

## Test Coverage Goals

- **Line Coverage**: > 85% (improved with additional test cases)
- **Branch Coverage**: > 80% (improved with edge case testing)
- **Method Coverage**: > 95% (comprehensive API key scenarios)

### Generating Coverage Reports

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Security Testing

### API Key Handling
- Verify no keys in logs
- Test key validation
- Check secure storage

### Network Security
- HTTPS enforcement
- Certificate validation
- Header security

## Test Environment

### System Requirements
- Java 21+
- Gradle with IntelliJ Platform Plugin 2.0.0
- Internet connection for dependency download
- Available ports for MockWebServer

### Test Isolation
- Each test uses separate mock server instance
- System properties are properly restored
- No shared state between tests

## Troubleshooting

### Common Test Failures

1. **Port conflicts**: MockWebServer can't bind to port
   - Solution: Use random ports or cleanup existing servers

2. **Timeout issues**: Tests fail due to timing
   - Solution: Increase timeout values or fix async handling

3. **Resource leaks**: Tests leave connections open
   - Solution: Proper cleanup in @AfterEach methods
