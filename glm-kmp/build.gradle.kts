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

import java.io.File
import java.util.Base64
import java.util.Properties
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

group = rootProject.group
version = rootProject.version

val jniDir = rootProject.projectDir.resolve("jni")
val glmDir = rootProject.projectDir.resolve("glm")
val cApiDir = jniDir.resolve("c_api")

val hostOs = OperatingSystem.current()
val hostArch = System.getProperty("os.arch").lowercase()

// Whether the current host can cross-compile the C library for the given
// Kotlin/Native target. Apple targets build from macOS via Xcode; linuxX64 and
// linuxArm64 build on Linux hosts (linuxArm64 via the aarch64-linux-gnu
// cross-compiler); mingwX64 builds on Linux hosts via MinGW (which matches
// Kotlin/Native's own MinGW linker) or natively on Windows.
fun canBuildNativeTarget(targetName: String): Boolean {
    return when {
        hostOs.isMacOsX && targetName.startsWith("macos") -> true
        hostOs.isMacOsX && targetName.startsWith("ios") -> true
        hostOs.isMacOsX && targetName.startsWith("tvos") -> true
        hostOs.isMacOsX && targetName.startsWith("watchos") -> true
        hostOs.isLinux && targetName == "linuxX64" -> true
        hostOs.isLinux && targetName == "linuxArm64" && hasAarch64CrossToolchain() -> true
        hostOs.isLinux && targetName == "mingwX64" && hasMingwCrossToolchain() -> true
        hostOs.isWindows && targetName == "mingwX64" -> true
        else -> false
    }
}

fun hasAarch64CrossToolchain(): Boolean {
    return System.getenv("PATH")?.split(File.pathSeparator).orEmpty().any { dir ->
        val f = File(dir, "aarch64-linux-gnu-gcc")
        f.isFile && f.canExecute()
    }
}

fun hasMingwCrossToolchain(): Boolean {
    return System.getenv("PATH")?.split(File.pathSeparator).orEmpty().any { dir ->
        val f = File(dir, "x86_64-w64-mingw32-gcc")
        f.isFile && f.canExecute()
    }
}

fun resolveCmakeExecutable(): String {
    val exeName = if (OperatingSystem.current().isWindows) "cmake.exe" else "cmake"

    System.getenv("PATH")?.split(File.pathSeparator).orEmpty().forEach { dir ->
        val candidate = File(dir, exeName)
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }

    val extraPaths = listOf(
        "/opt/homebrew/bin",
        "/usr/local/bin",
        "/usr/bin",
        "/opt/local/bin",
    )
    extraPaths.forEach { dir ->
        val candidate = File(dir, exeName)
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }

    val sdkCmakeRoot = resolveAndroidSdkDir()?.resolve("cmake")
    if (sdkCmakeRoot?.isDirectory == true) {
        val newest = sdkCmakeRoot.listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.name }
        val candidate = newest?.resolve("bin/$exeName")
        if (candidate?.isFile == true && candidate.canExecute()) return candidate.absolutePath
    }

    return exeName
}

val cmakeExecutable: String by lazy { resolveCmakeExecutable() }

// Resolves the Emscripten compiler (emcc) from PATH or common install dirs.
// CI uses the setup-emsdk action; locally `brew install emscripten` or the
// emsdk checkout under ~/emsdk both work.
fun resolveEmccExecutable(): String {
    System.getenv("PATH")?.split(File.pathSeparator).orEmpty().forEach { dir ->
        val candidate = File(dir, "emcc")
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }
    listOf(
        File(File(System.getProperty("user.home")), "emsdk/upstream/emscripten/emcc"),
        File("/opt/homebrew/opt/emscripten/bin/emcc"),
        File("/usr/local/opt/emscripten/bin/emcc"),
    ).forEach { candidate ->
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }
    return "emcc"
}

val emccExecutable: String by lazy { resolveEmccExecutable() }

