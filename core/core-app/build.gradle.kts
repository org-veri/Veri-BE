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
}
