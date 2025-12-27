plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":core:core-enum"))
    implementation(project(":clients:client-ocr"))
    implementation(project(":clients:client-aws"))
    implementation(project(":clients:client-search"))
    implementation(project(":support:common"))
    implementation(project(":support:logging"))
    implementation(project(":support:monitoring"))
    implementation(project(":storage:db-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
}
