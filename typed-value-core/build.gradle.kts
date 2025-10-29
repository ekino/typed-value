plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
}

kotlin {
  // Target platforms
  jvm { testRuns["test"].executionTask.configure { useJUnitPlatform() } }

  js(IR) {
    nodejs()
    browser { testTask { useMocha() } }
    binaries.library()
    useEsModules()
    generateTypeScriptDefinitions()

    compilations["main"].packageJson {
      customField("name", "@ekino/typed-value")
      customField("description", "Type-safe entity identifiers for TypeScript")
      customField("license", "Apache-2.0")
    }

    compilations.all {
      compileTaskProvider.configure {
        compilerOptions { freeCompilerArgs.add("-Xir-minimized-member-names=false") }
      }
    }
  }

  // Native targets - add as needed
  // macosArm64()
  // macosX64()
  // iosArm64()
  // iosX64()
  // iosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlin.stdlib)
        implementation(libs.kotlin.reflect)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.assertk)
      }
    }

    val jvmMain by getting

    val jvmTest by getting {
      dependencies {
        implementation(libs.junit.jupiter.api)
        implementation(libs.junit.jupiter.engine)
        implementation(libs.assertj)
      }
    }

    val jsMain by getting

    val jsTest by getting
  }
}

/**
 * Task to add phantom type parameters to generated TypeScript declarations. This enables
 * compile-time type safety in TypeScript without runtime overhead.
 */
val addPhantomTypes by
  tasks.registering {
    description = "Adds phantom type parameters to TypeScript declarations for compile-time safety"
    group = "build"

    // Run after the TypeScript validation task which generates the .d.mts file
    mustRunAfter(tasks.named("jsProductionLibraryValidateGeneratedByCompilerTypeScript"))

    doLast {
      val dtsFile = file("build/dist/js/productionLibrary/typed-value-typed-value-core.d.mts")
      if (dtsFile.exists()) {
        var content = dtsFile.readText()

        // Add phantom type parameters to classes
        content =
          content
            .replace(
              "export declare class TypedString {",
              "export declare class TypedString<T = unknown> {\n    private readonly __type?: T;",
            )
            .replace(
              "export declare class TypedInt {",
              "export declare class TypedInt<T = unknown> {\n    private readonly __type?: T;",
            )
            .replace(
              "export declare class TypedLong {",
              "export declare class TypedLong<T = unknown> {\n    private readonly __type?: T;",
            )

        // Add generic type parameters to factory functions
        content =
          content
            .replace(
              "export declare function createTypedString(value: string): TypedString;",
              "export declare function createTypedString<T = unknown>(value: string): TypedString<T>;",
            )
            .replace(
              "export declare function createTypedInt(value: number): TypedInt;",
              "export declare function createTypedInt<T = unknown>(value: number): TypedInt<T>;",
            )
            .replace(
              "export declare function createTypedLong(value: number): TypedLong;",
              "export declare function createTypedLong<T = unknown>(value: number): TypedLong<T>;",
            )

        // Add generic type parameters to compareTo methods
        content =
          content
            .replace(
              "compareTo(other: TypedString): number;",
              "compareTo(other: TypedString<T>): number;",
            )
            .replace(
              "compareTo(other: TypedInt): number;",
              "compareTo(other: TypedInt<T>): number;",
            )
            .replace(
              "compareTo(other: TypedLong): number;",
              "compareTo(other: TypedLong<T>): number;",
            )

        // The $metadata$ namespaces are internal Kotlin artifacts - we can leave them as-is
        // They don't affect TypeScript type safety

        dtsFile.writeText(content)
        logger.lifecycle("Added phantom type parameters to ${dtsFile.name}")
      } else {
        logger.warn("TypeScript declaration file not found: ${dtsFile.path}")
      }
    }
  }

// Hook the phantom types task into the build process
tasks.named("jsNodeProductionLibraryDistribution") { finalizedBy(addPhantomTypes) }

tasks.named("jsBrowserProductionLibraryDistribution") { finalizedBy(addPhantomTypes) }
