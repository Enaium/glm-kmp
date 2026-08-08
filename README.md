# glm-kmp

Kotlin Multiplatform bindings for [GLM](https://github.com/g-truc/glm) (OpenGL
Mathematics), the header-only C++ math library, from the `glm` git submodule.

GLM is pure computation, so the bindings cover every platform:

| Platform | Targets | Mechanism |
| --- | --- | --- |
| JVM desktop | Windows x64, Linux x64/arm64, macOS x64/arm64 | JNI shared library bundled per-OS/arch (`glm-kmp-jni-jvm-{os}-{arch}` artifacts) |
| Android | armeabi-v7a, arm64-v8a, x86, x86_64 | JNI `.so` files inside the AAR's `jniLibs` |
| Kotlin/Native | macosArm64, macosX64, linuxX64, linuxArm64, mingwX64, iosArm64, iosX64, iosSimulatorArm64, tvosArm64, tvosSimulatorArm64, watchosArm64, watchosSimulatorArm64, watchosDeviceArm64 | cinterop with the per-target static library embedded in the klib |
| JavaScript | Node/browser | Emscripten-compiled wasm embedded in the klib |
| WebAssembly | wasmJs (Node/browser) | Same Emscripten wasm |

All platforms share one flat C ABI (`jni/c_api/glm_wrapper.h`): every GLM
operation is dispatched through `glm_call(op, args, ...)` with packed float
arrays. The op table lives in `tools/gen_ops.py` (337 ops) and generates both
the C++ dispatcher and the Kotlin `Ops` enum, so the two can never drift apart.

## Usage

```kotlin
import cn.enaium.glm.*
import kotlin.math.PI

val model = mat4()
    .translated(1.0f, 2.0f, 3.0f)
    .rotated(PI.toFloat() / 4.0f, 0.0f, 1.0f, 0.0f)
    .scaled(2.0f, 2.0f, 2.0f)

val view = lookAt(
    eye = vec3(0.0f, 0.0f, 5.0f),
    center = vec3(0.0f, 0.0f, 0.0f),
    up = vec3(0.0f, 1.0f, 0.0f),
)

val clip = view * model * vec4(0.0f, 0.0f, 0.0f, 1.0f)

val q = quat(axis = vec3(0.0f, 0.0f, 1.0f), angle = PI.toFloat() / 2.0f)
val rotated = q * vec3(1.0f, 0.0f, 0.0f)
val slerped = q.slerped(other, 0.5f)
```

The Kotlin API uses native Kotlin idioms on top of the GLM functions:

- `operator` overloads: `+`, `-`, `*`, `/`, `unaryMinus`, `get`
- value semantics: `vec3(1, 2, 3) == vec3(1, 2, 3)`
- member extensions: `a.normalized()`, `a.clamped(0f, 1f)`, `a dot b`,
  `a cross b`, `a.slerped(b, t)`, `a.translated(1f, 2f, 3f)`, ...
- top-level scalar functions: `sin`, `cos`, `mix`, `clamp`, `smoothstep`, ...

### Gradle

```kotlin
repositories { mavenCentral() }

dependencies {
    implementation("cn.enaium.glm:glm-kmp:1.0.0")
}
```

## Modules

- `glm-kmp` – the library itself
- `example` – per-platform usage examples and tests (consumes the published
  artifact; run `./gradlew :glm-kmp:publishToMavenLocal` first)
- `jni/jvm/{linux,darwin,windows}-{x86_64,aarch64}` – JNI classifier jars
- `glm` – the GLM submodule (updated with `git submodule update --remote`)

## Building

```bash
# Native (Apple) targets need macOS + Xcode; linux/mingw targets need a Linux
# host with the aarch64-linux-gnu / MinGW cross toolchains; JS/Wasm need
# Emscripten (emcc) on PATH (CI uses the setup-emsdk action).

# Tests
./gradlew :glm-kmp:jvmTest                # JVM
./gradlew :glm-kmp:macosArm64Test         # Kotlin/Native
./gradlew :glm-kmp:jsTest                 # JavaScript (Node)
./gradlew :glm-kmp:wasmJsNodeTest         # WebAssembly (Node)
./gradlew :glm-kmp:testAndroidHostTest    # Android host tests

# Local Maven repository (the example consumes this)
./gradlew :glm-kmp:publishToMavenLocal \
    -Psigning.keyId=<KEY_ID> -Psigning.secretKeyRingFile=<KEY_RING> -Psigning.password=<PW>

# Run the example
./gradlew :example:runJvm
```

## Adding GLM functions

1. Add a row to the op table in `tools/gen_ops.py`
2. `python3 tools/gen_ops.py` – regenerates `jni/c_api/glm_wrapper.cpp` and
   `Ops.kt`
3. Expose it in the Kotlin API (`Vec2.kt`, `Mat4.kt`, ...) and add a test

## License

MIT, see [LICENSE](LICENSE).
