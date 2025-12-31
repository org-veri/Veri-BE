tasks.named<Jar>("bootJar").configure {
    enabled = true
}

tasks.named<Jar>("jar").configure {
    enabled = true
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":clients"))

    implementation("org.springframework.boot:spring-boot-starter")
}
