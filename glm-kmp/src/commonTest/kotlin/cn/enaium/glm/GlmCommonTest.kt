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

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

/**
 * Shared test suite; runs on every platform that can execute tests (JVM,
 * Android host tests, all Kotlin/Native targets, JS/Node and Wasm/Node).
 */
class GlmCommonTest {

    private fun assertVec(expected: FloatArray, actual: Vec2, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertVec(expected: FloatArray, actual: Vec3, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertVec(expected: FloatArray, actual: Vec4, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertMat(expected: FloatArray, actual: Mat4, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertMat(expected: FloatArray, actual: Mat3, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertMat(expected: FloatArray, actual: Mat2, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    private fun assertQuat(expected: FloatArray, actual: Quat, eps: Float = 1e-4f) {
        assertEquals(expected.size, actual.values.size)
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual.values[i]) <= eps,
                "component $i: expected ${expected[i]}, got ${actual.values[i]}",
            )
        }
    }

    // ==================== library plumbing ====================

    @Test
    fun versionMatchesSubmodule() {
        assertEquals("1.1.0", GLM_VERSION)
    }

    @Test
    fun opTableMatchesNative() {
        // The Op enum asserts size == glm_op_count() in its init block; this
        // test just makes the entry point explicit.
        assertTrue(Op.entries.size >= 300)
    }

    // ==================== scalars ====================

    @Test
    fun scalarTrig() {
        assertEquals(1.0f, sin(PI.toFloat() / 2.0f), 1e-5f)
        assertEquals(0.0f, cos(PI.toFloat() / 2.0f), 1e-5f)
        assertEquals(1.0f, tan(PI.toFloat() / 4.0f), 1e-5f)
        assertEquals(PI.toFloat() / 2.0f, asin(1.0f), 1e-5f)
        assertEquals(0.0f, acos(1.0f), 1e-5f)
        assertEquals(PI.toFloat() / 4.0f, atan(1.0f), 1e-5f)
        assertEquals(PI.toFloat() / 4.0f, atan2(1.0f, 1.0f), 1e-5f)
        assertEquals(0.0f, sinh(0.0f), 1e-5f)
        assertEquals(1.0f, cosh(0.0f), 1e-5f)
        assertEquals(0.0f, tanh(0.0f), 1e-5f)
    }

    @Test
    fun scalarCommon() {
        assertEquals(180.0f, degrees(PI.toFloat()), 1e-3f)
        assertEquals(PI.toFloat(), radians(180.0f), 1e-3f)
        assertEquals(3.0f, abs(-3.0f))
        assertEquals(1.0f, sign(42.0f))
        assertEquals(-1.0f, sign(-42.0f))
        assertEquals(2.0f, floor(2.7f))
        assertEquals(3.0f, ceil(2.1f))
        assertEquals(0.7f, fract(2.7f), 1e-5f)
        assertEquals(2.0f, trunc(2.7f))
        assertEquals(3.0f, round(2.5f))
        assertEquals(2.0f, roundEven(2.5f))
        assertEquals(3.0f, sqrt(9.0f))
        assertEquals(0.5f, inversesqrt(4.0f))
        assertEquals(8.0f, pow(2.0f, 3.0f))
        assertEquals(1.0f, mod(7.0f, 3.0f))
        assertEquals(3.0f, min(3.0f, 9.0f))
        assertEquals(9.0f, max(3.0f, 9.0f))
        assertEquals(5.0f, clamp(42.0f, -1.0f, 5.0f))
        assertEquals(2.5f, mix(1.0f, 4.0f, 0.5f))
        assertEquals(1.0f, step(1.0f, 2.0f))
        assertEquals(0.0f, step(1.0f, 0.5f))
        assertEquals(0.5f, smoothstep(0.0f, 1.0f, 0.5f))
        assertEquals(7.0f, fma(2.0f, 3.0f, 1.0f))
        assertTrue(isnan(0.0f / 0.0f))
        assertTrue(isinf(1.0f / 0.0f))
    }

    // ==================== Vec2 ====================

    @Test
    fun vec2Ops() {
        val a = vec2(1.0f, 2.0f)
        val b = vec2(3.0f, 4.0f)
        assertVec(floatArrayOf(4.0f, 6.0f), a + b)
        assertVec(floatArrayOf(-2.0f, -2.0f), a - b)
        assertVec(floatArrayOf(3.0f, 8.0f), a * b)
        assertVec(floatArrayOf(3.0f, 2.0f), b / a)
        assertVec(floatArrayOf(2.0f, 4.0f), a * 2.0f)
        assertVec(floatArrayOf(0.5f, 1.0f), a / 2.0f)
        assertVec(floatArrayOf(-1.0f, -2.0f), -a)
        assertEquals(11.0f, a dot b)
        assertEquals(2.236068f, a.length, 1e-5f)
        assertEquals(2.828427f, a.distance(b), 1e-5f)
        assertVec(floatArrayOf(0.4472136f, 0.8944272f), a.normalized())
        assertVec(floatArrayOf(1.0f, 2.0f), a.clamped(0.0f, 2.0f))
        assertVec(floatArrayOf(2.0f, 3.0f), a.mixed(b, 0.5f))
        assertVec(floatArrayOf(1.0f, 2.0f), a.minimized(b))
        assertVec(floatArrayOf(3.0f, 4.0f), a.maximized(b))
        assertVec(floatArrayOf(1.0f, 2.0f), a.minimized(2.0f))
        assertVec(floatArrayOf(2.0f, 2.0f), a.maximized(2.0f))
        assertVec(floatArrayOf(1.0f, 2.0f), a.abs())
        assertVec(floatArrayOf(-1.0f, -1.0f), (-a).signed())
        assertVec(floatArrayOf(1.0f, 2.0f), a.floored())
        assertVec(floatArrayOf(1.0f, 2.0f), vec2(1.7f, 2.3f).truncated())
        assertVec(floatArrayOf(2.0f, 2.0f), vec2(1.5f, 1.5f).rounded())
        assertVec(floatArrayOf(2.0f, 0.0f), vec2(1.5f, 0.5f).roundedEven())
        assertTrue(vec2(0.0f, 2.0f).any)
        assertTrue(vec2(1.0f, 2.0f).all)
        assertTrue(vec2(1.0f, 2.0f).equal(vec2(1.0f, 2.0f)))
        assertTrue(vec2(1.0f, 2.0f).notEqual(vec2(1.0f, 3.0f)))
        assertEquals(vec2(1.0f, 2.0f), vec2(1.0f, 2.0f))
        assertTrue(vec2(1.0f, 2.0f).toString().startsWith("vec2(1"))
    }

    @Test
    fun vec2Componentwise() {
        val v = vec2(2.0f, 9.0f)
        assertVec(floatArrayOf(1.4142135f, 3.0f), v.sqrt())
        assertVec(floatArrayOf(0.70710677f, 0.3333333f), v.inversesqrt())
        assertVec(floatArrayOf(8.0f, 27.0f), vec2(2.0f, 3.0f).pow(vec2(3.0f, 3.0f)))
        assertVec(floatArrayOf(2.0f, 0.0f), vec2(7.0f, 3.0f).mod(vec2(5.0f, 3.0f)))
        assertVec(floatArrayOf(1.0f, 2.0f), vec2(7.0f, 8.0f).mod(3.0f))
        assertVec(floatArrayOf(1.0f, -1.0f), vec2(2.0f, -1.0f).signed())
        assertVec(floatArrayOf(0.0f, 1.0f), vec2(0.0f, 1.0f).steped(0.5f))
        assertVec(floatArrayOf(0.0f, 1.0f), vec2(0.0f, 1.0f).steped(vec2(0.5f, 0.5f)))
        assertVec(floatArrayOf(0.5f, 0.5f), vec2(0.5f, 0.5f).smoothsteped(0.0f, 1.0f))
        assertVec(floatArrayOf(11.0f, 5.0f), vec2(1.0f, 1.0f).fma(vec2(2.0f, 2.0f), vec2(9.0f, 3.0f)))
    }

    // ==================== Vec3 ====================

    @Test
    fun vec3Ops() {
        val a = vec3(1.0f, 2.0f, 3.0f)
        val b = vec3(4.0f, 5.0f, 6.0f)
        assertVec(floatArrayOf(5.0f, 7.0f, 9.0f), a + b)
        assertVec(floatArrayOf(-3.0f, -3.0f, -3.0f), a - b)
        assertVec(floatArrayOf(4.0f, 10.0f, 18.0f), a * b)
        assertVec(floatArrayOf(4.0f, 2.5f, 2.0f), b / a)
        assertVec(floatArrayOf(2.0f, 4.0f, 6.0f), a * 2.0f)
        assertVec(floatArrayOf(-1.0f, -2.0f, -3.0f), -a)
        assertEquals(32.0f, a dot b)
        assertVec(floatArrayOf(-3.0f, 6.0f, -3.0f), a cross b)
        assertEquals(3.7416574f, a.length, 1e-5f)
        assertEquals(5.196152f, a.distance(b), 1e-5f)
        assertVec(floatArrayOf(0.26726124f, 0.5345225f, 0.8017837f), a.normalized())
        assertVec(floatArrayOf(1.0f, 2.0f, 3.0f), a.clamped(0.0f, 3.0f))
        assertVec(floatArrayOf(2.5f, 3.5f, 4.5f), a.mixed(b, 0.5f))
        assertVec(floatArrayOf(1.0f, 2.0f, 3.0f), a.minimized(b))
        assertVec(floatArrayOf(4.0f, 5.0f, 6.0f), a.maximized(b))
        assertVec(floatArrayOf(0.4f, 0.5f, 0.6f), vec3(1.4f, 1.5f, 1.6f).fracted())
        assertTrue(vec3(0.0f, 1.0f, 0.0f).any)
        assertTrue(vec3(1.0f, 1.0f, 1.0f).all)
        assertEquals(vec3(1.0f, 2.0f, 3.0f), vec3(1.0f, 2.0f, 3.0f))
        assertTrue(a.toString().startsWith("vec3(1"))
    }

    // ==================== Vec4 ====================

    @Test
    fun vec4Ops() {
        val a = vec4(1.0f, 2.0f, 3.0f, 4.0f)
        val b = vec4(5.0f, 6.0f, 7.0f, 8.0f)
        assertVec(floatArrayOf(6.0f, 8.0f, 10.0f, 12.0f), a + b)
        assertVec(floatArrayOf(-4.0f, -4.0f, -4.0f, -4.0f), a - b)
        assertVec(floatArrayOf(5.0f, 12.0f, 21.0f, 32.0f), a * b)
        assertVec(floatArrayOf(5.0f, 3.0f, 2.3333333f, 2.0f), b / a)
        assertVec(floatArrayOf(2.0f, 4.0f, 6.0f, 8.0f), a * 2.0f)
        assertVec(floatArrayOf(-1.0f, -2.0f, -3.0f, -4.0f), -a)
        assertEquals(70.0f, a dot b)
        assertEquals(5.4772257f, a.length, 1e-5f)
        assertEquals(8.0f, a.distance(b), 1e-5f)
        assertVec(floatArrayOf(0.18257418f, 0.36514837f, 0.5477226f, 0.73029673f), a.normalized())
        assertVec(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f), a.clamped(0.0f, 4.0f))
        assertVec(floatArrayOf(3.0f, 4.0f, 5.0f, 6.0f), a.mixed(b, 0.5f))
        assertVec(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f), a.minimized(b))
        assertVec(floatArrayOf(5.0f, 6.0f, 7.0f, 8.0f), a.maximized(b))
        assertVec(floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f), a.fracted())
        assertTrue(vec4(0.0f, 0.0f, 1.0f, 0.0f).any)
        assertTrue(vec4(1.0f, 1.0f, 1.0f, 1.0f).all)
        assertEquals(vec4(1.0f, 2.0f, 3.0f, 4.0f), vec4(1.0f, 2.0f, 3.0f, 4.0f))
        assertTrue(a.toString().startsWith("vec4(1"))
    }

