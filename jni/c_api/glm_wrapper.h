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
 * C ABI over GLM (header-only C++ math library from the glm submodule).
 *
 * Every GLM operation is dispatched through glm_call with a flat float ABI:
 *
 *   glm_call(op, args, arg_count, out, out_count)
 *
 * where `args` packs all inputs contiguously (vectors/matrices/quaternions
 * in column-major or x,y,z,w order, scalars as single floats) and `out`
 * receives the result.  Scalar results are a single float; boolean results
 * are 1.0f/0.0f.  Op codes are defined by tools/gen_ops.py; the Kotlin side
 * mirrors them in Ops.kt with the exact same order.
 */

#ifndef GLM_WRAPPER_H
#define GLM_WRAPPER_H

#ifdef __cplusplus
extern "C" {
#endif

/* Number of ops in the dispatch table (matches the Kotlin Op enum size). */
int glm_op_count(void);

/* Packed GLM version of the submodule: (major << 16) | (minor << 8) | patch. */
int glm_version(void);

/*
 * Dispatches one GLM operation.
 *
 * op        op code (0 .. glm_op_count()-1)
 * args      packed input floats (may be NULL when arg_count == 0)
 * arg_count number of input floats
 * out       output buffer, must hold at least out_count floats
 * out_count expected number of output floats
 */
void glm_call(int op, const float* args, int arg_count, float* out, int out_count);

#ifdef __cplusplus
}
#endif

#endif /* GLM_WRAPPER_H */
