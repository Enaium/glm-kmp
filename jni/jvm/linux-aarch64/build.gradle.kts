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

/*
 * Per-OS/arch JNI artifact: linux-aarch64.
 * Ships libglm_wrapper.so as a classpath resource at
 * /cn/enaium/glm/native/linux-aarch64/. Built natively on Linux ARM64 hosts
 * or cross-compiled on Linux x86_64 hosts when the aarch64-linux-gnu
 * toolchain is installed (CI does this via
 * `sudo apt-get install gcc-aarch64-linux-gnu g++-aarch64-linux-gnu`).
 */
import org.gradle.internal.os.OperatingSystem

plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

group = rootProject.group
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val jniOs = "linux"
val jniArch = "aarch64"
val classifier = "$jniOs-$jniArch"
val libFile = "libglm_wrapper.so"
val resourceDir = "cn/enaium/glm/native/$classifier"

val host = OperatingSystem.current()
val hostArch = System.getProperty("os.arch").lowercase()
val hostIsLinuxX64 = host.isLinux && (hostArch == "amd64" || hostArch == "x86_64")
val hostIsLinuxArm64 = host.isLinux && (hostArch == "aarch64" || hostArch == "arm64")

fun hasAarch64CrossToolchain(): Boolean {
    return System.getenv("PATH")?.split(File.pathSeparator).orEmpty().any { dir ->
        val f = File(dir, "aarch64-linux-gnu-gcc")
        f.isFile && f.canExecute()
    }
}

val canBuildHere = hostIsLinuxArm64 || (hostIsLinuxX64 && hasAarch64CrossToolchain())

val nativeOutputDir = layout.buildDirectory.dir("jni-native/$classifier")
val cmakeBuildDir = layout.buildDirectory.dir("cmake-jni/$classifier")

val configureJniLibrary by tasks.registering(Exec::class) {
    group = "build"
    description = "cmake-configures libglm_wrapper for $classifier."
    onlyIf { canBuildHere }
    val outDir = nativeOutputDir.get().asFile
    val buildDir = cmakeBuildDir.get().asFile
    doFirst {
        outDir.mkdirs()
        buildDir.mkdirs()
    }
    workingDir = buildDir
    val javaHome = System.getProperty("java.home") ?: System.getenv("JAVA_HOME") ?: ""
    val jniInclude = if (javaHome.isNotEmpty()) "$javaHome/include" else ""
    val args = mutableListOf(
        "cmake",
        rootProject.file("jni").absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DJNI_INCLUDE_DIR=$jniInclude",
        "-DJNI_INCLUDE_DIR_PLATFORM=$jniInclude/linux",
        "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=${outDir.absolutePath}",
    )
    if (hostIsLinuxX64) {
        args += listOf(
            "-DCMAKE_SYSTEM_NAME=Linux",
            "-DCMAKE_SYSTEM_PROCESSOR=aarch64",
            "-DCMAKE_C_COMPILER=aarch64-linux-gnu-gcc",
            "-DCMAKE_CXX_COMPILER=aarch64-linux-gnu-g++",
        )
    }
    commandLine(args)
}

val buildJniLibrary by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds libglm_wrapper.so for $classifier."
    onlyIf { canBuildHere }
    dependsOn(configureJniLibrary)
    workingDir = cmakeBuildDir.get().asFile
    commandLine("cmake", "--build", ".", "--config", "Release")
    inputs.files(rootProject.file("jni/CMakeLists.txt"), rootProject.file("jni/jni_bridge.cpp"))
    inputs.dir(rootProject.file("jni/c_api"))
    inputs.dir(rootProject.file("glm"))
    outputs.file(nativeOutputDir.map { it.file(libFile) })
}

tasks.named<Copy>("processResources") {
    dependsOn(buildJniLibrary)
    // Use the build task's declared outputs (lazily resolved at execution
    // time) instead of the directory Provider, which may be snapshotted
    // empty at configuration time.
    from(buildJniLibrary.map { it.outputs.files }) {
        include(libFile)
        into(resourceDir)
    }
}

// Signing is enabled when a signing key is provided via -Psigning.* (CI
// passes the repository secrets; local Maven Local tests can skip signing).
val signingKeyId = providers.gradleProperty("signing.keyId").orNull

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (!signingKeyId.isNullOrBlank()) {
        signAllPublications()
    }
    coordinates(
        groupId = rootProject.group.toString(),
        artifactId = "glm-kmp-jni-jvm-$classifier",
        version = rootProject.version.toString(),
    )
    pom {
        name.set("glm-kmp-jni-jvm-$classifier")
        description.set(
            "Prebuilt JNI shared library for glm-kmp on $jniOs/$jniArch. " +
                "Loaded automatically by NativeLoader; not intended to be depended on directly.",
        )
        url.set("https://github.com/Enaium/glm-kmp")
        inceptionYear.set("2026")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Enaium")
            }
        }
        scm {
            url.set("https://github.com/Enaium/glm-kmp")
            connection.set("scm:git:git@github.com:Enaium/glm-kmp.git")
            developerConnection.set("scm:git:git@github.com:Enaium/glm-kmp.git")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/Enaium/glm-kmp/issues")
        }
    }
}
