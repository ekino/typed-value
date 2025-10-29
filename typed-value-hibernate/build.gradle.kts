plugins { id("org.jetbrains.kotlin.jvm") }

dependencies {
  api(project(":typed-value-core"))

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.jakarta.persistence.api)
  compileOnly(libs.spring.data.jpa)
  compileOnly(libs.hibernate.core)

  testImplementation(libs.bundles.testing)
  testImplementation(libs.jakarta.persistence.api)
  testImplementation(libs.spring.data.jpa)
  testImplementation(libs.hibernate.core)
}
