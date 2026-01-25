plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.4"
}

group = "com.back"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    // --- Kotlin ---
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    // --- Database ---
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // --- Redis & Cache ---
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // --- JWT ---
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // --- OpenAPI ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // --- Jakarta Annotation ---
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    // --- Dotenv ---
    implementation("me.paulschwarz:spring-dotenv:5.1.0")

    // --- Querydsl ---
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")

    // --- Apache Commons Lang (CVE 대응) ---
    implementation("org.apache.commons:commons-lang3:3.20.0") {
        because("Fix CVE-2025-48924: Uncontrolled Recursion vulnerability")
    }

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.2")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}