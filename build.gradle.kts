import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    application
    java
}

group = "nl.dannyjelsma"
version = "1.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:4.4.0_351")
    implementation("com.github.JDA-Applications.JDA-Utilities:jda-utilities:804d58a5ed")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

application {
    mainClass.set("nl.dannyjelsma.soundboardbot.SoundBoardBot")
}