plugins {
    id("java")
}

group = "com.avianmc.sprocket"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.raylabz:opensimplex:1.0.3")
}

tasks.test {
    useJUnitPlatform()
}