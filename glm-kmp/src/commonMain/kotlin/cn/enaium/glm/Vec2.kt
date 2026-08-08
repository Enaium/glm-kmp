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

/** A 2-component float vector backed by GLM. */
class Vec2 internal constructor(internal val values: FloatArray) {

    var x: Float
        get() = values[0]
        set(value) { values[0] = value }
    var y: Float
        get() = values[1]
        set(value) { values[1] = value }

    operator fun get(index: Int): Float = values[index]

    operator fun plus(other: Vec2): Vec2 = Vec2(call(Op.VEC2_ADD, values, other.values))
    operator fun minus(other: Vec2): Vec2 = Vec2(call(Op.VEC2_SUB, values, other.values))
    operator fun times(other: Vec2): Vec2 = Vec2(call(Op.VEC2_MUL, values, other.values))
    operator fun div(other: Vec2): Vec2 = Vec2(call(Op.VEC2_DIV, values, other.values))
    operator fun times(scalar: Float): Vec2 = Vec2(call(Op.VEC2_SCALE, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Vec2 = Vec2(call(Op.VEC2_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Vec2 = Vec2(call(Op.VEC2_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Vec2 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "vec2($x, $y)"
}

/** Constructs a vec2 from two components. */
fun vec2(x: Float, y: Float): Vec2 = Vec2(call(Op.VEC2_FROM, floatArrayOf(x, y)))

/** Constructs a vec2 with all components set to [scalar]. */
fun vec2(scalar: Float): Vec2 = vec2(scalar, scalar)

// =========================================================================
// Member extensions over the GLM vector function set
// =========================================================================

infix fun Vec2.dot(other: Vec2): Float = callF(Op.VEC2_DOT, values, other.values)
fun Vec2.distance(other: Vec2): Float = callF(Op.VEC2_DISTANCE, values, other.values)

val Vec2.length: Float get() = callF(Op.VEC2_LENGTH, values)

fun Vec2.normalized(): Vec2 = Vec2(call(Op.VEC2_NORMALIZE, values))
fun Vec2.clamped(minVal: Float, maxVal: Float): Vec2 =
    Vec2(call(Op.VEC2_CLAMP, values, floatArrayOf(minVal, maxVal)))
fun Vec2.clamped(minVal: Vec2, maxVal: Vec2): Vec2 =
    Vec2(call(Op.VEC2_CLAMP_V, values, minVal.values, maxVal.values))
fun Vec2.mixed(other: Vec2, a: Float): Vec2 =
    Vec2(call(Op.VEC2_MIX, values, other.values, floatArrayOf(a)))
fun Vec2.mixed(other: Vec2, a: Vec2): Vec2 =
    Vec2(call(Op.VEC2_MIX_V, values, other.values, a.values))
fun Vec2.minimized(other: Vec2): Vec2 = Vec2(call(Op.VEC2_MIN, values, other.values))
fun Vec2.maximized(other: Vec2): Vec2 = Vec2(call(Op.VEC2_MAX, values, other.values))
fun Vec2.minimized(scalar: Float): Vec2 = Vec2(call(Op.VEC2_MIN_S, values, floatArrayOf(scalar)))
fun Vec2.maximized(scalar: Float): Vec2 = Vec2(call(Op.VEC2_MAX_S, values, floatArrayOf(scalar)))
fun Vec2.reflected(normal: Vec2): Vec2 = Vec2(call(Op.VEC2_REFLECT, values, normal.values))
fun Vec2.refracted(normal: Vec2, eta: Float): Vec2 =
    Vec2(call(Op.VEC2_REFRACT, values, normal.values, floatArrayOf(eta)))
fun Vec2.faceforward(i: Vec2, nRef: Vec2): Vec2 =
    Vec2(call(Op.VEC2_FACEFORWARD, values, i.values, nRef.values))
fun Vec2.steped(edge: Vec2): Vec2 = Vec2(call(Op.VEC2_STEP, edge.values, values))
fun Vec2.steped(edge: Float): Vec2 = Vec2(call(Op.VEC2_STEP_S, floatArrayOf(edge), values))
fun Vec2.smoothsteped(edge0: Float, edge1: Float): Vec2 =
    Vec2(call(Op.VEC2_SMOOTHSTEP, floatArrayOf(edge0, edge1), values))
fun Vec2.smoothsteped(edge0: Vec2, edge1: Vec2): Vec2 =
    Vec2(call(Op.VEC2_SMOOTHSTEP_V, edge0.values, edge1.values, values))

val Vec2.any: Boolean get() = callB(Op.VEC2_ANY, values)
val Vec2.all: Boolean get() = callB(Op.VEC2_ALL, values)
fun Vec2.equal(other: Vec2): Boolean = callB(Op.VEC2_EQUAL, values, other.values)
fun Vec2.notEqual(other: Vec2): Boolean = callB(Op.VEC2_NOT_EQUAL, values, other.values)

fun Vec2.abs(): Vec2 = Vec2(call(Op.VEC2_ABS, values))
fun Vec2.signed(): Vec2 = Vec2(call(Op.VEC2_SIGN, values))
fun Vec2.floored(): Vec2 = Vec2(call(Op.VEC2_FLOOR, values))
fun Vec2.ceiled(): Vec2 = Vec2(call(Op.VEC2_CEIL, values))
fun Vec2.fracted(): Vec2 = Vec2(call(Op.VEC2_FRACT, values))
fun Vec2.truncated(): Vec2 = Vec2(call(Op.VEC2_TRUNC, values))
fun Vec2.rounded(): Vec2 = Vec2(call(Op.VEC2_ROUND, values))
fun Vec2.roundedEven(): Vec2 = Vec2(call(Op.VEC2_ROUNDEVEN, values))
fun Vec2.sqrt(): Vec2 = Vec2(call(Op.VEC2_SQRT, values))
fun Vec2.inversesqrt(): Vec2 = Vec2(call(Op.VEC2_INVERSESQRT, values))
fun Vec2.sin(): Vec2 = Vec2(call(Op.VEC2_SIN, values))
fun Vec2.cos(): Vec2 = Vec2(call(Op.VEC2_COS, values))
fun Vec2.tan(): Vec2 = Vec2(call(Op.VEC2_TAN, values))
fun Vec2.asin(): Vec2 = Vec2(call(Op.VEC2_ASIN, values))
fun Vec2.acos(): Vec2 = Vec2(call(Op.VEC2_ACOS, values))
fun Vec2.atan(): Vec2 = Vec2(call(Op.VEC2_ATAN, values))
fun Vec2.atan2(other: Vec2): Vec2 = Vec2(call(Op.VEC2_ATAN2, values, other.values))
fun Vec2.sinh(): Vec2 = Vec2(call(Op.VEC2_SINH, values))
fun Vec2.cosh(): Vec2 = Vec2(call(Op.VEC2_COSH, values))
fun Vec2.tanh(): Vec2 = Vec2(call(Op.VEC2_TANH, values))
fun Vec2.exp(): Vec2 = Vec2(call(Op.VEC2_EXP, values))
fun Vec2.log(): Vec2 = Vec2(call(Op.VEC2_LOG, values))
fun Vec2.exp2(): Vec2 = Vec2(call(Op.VEC2_EXP2, values))
fun Vec2.log2(): Vec2 = Vec2(call(Op.VEC2_LOG2, values))
fun Vec2.pow(other: Vec2): Vec2 = Vec2(call(Op.VEC2_POW, values, other.values))
fun Vec2.mod(other: Vec2): Vec2 = Vec2(call(Op.VEC2_MOD, values, other.values))
fun Vec2.mod(scalar: Float): Vec2 = Vec2(call(Op.VEC2_MOD_S, values, floatArrayOf(scalar)))
fun Vec2.radians(): Vec2 = Vec2(call(Op.VEC2_RADIANS, values))
fun Vec2.degrees(): Vec2 = Vec2(call(Op.VEC2_DEGREES, values))
fun Vec2.isnan(): Vec2 = Vec2(call(Op.VEC2_IS_NAN, values))
fun Vec2.isinf(): Vec2 = Vec2(call(Op.VEC2_IS_INF, values))
fun Vec2.fma(b: Vec2, c: Vec2): Vec2 = Vec2(call(Op.VEC2_FMA, values, b.values, c.values))
