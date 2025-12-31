tasks.named<Jar>("bootJar").configure {
    enabled = true
}

tasks.named<Jar>("jar").configure {
    enabled = true
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":clients"))  // Merged client-aws, client-ocr, client-search

    implementation("org.springframework.boot:spring-boot-starter")
}
