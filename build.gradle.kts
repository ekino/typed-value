plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.spring) apply false
  alias(libs.plugins.kotlin.jpa) apply false
  alias(libs.plugins.kotlin.kapt) apply false
  alias(libs.plugins.spring.boot) apply false
  alias(libs.plugins.spring.dependency.management) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.gradle.maven.publish.plugin) apply false
}

// Modules that should not be published to Maven
val nonPublishableModules = listOf("typed-value-integration-tests", "typescript-integration")

allprojects {
  group = "com.ekino.oss"
  version =
    when {
      // CI environment - GitHub Actions
      System.getenv("GITHUB_ACTIONS") != null -> {
        // Use GITHUB_REF_NAME for tag-based releases (unified versioning: only v* tags)
        val tag = System.getenv("GITHUB_REF_NAME")?.takeIf { it.startsWith("v") }

        tag?.removePrefix("v") // v1.0.0 -> 1.0.0
          ?: // Use git describe to match CI pipeline versioning exactly
          runCatching {
              val gitDescribe =
                providers
                  .exec { commandLine("git", "describe", "--tags", "--always", "--dirty", "--abbrev=7") }
                  .standardOutput
                  .asText
                  .get()
                  .trim()
              val version = gitDescribe.removePrefix("v")
              if (version.contains("-")) {
                "$version-SNAPSHOT"
              } else {
                "$version-SNAPSHOT"
              }
            }
            .getOrElse {
              // Fallback to commit SHA
              val sha = System.getenv("GITHUB_SHA") ?: "unknown"
              "${sha.take(7)}-SNAPSHOT"
            }
      }
      // Local development - ALWAYS use localVersion from gradle.properties
      else -> project.findProperty("localVersion") as String? ?: "1.1.0-SNAPSHOT"
    }

  repositories { mavenCentral() }
}

subprojects {
  // typed-value-core uses kotlin-multiplatform, integration-tests defines its own plugins
  if (name != "typed-value-core" && name !in nonPublishableModules) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
  }

  // Apply publishing plugin to publishable modules
  if (name !in nonPublishableModules) {
    apply(plugin = "com.vanniktech.maven.publish")
  }

  // Skip spotless and detekt for typescript-integration (no Kotlin sources)
  if (name != "typescript-integration") {
    apply(plugin = "com.diffplug.spotless")
  }
  // Skip detekt for integration-tests due to Kotlin version compatibility
  if (name !in nonPublishableModules) {
    apply(plugin = "io.gitlab.arturbosch.detekt")
  }

  if (name != "typescript-integration") {
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
      val licenseHeaderText =
        """
        |/*
        | * Copyright (c) 2025 ekino (https://www.ekino.com/)
        | */
        """
          .trimMargin()

      kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        licenseHeader(licenseHeaderText)
        ktfmt().googleStyle()
      }
      kotlinGradle {
        target("*.gradle.kts")
        ktfmt().googleStyle()
      }
      java {
        target("**/*.java")
        targetExclude("**/build/**")
        licenseHeader(licenseHeaderText)
        removeUnusedImports()
        googleJavaFormat()
      }
      format("typescript") {
        target("**/*.ts", "**/*.mts")
        targetExclude("**/build/**", "**/node_modules/**")
        licenseHeader(licenseHeaderText, "(import|export|const|let|var|type|interface|function|class|/\\*\\*)")
      }
    }
  }

  if (name !in nonPublishableModules) {
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
      buildUponDefaultConfig = true
      allRules = false
      config.setFrom(files("$rootDir/detekt.yml"))
    }
  }

  // JVM-specific configuration (exclude multiplatform and integration tests)
  if (name != "typed-value-core" && name !in nonPublishableModules) {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
      }
    }

    tasks.withType<Test> { useJUnitPlatform() }

    extensions.configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
    }
  }

  // Maven Central publishing configuration
  if (name !in nonPublishableModules) {
    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
      coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString(),
      )

      publishToMavenCentral(automaticRelease = true)
      signAllPublications()

      pom {
        name.set(project.name)
        description.set("Type-safe entity identifiers for Kotlin Multiplatform")
        url.set("https://github.com/ekino/typed-value")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }

        developers {
          developer {
            id.set("Benoit.Havret")
            name.set("Benoît Havret")
            email.set("benoit.havret@ekino.com")
            organization.set("ekino")
            organizationUrl.set("https://github.com/ekino")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/ekino/typed-value.git")
          developerConnection.set("scm:git:ssh://github.com/ekino/typed-value.git")
          url.set("https://github.com/ekino/typed-value")
        }
      }
    }
  }
}
