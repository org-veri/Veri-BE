plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":support:logging"))
    implementation(project(":support:monitoring"))

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("me.miensoap:fluent:1.0-SNAPSHOT") {
        isChanging = true
    }

    // S3
    implementation(platform("software.amazon.awssdk:bom:2.25.1"))
    implementation("software.amazon.awssdk:s3")
    implementation("io.github.miensoap:aws-s3:1.0.3")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Local runtime
    runtimeOnly("com.h2database:h2")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
}
