/*
 * Copyright (c) 2026 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js {
        nodejs()
        binaries.executable()
    }

    wasmJs {
        nodejs()
        binaries.executable()
    }

    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()

    val nativeTargets = listOf(macosArm64(), macosX64(), linuxX64(), mingwX64())
    nativeTargets.forEach { target ->
        target.binaries.executable {
            entryPoint = "cn.enaium.glm.example.main"
        }
    }

    sourceSets {
        // Consume the artifact published to the local Maven repository
        // (run `./gradlew :glm-kmp:publishToMavenLocal` first).
        getByName("commonMain") {
            dependencies {
                implementation("cn.enaium.glm:glm-kmp:1.0.0")
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // The default hierarchy template is disabled project-wide, so the
        // shared native source set is wired up manually.
        val nativeMain = create("nativeMain")
        val nativeTest = create("nativeTest")
        nativeMain.dependsOn(getByName("commonMain"))
        nativeTest.dependsOn(getByName("commonTest"))

        listOf("macosArm64Main", "macosX64Main", "linuxX64Main", "mingwX64Main").forEach {
            getByName(it).dependsOn(nativeMain)
        }
        listOf("macosArm64Test", "macosX64Test", "linuxX64Test", "mingwX64Test").forEach {
            getByName(it).dependsOn(nativeTest)
        }
    }
}

// Runs the JVM example: `./gradlew :example:runJvm`
tasks.register<JavaExec>("runJvm") {
    group = "application"
    description = "Runs the JVM example."
    dependsOn("jvmMainClasses")
    val jvmTarget = kotlin.targets["jvm"]
    classpath = jvmTarget.compilations["main"].output.allOutputs +
        configurations["jvmRuntimeClasspath"]
    mainClass.set("cn.enaium.glm.example.MainKt")
}
