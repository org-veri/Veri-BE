dependencies {
    implementation(project(":core:core-enum"))
    implementation(project(":support:common"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    api(platform("org.springframework.modulith:spring-modulith-bom:2.0.1"))
    api("org.springframework.modulith:spring-modulith-starter-core")

    api("me.miensoap:fluent:1.0-SNAPSHOT") {
        isChanging = true
    }

}
