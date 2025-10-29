plugins { id("org.jetbrains.kotlin.jvm") }

dependencies {
  api(project(":typed-value-core"))

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  implementation(libs.jackson.databind)
  implementation(libs.jackson.module.kotlin)

  // Testing
  testImplementation(libs.bundles.testing)
}
