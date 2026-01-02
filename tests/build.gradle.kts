plugins {
    jacoco
}

dependencies {
    testImplementation(project(":core:core-app"))
    testImplementation(project(":core:core-api"))
    testImplementation(project(mapOf("path" to ":core:core-api", "configuration" to "runtimeElements")))
    testImplementation(project(":core:core-enum"))
    testImplementation(project(":clients"))
    testImplementation(project(":storage:db-core"))
    testImplementation(project(":support:common"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testRuntimeOnly("com.h2database:h2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

    val targetProjects = listOf(
        project(":core:core-api"),
        project(":core:core-app"),
        project(":core:core-enum"),
        project(":clients"),
        project(":storage:db-core"),
        project(":support:common"),
        project(":support:logging"),
        project(":support:monitoring")
    )

    sourceDirectories.setFrom(
        files(
            targetProjects.map { target ->
                target.projectDir.resolve("src/main/java")
            } + targetProjects.map { target ->
                target.projectDir.resolve("src/main/kotlin")
            }
        )
    )

    val classTrees = targetProjects.flatMap { target ->
        listOf(
            target.layout.buildDirectory.dir("classes/java/main").get().asFile,
            target.layout.buildDirectory.dir("classes/kotlin/main").get().asFile
        )
    }.map { classesDir ->
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
