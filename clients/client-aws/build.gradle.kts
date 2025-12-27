plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    api("jakarta.validation:jakarta.validation-api")

    api("io.github.miensoap:aws-s3:1.0.3")
    implementation(platform("software.amazon.awssdk:bom:2.25.1"))
    implementation("software.amazon.awssdk:s3")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
