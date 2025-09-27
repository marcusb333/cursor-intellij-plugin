plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
    application
}

group = "com.cursor"
version = "0.5.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    intellijPlatform {
        // Support both IC (Community) and IU (Ultimate) builds
        val platformType = providers.gradleProperty("platformType").getOrElse("IU")
        val platformVersion = providers.gradleProperty("platformVersion").getOrElse("2025.2")

        when (platformType) {
            "IC" -> {
                // For versions before 2025.3, use intellijIdeaCommunity
                if (platformVersion < "2025.3") {
                    intellijIdeaCommunity(platformVersion)
                } else {
                    // Starting with 2025.3, IC is not available as a target
                    error("IntelliJ IDEA Community (IC) is not available for version $platformVersion and later. Use IU instead.")
                }
            }
            "IU" -> {
                // For newer versions, still use intellijIdeaUltimate
                intellijIdeaUltimate(platformVersion)
            }
            else -> error("Unknown platform type: $platformType. Use IC for Community or IU for Ultimate.")
        }
        
        // Bundled plugins required for testing
        bundledPlugin("com.intellij.java")
    }

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
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

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

// Publishing configuration for JetBrains Marketplace
publishing {
    publications {
        create<MavenPublication>("plugin") {
            from(components["java"])
            
            groupId = project.group.toString()
            artifactId = "cursor-intellij-plugin"
            version = project.version.toString()
            
            pom {
                name.set("Cursor AI IntelliJ Plugin")
                description.set("Integrate Cursor's powerful AI chatbot directly into IntelliJ IDEA")
                url.set("https://github.com/${System.getenv("GITHUB_REPOSITORY") ?: "your-username/cursor-intellij-plugin"}")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("cursor")
                        name.set("Cursor Team")
                        email.set("support@cursor.com")
                    }
                }
            }
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
