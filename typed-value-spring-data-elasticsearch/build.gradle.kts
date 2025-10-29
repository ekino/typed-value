plugins { id("org.jetbrains.kotlin.jvm") }

dependencies {
  api(project(":typed-value-core"))

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.spring.data.elasticsearch)

  // Testing
  testImplementation(libs.bundles.testing)
  testImplementation(libs.spring.data.elasticsearch)
}
