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
import kotlin.math.PI

/**
 * Shared demonstration snippets, runnable on every platform the example
 * targets. Each platform's `main` calls [runAll] and prints the results.
 */
object GlmExamples {

    fun runAll(): String {
        val sb = StringBuilder()
        sb.appendLine("GLM version: $GLM_VERSION")

        sb.appendLine("\n-- vectors --")
        val a = vec3(1.0f, 2.0f, 3.0f)
        val b = vec3(4.0f, 5.0f, 6.0f)
        sb.appendLine("a + b            = ${a + b}")
        sb.appendLine("a dot b          = ${a dot b}")
        sb.appendLine("a cross b        = ${a cross b}")
        sb.appendLine("a normalized     = ${a.normalized()}")
        sb.appendLine("mix(a, b, 0.5)   = ${a.mixed(b, 0.5f)}")

        sb.appendLine("\n-- matrices --")
        val model = mat4()
            .translated(1.0f, 2.0f, 3.0f)
            .rotated(PI.toFloat() / 4.0f, 0.0f, 1.0f, 0.0f)
            .scaled(2.0f, 2.0f, 2.0f)
        val view = lookAt(
            eye = vec3(0.0f, 0.0f, 5.0f),
            center = vec3(0.0f, 0.0f, 0.0f),
            up = vec3(0.0f, 1.0f, 0.0f),
        )
        val projection = perspective(
            fovyRadians = PI.toFloat() / 2.0f,
            aspect = 16.0f / 9.0f,
            near = 0.1f,
            far = 100.0f,
        )
        sb.appendLine("model          = $model")
        sb.appendLine("model * origin = ${model * vec4(0.0f, 0.0f, 0.0f, 1.0f)}")
        sb.appendLine("view           = $view")
        sb.appendLine("view * origin  = ${view * vec4(0.0f, 0.0f, 0.0f, 1.0f)}")
        sb.appendLine("projection     = $projection")
        sb.appendLine("det(model)     = ${model.determinant}")

        sb.appendLine("\n-- quaternions --")
        val q = quat(axis = vec3(0.0f, 0.0f, 1.0f), angle = PI.toFloat() / 2.0f)
        val r = q.rotated(PI.toFloat() / 2.0f, vec3(1.0f, 0.0f, 0.0f))
        sb.appendLine("q              = $q")
        sb.appendLine("q * vec3(1,0,0) = ${q * vec3(1.0f, 0.0f, 0.0f)}")
        sb.appendLine("slerp(q, r, .5) = ${q.slerped(r, 0.5f)}")
        sb.appendLine("q.toMat4()      = ${q.toMat4()}")

        sb.appendLine("\n-- scalars --")
        sb.appendLine("sin(pi/2)       = ${sin(PI.toFloat() / 2.0f)}")
        sb.appendLine("clamp(42, 0, 5) = ${clamp(42.0f, 0.0f, 5.0f)}")
        sb.appendLine("mix(1, 4, 0.5)  = ${mix(1.0f, 4.0f, 0.5f)}")
        return sb.toString()
    }
}
