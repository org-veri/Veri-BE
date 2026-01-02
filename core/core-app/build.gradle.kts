tasks.named<Jar>("bootJar").configure {
    enabled = true
}

tasks.named<Jar>("jar").configure {
    enabled = true
}

springBoot {
    mainClass.set("org.veri.be.Application") // Java25 psvm 간소화 적용시 자동 인식 불가
}

dependencies {
    implementation(project(":core:core-api"))
    implementation(project(":clients"))

    implementation("org.springframework.boot:spring-boot-starter")
}
