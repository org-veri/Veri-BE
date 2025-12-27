plugins {
    java
    jacoco
}

dependencies {
    testImplementation(project(":core:core-api"))
    testImplementation(project(mapOf("path" to ":core:core-api", "configuration" to "runtimeElements")))
    testImplementation(project(":core:core-enum"))
    testImplementation(project(":clients:client-ocr"))
    testImplementation(project(":clients:client-aws"))
    testImplementation(project(":clients:client-search"))
    testImplementation(project(":storage:db-core"))
    testImplementation(project(":support:common"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
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

    val appMainClasses = project(":core:core-api").layout.buildDirectory.dir("classes/java/main").get().asFile
    val appKotlinClasses = project(":core:core-api").layout.buildDirectory.dir("classes/kotlin/main").get().asFile
    val classTrees = listOf(appMainClasses, appKotlinClasses).map { classesDir ->
        fileTree(classesDir) {
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
    }

    classDirectories.setFrom(files(classTrees))

    finalizedBy(tasks.jacocoTestCoverageVerification)
}
