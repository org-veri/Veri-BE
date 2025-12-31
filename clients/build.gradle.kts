dependencies {
    implementation(project(":core:core-api"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // AWS
    api("io.github.miensoap:aws-s3:1.0.3")
    implementation(platform("software.amazon.awssdk:bom:2.25.1"))
    implementation("software.amazon.awssdk:s3")
}
