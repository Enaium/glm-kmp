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

package cn.enaium.glm.example

import cn.enaium.glm.*
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Example-level tests exercising the published artifact end to end; runs on
 * every platform the example targets.
 */
class ExampleCommonTest {

    @Test
    fun runAllSmokeTest() {
        val output = GlmExamples.runAll()
        assertTrue(output.contains("GLM version: 1.1.0"))
        assertTrue(output.contains("a dot b"))
        // Float formatting differs across platforms (e.g. "vec4(0, 0, -5, 1)"
        // on JS vs "vec4(0.0, 0.0, -5.0, 1.0)" on JVM/Native), so only the
        // interesting numbers are asserted.
        assertTrue(output.contains("view * origin"))
        assertTrue(output.contains("-5"))
        assertTrue(output.contains("q * vec3(1,0,0) = vec3("))
        assertTrue(output.contains("0.99999994"))
        assertTrue(output.contains("sin(pi/2)       = 1"))
    }

    @Test
    fun valueSemantics() {
        val v = vec4(1.0f, 2.0f, 3.0f, 4.0f)
        val w = v * 2.0f - vec4(1.0f, 1.0f, 1.0f, 1.0f)
        assertTrue(w == vec4(1.0f, 3.0f, 5.0f, 7.0f))
    }
}