kotlin {
    // ==================== JVM ====================
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // ==================== Android ====================
    android {
        namespace = "cn.enaium.glm"
        compileSdk = 37
        minSdk = 21

        withHostTest {}

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    // ==================== JS / Wasm ====================
    js {
        nodejs()
    }

    wasmJs {
        nodejs()
    }

    // ==================== Native ====================
    macosArm64()
    macosX64()

    linuxX64()
    linuxArm64()

    mingwX64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    tvosArm64()
    tvosSimulatorArm64()

    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    // ==================== cinterop for all native targets ====================
    targets.withType<KotlinNativeTarget> {
        val targetName = this.name
        val canBuild = canBuildNativeTarget(targetName)
        compilations.getByName("main") {
            cinterops {
                create("glmWrapper") {
                    defFile(project.file("src/nativeInterop/cinterop/glm_wrapper.def"))
                    includeDirs(
                        project.file("src/nativeInterop/cinterop"),
                        cApiDir,
                    )
                    if (canBuild) {
                        // Embed the per-target static library into the produced
                        // cinterop klib. Targets that can't be built on this host
                        // still get bindings (for klib publishing); the static
                        // library is built and embedded when building on the
                        // matching host.
                        val outputDir = layout.buildDirectory.dir("native/$targetName").get().asFile
                        extraOpts(
                            "-libraryPath", outputDir.absolutePath,
                            "-staticLibrary", "libglm_wrapper.a",
                        )
                    }
                }
            }
            defaultSourceSet.kotlin.srcDir("src/nativeMain/kotlin")
        }
    }

    // ==================== Source sets ====================
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        getByName("jvmMain") {
            dependencies {
                // Bundle all five JNI artifacts so consumers get the right
                // native binary out of the box; NativeLoader picks one at
                // runtime by os.name/os.arch.
                runtimeOnly(project(":jni-jvm-linux-x86_64"))
                runtimeOnly(project(":jni-jvm-linux-aarch64"))
                runtimeOnly(project(":jni-jvm-darwin-x86_64"))
                runtimeOnly(project(":jni-jvm-darwin-aarch64"))
                runtimeOnly(project(":jni-jvm-windows-x86_64"))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(libs.junit.jupiter)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }

        getByName("androidMain") {
            // Share the same JNI source code with JVM. On Android the
            // NativeLoader falls back to System.loadLibrary since the .so is
            // bundled inside the AAR's jniLibs.
            kotlin.srcDir("src/jvmMain/kotlin")
        }

        getByName("wasmJsMain") {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }

        // Emscripten glue, generated at build time (see below).
        getByName("jsMain") {
            kotlin.srcDir(layout.buildDirectory.dir("generated/glue/jsMain"))
        }
        getByName("wasmJsMain") {
            kotlin.srcDir(layout.buildDirectory.dir("generated/glue/wasmJsMain"))
        }
    }
}

// ==================== Desktop JVM ====================
// The JNI library is provided by the :jni-jvm-{linux,darwin}-{x86_64,aarch64}
// subprojects. Their JARs ship libglm_wrapper as a classpath resource that
// NativeLoader extracts at runtime - so :jvmTest needs no java.library.path
// tweak.
//
// Android host-test targets (Android unit tests running on the JVM) call
// System.loadLibrary directly from GlmNative.jvm.kt, so they still need a flat
// directory containing libglm_wrapper.{so,dylib}. We reuse the host's
// :jni-jvm-* subproject build output for that.
val hostJniProjectName = run {
    val arch = System.getProperty("os.arch").lowercase()
    val os = OperatingSystem.current()
    val archClassifier = when (arch) {
        "amd64", "x86_64", "x64" -> "x86_64"
        "aarch64", "arm64" -> "aarch64"
        else -> null
    }
    val osClassifier = when {
        os.isLinux -> "linux"
        os.isMacOsX -> "darwin"
        os.isWindows -> "windows"
        else -> null
    }
    if (osClassifier != null && archClassifier != null) {
        ":jni-jvm-$osClassifier-$archClassifier"
    } else {
        null
    }
}

if (hostJniProjectName != null) {
    val hostJniProject = project(hostJniProjectName)
    val hostNativeDir = hostJniProject.layout.buildDirectory.dir(
        "jni-native/${hostJniProjectName.removePrefix(":jni-jvm-")}",
    )

    tasks.withType<Test>().configureEach {
        if (name.contains("AndroidHostTest", ignoreCase = true)) {
            dependsOn("$hostJniProjectName:buildJniLibrary")
            systemProperty("java.library.path", hostNativeDir.get().asFile.absolutePath)
        }
    }
}

// ==================== Native: build static C library for each target ====================
fun registerNativeBuildTasks(targetName: String, cmakeFlags: List<String> = emptyList()) {
    val outputDir = layout.buildDirectory.dir("native/$targetName").get().asFile
    val cmakeBuildDir = layout.buildDirectory.dir("cmake-$targetName").get().asFile

    val configureTask = tasks.register<Exec>("configureNative_$targetName") {
        onlyIf { canBuildNativeTarget(targetName) }
        doFirst {
            cmakeBuildDir.mkdirs()
            outputDir.mkdirs()
        }
        workingDir = cmakeBuildDir
        commandLine(
            listOf(
                cmakeExecutable, jniDir.absolutePath,
                "-DCMAKE_BUILD_TYPE=Release",
                "-DBUILD_JNI=OFF",
                "-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=${outputDir.absolutePath}",
            ) + cmakeFlags,
        )
    }

    val buildTask = tasks.register<Exec>("buildNative_$targetName") {
        onlyIf { canBuildNativeTarget(targetName) }
        dependsOn(configureTask)
        workingDir = cmakeBuildDir
        commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    }

    tasks.matching {
        it.name.startsWith("cinteropGlmWrapper") &&
            it.name.endsWith(targetName.replaceFirstChar { c -> c.uppercase() })
    }.configureEach {
        dependsOn(buildTask)
    }
}

if (hostOs.isMacOsX) {
    registerNativeBuildTasks(
        "macosArm64",
        listOf(
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
        ),
    )
    registerNativeBuildTasks(
        "macosX64",
        listOf(
            "-DCMAKE_OSX_ARCHITECTURES=x86_64",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
        ),
    )
    registerNativeBuildTasks(
        "iosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=iphoneos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "iosX64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
            "-DCMAKE_OSX_ARCHITECTURES=x86_64",
            "-DCMAKE_OSX_SYSROOT=iphonesimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "iosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=iphonesimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "tvosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=tvOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=appletvos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "tvosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=tvOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=appletvsimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchsimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosDeviceArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
} else if (hostOs.isLinux) {
    registerNativeBuildTasks("linuxX64")
    registerNativeBuildTasks(
        "linuxArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=Linux",
            "-DCMAKE_SYSTEM_PROCESSOR=aarch64",
            "-DCMAKE_C_COMPILER=aarch64-linux-gnu-gcc",
            "-DCMAKE_CXX_COMPILER=aarch64-linux-gnu-g++",
        ),
    )
    // mingwX64 is cross-compiled with MinGW-w64; install it with
    // `sudo apt-get install gcc-mingw-w64-x86-64 g++-mingw-w64-x86-64`.
    registerNativeBuildTasks(
        "mingwX64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=Windows",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
            "-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc",
            "-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++",
            "-DCMAKE_RC_COMPILER=x86_64-w64-mingw32-windres",
        ),
    )
}

