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

/** A 3x3 float matrix in column-major order, backed by GLM. */
class Mat3 internal constructor(internal val values: FloatArray) {

    /** Column [column] (0..2) as a copy. */
    operator fun get(column: Int): Vec3 =
        Vec3(floatArrayOf(values[column * 3], values[column * 3 + 1], values[column * 3 + 2]))

    /** Component access: [column], then [row]. */
    operator fun get(column: Int, row: Int): Float = values[column * 3 + row]

    operator fun plus(other: Mat3): Mat3 = Mat3(call(Op.MAT3_ADD, values, other.values))
    operator fun minus(other: Mat3): Mat3 = Mat3(call(Op.MAT3_SUB, values, other.values))
    operator fun times(other: Mat3): Mat3 = Mat3(call(Op.MAT3_MUL, values, other.values))
    operator fun times(other: Vec3): Vec3 = Vec3(call(Op.MAT3_MUL_VEC, values, other.values))
    operator fun times(scalar: Float): Mat3 = Mat3(call(Op.MAT3_MUL_S, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Mat3 = Mat3(call(Op.MAT3_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Mat3 = Mat3(call(Op.MAT3_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Mat3 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String =
        "mat3x3((${this[0, 0]}, ${this[0, 1]}, ${this[0, 2]}), " +
            "(${this[1, 0]}, ${this[1, 1]}, ${this[1, 2]}), " +
            "(${this[2, 0]}, ${this[2, 1]}, ${this[2, 2]}))"
}

/** Identity 3x3 matrix. */
fun mat3(): Mat3 = Mat3(call(Op.MAT3_IDENTITY))

/** Zero 3x3 matrix. */
fun mat3Zero(): Mat3 = Mat3(call(Op.MAT3_ZERO))

/** Constructs a 3x3 matrix from three column vectors. */
fun mat3(col0: Vec3, col1: Vec3, col2: Vec3): Mat3 =
    Mat3(call(Op.MAT3_FROM, col0.values, col1.values, col2.values))

fun Mat3.transposed(): Mat3 = Mat3(call(Op.MAT3_TRANSPOSE, values))
fun Mat3.inversed(): Mat3 = Mat3(call(Op.MAT3_INVERSE, values))
val Mat3.determinant: Float get() = callF(Op.MAT3_DETERMINANT, values)
val Mat3.trace: Float get() = callF(Op.MAT3_TRACE, values)
fun Mat3.compMul(other: Mat3): Mat3 = Mat3(call(Op.MAT3_COMP_MUL, values, other.values))
