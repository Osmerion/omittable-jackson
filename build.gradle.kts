/*
 * Copyright 2025-2026 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(buildDeps.plugins.gradle.buildconfig)
    id("com.osmerion.maven-publish-conventions")
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])

        pom {
            description = "A support library providing a Jackson module for the Omittable library to support serializing and deserializing omittable types."
        }
    }
}

buildConfig {
    packageName = "com.osmerion.omittable.jackson.internal"

    buildConfigField("GROUP_ID", publishing.publications.named<MavenPublication>("mavenJava").map { it.groupId })
    buildConfigField("ARTIFACT_ID", publishing.publications.named<MavenPublication>("mavenJava").map { it.artifactId })
    buildConfigField("VERSION_MAJOR", version.toString().substringBefore('.').toInt())
    buildConfigField("VERSION_MINOR", version.toString().substringAfter('.').substringBefore('.').toInt())
    buildConfigField("VERSION_PATCH", version.toString().substringAfter('.').substringAfter('.').substringBefore('-').toInt())
    buildConfigField("SNAPSHOT_INFO", version.toString().let { if (it.contains('-')) it.substringAfter('-') else null })
}

dependencies {
    api(libs.jackson.databind)
    api(libs.jspecify)
    api(libs.omittable)

    testImplementation(project.dependencies.platform(buildDeps.junit.bom))
    testImplementation(buildDeps.assertj.core)
    testImplementation(buildDeps.junit.jupiter.api)

    testRuntimeOnly(buildDeps.junit.jupiter.engine)
    testRuntimeOnly(buildDeps.junit.platform.launcher)
}
