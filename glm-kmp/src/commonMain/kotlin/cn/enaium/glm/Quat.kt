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
 * A unit quaternion backed by GLM, stored as (x, y, z, w) like GLM's
 * `glm::quat` data layout.
 */
class Quat internal constructor(internal val values: FloatArray) {

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

    operator fun plus(other: Quat): Quat = Quat(call(Op.QUAT_ADD, values, other.values))
    operator fun minus(other: Quat): Quat = Quat(call(Op.QUAT_SUB, values, other.values))
    operator fun times(other: Quat): Quat = Quat(call(Op.QUAT_MUL, values, other.values))
    operator fun times(other: Vec3): Vec3 = Vec3(call(Op.QUAT_MUL_VEC3, values, other.values))
    operator fun times(scalar: Float): Quat = Quat(call(Op.QUAT_SCALE, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Quat = Quat(call(Op.QUAT_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Quat && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "quat($x, $y, $z, $w)"
}

/** The identity quaternion. */
fun quat(): Quat = Quat(call(Op.QUAT_IDENTITY))

/**
 * Constructs a quaternion from components in GLM's constructor order:
 * (w, x, y, z).
 */
fun quat(w: Float, x: Float, y: Float, z: Float): Quat =
    Quat(call(Op.QUAT_FROM, floatArrayOf(w, x, y, z)))

/** Constructs a quaternion from an axis-angle representation. */
fun quat(axis: Vec3, angle: Float): Quat = Quat(call(Op.QUAT_AXIS_ANGLE, floatArrayOf(angle), axis.values))

fun Quat.conjugated(): Quat = Quat(call(Op.QUAT_CONJUGATE, values))
fun Quat.inversed(): Quat = Quat(call(Op.QUAT_INVERSE, values))
fun Quat.normalized(): Quat = Quat(call(Op.QUAT_NORMALIZE, values))
infix fun Quat.dot(other: Quat): Float = callF(Op.QUAT_DOT, values, other.values)
val Quat.length: Float get() = callF(Op.QUAT_LENGTH, values)
infix fun Quat.cross(other: Quat): Quat = Quat(call(Op.QUAT_CROSS, values, other.values))
fun Quat.lerped(other: Quat, a: Float): Quat =
    Quat(call(Op.QUAT_LERP, values, other.values, floatArrayOf(a)))
fun Quat.slerped(other: Quat, a: Float): Quat =
    Quat(call(Op.QUAT_SLERP, values, other.values, floatArrayOf(a)))
fun Quat.rotated(angle: Float, axis: Vec3): Quat =
    Quat(call(Op.QUAT_ROTATE, values, floatArrayOf(angle, axis.x, axis.y, axis.z)))

val Quat.angle: Float get() = callF(Op.QUAT_ANGLE, values)
val Quat.axis: Vec3 get() = Vec3(call(Op.QUAT_AXIS, values))
val Quat.pitch: Float get() = callF(Op.QUAT_PITCH, values)
val Quat.yaw: Float get() = callF(Op.QUAT_YAW, values)
val Quat.roll: Float get() = callF(Op.QUAT_ROLL, values)
val Quat.eulerAngles: Vec3 get() = Vec3(call(Op.QUAT_EULER_ANGLES, values))

fun Quat.toMat4(): Mat4 = Mat4(call(Op.QUAT_TO_MAT4, values))
fun Quat.toMat3(): Mat3 = Mat3(call(Op.QUAT_TO_MAT3, values))
fun Mat4.toQuat(): Quat = Quat(call(Op.MAT4_TO_QUAT, values))
fun Mat3.toQuat(): Quat = Quat(call(Op.MAT3_TO_QUAT, values))
