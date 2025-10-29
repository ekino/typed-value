plugins { id("org.jetbrains.kotlin.jvm") }

dependencies {
  api(project(":typed-value-core"))

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.spring.core)
  compileOnly(libs.spring.context)
  compileOnly(libs.spring.webmvc)
  compileOnly(libs.spring.boot.autoconfigure)

  // Testing
  testImplementation(libs.bundles.testing)
  testImplementation(libs.spring.core)
  testImplementation(libs.spring.context)
  testImplementation(libs.spring.webmvc)
}
