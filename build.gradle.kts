plugins {
    java
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

allprojects {
    group = "${property("projectGroup")}"
    version = "${property("applicationVersion")}"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of("${property("javaVersion")}")
        }
    }

    tasks.named<Jar>("bootJar").configure {
        enabled = false
    }

    tasks.named<Jar>("jar").configure {
        enabled = true
    }

    tasks.test {
        useJUnitPlatform {
            excludeTags("develop", "restdocs")
        }
    }
}
