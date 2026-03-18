plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.jpa)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

dependencies {
  // typed-value modules (the libraries we're testing)
  implementation(project(":typed-value-core"))
  implementation(project(":typed-value-jackson"))
  implementation(project(":typed-value-hibernate"))
  implementation(project(":typed-value-spring"))
  implementation(project(":typed-value-querydsl"))
  implementation(project(":typed-value-spring-data-elasticsearch"))

  // Spring Boot
  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.data.elasticsearch)
  implementation(libs.kotlin.reflect)
  runtimeOnly(libs.kotlin.compiler.embeddable)

  // QueryDSL (OpenFeign fork for Jakarta EE support)
  implementation(libs.querydsl.feign.core)
  implementation(libs.querydsl.feign.jpa)
  kapt(libs.querydsl.feign.apt) { artifact { classifier = "jpa" } }

  // Test dependencies
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.starter.webmvc.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation(libs.testcontainers.elasticsearch)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.kotlin.test.junit5)
  testImplementation(libs.assertk)
  testImplementation(libs.datafaker)
  testRuntimeOnly(libs.postgresql)
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict", "-Xcontext-parameters") } }

tasks.withType<Test> { useJUnitPlatform() }

// Disable bootJar since this is a test-only module, not a runnable application
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") { enabled = false }

// Enable plain jar for compilation purposes
tasks.named<Jar>("jar") { enabled = true }

// Configure kapt for QueryDSL Q-class generation
kapt { arguments { arg("querydsl.entityAccessors", "true") } }
