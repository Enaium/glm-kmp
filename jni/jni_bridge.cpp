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

#include <jni.h>
#include "glm_wrapper.h"

// ============================================================================
// JNI entry points for cn.enaium.glm.GlmNative.  GLM is a pure computation
// library, so a single flat dispatch (op + packed floats in/out) is enough:
// the Kotlin side knows the exact input/output float counts of every op.
// ============================================================================

extern "C" JNIEXPORT void JNICALL
Java_cn_enaium_glm_Jni_nativeCall(
    JNIEnv* env, jclass, jint op, jfloatArray args, jint argCount,
    jfloatArray out, jint outCount)
{
    jfloat* a = args != nullptr ? env->GetFloatArrayElements(args, nullptr) : nullptr;
    jfloat* o = env->GetFloatArrayElements(out, nullptr);
    glm_call(static_cast<int>(op), a, static_cast<int>(argCount), o, static_cast<int>(outCount));
    if (a != nullptr) {
        env->ReleaseFloatArrayElements(args, a, JNI_ABORT);
    }
    env->ReleaseFloatArrayElements(out, o, 0);
}

extern "C" JNIEXPORT jint JNICALL
Java_cn_enaium_glm_Jni_nativeOpCount(JNIEnv*, jclass)
{
    return glm_op_count();
}

extern "C" JNIEXPORT jint JNICALL
Java_cn_enaium_glm_Jni_nativeVersion(JNIEnv*, jclass)
{
    return glm_version();
}
