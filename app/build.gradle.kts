plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
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
    implementation("io.github.miensoap:aws-s3:1.0.3")
    implementation(platform("software.amazon.awssdk:bom:2.25.1"))
    implementation("software.amazon.awssdk:s3")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Test
    runtimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
}

tasks.named<Test>("test") {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    doFirst {
        val reportPath = reports.html.outputLocation.get().asFile
        println("\n[Will be generated] JaCoCo Report: file://${reportPath}/index.html\n")
    }

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/mock/*",
                    "**/dto/**",
                    "**/aop/**",
                    "**/config/**",
                    "**/lib/*",
                    "**/event/**",
                    "**/exception/**",
                    "**/*Application*",
                    "**/*InitData*"
                )
            }
        })
    )

    finalizedBy(tasks.jacocoTestCoverageVerification)
}
