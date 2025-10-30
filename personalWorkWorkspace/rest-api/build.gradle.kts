plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    // Добавляем плагин для генерации OpenAPI (Swagger)
    id("org.springdoc.openapi-gradle-plugin") version "1.6.0"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // Flyway PostgreSQL
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.15.2")

    // SpringDoc OpenAPI WebMVC UI (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // OAuth2 Resource Server
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Keycloak Core and Admin Client
    implementation("org.keycloak:keycloak-core:25.0.6")
    compileOnly("org.keycloak:keycloak-services:25.0.6")
    implementation("org.keycloak:keycloak-admin-client:25.0.6")

    // MySQL Connector
    implementation("mysql:mysql-connector-java:8.0.33")

    // JGit
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.2.0.202503040940-r")

    // Spring Boot Web and Security
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // SpringDoc OpenAPI WebFlux UI (если нужен)
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")

    // Ваши проекты
    implementation(project(":workspace-lib"))
    implementation(project(":kafka-attributes"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Конфигурация плагина OpenAPI (пример)
openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$buildDir/generated/openapi"))
    outputFileName.set("openapi.yaml")
    waitTimeInSeconds.set(10)
}
