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

/**
 * Packs the given inputs into one flat float array, dispatches [op] and
 * returns the freshly allocated result.
 */
private val opTableChecked: Boolean by lazy {
    val native = GlmNative.opCount()
    check(Op.entries.size == native) {
        "op table mismatch: kotlin=${Op.entries.size}, native=$native"
    }
    true
}

internal fun call(op: Op, vararg inputs: FloatArray): FloatArray {
    opTableChecked
    val args = FloatArray(op.inSize)
    var offset = 0
    for (input in inputs) {
        input.copyInto(args, offset)
        offset += input.size
    }
    val out = FloatArray(op.outSize)
    GlmNative.call(op.ordinal, args, out)
    return out
}

/** Dispatches an op with a single-float result. */
internal fun callF(op: Op, vararg inputs: FloatArray): Float = call(op, *inputs)[0]

/** Dispatches an op with a boolean result (encoded as 1.0f/0.0f). */
internal fun callB(op: Op, vararg inputs: FloatArray): Boolean = call(op, *inputs)[0] != 0f

// =========================================================================
// Scalar functions (GLM "genType" overloads)
// =========================================================================

fun sin(x: Float): Float = callF(Op.SIN, floatArrayOf(x))
fun cos(x: Float): Float = callF(Op.COS, floatArrayOf(x))
fun tan(x: Float): Float = callF(Op.TAN, floatArrayOf(x))
fun asin(x: Float): Float = callF(Op.ASIN, floatArrayOf(x))
fun acos(x: Float): Float = callF(Op.ACOS, floatArrayOf(x))
fun atan(x: Float): Float = callF(Op.ATAN, floatArrayOf(x))
fun atan2(y: Float, x: Float): Float = callF(Op.ATAN2, floatArrayOf(y, x))
fun sinh(x: Float): Float = callF(Op.SINH, floatArrayOf(x))
fun cosh(x: Float): Float = callF(Op.COSH, floatArrayOf(x))
fun tanh(x: Float): Float = callF(Op.TANH, floatArrayOf(x))
fun asinh(x: Float): Float = callF(Op.ASINH, floatArrayOf(x))
fun acosh(x: Float): Float = callF(Op.ACOSH, floatArrayOf(x))
fun atanh(x: Float): Float = callF(Op.ATANH, floatArrayOf(x))

fun radians(degrees: Float): Float = callF(Op.RADIANS, floatArrayOf(degrees))
fun degrees(radians: Float): Float = callF(Op.DEGREES, floatArrayOf(radians))

fun abs(x: Float): Float = callF(Op.ABS, floatArrayOf(x))
fun sign(x: Float): Float = callF(Op.SIGN, floatArrayOf(x))
fun floor(x: Float): Float = callF(Op.FLOOR, floatArrayOf(x))
fun ceil(x: Float): Float = callF(Op.CEIL, floatArrayOf(x))
fun fract(x: Float): Float = callF(Op.FRACT, floatArrayOf(x))
fun trunc(x: Float): Float = callF(Op.TRUNC, floatArrayOf(x))
fun round(x: Float): Float = callF(Op.ROUND, floatArrayOf(x))
fun roundEven(x: Float): Float = callF(Op.ROUNDEVEN, floatArrayOf(x))

fun sqrt(x: Float): Float = callF(Op.SQRT, floatArrayOf(x))
fun inversesqrt(x: Float): Float = callF(Op.INVERSESQRT, floatArrayOf(x))

fun exp(x: Float): Float = callF(Op.EXP, floatArrayOf(x))
fun log(x: Float): Float = callF(Op.LOG, floatArrayOf(x))
fun exp2(x: Float): Float = callF(Op.EXP2, floatArrayOf(x))
fun log2(x: Float): Float = callF(Op.LOG2, floatArrayOf(x))
fun pow(x: Float, y: Float): Float = callF(Op.POW, floatArrayOf(x, y))

fun mod(x: Float, y: Float): Float = callF(Op.MOD, floatArrayOf(x, y))
fun min(x: Float, y: Float): Float = callF(Op.MIN, floatArrayOf(x, y))
fun max(x: Float, y: Float): Float = callF(Op.MAX, floatArrayOf(x, y))
fun clamp(x: Float, minVal: Float, maxVal: Float): Float =
    callF(Op.CLAMP, floatArrayOf(x, minVal, maxVal))
fun mix(x: Float, y: Float, a: Float): Float = callF(Op.MIX, floatArrayOf(x, y, a))
fun step(edge: Float, x: Float): Float = callF(Op.STEP, floatArrayOf(edge, x))
fun smoothstep(edge0: Float, edge1: Float, x: Float): Float =
    callF(Op.SMOOTHSTEP, floatArrayOf(edge0, edge1, x))
fun fma(a: Float, b: Float, c: Float): Float = callF(Op.FMA, floatArrayOf(a, b, c))

fun isnan(x: Float): Boolean = callB(Op.IS_NAN, floatArrayOf(x))
fun isinf(x: Float): Boolean = callB(Op.IS_INF, floatArrayOf(x))

/** The GLM version of the underlying submodule, e.g. "1.1.0". */
val GLM_VERSION: String
    get() {
        val v = GlmNative.version()
        return "${v shr 16}.${(v shr 8) and 0xFF}.${v and 0xFF}"
    }
