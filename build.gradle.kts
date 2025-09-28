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
        intellijDependencies()
    }
}

dependencies {
    intellijPlatform {
        // Use a stable version of IntelliJ Platform
        val platformVersion = providers.gradleProperty("platformVersion").getOrElse("2024.2")
        intellijIdeaUltimate(platformVersion)
        
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
