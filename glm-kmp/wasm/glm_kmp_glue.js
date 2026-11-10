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
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
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
 * ESM wrapper for the @enaium/glm-kmp npm package.
 *
 * The Emscripten build uses -sSINGLE_FILE=1 (the wasm is embedded in the
 * glue) and -sWASM_ASYNC_COMPILATION=0, so the whole module fits in one
 * base64 string and is usable synchronously. `atob` exists in every
 * supported runtime (browsers, Node >= 16), so no asset fetching or file
 * resolution is involved.
 *
 *   import { glmCall } from '@enaium/glm-kmp';
 *   const cross = glmCall(op, [1, 0, 0, 0, 1, 0], 3);
 *
 * Op codes follow the enum in `Ops.kt` / the dispatch table in
 * `jni/c_api/glm_wrapper.cpp` (tools/gen_ops.py is the single source of
 * truth). The underlying C ABI is documented in `jni/c_api/glm_wrapper.h`;
 * `initGlmKmp()` returns the raw Emscripten module for callers that need
 * _malloc/_free/HEAPF32 directly.
 *
 * This file is a template: `:glm-kmp:assembleWasmNpm` substitutes
 * __GLM_GLUE_B64__ and writes build/npm/glm_kmp.js.
 */

const GLUE_B64 = '__GLM_GLUE_B64__';

let cachedModule;

/**
 * Instantiates (once) and returns the Emscripten GLM module.
 *
 * The module exposes the C ABI: `_malloc`, `_free`, `_glm_call`,
 * `_glm_op_count`, `_glm_version` and the `HEAPF32` view.
 */
export function initGlmKmp() {
    if (!cachedModule) {
        cachedModule = new Function(atob(GLUE_B64) + '; return Module')();
    }
    return cachedModule;
}

/** Number of operations in the GLM dispatch table. */
export function glmOpCount() {
    return initGlmKmp()._glm_op_count();
}

/** Packed GLM version of the submodule: (major << 16) | (minor << 8) | patch. */
export function glmVersion() {
    return initGlmKmp()._glm_version();
}

/**
 * Dispatches one GLM operation and returns its result as a Float32Array.
 *
 * @param {number} op op code (0 .. glmOpCount() - 1)
 * @param {ArrayLike<number>} args packed input floats
 * @param {number} outCount expected number of output floats
 * @returns {Float32Array} the outCount result floats
 */
export function glmCall(op, args, outCount) {
    const mod = initGlmKmp();
    const argBytes = args.length * 4;
    const argPtr = argBytes ? mod._malloc(argBytes) : 0;
    const outPtr = mod._malloc(outCount * 4);
    try {
        if (argBytes) {
            const heap = mod.HEAPF32;
            const base = argPtr >> 2;
            for (let i = 0; i < args.length; i++) {
                heap[base + i] = args[i];
            }
        }
        mod._glm_call(op, argPtr, args.length, outPtr, outCount);
        // Re-read HEAPF32: the heap is replaced when the module grows its
        // memory (ALLOW_MEMORY_GROWTH builds).
        const heap = mod.HEAPF32;
        const base = outPtr >> 2;
        return heap.slice(base, base + outCount);
    } finally {
        if (argPtr) mod._free(argPtr);
        mod._free(outPtr);
    }
}
