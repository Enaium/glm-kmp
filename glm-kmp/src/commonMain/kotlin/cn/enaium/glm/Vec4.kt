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

/** A 4-component float vector backed by GLM. */
class Vec4 internal constructor(internal val values: FloatArray) {

    var x: Float
        get() = values[0]
        set(value) { values[0] = value }
    var y: Float
        get() = values[1]
        set(value) { values[1] = value }
    var z: Float
        get() = values[2]
        set(value) { values[2] = value }
    var w: Float
        get() = values[3]
        set(value) { values[3] = value }

    operator fun get(index: Int): Float = values[index]

    operator fun plus(other: Vec4): Vec4 = Vec4(call(Op.VEC4_ADD, values, other.values))
    operator fun minus(other: Vec4): Vec4 = Vec4(call(Op.VEC4_SUB, values, other.values))
    operator fun times(other: Vec4): Vec4 = Vec4(call(Op.VEC4_MUL, values, other.values))
    operator fun div(other: Vec4): Vec4 = Vec4(call(Op.VEC4_DIV, values, other.values))
    operator fun times(scalar: Float): Vec4 = Vec4(call(Op.VEC4_SCALE, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Vec4 = Vec4(call(Op.VEC4_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Vec4 = Vec4(call(Op.VEC4_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Vec4 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "vec4($x, $y, $z, $w)"
}

/** Constructs a vec4 from four components. */
fun vec4(x: Float, y: Float, z: Float, w: Float): Vec4 =
    Vec4(call(Op.VEC4_FROM, floatArrayOf(x, y, z, w)))

/** Constructs a vec4 with all components set to [scalar]. */
fun vec4(scalar: Float): Vec4 = vec4(scalar, scalar, scalar, scalar)

// =========================================================================
// Member extensions over the GLM vector function set
// =========================================================================

infix fun Vec4.dot(other: Vec4): Float = callF(Op.VEC4_DOT, values, other.values)
fun Vec4.distance(other: Vec4): Float = callF(Op.VEC4_DISTANCE, values, other.values)

val Vec4.length: Float get() = callF(Op.VEC4_LENGTH, values)

fun Vec4.normalized(): Vec4 = Vec4(call(Op.VEC4_NORMALIZE, values))
fun Vec4.clamped(minVal: Float, maxVal: Float): Vec4 =
    Vec4(call(Op.VEC4_CLAMP, values, floatArrayOf(minVal, maxVal)))
fun Vec4.clamped(minVal: Vec4, maxVal: Vec4): Vec4 =
    Vec4(call(Op.VEC4_CLAMP_V, values, minVal.values, maxVal.values))
fun Vec4.mixed(other: Vec4, a: Float): Vec4 =
    Vec4(call(Op.VEC4_MIX, values, other.values, floatArrayOf(a)))
fun Vec4.mixed(other: Vec4, a: Vec4): Vec4 =
    Vec4(call(Op.VEC4_MIX_V, values, other.values, a.values))
fun Vec4.minimized(other: Vec4): Vec4 = Vec4(call(Op.VEC4_MIN, values, other.values))
fun Vec4.maximized(other: Vec4): Vec4 = Vec4(call(Op.VEC4_MAX, values, other.values))
fun Vec4.minimized(scalar: Float): Vec4 = Vec4(call(Op.VEC4_MIN_S, values, floatArrayOf(scalar)))
fun Vec4.maximized(scalar: Float): Vec4 = Vec4(call(Op.VEC4_MAX_S, values, floatArrayOf(scalar)))
fun Vec4.reflected(normal: Vec4): Vec4 = Vec4(call(Op.VEC4_REFLECT, values, normal.values))
fun Vec4.refracted(normal: Vec4, eta: Float): Vec4 =
    Vec4(call(Op.VEC4_REFRACT, values, normal.values, floatArrayOf(eta)))
fun Vec4.faceforward(i: Vec4, nRef: Vec4): Vec4 =
    Vec4(call(Op.VEC4_FACEFORWARD, values, i.values, nRef.values))
fun Vec4.steped(edge: Vec4): Vec4 = Vec4(call(Op.VEC4_STEP, edge.values, values))
fun Vec4.steped(edge: Float): Vec4 = Vec4(call(Op.VEC4_STEP_S, floatArrayOf(edge), values))
fun Vec4.smoothsteped(edge0: Float, edge1: Float): Vec4 =
    Vec4(call(Op.VEC4_SMOOTHSTEP, floatArrayOf(edge0, edge1), values))
fun Vec4.smoothsteped(edge0: Vec4, edge1: Vec4): Vec4 =
    Vec4(call(Op.VEC4_SMOOTHSTEP_V, edge0.values, edge1.values, values))

val Vec4.any: Boolean get() = callB(Op.VEC4_ANY, values)
val Vec4.all: Boolean get() = callB(Op.VEC4_ALL, values)
fun Vec4.equal(other: Vec4): Boolean = callB(Op.VEC4_EQUAL, values, other.values)
fun Vec4.notEqual(other: Vec4): Boolean = callB(Op.VEC4_NOT_EQUAL, values, other.values)

fun Vec4.abs(): Vec4 = Vec4(call(Op.VEC4_ABS, values))
fun Vec4.signed(): Vec4 = Vec4(call(Op.VEC4_SIGN, values))
fun Vec4.floored(): Vec4 = Vec4(call(Op.VEC4_FLOOR, values))
fun Vec4.ceiled(): Vec4 = Vec4(call(Op.VEC4_CEIL, values))
fun Vec4.fracted(): Vec4 = Vec4(call(Op.VEC4_FRACT, values))
fun Vec4.truncated(): Vec4 = Vec4(call(Op.VEC4_TRUNC, values))
fun Vec4.rounded(): Vec4 = Vec4(call(Op.VEC4_ROUND, values))
fun Vec4.roundedEven(): Vec4 = Vec4(call(Op.VEC4_ROUNDEVEN, values))
fun Vec4.sqrt(): Vec4 = Vec4(call(Op.VEC4_SQRT, values))
fun Vec4.inversesqrt(): Vec4 = Vec4(call(Op.VEC4_INVERSESQRT, values))
fun Vec4.sin(): Vec4 = Vec4(call(Op.VEC4_SIN, values))
fun Vec4.cos(): Vec4 = Vec4(call(Op.VEC4_COS, values))
fun Vec4.tan(): Vec4 = Vec4(call(Op.VEC4_TAN, values))
fun Vec4.asin(): Vec4 = Vec4(call(Op.VEC4_ASIN, values))
fun Vec4.acos(): Vec4 = Vec4(call(Op.VEC4_ACOS, values))
fun Vec4.atan(): Vec4 = Vec4(call(Op.VEC4_ATAN, values))
fun Vec4.atan2(other: Vec4): Vec4 = Vec4(call(Op.VEC4_ATAN2, values, other.values))
fun Vec4.sinh(): Vec4 = Vec4(call(Op.VEC4_SINH, values))
fun Vec4.cosh(): Vec4 = Vec4(call(Op.VEC4_COSH, values))
fun Vec4.tanh(): Vec4 = Vec4(call(Op.VEC4_TANH, values))
fun Vec4.exp(): Vec4 = Vec4(call(Op.VEC4_EXP, values))
fun Vec4.log(): Vec4 = Vec4(call(Op.VEC4_LOG, values))
fun Vec4.exp2(): Vec4 = Vec4(call(Op.VEC4_EXP2, values))
fun Vec4.log2(): Vec4 = Vec4(call(Op.VEC4_LOG2, values))
fun Vec4.pow(other: Vec4): Vec4 = Vec4(call(Op.VEC4_POW, values, other.values))
fun Vec4.mod(other: Vec4): Vec4 = Vec4(call(Op.VEC4_MOD, values, other.values))
fun Vec4.mod(scalar: Float): Vec4 = Vec4(call(Op.VEC4_MOD_S, values, floatArrayOf(scalar)))
fun Vec4.radians(): Vec4 = Vec4(call(Op.VEC4_RADIANS, values))
fun Vec4.degrees(): Vec4 = Vec4(call(Op.VEC4_DEGREES, values))
fun Vec4.isnan(): Vec4 = Vec4(call(Op.VEC4_IS_NAN, values))
fun Vec4.isinf(): Vec4 = Vec4(call(Op.VEC4_IS_INF, values))
fun Vec4.fma(b: Vec4, c: Vec4): Vec4 = Vec4(call(Op.VEC4_FMA, values, b.values, c.values))
