plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "io.threethirtythree"
version = "0.6.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Use create() with IC type for 2024.2 - multi-OS archive from Maven (installer resolution has issues)
        val platformVersion = providers.gradleProperty("platformVersion").getOrElse("2024.2")
        create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, platformVersion) {
            useInstaller.set(false)
        }
        jetbrainsRuntime()  // Required when using multi-OS archives (useInstaller = false)
        
        // Bundled plugins required for testing
        bundledPlugin("com.intellij.java")
    }

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // implementation("com.azure:azure-identity:1.17.0")

    // Testing dependencies - updated for compatibility with IntelliJ 2025.2
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.11.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.assertj:assertj-core:3.25.1")
    
    // JUnit 4 compatibility for TestRule
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

// Exclude Kotlin stdlib from runtime dependencies to avoid conflicts with IntelliJ Platform
configurations.runtimeClasspath {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
}


tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll("-Xjvm-default=all")
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

// JetBrains Marketplace publishing configuration
intellijPlatform {
    publishing {
        token.set(System.getenv("JETBRAINS_MARKETPLACE_TOKEN") ?: "")
        channels.set(listOf("default"))
    }
}

// IntelliJ Plugin specific tasks
tasks {
    patchPluginXml {
        sinceBuild.set("252")
        untilBuild.set("999.*")
        changeNotes.set("""
            <h3>Version 0.6.0</h3>
            <ul>
                <li>🚀 Compatible with both IntelliJ IDEA Community (IC) and Ultimate (IU) editions</li>
                <li>🔧 Fixed dependency conflicts and build system issues</li>
                <li>⚡ Migrated to Java HttpClient for reliable HTTP communication</li>
                <li>📈 Improved error handling and interface design</li>
                <li>🧪 All tests passing with comprehensive coverage</li>
                <li>🎯 CompletionsChatAsyncService now uses Dispatchers.Main</li>
                <li>🔗 Direct Cursor API integration with proper authentication</li>
                <li>🔄 Class-level coroutine scope with proper lifecycle management</li>
                <li>🧹 Resource cleanup and disposal mechanisms</li>
                <li>📝 Streamlined GitHub PR template with AI auto-fill workflow</li>
                <li>🗑️ Removed deprecated PR description generator action</li>
                <li>🔄 GitHub Actions workflow for intelligent PR template population</li>
                <li>🔧 Improved logging framework integration</li>
                <li>🐛 Fixed version consistency issues</li>
            </ul>
        """.trimIndent())
    }
    
    verifyPlugin {
        // Plugin verification settings
    }
    
    runIde {
        jvmArgs("-Xmx2048m")
    }
}
