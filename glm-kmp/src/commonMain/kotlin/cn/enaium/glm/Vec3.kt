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

/** A 3-component float vector backed by GLM. */
class Vec3 internal constructor(internal val values: FloatArray) {

    var x: Float
        get() = values[0]
        set(value) { values[0] = value }
    var y: Float
        get() = values[1]
        set(value) { values[1] = value }
    var z: Float
        get() = values[2]
        set(value) { values[2] = value }

    operator fun get(index: Int): Float = values[index]

    operator fun plus(other: Vec3): Vec3 = Vec3(call(Op.VEC3_ADD, values, other.values))
    operator fun minus(other: Vec3): Vec3 = Vec3(call(Op.VEC3_SUB, values, other.values))
    operator fun times(other: Vec3): Vec3 = Vec3(call(Op.VEC3_MUL, values, other.values))
    operator fun div(other: Vec3): Vec3 = Vec3(call(Op.VEC3_DIV, values, other.values))
    operator fun times(scalar: Float): Vec3 = Vec3(call(Op.VEC3_SCALE, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Vec3 = Vec3(call(Op.VEC3_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Vec3 = Vec3(call(Op.VEC3_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Vec3 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "vec3($x, $y, $z)"
}

/** Constructs a vec3 from three components. */
fun vec3(x: Float, y: Float, z: Float): Vec3 = Vec3(call(Op.VEC3_FROM, floatArrayOf(x, y, z)))

/** Constructs a vec3 with all components set to [scalar]. */
fun vec3(scalar: Float): Vec3 = vec3(scalar, scalar, scalar)

// =========================================================================
// Member extensions over the GLM vector function set
// =========================================================================

infix fun Vec3.dot(other: Vec3): Float = callF(Op.VEC3_DOT, values, other.values)
infix fun Vec3.cross(other: Vec3): Vec3 = Vec3(call(Op.VEC3_CROSS, values, other.values))
fun Vec3.distance(other: Vec3): Float = callF(Op.VEC3_DISTANCE, values, other.values)

val Vec3.length: Float get() = callF(Op.VEC3_LENGTH, values)

fun Vec3.normalized(): Vec3 = Vec3(call(Op.VEC3_NORMALIZE, values))
fun Vec3.clamped(minVal: Float, maxVal: Float): Vec3 =
    Vec3(call(Op.VEC3_CLAMP, values, floatArrayOf(minVal, maxVal)))
fun Vec3.clamped(minVal: Vec3, maxVal: Vec3): Vec3 =
    Vec3(call(Op.VEC3_CLAMP_V, values, minVal.values, maxVal.values))
fun Vec3.mixed(other: Vec3, a: Float): Vec3 =
    Vec3(call(Op.VEC3_MIX, values, other.values, floatArrayOf(a)))
fun Vec3.mixed(other: Vec3, a: Vec3): Vec3 =
    Vec3(call(Op.VEC3_MIX_V, values, other.values, a.values))
fun Vec3.minimized(other: Vec3): Vec3 = Vec3(call(Op.VEC3_MIN, values, other.values))
fun Vec3.maximized(other: Vec3): Vec3 = Vec3(call(Op.VEC3_MAX, values, other.values))
fun Vec3.minimized(scalar: Float): Vec3 = Vec3(call(Op.VEC3_MIN_S, values, floatArrayOf(scalar)))
fun Vec3.maximized(scalar: Float): Vec3 = Vec3(call(Op.VEC3_MAX_S, values, floatArrayOf(scalar)))
fun Vec3.reflected(normal: Vec3): Vec3 = Vec3(call(Op.VEC3_REFLECT, values, normal.values))
fun Vec3.refracted(normal: Vec3, eta: Float): Vec3 =
    Vec3(call(Op.VEC3_REFRACT, values, normal.values, floatArrayOf(eta)))
fun Vec3.faceforward(i: Vec3, nRef: Vec3): Vec3 =
    Vec3(call(Op.VEC3_FACEFORWARD, values, i.values, nRef.values))
fun Vec3.steped(edge: Vec3): Vec3 = Vec3(call(Op.VEC3_STEP, edge.values, values))
fun Vec3.steped(edge: Float): Vec3 = Vec3(call(Op.VEC3_STEP_S, floatArrayOf(edge), values))
fun Vec3.smoothsteped(edge0: Float, edge1: Float): Vec3 =
    Vec3(call(Op.VEC3_SMOOTHSTEP, floatArrayOf(edge0, edge1), values))
fun Vec3.smoothsteped(edge0: Vec3, edge1: Vec3): Vec3 =
    Vec3(call(Op.VEC3_SMOOTHSTEP_V, edge0.values, edge1.values, values))

val Vec3.any: Boolean get() = callB(Op.VEC3_ANY, values)
val Vec3.all: Boolean get() = callB(Op.VEC3_ALL, values)
fun Vec3.equal(other: Vec3): Boolean = callB(Op.VEC3_EQUAL, values, other.values)
fun Vec3.notEqual(other: Vec3): Boolean = callB(Op.VEC3_NOT_EQUAL, values, other.values)

fun Vec3.abs(): Vec3 = Vec3(call(Op.VEC3_ABS, values))
fun Vec3.signed(): Vec3 = Vec3(call(Op.VEC3_SIGN, values))
fun Vec3.floored(): Vec3 = Vec3(call(Op.VEC3_FLOOR, values))
fun Vec3.ceiled(): Vec3 = Vec3(call(Op.VEC3_CEIL, values))
fun Vec3.fracted(): Vec3 = Vec3(call(Op.VEC3_FRACT, values))
fun Vec3.truncated(): Vec3 = Vec3(call(Op.VEC3_TRUNC, values))
fun Vec3.rounded(): Vec3 = Vec3(call(Op.VEC3_ROUND, values))
fun Vec3.roundedEven(): Vec3 = Vec3(call(Op.VEC3_ROUNDEVEN, values))
fun Vec3.sqrt(): Vec3 = Vec3(call(Op.VEC3_SQRT, values))
fun Vec3.inversesqrt(): Vec3 = Vec3(call(Op.VEC3_INVERSESQRT, values))
fun Vec3.sin(): Vec3 = Vec3(call(Op.VEC3_SIN, values))
fun Vec3.cos(): Vec3 = Vec3(call(Op.VEC3_COS, values))
fun Vec3.tan(): Vec3 = Vec3(call(Op.VEC3_TAN, values))
fun Vec3.asin(): Vec3 = Vec3(call(Op.VEC3_ASIN, values))
fun Vec3.acos(): Vec3 = Vec3(call(Op.VEC3_ACOS, values))
fun Vec3.atan(): Vec3 = Vec3(call(Op.VEC3_ATAN, values))
fun Vec3.atan2(other: Vec3): Vec3 = Vec3(call(Op.VEC3_ATAN2, values, other.values))
fun Vec3.sinh(): Vec3 = Vec3(call(Op.VEC3_SINH, values))
fun Vec3.cosh(): Vec3 = Vec3(call(Op.VEC3_COSH, values))
fun Vec3.tanh(): Vec3 = Vec3(call(Op.VEC3_TANH, values))
fun Vec3.exp(): Vec3 = Vec3(call(Op.VEC3_EXP, values))
fun Vec3.log(): Vec3 = Vec3(call(Op.VEC3_LOG, values))
fun Vec3.exp2(): Vec3 = Vec3(call(Op.VEC3_EXP2, values))
fun Vec3.log2(): Vec3 = Vec3(call(Op.VEC3_LOG2, values))
fun Vec3.pow(other: Vec3): Vec3 = Vec3(call(Op.VEC3_POW, values, other.values))
fun Vec3.mod(other: Vec3): Vec3 = Vec3(call(Op.VEC3_MOD, values, other.values))
fun Vec3.mod(scalar: Float): Vec3 = Vec3(call(Op.VEC3_MOD_S, values, floatArrayOf(scalar)))
fun Vec3.radians(): Vec3 = Vec3(call(Op.VEC3_RADIANS, values))
fun Vec3.degrees(): Vec3 = Vec3(call(Op.VEC3_DEGREES, values))
fun Vec3.isnan(): Vec3 = Vec3(call(Op.VEC3_IS_NAN, values))
fun Vec3.isinf(): Vec3 = Vec3(call(Op.VEC3_IS_INF, values))
fun Vec3.fma(b: Vec3, c: Vec3): Vec3 = Vec3(call(Op.VEC3_FMA, values, b.values, c.values))
