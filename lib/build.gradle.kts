import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(platform("com.google.cloud:libraries-bom:26.48.0"))
    implementation("com.google.cloud:google-cloud-secretmanager")
    implementation("com.google.cloud:google-cloud-kms")

    implementation("org.testcontainers:testcontainers:1.20.2")
    implementation("org.wiremock:wiremock-grpc-extension:0.8.1")
    implementation("ch.qos.logback:logback-classic:1.5.9")

    testImplementation("org.testcontainers:junit-jupiter:1.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

val buildDockerImage by tasks.registering(DockerBuildImage::class) {
    group = "docker"
    description = "Builds wiremock gcp docker image."
    inputDir.set(file("../docker"))
    dockerFile.set(file("../docker/Dockerfile"))
    images.add("wiremock-gcp-grpc:latest")
}
