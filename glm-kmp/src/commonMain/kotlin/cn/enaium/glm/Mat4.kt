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

/** A 4x4 float matrix in column-major order, backed by GLM. */
class Mat4 internal constructor(internal val values: FloatArray) {

    /** Column [column] (0..3) as a copy. */
    operator fun get(column: Int): Vec4 = Vec4(
        floatArrayOf(
            values[column * 4],
            values[column * 4 + 1],
            values[column * 4 + 2],
            values[column * 4 + 3],
        ),
    )

    /** Component access: [column], then [row]. */
    operator fun get(column: Int, row: Int): Float = values[column * 4 + row]

    operator fun plus(other: Mat4): Mat4 = Mat4(call(Op.MAT4_ADD, values, other.values))
    operator fun minus(other: Mat4): Mat4 = Mat4(call(Op.MAT4_SUB, values, other.values))
    operator fun times(other: Mat4): Mat4 = Mat4(call(Op.MAT4_MUL, values, other.values))
    operator fun times(other: Vec4): Vec4 = Vec4(call(Op.MAT4_MUL_VEC, values, other.values))
    operator fun times(scalar: Float): Mat4 = Mat4(call(Op.MAT4_MUL_S, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Mat4 = Mat4(call(Op.MAT4_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Mat4 = Mat4(call(Op.MAT4_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Mat4 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String =
        "mat4x4((${this[0, 0]}, ${this[0, 1]}, ${this[0, 2]}, ${this[0, 3]}), " +
            "(${this[1, 0]}, ${this[1, 1]}, ${this[1, 2]}, ${this[1, 3]}), " +
            "(${this[2, 0]}, ${this[2, 1]}, ${this[2, 2]}, ${this[2, 3]}), " +
            "(${this[3, 0]}, ${this[3, 1]}, ${this[3, 2]}, ${this[3, 3]}))"
}

/** Identity 4x4 matrix. */
fun mat4(): Mat4 = Mat4(call(Op.MAT4_IDENTITY))

/** Zero 4x4 matrix. */
fun mat4Zero(): Mat4 = Mat4(call(Op.MAT4_ZERO))

/** Constructs a 4x4 matrix from four column vectors. */
fun mat4(col0: Vec4, col1: Vec4, col2: Vec4, col3: Vec4): Mat4 =
    Mat4(call(Op.MAT4_FROM, col0.values, col1.values, col2.values, col3.values))

fun Mat4.transposed(): Mat4 = Mat4(call(Op.MAT4_TRANSPOSE, values))
fun Mat4.inversed(): Mat4 = Mat4(call(Op.MAT4_INVERSE, values))
val Mat4.determinant: Float get() = callF(Op.MAT4_DETERMINANT, values)
val Mat4.trace: Float get() = callF(Op.MAT4_TRACE, values)
fun Mat4.compMul(other: Mat4): Mat4 = Mat4(call(Op.MAT4_COMP_MUL, values, other.values))

/** Widens a 3x3 matrix to 4x4 (right/bottom columns and row get 0/1). */
fun Mat3.toMat4(): Mat4 = Mat4(call(Op.MAT3_TO_MAT4, values))

/** Narrows a 4x4 matrix to 3x3 (drops the 4th row and column). */
fun Mat4.toMat3(): Mat3 = Mat3(call(Op.MAT4_TO_MAT3, values))
