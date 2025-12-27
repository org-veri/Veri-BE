plugins {
    id("java")
}

group = "org.veri"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":clients:client-aws"))
    implementation(project(":clients:client-ocr"))
    implementation(project(":clients:client-search"))

    implementation("org.springframework.boot:spring-boot-starter")
}