// ==================== JS/Wasm: Emscripten glue ====================
// The C ABI wrapper is compiled to a single self-contained JS file
// (wasm embedded as base64, synchronous instantiation, no node/browser
// dependencies via -sENVIRONMENT=web). The glue is embedded into the Kotlin
// sources as a base64 constant and evaluated at runtime with
// `new Function(atob(...) + '; return Module')()`, which works in both Node
// and browsers and needs no file resolution.
val emsGlueFile = layout.buildDirectory.file("ems/glm.js")

val buildGlmJs by tasks.registering(Exec::class) {
    group = "build"
    description = "Compiles the GLM C ABI wrapper to WebAssembly with Emscripten."
    workingDir = rootDir
    inputs.files(cApiDir.resolve("glm_wrapper.cpp"), cApiDir.resolve("glm_wrapper.h"))
    outputs.file(emsGlueFile)
    commandLine(
        emccExecutable,
        "-O2",
        "-std=c++17",
        "-I", glmDir.absolutePath,
        "-I", cApiDir.absolutePath,
        "-sSINGLE_FILE=1",
        "-sWASM_ASYNC_COMPILATION=0",
        "-sENVIRONMENT=web",
        "-sEXPORTED_RUNTIME_METHODS=HEAPF32",
        "-sEXPORTED_FUNCTIONS=_glm_call,_glm_op_count,_glm_version,_malloc,_free",
        "-o", emsGlueFile.get().asFile.absolutePath,
        cApiDir.resolve("glm_wrapper.cpp").absolutePath,
    )
}

val jsGlueDir = layout.buildDirectory.dir("generated/glue/jsMain")
val wasmJsGlueDir = layout.buildDirectory.dir("generated/glue/wasmJsMain")

val generateGlue by tasks.registering {
    group = "build"
    description = "Embeds the Emscripten glue into the js/wasmJs source sets."
    dependsOn(buildGlmJs)
    inputs.file(emsGlueFile)
    outputs.dir(jsGlueDir)
    outputs.dir(wasmJsGlueDir)
    doLast {
        // The glue is UTF-8 text whose non-ASCII characters are the raw wasm
        // bytes (code points 0..255). Re-encode it to Latin-1 (one byte per
        // character) so a plain atob() at runtime yields the exact glue text
        // without needing a TextDecoder round-trip.
        val glueText = emsGlueFile.get().asFile.readText(Charsets.UTF_8)
        val b64 = Base64.getEncoder().encodeToString(glueText.toByteArray(Charsets.ISO_8859_1))
        // Kotlin/JS: dynamic module handle.
        val jsKt = """
            // Generated by the :glm-kmp build — do not edit by hand.
            package cn.enaium.glm

            internal fun loadGlmModule(): dynamic =
                js("new Function(atob('$b64') + '; return Module')()")
        """.trimIndent() + "\n"
        // Kotlin/Wasm: external class describing the Emscripten module surface.
        val wasmKt = """
            // Generated by the :glm-kmp build — do not edit by hand.
            package cn.enaium.glm

            import org.khronos.webgl.Float32Array

            internal external class GlmModule : JsAny {
                fun _malloc(size: Int): Int
                fun _free(ptr: Int)
                fun _glm_call(op: Int, args: Int, argCount: Int, out: Int, outCount: Int)
                fun _glm_op_count(): Int
                fun _glm_version(): Int
                val HEAPF32: Float32Array
            }

            internal fun loadGlmModule(): GlmModule =
                js("new Function(atob('$b64') + '; return Module')()")
        """.trimIndent() + "\n"
        jsGlueDir.get().asFile.mkdirs()
        wasmJsGlueDir.get().asFile.mkdirs()
        jsGlueDir.get().asFile.resolve("GlmGlue.kt").writeText(jsKt)
        wasmJsGlueDir.get().asFile.resolve("GlmGlue.kt").writeText(wasmKt)
    }
}

