import org.jmailen.gradle.kotlinter.support.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.kotlin.kapt") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.5.3"
    id("org.jmailen.kotlinter") version "5.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

version = "0.1"
group = "com.leeturner.xport"

val kotlinVersion= project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    kapt("info.picocli:picocli-codegen")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    
    implementation("info.picocli:picocli")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation(platform("dev.forkhandles:forkhandles-bom:2.22.3.0"))
    implementation("dev.forkhandles:result4k")
    
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.strikt:strikt-core:0.35.1")
    testImplementation("dev.forkhandles:result4k-strikt")
    testImplementation("org.skyscreamer:jsonassert:2.0-rc1")
}

application {
    mainClass = "com.leeturner.xport.cli.XportCommand"
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlinter {
    reporters = arrayOf(ReporterType.checkstyle.name, ReporterType.plain.name, ReporterType.html.name )
}

detekt {
    toolVersion = "1.23.6"
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.leeturner.xport.*")
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}


