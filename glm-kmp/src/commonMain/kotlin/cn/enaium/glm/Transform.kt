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

// =========================================================================
// GLM gtc/matrix_transform bindings
// =========================================================================

/** Applies a translation to [this] matrix. */
fun Mat4.translated(v: Vec3): Mat4 = Mat4(call(Op.MAT4_TRANSLATE, values, v.values))

/** Applies a translation to [this] matrix. */
fun Mat4.translated(x: Float, y: Float, z: Float): Mat4 =
    Mat4(call(Op.MAT4_TRANSLATE_XYZ, values, floatArrayOf(x, y, z)))

/** Rotates [this] matrix by [angle] radians around the given axis. */
fun Mat4.rotated(angle: Float, axis: Vec3): Mat4 =
    Mat4(call(Op.MAT4_ROTATE, values, floatArrayOf(angle, axis.x, axis.y, axis.z)))

/** Rotates [this] matrix by [angle] radians around the given axis. */
fun Mat4.rotated(angle: Float, x: Float, y: Float, z: Float): Mat4 =
    Mat4(call(Op.MAT4_ROTATE, values, floatArrayOf(angle, x, y, z)))

/** Rotates [this] matrix by [angle] radians around the X axis. */
fun Mat4.rotatedX(angle: Float): Mat4 = Mat4(call(Op.MAT4_ROTATE_X, values, floatArrayOf(angle)))

/** Rotates [this] matrix by [angle] radians around the Y axis. */
fun Mat4.rotatedY(angle: Float): Mat4 = Mat4(call(Op.MAT4_ROTATE_Y, values, floatArrayOf(angle)))

/** Rotates [this] matrix by [angle] radians around the Z axis. */
fun Mat4.rotatedZ(angle: Float): Mat4 = Mat4(call(Op.MAT4_ROTATE_Z, values, floatArrayOf(angle)))

/** Applies a scale to [this] matrix. */
fun Mat4.scaled(v: Vec3): Mat4 = Mat4(call(Op.MAT4_SCALE, values, v.values))

/** Applies a scale to [this] matrix. */
fun Mat4.scaled(x: Float, y: Float, z: Float): Mat4 =
    Mat4(call(Op.MAT4_SCALE_XYZ, values, floatArrayOf(x, y, z)))

/** Builds a perspective projection matrix (right-handed, -1..1 depth). */
fun perspective(fovyRadians: Float, aspect: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_PERSPECTIVE, floatArrayOf(fovyRadians, aspect, near, far)))

/** Builds a left-handed perspective projection matrix. */
fun perspectiveLH(fovyRadians: Float, aspect: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_PERSPECTIVE_LH, floatArrayOf(fovyRadians, aspect, near, far)))

/** Builds a right-handed perspective projection matrix. */
fun perspectiveRH(fovyRadians: Float, aspect: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_PERSPECTIVE_RH, floatArrayOf(fovyRadians, aspect, near, far)))

/** Builds an orthographic projection matrix. */
fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_ORTHO, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a left-handed orthographic projection matrix. */
fun orthoLH(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_ORTHO_LH, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a right-handed orthographic projection matrix. */
fun orthoRH(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_ORTHO_RH, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a perspective projection matrix from a frustum. */
fun frustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_FRUSTUM, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a left-handed perspective projection matrix from a frustum. */
fun frustumLH(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_FRUSTUM_LH, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a right-handed perspective projection matrix from a frustum. */
fun frustumRH(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 =
    Mat4(call(Op.MAT4_FRUSTUM_RH, floatArrayOf(left, right, bottom, top, near, far)))

/** Builds a look-at view matrix (right-handed, forward = -Z). */
fun lookAt(eye: Vec3, center: Vec3, up: Vec3): Mat4 =
    Mat4(call(Op.MAT4_LOOK_AT, eye.values, center.values, up.values))

/** Builds a left-handed look-at view matrix. */
fun lookAtLH(eye: Vec3, center: Vec3, up: Vec3): Mat4 =
    Mat4(call(Op.MAT4_LOOK_AT_LH, eye.values, center.values, up.values))

/** Builds a right-handed look-at view matrix. */
fun lookAtRH(eye: Vec3, center: Vec3, up: Vec3): Mat4 =
    Mat4(call(Op.MAT4_LOOK_AT_RH, eye.values, center.values, up.values))
