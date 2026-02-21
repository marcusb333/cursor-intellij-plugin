---
alwaysApply: true
---

# Cursor IntelliJ Plugin - Project Rules

## Project Context

This is an IntelliJ plugin that integrates Cursor's AI chatbot into IntelliJ IDEA. It provides code assistance, generation, and explanation capabilities.

- **Language**: Kotlin
- **Runtime**: Java 21
- **Build**: Gradle with Kotlin DSL
- **Key packages**: `io.threethirtythree.plugin` (service, ui, settings, actions)

## Workflow

- **Branch per task**: Create a new branch for each task or feature (e.g., `feature/foo`, `fix/bar`)
- **Test before push**: Run `./gradlew test` (or `./gradlew build`) before pushing changes
- **Breaking changes**: For changes that affect API compatibility or behavior across Cursor models (Claude, GPT, Gemini, Grok, Composer), ask the user to create a PR for review before merging

## Development Conventions

- Use companion objects for factory methods (e.g., `getInstance(project)`)
- Add KDoc comments for public methods and classes
- Follow IntelliJ Platform patterns: `@Service`, `AnAction`, `Project` from existing code
- Testing: JUnit 5, Mockito; integration tests that require API key use `@Disabled`

## Cursor API Integration

- **Endpoint**: `https://api.cursor.com/v1/chat/completions`
- **Model**: Plugin currently hardcodes `gpt-3.5-turbo` in `CompletionsChatAsyncService.kt` (line 108)
- **Auth**: Bearer token from plugin settings or `CURSOR_API_KEY` environment variable

## Cursor Models Reference

Models supported by Cursor (see [cursor.com/docs/models](https://cursor.com/docs/models)):

| Model | Default Context | Max Mode |
|-------|-----------------|----------|
| Claude 4.6 Opus | 200k | 1M |
| Claude 4.6 Sonnet | 200k | 1M |
| Composer 1.5 | 200k | - |
| Gemini 3 Flash | 200k | 1M |
| Gemini 3.1 Pro | 200k | 1M |
| GPT-5.2 | 272k | - |
| GPT-5.3 Codex | 272k | - |
| Grok Code | 256k | - |

Note: API model IDs may differ from display names. The Cursor API accepts OpenAI-compatible model names.

## Key Files

- `build.gradle.kts` - IntelliJ Platform dependency uses `create(IntelliJPlatformType.IntellijIdeaCommunity)` with `useInstaller=false`
- `src/main/kotlin/` - Plugin implementation
- `src/main/resources/META-INF/plugin.xml` - Plugin descriptor
