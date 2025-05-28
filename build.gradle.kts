plugins {
    kotlin("jvm") version "2.1.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.antlr:antlr4:4.12.0")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.junit.platform:junit-platform-suite-engine:1.9.2")
    implementation(kotlin("reflect"))
    implementation("org.slf4j:slf4j-simple:1.7.36")
}

tasks.test {
    useJUnitPlatform()
}

