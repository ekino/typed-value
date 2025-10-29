
import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
  base
  alias(libs.plugins.node.gradle)
}

// Node.js/pnpm configuration
node {
  version.set("20.10.0")
  pnpmVersion.set("8.15.0")
  download.set(true)
  workDir.set(file("${project.projectDir}/.cache/nodejs"))
  pnpmWorkDir.set(file("${project.projectDir}/.cache/pnpm"))
}

// Path to the JS build output from typed-value-core
val jsPackageDir =
  file("${rootProject.projectDir}/typed-value-core/build/dist/js/productionLibrary")

// Configure the built-in pnpmInstall task
tasks.named("pnpmInstall") {
  // Ensure JS package exists before installing (both Node and Browser distributions write to the same dir)
  dependsOn(
    ":typed-value-core:jsNodeProductionLibraryDistribution",
    ":typed-value-core:jsBrowserProductionLibraryDistribution"
  )

  inputs.file("package.json")
  inputs.dir(jsPackageDir)
  outputs.dir("node_modules")
}

// Task: Run TypeScript type checking
val typeCheck by
  tasks.registering(PnpmTask::class) {
    description = "Run TypeScript type checking"
    group = "typescript"

    dependsOn("pnpmInstall")

    args.set(listOf("run", "type-check"))

    inputs.dir("tests")
    inputs.file("tsconfig.json")
    inputs.dir(jsPackageDir)
  }

// Task: Run Vitest tests
val pnpmTest by
  tasks.registering(PnpmTask::class) {
    description = "Run TypeScript integration tests with Vitest"
    group = "typescript"

    dependsOn("pnpmInstall")

    args.set(listOf("test"))

    inputs.dir("tests")
    inputs.file("vitest.config.ts")
    inputs.file("tsconfig.json")
    inputs.dir(jsPackageDir)
  }

// Wire into standard Gradle lifecycle
tasks.named("check") { dependsOn(typeCheck, pnpmTest) }

// Clean task
tasks.named("clean") {
  doLast {
    delete("node_modules")
    delete(".cache")
  }
}
