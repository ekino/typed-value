plugins { id("org.jetbrains.kotlin.jvm") }

dependencies {
  api(project(":typed-value-core"))

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.querydsl.core)

  // For Q-classes supporting typed-value-hibernate abstract entities
  compileOnly(project(":typed-value-hibernate"))
  compileOnly(libs.querydsl.feign.core)
  compileOnly(libs.querydsl.feign.jpa)
  compileOnly(libs.jakarta.persistence.api)

  // Testing
  testImplementation(libs.bundles.testing)
  testImplementation(libs.querydsl.core)
}
