plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":core:core-api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
