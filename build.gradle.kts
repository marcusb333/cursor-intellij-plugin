plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.0.0"
    application
}

group = "com.cursor"
version = "0.0.4"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    intellijPlatform {
        // Build with 2024.3.6 but plugin is compatible with all versions
        intellijIdeaCommunity("2024.3.6")
        instrumentationTools()
    }

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    // implementation("com.azure:azure-identity:1.17.0")

    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.assertj:assertj-core:3.25.1")
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

application {
    mainClass = "com.cursor.plugin.CursorPluginService"
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll("-Xjvm-default=all")
        }
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