    // ==================== matrices ====================

    @Test
    fun mat2Ops() {
        val a = mat2(vec2(1.0f, 2.0f), vec2(3.0f, 4.0f))
        val b = mat2(vec2(5.0f, 6.0f), vec2(7.0f, 8.0f))
        // column-major: a * b
        assertMat(floatArrayOf(23.0f, 34.0f, 31.0f, 46.0f), a * b)
        assertVec(floatArrayOf(7.0f, 10.0f), a * vec2(1.0f, 2.0f))
        assertMat(floatArrayOf(2.0f, 4.0f, 6.0f, 8.0f), a * 2.0f)
        assertMat(floatArrayOf(1.0f, 3.0f, 2.0f, 4.0f), a.transposed())
        assertEquals(-2.0f, a.determinant, 1e-5f)
        assertMat(floatArrayOf(-2.0f, 1.0f, 1.5f, -0.5f), a.inversed())
        assertEquals(5.0f, a.trace, 1e-5f)
        assertMat(floatArrayOf(5.0f, 12.0f, 21.0f, 32.0f), a.compMul(b))
    }

    @Test
    fun mat3Ops() {
        val a = mat3(vec3(1.0f, 2.0f, 3.0f), vec3(4.0f, 5.0f, 6.0f), vec3(7.0f, 8.0f, 10.0f))
        val b = mat3(vec3(10.0f, 11.0f, 12.0f), vec3(13.0f, 14.0f, 15.0f), vec3(16.0f, 17.0f, 18.0f))
        assertVec(floatArrayOf(30.0f, 36.0f, 45.0f), a * vec3(1.0f, 2.0f, 3.0f))
        assertMat(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 10.0f), a)
        assertEquals(-3.0f, a.determinant, 1e-5f)
        assertMat(floatArrayOf(1.0f, 4.0f, 7.0f, 2.0f, 5.0f, 8.0f, 3.0f, 6.0f, 10.0f), a.transposed())
        val inv = a.inversed()
        val id = a * inv
        assertMat(
            floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            id, eps = 1e-4f,
        )
        assertEquals(16.0f, a.trace, 1e-5f)
        assertMat(floatArrayOf(10.0f, 22.0f, 36.0f, 52.0f, 70.0f, 90.0f, 112.0f, 136.0f, 180.0f), a.compMul(b))
    }

    @Test
    fun mat4Ops() {
        val a = mat4(
            vec4(1.0f, 2.0f, 3.0f, 4.0f),
            vec4(5.0f, 6.0f, 7.0f, 8.0f),
            vec4(9.0f, 10.0f, 11.0f, 12.0f),
            vec4(13.0f, 14.0f, 15.0f, 16.0f),
        )
        val b = mat4(
            vec4(16.0f, 15.0f, 14.0f, 13.0f),
            vec4(12.0f, 11.0f, 10.0f, 9.0f),
            vec4(8.0f, 7.0f, 6.0f, 5.0f),
            vec4(4.0f, 3.0f, 2.0f, 1.0f),
        )
        assertVec(floatArrayOf(90.0f, 100.0f, 110.0f, 120.0f), a * vec4(1.0f, 2.0f, 3.0f, 4.0f))
        assertMat(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f), a)
        assertEquals(0.0f, a.determinant, 1e-5f)
        assertEquals(34.0f, a.trace, 1e-5f)
        val id = mat4()
        assertMat(
            floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            id * id,
        )
        // singular matrix a: inverse of identity is identity
        assertMat(
            floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            id.inversed(),
        )
        val m3 = mat4().toMat3()
        assertMat(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f), m3)
        assertMat(
            floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            m3.toMat4(),
        )
        // a * b with the classic 1..16 x 16..1 product
        assertMat(
            floatArrayOf(386.0f, 444.0f, 502.0f, 560.0f, 274.0f, 316.0f, 358.0f, 400.0f, 162.0f, 188.0f, 214.0f, 240.0f, 50.0f, 60.0f, 70.0f, 80.0f),
            a * b,
        )
    }

    // ==================== transforms ====================

    @Test
    fun transforms() {
        val t = mat4().translated(1.0f, 2.0f, 3.0f)
        assertVec(floatArrayOf(2.0f, 4.0f, 6.0f, 1.0f), t * vec4(1.0f, 2.0f, 3.0f, 1.0f))
        val s = mat4().scaled(2.0f, 3.0f, 4.0f)
        assertVec(floatArrayOf(2.0f, 6.0f, 12.0f, 1.0f), s * vec4(1.0f, 2.0f, 3.0f, 1.0f))
        val r = mat4().rotated(PI.toFloat() / 2.0f, 0.0f, 0.0f, 1.0f)
        assertVec(floatArrayOf(-1.0f, 1.0f, 0.0f, 1.0f), r * vec4(1.0f, 1.0f, 0.0f, 1.0f), 1e-4f)
        val p = perspective(PI.toFloat() / 2.0f, 1.0f, 0.1f, 100.0f)
        val clip = p * vec4(0.0f, 0.0f, -0.1f, 1.0f)
        assertVec(floatArrayOf(0.0f, 0.0f, -0.1f, 0.1f), clip, 1e-4f)
        assertVec(floatArrayOf(0.0f, 0.0f, -1.0f, 1.0f), clip / clip.w, 1e-4f)
        val o = ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
        assertVec(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f), o * vec4(0.0f, 0.0f, 0.0f, 1.0f), 1e-4f)
        val view = lookAt(vec3(0.0f, 0.0f, 5.0f), vec3(0.0f, 0.0f, 0.0f), vec3(0.0f, 1.0f, 0.0f))
        // camera at z=5 looking at origin: origin maps to (0, 0, -5)
        assertVec(floatArrayOf(0.0f, 0.0f, -5.0f, 1.0f), view * vec4(0.0f, 0.0f, 0.0f, 1.0f), 1e-4f)
    }

    // ==================== quaternions ====================

    @Test
    fun quatOps() {
        val q = quat(0.7071068f, 0.0f, 0.0f, 0.7071068f) // 90deg around Z: (w, x, y, z)
        assertQuat(floatArrayOf(0.0f, 0.0f, 0.7071068f, 0.7071068f), q)
        assertVec(floatArrayOf(-1.0f, 1.0f, 0.0f), q * vec3(1.0f, 1.0f, 0.0f), 1e-4f)
        assertQuat(floatArrayOf(0.0f, 0.0f, -0.7071068f, 0.7071068f), q.conjugated())
        assertQuat(floatArrayOf(0.0f, 0.0f, -0.7071068f, 0.7071068f), q.inversed())
        assertQuat(floatArrayOf(0.0f, 0.0f, 0.7071068f, 0.7071068f), q.normalized())
        assertEquals(1.0f, q.length, 1e-5f)
        assertQuat(floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f), q * q, 1e-4f) // 180deg
        val m = q.toMat4()
        assertVec(floatArrayOf(-1.0f, 1.0f, 0.0f, 1.0f), m * vec4(1.0f, 1.0f, 0.0f, 1.0f), 1e-4f)
        val back = m.toQuat()
        assertQuat(floatArrayOf(0.0f, 0.0f, 0.7071068f, 0.7071068f), back, 1e-4f)
        val slerped = quat().slerped(q, 0.5f)
        assertEquals(0.38268343f, slerped.z, 1e-4f)
        assertEquals(0.9238795f, slerped.w, 1e-4f)
        assertEquals(PI / 2, q.angle.toDouble(), 1e-5)
        assertVec(floatArrayOf(0.0f, 0.0f, 1.0f), q.axis)
        assertTrue(q.toString().startsWith("quat(0"))
    }

    // ==================== conversions & errors ====================

    @Test
    fun opTableGuardCatchesDrift() {
        // If the generated op table ever drifts from the C++ dispatch table,
        // the Op enum's init check throws at class-load time. Referencing the
        // entries here forces the check on every platform.
        assertEquals(GlmNative.opCount(), Op.entries.size)
    }
}
