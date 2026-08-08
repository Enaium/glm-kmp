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

package cn.enaium.glm

// =========================================================================
// JNI bridge – loads the native library and provides external declarations
// =========================================================================

internal object Jni {
    init {
        NativeLoader.load()
    }

    external fun nativeCall(op: Int, args: FloatArray, argCount: Int, out: FloatArray, outCount: Int)
    external fun nativeOpCount(): Int
    external fun nativeVersion(): Int
}

// =========================================================================
// JVM actual implementation
// =========================================================================

internal actual object GlmNative {
    actual fun call(op: Int, args: FloatArray, out: FloatArray) {
        Jni.nativeCall(op, args, args.size, out, out.size)
    }

    actual fun opCount(): Int = Jni.nativeOpCount()

    actual fun version(): Int = Jni.nativeVersion()
}
