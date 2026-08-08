# glm-kmp

[![Maven Central](https://img.shields.io/maven-central/v/cn.enaium.glm/glm-kmp?label=Maven%20Central)](https://central.sonatype.com/artifact/cn.enaium.glm/glm-kmp)
[![License](https://img.shields.io/github/license/Enaium/glm-kmp)](https://github.com/Enaium/glm-kmp/blob/main/LICENSE)
[![GitHub Actions](https://img.shields.io/github/actions/workflow/status/Enaium/glm-kmp/test.yml?label=test)](https://github.com/Enaium/glm-kmp/actions/workflows/test.yml)
[![GitHub Repo stars](https://img.shields.io/github/stars/Enaium/glm-kmp?style=social)](https://github.com/Enaium/glm-kmp)

Kotlin Multiplatform bindings for [GLM](https://github.com/g-truc/glm) (OpenGL
Mathematics) — the header-only C++ math library for graphics and simulation,
wrapped from the `glm` git submodule. GLM is pure computation, so the bindings
cover every platform with the full core function set: vectors, matrices,
quaternions, transforms and scalar math, exposed with idiomatic Kotlin
operator overloads and member extensions.

## Supported Platforms

| Platform       | Targets                                                     | Mechanism                                  |
| -------------- | ----------------------------------------------------------- | ------------------------------------------ |
| **Android**    | arm64-v8a, armeabi-v7a, x86, x86_64                          | JNI (shared library via CMake)             |
| **JVM**        | Linux x86_64/aarch64, macOS arm64/x86_64, Windows x86_64     | JNI (per-OS/arch JAR resource, auto-extracted by `NativeLoader`) |
| **iOS**        | arm64, x64, simulatorArm64                                   | Kotlin/Native cinterop (static library)    |
| **macOS**      | arm64, x86_64                                                | Kotlin/Native cinterop (static library)    |
| **Linux**      | x86_64, arm64                                                | Kotlin/Native cinterop (static library)    |
| **Windows**    | mingwX64                                                     | Kotlin/Native cinterop (static library)    |
| **tvOS**       | arm64, simulatorArm64                                        | Kotlin/Native cinterop (static library)    |
| **watchOS**    | arm64, simulatorArm64, deviceArm64                           | Kotlin/Native cinterop (static library)    |
| **JavaScript** | Node/browser                                                 | Emscripten-compiled wasm embedded in the klib |
| **Wasm**       | wasmJs (Node/browser)                                        | Same Emscripten wasm                       |

## Gradle Dependency

**Kotlin Multiplatform / Android / JS / Wasm:**

```kotlin
implementation("cn.enaium.glm:glm-kmp:1.0.0")
```

**JVM:** the right native binary is resolved automatically — the `glm-kmp`
artifact pulls in the matching `:jni-jvm-*` sibling on the classpath:

- `glm-kmp-jni-jvm-linux-x86_64`
- `glm-kmp-jni-jvm-linux-aarch64`
- `glm-kmp-jni-jvm-darwin-x86_64`
- `glm-kmp-jni-jvm-darwin-aarch64`
- `glm-kmp-jni-jvm-windows-x86_64`

`NativeLoader` detects `os.name`/`os.arch` at runtime, extracts the matching
binary from the classpath to a temp directory, and `System.load`s it. No
`java.library.path` setup is required for downstream JVM consumers.

## Quick Start

```kotlin
import cn.enaium.glm.*
import kotlin.math.PI

// Vectors
val a = vec3(1.0f, 2.0f, 3.0f)
val b = vec3(4.0f, 5.0f, 6.0f)
val sum = a + b
val dot = a dot b
val cross = a cross b
val normalized = a.normalized()

// Matrices
val model = mat4()
    .translated(1.0f, 2.0f, 3.0f)
    .rotated(PI.toFloat() / 4.0f, 0.0f, 1.0f, 0.0f)
    .scaled(2.0f, 2.0f, 2.0f)

val view = lookAt(
    eye = vec3(0.0f, 0.0f, 5.0f),
    center = vec3(0.0f, 0.0f, 0.0f),
    up = vec3(0.0f, 1.0f, 0.0f),
)
val projection = perspective(
    fovyRadians = PI.toFloat() / 2.0f,
    aspect = 16.0f / 9.0f,
    near = 0.1f,
    far = 100.0f,
)
val clip = projection * view * model * vec4(0.0f, 0.0f, 0.0f, 1.0f)

// Quaternions
val q = quat(axis = vec3(0.0f, 0.0f, 1.0f), angle = PI.toFloat() / 2.0f)
val rotated = q * vec3(1.0f, 0.0f, 0.0f)
val slerped = q.slerped(quat().rotated(1.0f, vec3(1.0f, 0.0f, 0.0f)), 0.5f)

// Scalars
val x = clamp(mix(1.0f, 4.0f, 0.5f), 0.0f, 2.0f)  // 2.0
```

## API Reference

### Types

| Type    | Description                                              |
| ------- | -------------------------------------------------------- |
| `Vec2`  | 2-component vector (`x`, `y`)                            |
| `Vec3`  | 3-component vector (`x`, `y`, `z`)                       |
| `Vec4`  | 4-component vector (`x`, `y`, `z`, `w`)                  |
| `Mat2`  | 2x2 matrix (column-major, `m[column, row]`)              |
| `Mat3`  | 3x3 matrix (column-major, `m[column, row]`)              |
| `Mat4`  | 4x4 matrix (column-major, `m[column, row]`)              |
| `Quat`  | Quaternion (`x`, `y`, `z`, `w`)                          |

All types are value types (`vec3(1f, 2f, 3f) == vec3(1f, 2f, 3f)`) with
`operator` overloads for `+`, `-`, `*`, `/`, `unaryMinus` and `get`.

### Vector members

| Expression                                  | Description                          |
| ------------------------------------------- | ------------------------------------ |
| `a + b`, `a - b`, `a * b`, `a / b`          | Component-wise arithmetic            |
| `a * 2f`, `a / 2f`, `-a`                    | Scalar arithmetic                    |
| `a dot b`                                   | Dot product                          |
| `a cross b` (Vec3)                          | Cross product                        |
| `a.length`, `a.distance(b)`                 | Length / distance                    |
| `a.normalized()`                            | Unit vector                          |
| `a.clamped(min, max)`, `a.mixed(b, t)`      | Clamp / linear interpolation         |
| `a.minimized(b)`, `a.maximized(b)`          | Component-wise min / max             |
| `a.reflected(n)`, `a.refracted(n, eta)`     | Reflection / refraction              |
| `a.steped(edge)`, `a.smoothsteped(e0, e1)`  | Step / smoothstep                    |
| `a.sin()`, `a.cos()`, `a.sqrt()`, ...       | Component-wise functions             |
| `a.any`, `a.all`, `a.equal(b)`              | Relational predicates                |

### Matrix members

| Expression                       | Description                      |
| -------------------------------- | -------------------------------- |
| `m * n`, `m * v`, `m * 2f`, `m + n` | Matrix algebra                  |
| `m.transposed()`, `m.inversed()` | Transpose / inverse               |
| `m.determinant`, `m.trace`       | Determinant / trace               |
| `m.compMul(n)`                   | Component-wise multiplication     |
| `m.translated(v)`, `m.rotated(a, axis)`, `m.scaled(v)` | Transformations |
| `perspective(...)`, `ortho(...)`, `frustum(...)`, `lookAt(...)` | Projection / view matrices |

### Quaternion members

| Expression                       | Description                      |
| -------------------------------- | -------------------------------- |
| `q * r`, `q * v`, `q * 2f`       | Quaternion algebra               |
| `q.conjugated()`, `q.inversed()` | Conjugate / inverse              |
| `q.normalized()`, `q.length`     | Normalize / length               |
| `q.slerped(r, t)`, `q.lerped(r, t)` | Spherical / linear interpolation |
| `q.angle`, `q.axis`, `q.eulerAngles`, `q.pitch`, `q.yaw`, `q.roll` | Decompose |
| `q.toMat4()`, `q.toMat3()`       | Convert to rotation matrix       |

### Scalar functions

`sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `atan2`, `sinh`, `cosh`,
`tanh`, `asinh`, `acosh`, `atanh`, `radians`, `degrees`, `abs`, `sign`,
`floor`, `ceil`, `fract`, `trunc`, `round`, `roundEven`, `sqrt`,
`inversesqrt`, `exp`, `log`, `exp2`, `log2`, `pow`, `mod`, `min`, `max`,
`clamp`, `mix`, `step`, `smoothstep`, `fma`, `isnan`, `isinf`

## Example

The [`example/`](example/) module exercises the published artifact on every
desktop platform with a shared demo (`GlmExamples.runAll()`) plus tests:

- **JVM** — `./gradlew :example:runJvm`
- **Kotlin/Native** — `./gradlew :example:runDebugExecutableMacosArm64` (and
  the equivalent linux/mingw/windows executables)
- **JavaScript** — `./gradlew :example:jsNodeDevelopmentRun`
- **Wasm** — `./gradlew :example:wasmJsNodeDevelopmentRun`

## Building from Source

### Prerequisites

- JDK 17+
- CMake 3.16+
- Android SDK + NDK (for Android targets)
- Xcode command-line tools (for iOS/macOS/tvOS/watchOS targets)
- Emscripten (`emcc` on `PATH`, for JS/Wasm targets)
- `aarch64-linux-gnu-gcc` / `x86_64-w64-mingw32-gcc` (Linux hosts, for the
  linuxArm64/mingwX64 cross-compiled targets)

### Clone with submodules

```bash
git clone --recursive https://github.com/Enaium/glm-kmp.git
cd glm-kmp
```

### Publish to Maven Local

```bash
./gradlew :glm-kmp:publishToMavenLocal
```

### Run tests

```bash
./gradlew :glm-kmp:jvmTest        # JVM (JNI)
./gradlew :glm-kmp:macosArm64Test # macOS native
./gradlew :glm-kmp:jsTest         # JavaScript (Node)
./gradlew :glm-kmp:wasmJsNodeTest # WebAssembly (Node)
./gradlew :glm-kmp:testAndroidHostTest # Android host tests
```

## Project Structure

```
glm-kmp/
├── glm/                     # Git submodule (GLM C++ headers)
├── jni/
│   ├── CMakeLists.txt       # JNI shared library + static library build
│   ├── jni_bridge.cpp       # JNI bridge (C++ → JVM/Android)
│   ├── c_api/               # C ABI dispatcher (glm_wrapper.h/.cpp, generated)
│   └── jvm/                 # Per-OS/arch JNI publication subprojects
│       ├── darwin-aarch64, darwin-x86_64
│       ├── linux-x86_64, linux-aarch64
│       └── windows-x86_64
├── glm-kmp/                 # Kotlin Multiplatform module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/      # API: Vec2/3/4, Mat2/3/4, Quat, transforms
│       ├── commonTest/      # Shared test suite (runs on every platform)
│       ├── jvmMain/         # JVM actual (JNI) + NativeLoader
│       ├── androidMain/     # Android actual (JNI)
│       ├── nativeMain/      # Native actual (cinterop)
│       ├── jsMain/          # JavaScript actual (Emscripten wasm)
│       ├── wasmJsMain/      # WebAssembly actual
│       └── nativeInterop/cinterop/
├── example/                 # Multiplatform usage examples + tests
├── tools/
│   └── gen_ops.py           # Single source of truth for the 337-op table
└── .github/workflows/       # publish + test
```

## License

[MIT](LICENSE) — see the [LICENSE](LICENSE) file.
