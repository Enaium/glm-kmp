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

/** A 2x2 float matrix in column-major order, backed by GLM. */
class Mat2 internal constructor(internal val values: FloatArray) {

    /** Column [column] (0 or 1) as a copy. */
    operator fun get(column: Int): Vec2 = Vec2(floatArrayOf(values[column * 2], values[column * 2 + 1]))

    /** Component access: [column], then [row]. */
    operator fun get(column: Int, row: Int): Float = values[column * 2 + row]

    operator fun plus(other: Mat2): Mat2 = Mat2(call(Op.MAT2_ADD, values, other.values))
    operator fun minus(other: Mat2): Mat2 = Mat2(call(Op.MAT2_SUB, values, other.values))
    operator fun times(other: Mat2): Mat2 = Mat2(call(Op.MAT2_MUL, values, other.values))
    operator fun times(other: Vec2): Vec2 = Vec2(call(Op.MAT2_MUL_VEC, values, other.values))
    operator fun times(scalar: Float): Mat2 = Mat2(call(Op.MAT2_MUL_S, values, floatArrayOf(scalar)))
    operator fun div(scalar: Float): Mat2 = Mat2(call(Op.MAT2_DIV_S, values, floatArrayOf(scalar)))
    operator fun unaryMinus(): Mat2 = Mat2(call(Op.MAT2_NEG, values))

    override fun equals(other: Any?): Boolean =
        other is Mat2 && values.contentEquals(other.values)

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String =
        "mat2x2((${this[0, 0]}, ${this[0, 1]}), (${this[1, 0]}, ${this[1, 1]}))"
}

/** Identity 2x2 matrix. */
fun mat2(): Mat2 = Mat2(call(Op.MAT2_IDENTITY))

/** Zero 2x2 matrix. */
fun mat2Zero(): Mat2 = Mat2(call(Op.MAT2_ZERO))

/** Constructs a 2x2 matrix from two column vectors. */
fun mat2(col0: Vec2, col1: Vec2): Mat2 =
    Mat2(call(Op.MAT2_FROM, col0.values, col1.values))

fun Mat2.transposed(): Mat2 = Mat2(call(Op.MAT2_TRANSPOSE, values))
fun Mat2.inversed(): Mat2 = Mat2(call(Op.MAT2_INVERSE, values))
val Mat2.determinant: Float get() = callF(Op.MAT2_DETERMINANT, values)
val Mat2.trace: Float get() = callF(Op.MAT2_TRACE, values)
fun Mat2.compMul(other: Mat2): Mat2 = Mat2(call(Op.MAT2_COMP_MUL, values, other.values))