tasks.named("compileKotlinJs").configure { dependsOn(generateGlue) }
tasks.named("compileKotlinWasmJs").configure { dependsOn(generateGlue) }

// Sources jars pick up the generated glue sources too.
tasks.matching {
    it.name.contains("SourcesJar") && (it.name.startsWith("js") || it.name.startsWith("wasmJs"))
}.configureEach { dependsOn(generateGlue) }

// ==================== Android: build JNI shared library per ABI ====================
val androidJniAbis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
val androidApiLevel = 21

val pinnedAndroidNdkVersion = "27.0.12077973"

fun resolveAndroidSdkDir(): java.io.File? {
    listOf("ANDROID_HOME", "ANDROID_SDK_ROOT").forEach { key ->
        System.getenv(key)?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    val localProps = rootProject.file("local.properties")
    if (localProps.isFile) {
        val props = Properties().apply { localProps.inputStream().use { load(it) } }
        props.getProperty("sdk.dir")?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    return null
}

fun resolveAndroidNdkDir(): java.io.File? {
    listOf("ANDROID_NDK_HOME", "ANDROID_NDK_ROOT", "NDK_HOME").forEach { key ->
        System.getenv(key)?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    val sdk = resolveAndroidSdkDir() ?: return null
    val ndkParent = sdk.resolve("ndk")
    if (!ndkParent.isDirectory) return null
    val pinned = ndkParent.resolve(pinnedAndroidNdkVersion)
    if (pinned.isDirectory) return pinned
    return ndkParent.listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }
}

val androidJniLibsDir = layout.buildDirectory.dir("jniLibs")
val resolvedAndroidNdk = resolveAndroidNdkDir()
val androidNdkToolchain = resolvedAndroidNdk?.resolve("build/cmake/android.toolchain.cmake")

val buildAndroidJniLibs by tasks.registering {
    group = "build"
    description = "Builds the JNI shared library for all Android ABIs."
}

androidJniAbis.forEach { abi ->
    val outputDir = layout.buildDirectory.dir("jniLibs/$abi")
    val cmakeBuildDir = layout.buildDirectory.dir("cmake-android-$abi").get().asFile

    val configureTask = tasks.register<Exec>("configureAndroidJni_$abi") {
        onlyIf { androidNdkToolchain?.isFile == true }
        doFirst {
            cmakeBuildDir.mkdirs()
            outputDir.get().asFile.mkdirs()
        }
        workingDir = cmakeBuildDir
        commandLine(
            cmakeExecutable, jniDir.absolutePath,
            "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_TOOLCHAIN_FILE=${androidNdkToolchain?.absolutePath ?: ""}",
            "-DANDROID_ABI=$abi",
            "-DANDROID_PLATFORM=android-$androidApiLevel",
            "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=${outputDir.get().asFile.absolutePath}",
        )
    }

    val buildTask = tasks.register<Exec>("buildAndroidJni_$abi") {
        onlyIf { androidNdkToolchain?.isFile == true }
        dependsOn(configureTask)
        workingDir = cmakeBuildDir
        commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    }

    buildAndroidJniLibs.configure { dependsOn(buildTask) }
}

androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addStaticSourceDirectory(
            androidJniLibsDir.get().asFile.absolutePath,
        )
    }
}

tasks.matching { it.name.startsWith("merge") && it.name.contains("JniLibFolders") }
    .configureEach { dependsOn(buildAndroidJniLibs) }

// ==================== Publishing ====================
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "glm-kmp",
        // null -> the plugin falls back to project.version
        version = null,
    )

    pom {
        name.set("glm-kmp")
        description.set(
            "Kotlin Multiplatform bindings for the GLM (OpenGL Mathematics) C++ library: " +
                "vec2/3/4, mat2/3/4, quaternions, transforms and the full core function set " +
                "on JVM, Android, Kotlin/Native, JS and Wasm.",
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
