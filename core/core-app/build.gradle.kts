tasks.named<Jar>("bootJar").configure {
    enabled = true
}

tasks.named<Jar>("jar").configure {
    enabled = true
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":clients:client-aws"))
    implementation(project(":clients:client-ocr"))
    implementation(project(":clients:client-search"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation(platform("org.springframework.modulith:spring-modulith-bom:2.0.1"))
    implementation("org.springframework.modulith:spring-modulith-starter-core")
}
