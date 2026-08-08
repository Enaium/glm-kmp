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
// JavaScript actual implementation.
//
// The Emscripten-compiled GLM wrapper (generated GlmGlue.kt) is loaded
// synchronously at first use; float arrays are copied through the wasm heap
// with _malloc/_free.
// =========================================================================

internal actual object GlmNative {
    private val module: dynamic = loadGlmModule()

    actual fun call(op: Int, args: FloatArray, out: FloatArray) {
        val inPtr = module._malloc(args.size * 4)
        val outPtr = module._malloc(out.size * 4)
        try {
            module.HEAPF32.set(args, inPtr / 4)
            module._glm_call(op, inPtr, args.size, outPtr, out.size)
            out.unsafeCast<org.khronos.webgl.Float32Array>()
                .set(
                    module.HEAPF32.subarray(outPtr / 4, outPtr / 4 + out.size)
                        .unsafeCast<org.khronos.webgl.Float32Array>(),
                )
        } finally {
            module._free(inPtr)
            module._free(outPtr)
        }
    }

    actual fun opCount(): Int = module._glm_op_count()

    actual fun version(): Int = module._glm_version()
}
