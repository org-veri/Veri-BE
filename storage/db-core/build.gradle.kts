dependencies {
    implementation(project(":core:core-enum"))
    implementation(project(":support:common"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    api("me.miensoap:fluent:1.0-SNAPSHOT") {
        isChanging = true
    }

}
