plugins { `java-platform` }

dependencies {
  constraints {
    api(project(":typed-value-core"))
    api(project(":typed-value-jackson"))
    api(project(":typed-value-hibernate"))
    api(project(":typed-value-spring"))
    api(project(":typed-value-querydsl"))
    api(project(":typed-value-spring-data-elasticsearch"))
  }
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
  configure(com.vanniktech.maven.publish.JavaPlatform())
}
