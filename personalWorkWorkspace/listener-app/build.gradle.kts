plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // https://mvnrepository.com/artifact/org.keycloak/keycloak-server-spi
    compileOnly("org.keycloak:keycloak-server-spi:25.0.1")
// https://mvnrepository.com/artifact/org.keycloak/keycloak-core
    implementation("org.keycloak:keycloak-core:25.0.1")

// https://mvnrepository.com/artifact/org.keycloak/keycloak-server-spi-private
    compileOnly("org.keycloak:keycloak-server-spi-private:25.0.1")
    implementation("org.keycloak:keycloak-admin-client:25.0.3")
    // https://mvnrepository.com/artifact/org.keycloak/keycloak-services
    compileOnly("org.keycloak:keycloak-services:25.0.1")


}

tasks.withType<Test> {
    useJUnitPlatform()
}
