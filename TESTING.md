# Testing Documentation

## Overview

This document describes the testing strategy, test structure, and how to run tests for the Cursor AI IntelliJ Plugin. The project maintains comprehensive test coverage with 17 tests achieving 100% pass rate.

## Test Architecture

### Testing Framework Stack

- **JUnit 5** (Jupiter) - Core testing framework
- **Mockito** - Mocking framework for unit tests
- **AssertJ** - Fluent assertion library
- **MockWebServer** - HTTP server mocking for API integration tests

### Test Structure

```
src/test/java/com/cursor/plugin/
├── CursorAIServiceTest.java       # API service integration tests (5 tests)
├── ExplainCodeActionTest.java     # Code explanation action tests
├── GenerateCodeActionTest.java    # Code generation action tests
└── OpenCursorAIActionTest.java    # Panel opening action tests
```

## Test Categories

### 1. Unit Tests

**CursorAIServiceTest** - Core API integration testing
- ✅ Service instance creation and retrieval
- ✅ API communication with valid credentials
- ✅ Error handling for missing API keys
- ✅ Network error handling
- ✅ Malformed response parsing

**Action Tests** - UI action functionality
- ✅ Context menu action registration
- ✅ Action execution and error handling
- ✅ User interaction scenarios

### 2. Integration Tests

- **Mock Server Testing**: Uses OkHttp MockWebServer to simulate API responses
- **Error Scenario Testing**: Validates proper error handling for various failure modes
- **Timeout Testing**: Ensures proper handling of network timeouts

## Running Tests

### Command Line

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests CursorAIServiceTest

# Run tests with detailed output
./gradlew test --info

# Run tests and generate coverage report
./gradlew test jacocoTestReport
```

### IDE Integration

Tests can be run directly from IntelliJ IDEA:
1. Right-click on test files or test methods
2. Select "Run Test" or "Debug Test"
3. View results in the Test Runner window

## Test Configuration

### Environment Setup

Tests automatically handle:
- API key configuration via system properties
- Mock server initialization and cleanup
- Resource management (HTTP clients, connections)

### Test Properties

Located in `src/test/resources/test.properties`:
- Test timeout configurations
- Mock server settings
- Debug logging levels

## Detailed Test Descriptions

### CursorAIServiceTest

#### testGetInstance()
- **Purpose**: Verify service instance retrieval from IntelliJ's service container
- **Method**: Uses Mockito to mock Project service
- **Assertions**: Service instance is not null and correct type

#### testSendMessageWithValidApiKey()
- **Purpose**: Test successful API communication
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

### Action Tests

#### Context Menu Integration
- Verify actions are properly registered
- Test action availability based on context
- Validate proper editor integration

#### Error Handling
- Network connectivity issues
- API authentication failures
- Invalid code selections

## Mock Server Testing

### Setup
```java
MockWebServer mockServer = new MockWebServer();
mockServer.start();
String baseUrl = mockServer.url("/").toString();
```

### Response Mocking
```java
mockServer.enqueue(new MockResponse()
    .setResponseCode(200)
    .setBody(jsonResponse.toString())
    .addHeader("Content-Type", "application/json"));
```

### Request Validation
```java
RecordedRequest request = mockServer.takeRequest();
assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-api-key");
```

## Asynchronous Testing

### CountDownLatch Pattern
```java
CountDownLatch latch = new CountDownLatch(1);
AtomicReference<String> result = new AtomicReference<>();

callback = new CursorAIResponseCallback() {
    @Override
    public void onSuccess(String response) {
        result.set(response);
        latch.countDown();
    }
};

service.sendMessage("test", "context", callback);
assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
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

### GitHub Actions (Future)
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - run: ./gradlew test
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

```java
@ExtendWith(MockitoExtension.class)
class NewFeatureTest {
    
    @Mock
    private Project mockProject;
    
    private MockWebServer mockServer;
    private ServiceUnderTest service;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
        service = new ServiceUnderTest(mockProject, mockServer.url("/").toString());
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (mockServer != null) {
            mockServer.shutdown();
        }
    }
    
    @Test
    void shouldHandleSuccessfulOperation() {
        // Arrange
        mockServer.enqueue(new MockResponse().setResponseCode(200));
        
        // Act
        Result result = service.performOperation();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
```

## Debugging Tests

### Common Issues

1. **Timeout failures**: Increase timeout values or check async handling
2. **Mock server issues**: Verify server startup and proper URL usage
3. **Resource leaks**: Ensure proper cleanup in tearDown methods

### Debug Techniques

```java
// Enable debug logging
System.setProperty("com.cursor.plugin.debug", "true");

// Add debug output
System.out.println("Request: " + mockServer.takeRequest());

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

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 90%

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
- Java 17+
- Gradle 8.14+
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
