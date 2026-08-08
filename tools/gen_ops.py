#!/usr/bin/env python3
"""
Single source of truth for the GLM C ABI binding surface.

Generates:
  jni/c_api/glm_wrapper.cpp  -- C ABI dispatcher over the GLM C++ library
  glm-kmp/src/commonMain/kotlin/cn/enaium/glm/Ops.kt -- Kotlin op enum

Every op has a fixed input size (floats, packed contiguously: vectors,
matrices, quaternions and scalars) and a fixed output size.  Bool results are
encoded as 1.0f / 0.0f.  Keep the table sorted by logical groups so the op
codes stay stable and readable.
"""

import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
C_API_DIR = os.path.join(ROOT, "jni", "c_api")
OPS_KT = os.path.join(
    ROOT, "glm-kmp", "src", "commonMain", "kotlin", "cn", "enaium", "glm", "Ops.kt",
)

# ---------------------------------------------------------------------------
# Argument/result kinds: size in floats, C++ read expression, C++ write
# expression.  Placeholders: {n} name, {call} the generated GLM call.
# ---------------------------------------------------------------------------

S = 1
V2, V3, V4 = 2, 3, 4
M2, M3, M4 = 4, 9, 16
Q = 4
B = 1  # single bool (1 float out)
BV2, BV3, BV4 = 2, 3, 4  # bool vectors (floats out)

READ = {
    "s": "float {n} = e.next();",
    "v2": "glm::vec2 {n}; memcpy(&{n}, e.next({s}), {b});",
    "v3": "glm::vec3 {n}; memcpy(&{n}, e.next({s}), {b});",
    "v4": "glm::vec4 {n}; memcpy(&{n}, e.next({s}), {b});",
    "m2": "glm::mat2 {n}; memcpy(&{n}, e.next({s}), {b});",
    "m3": "glm::mat3 {n}; memcpy(&{n}, e.next({s}), {b});",
    "m4": "glm::mat4 {n}; memcpy(&{n}, e.next({s}), {b});",
    "q": "glm::quat {n}; memcpy(&{n}, e.next({s}), {b});",
}

WRITE = {
    "s": "float r = {call}; memcpy(out, &r, sizeof r);",
    "v2": "glm::vec2 r = {call}; memcpy(out, &r, sizeof r);",
    "v3": "glm::vec3 r = {call}; memcpy(out, &r, sizeof r);",
    "v4": "glm::vec4 r = {call}; memcpy(out, &r, sizeof r);",
    "m2": "glm::mat2 r = {call}; memcpy(out, &r, sizeof r);",
    "m3": "glm::mat3 r = {call}; memcpy(out, &r, sizeof r);",
    "m4": "glm::mat4 r = {call}; memcpy(out, &r, sizeof r);",
    "q": "glm::quat r = {call}; memcpy(out, &r, sizeof r);",
    "b": "out[0] = ({call}) ? 1.0f : 0.0f;",
    "bv2": "glm::bvec2 r = {call}; out[0] = r[0] ? 1.0f : 0.0f; out[1] = r[1] ? 1.0f : 0.0f;",
    "bv3": "glm::bvec3 r = {call}; out[0] = r[0] ? 1.0f : 0.0f; out[1] = r[1] ? 1.0f : 0.0f; out[2] = r[2] ? 1.0f : 0.0f;",
    "bv4": "glm::bvec4 r = {call}; out[0] = r[0] ? 1.0f : 0.0f; out[1] = r[1] ? 1.0f : 0.0f; out[2] = r[2] ? 1.0f : 0.0f; out[3] = r[3] ? 1.0f : 0.0f;",
}

SIZE = {"s": 1, "v2": 2, "v3": 3, "v4": 4, "m2": 4, "m3": 9, "m4": 16, "q": 4, "b": 1, "bv2": 2, "bv3": 3, "bv4": 4}
B = "b"
BV2, BV3, BV4 = "bv2", "bv3", "bv4"
S = "s"
V2, V3, V4 = "v2", "v3", "v4"
M2, M3, M4 = "m2", "m3", "m4"
Q = "q"

# ---------------------------------------------------------------------------
# Op table.  Each row: (NAME, [input kinds...], output kind, GLM expression).
# Vector/matrix/quat inputs are named a, b, c in order; scalars s, t, u.
# ---------------------------------------------------------------------------

OPS = []


def unary(name, fn, kind):
    OPS.append((name, [kind], kind, "glm::{f}(a)".format(f=fn)))


def binary(name, fn, kind):
    OPS.append((name, [kind, kind], kind, "glm::{f}(a, b)".format(f=fn)))


def unary_s(name, fn):
    OPS.append((name, [S], S, "glm::{f}(s)".format(f=fn)))


def binary_s(name, fn):
    OPS.append((name, [S, S], S, "glm::{f}(s, t)".format(f=fn)))


# ---- scalars --------------------------------------------------------------

for f in ["sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh",
          "asinh", "acosh", "atanh", "radians", "degrees", "abs", "sign",
          "floor", "ceil", "fract", "trunc", "round", "roundEven", "sqrt",
          "inversesqrt", "exp", "log", "exp2", "log2"]:
    unary_s(f.upper(), f)
binary_s("ATAN2", "atan")
binary_s("POW", "pow")
binary_s("MOD", "mod")
binary_s("MIN", "min")
binary_s("MAX", "max")
binary_s("STEP", "step")
OPS.append(("CLAMP", [S, S, S], S, "glm::clamp(s, t, u)"))
OPS.append(("MIX", [S, S, S], S, "glm::mix(s, t, u)"))
OPS.append(("SMOOTHSTEP", [S, S, S], S, "glm::smoothstep(s, t, u)"))
OPS.append(("FMA", [S, S, S], S, "glm::fma(s, t, u)"))
OPS.append(("IS_NAN", [S], B, "glm::isnan(s)"))
OPS.append(("IS_INF", [S], B, "glm::isinf(s)"))

# ---- vectors --------------------------------------------------------------

VEC_LEN = {"VEC2": V2, "VEC3": V3, "VEC4": V4}


def vec_ops(prefix, n, bvec):
    vec = prefix
    for name, expr in [
        ("ADD", "a + b"), ("SUB", "a - b"), ("MUL", "a * b"), ("DIV", "a / b"),
    ]:
        OPS.append((vec + "_" + name, [n, n], n, expr))
    OPS.append((vec + "_SCALE", [n, S], n, "a * s"))
    OPS.append((vec + "_DIV_S", [n, S], n, "a / s"))
    OPS.append((vec + "_NEG", [n], n, "-a"))
    OPS.append((vec + "_DOT", [n, n], S, "glm::dot(a, b)"))
    OPS.append((vec + "_LENGTH", [n], S, "glm::length(a)"))
    OPS.append((vec + "_DISTANCE", [n, n], S, "glm::distance(a, b)"))
    OPS.append((vec + "_NORMALIZE", [n], n, "glm::normalize(a)"))
    OPS.append((vec + "_CLAMP", [n, S, S], n, "glm::clamp(a, s, t)"))
    OPS.append((vec + "_CLAMP_V", [n, n, n], n, "glm::clamp(a, b, c)"))
    OPS.append((vec + "_MIX", [n, n, S], n, "glm::mix(a, b, s)"))
    OPS.append((vec + "_MIX_V", [n, n, n], n, "glm::mix(a, b, c)"))
    OPS.append((vec + "_MIN", [n, n], n, "glm::min(a, b)"))
    OPS.append((vec + "_MAX", [n, n], n, "glm::max(a, b)"))
    OPS.append((vec + "_MIN_S", [n, S], n, "glm::min(a, s)"))
    OPS.append((vec + "_MAX_S", [n, S], n, "glm::max(a, s)"))
    OPS.append((vec + "_REFLECT", [n, n], n, "glm::reflect(a, b)"))
    OPS.append((vec + "_REFRACT", [n, n, S], n, "glm::refract(a, b, s)"))
    OPS.append((vec + "_FACEFORWARD", [n, n, n], n, "glm::faceforward(a, b, c)"))
    OPS.append((vec + "_STEP", [n, n], n, "glm::step(a, b)"))
    OPS.append((vec + "_STEP_S", [S, n], n, "glm::step(s, a)"))
    OPS.append((vec + "_SMOOTHSTEP", [S, S, n], n, "glm::smoothstep(s, t, a)"))
    OPS.append((vec + "_SMOOTHSTEP_V", [n, n, n], n, "glm::smoothstep(a, b, c)"))
    zero = "glm::{t}(0.0f)".format(t=prefix.lower())
    OPS.append((vec + "_ANY", [n], B, "glm::any(glm::notEqual(a, {z}))".format(z=zero)))
    OPS.append((vec + "_ALL", [n], B, "glm::all(glm::notEqual(a, {z}))".format(z=zero)))
    OPS.append((vec + "_EQUAL", [n, n], B, "glm::all(glm::equal(a, b))"))
    OPS.append((vec + "_NOT_EQUAL", [n, n], B, "glm::any(glm::notEqual(a, b))"))
    for name, fn in [
        ("LESS", "lessThan"), ("LESS_EQUAL", "lessThanEqual"),
        ("GREATER", "greaterThan"), ("GREATER_EQUAL", "greaterThanEqual"),
    ]:
        OPS.append((vec + "_" + name, [n, n], bvec, "glm::{f}(a, b)".format(f=fn)))
    for f in ["abs", "sign", "floor", "ceil", "fract", "trunc", "round",
              "roundEven", "sqrt", "inversesqrt", "sin", "cos", "tan",
              "asin", "acos", "atan", "sinh", "cosh", "tanh", "exp", "log",
              "exp2", "log2", "radians", "degrees"]:
        unary(vec + "_" + f.upper(), f, n)
    binary(vec + "_ATAN2", "atan", n)
    binary(vec + "_POW", "pow", n)
    binary(vec + "_MOD", "mod", n)
    OPS.append((vec + "_MOD_S", [n, S], n, "glm::mod(a, s)"))
    OPS.append((vec + "_FMA", [n, n, n], n, "glm::fma(a, b, c)"))
    OPS.append((vec + "_IS_NAN", [n], bvec, "glm::isnan(a)"))
    OPS.append((vec + "_IS_INF", [n], bvec, "glm::isinf(a)"))


vec_ops("VEC2", V2, BV2)
vec_ops("VEC3", V3, BV3)
vec_ops("VEC4", V4, BV4)
OPS.append(("VEC3_CROSS", [V3, V3], V3, "glm::cross(a, b)"))
OPS.append(("VEC2_FROM", [S, S], V2, "glm::vec2(s, t)"))
OPS.append(("VEC3_FROM", [S, S, S], V3, "glm::vec3(s, t, u)"))
OPS.append(("VEC4_FROM", [S, S, S, S], V4, "glm::vec4(s, t, u, v)"))

# ---- matrices -------------------------------------------------------------


def mat_ops(prefix, n, vecn, b):
    OPS.append((prefix + "_IDENTITY", [], n, "glm::{t}(1.0f)".format(t=b)))
    OPS.append((prefix + "_ZERO", [], n, "glm::{t}(0.0f)".format(t=b)))
    for name, expr in [("ADD", "a + b"), ("SUB", "a - b"), ("MUL", "a * b")]:
        OPS.append((prefix + "_" + name, [n, n], n, expr))
    OPS.append((prefix + "_MUL_VEC", [n, vecn], vecn, "a * b"))
    OPS.append((prefix + "_MUL_S", [n, S], n, "a * s"))
    OPS.append((prefix + "_DIV_S", [n, S], n, "a / s"))
    OPS.append((prefix + "_NEG", [n], n, "-a"))
    OPS.append((prefix + "_TRANSPOSE", [n], n, "glm::transpose(a)"))
    OPS.append((prefix + "_INVERSE", [n], n, "glm::inverse(a)"))
    OPS.append((prefix + "_DETERMINANT", [n], S, "glm::determinant(a)"))
    diag = {"MAT2": "a[0][0] + a[1][1]", "MAT3": "a[0][0] + a[1][1] + a[2][2]", "MAT4": "a[0][0] + a[1][1] + a[2][2] + a[3][3]"}[prefix]
    OPS.append((prefix + "_TRACE", [n], S, diag))
    OPS.append((prefix + "_COMP_MUL", [n, n], n, "glm::matrixCompMult(a, b)"))


mat_ops("MAT2", M2, V2, "mat2")
mat_ops("MAT3", M3, V3, "mat3")
mat_ops("MAT4", M4, V4, "mat4")
OPS.append(("MAT2_FROM", [S, S, S, S], M2, "glm::mat2(s, t, u, v)"))
OPS.append(("MAT3_FROM", [S] * 9, M3, "glm::mat3(s, t, u, v, w, x, y, z, aa)"))
OPS.append(("MAT4_FROM", [S] * 16, M4,
            "glm::mat4(s, t, u, v, w, x, y, z, aa, ab, ac, ad, ae, af, ag, ah)"))
OPS.append(("MAT3_TO_MAT4", [M3], M4, "glm::mat4(a)"))
OPS.append(("MAT4_TO_MAT3", [M4], M3, "glm::mat3(a)"))

# ---- transforms (mat4 output) ---------------------------------------------

OPS.append(("MAT4_TRANSLATE", [M4, V3], M4, "glm::translate(a, b)"))
OPS.append(("MAT4_TRANSLATE_XYZ", [M4, S, S, S], M4, "glm::translate(a, glm::vec3(s, t, u))"))
OPS.append(("MAT4_ROTATE", [M4, S, S, S, S], M4, "glm::rotate(a, s, glm::vec3(t, u, v))"))
OPS.append(("MAT4_ROTATE_X", [M4, S], M4, "glm::rotate(a, s, glm::vec3(1.0f, 0.0f, 0.0f))"))
OPS.append(("MAT4_ROTATE_Y", [M4, S], M4, "glm::rotate(a, s, glm::vec3(0.0f, 1.0f, 0.0f))"))
OPS.append(("MAT4_ROTATE_Z", [M4, S], M4, "glm::rotate(a, s, glm::vec3(0.0f, 0.0f, 1.0f))"))
OPS.append(("MAT4_SCALE", [M4, V3], M4, "glm::scale(a, b)"))
OPS.append(("MAT4_SCALE_XYZ", [M4, S, S, S], M4, "glm::scale(a, glm::vec3(s, t, u))"))
OPS.append(("MAT4_PERSPECTIVE", [S, S, S, S], M4, "glm::perspective(s, t, u, v)"))
OPS.append(("MAT4_PERSPECTIVE_LH", [S, S, S, S], M4, "glm::perspectiveLH(s, t, u, v)"))
OPS.append(("MAT4_PERSPECTIVE_RH", [S, S, S, S], M4, "glm::perspectiveRH(s, t, u, v)"))
OPS.append(("MAT4_ORTHO", [S, S, S, S, S, S], M4, "glm::ortho(s, t, u, v, w, x)"))
OPS.append(("MAT4_ORTHO_LH", [S, S, S, S, S, S], M4, "glm::orthoLH(s, t, u, v, w, x)"))
OPS.append(("MAT4_ORTHO_RH", [S, S, S, S, S, S], M4, "glm::orthoRH(s, t, u, v, w, x)"))
OPS.append(("MAT4_FRUSTUM", [S, S, S, S, S, S], M4, "glm::frustum(s, t, u, v, w, x)"))
OPS.append(("MAT4_FRUSTUM_LH", [S, S, S, S, S, S], M4, "glm::frustumLH(s, t, u, v, w, x)"))
OPS.append(("MAT4_FRUSTUM_RH", [S, S, S, S, S, S], M4, "glm::frustumRH(s, t, u, v, w, x)"))
OPS.append(("MAT4_LOOK_AT", [V3, V3, V3], M4, "glm::lookAt(a, b, c)"))
OPS.append(("MAT4_LOOK_AT_LH", [V3, V3, V3], M4, "glm::lookAtLH(a, b, c)"))
OPS.append(("MAT4_LOOK_AT_RH", [V3, V3, V3], M4, "glm::lookAtRH(a, b, c)"))

# ---- quaternions ----------------------------------------------------------

OPS.append(("QUAT_IDENTITY", [], Q, "glm::quat(1.0f, 0.0f, 0.0f, 0.0f)"))
for name, expr in [("ADD", "a + b"), ("SUB", "a - b"), ("MUL", "a * b")]:
    OPS.append(("QUAT_" + name, [Q, Q], Q, expr))
OPS.append(("QUAT_MUL_VEC3", [Q, V3], V3, "a * b"))
OPS.append(("QUAT_SCALE", [Q, S], Q, "a * s"))
OPS.append(("QUAT_NEG", [Q], Q, "-a"))
OPS.append(("QUAT_CONJUGATE", [Q], Q, "glm::conjugate(a)"))
OPS.append(("QUAT_INVERSE", [Q], Q, "glm::inverse(a)"))
OPS.append(("QUAT_NORMALIZE", [Q], Q, "glm::normalize(a)"))
OPS.append(("QUAT_DOT", [Q, Q], S, "glm::dot(a, b)"))
OPS.append(("QUAT_LENGTH", [Q], S, "glm::length(a)"))
OPS.append(("QUAT_CROSS", [Q, Q], Q, "glm::cross(a, b)"))
OPS.append(("QUAT_LERP", [Q, Q, S], Q, "glm::lerp(a, b, s)"))
OPS.append(("QUAT_SLERP", [Q, Q, S], Q, "glm::slerp(a, b, s)"))
OPS.append(("QUAT_AXIS_ANGLE", [S, V3], Q, "glm::angleAxis(s, a)"))
OPS.append(("QUAT_ANGLE", [Q], S, "glm::angle(a)"))
OPS.append(("QUAT_AXIS", [Q], V3, "glm::axis(a)"))
OPS.append(("QUAT_ROTATE", [Q, S, S, S, S], Q, "glm::rotate(a, s, glm::vec3(t, u, v))"))
OPS.append(("QUAT_PITCH", [Q], S, "glm::pitch(a)"))
OPS.append(("QUAT_YAW", [Q], S, "glm::yaw(a)"))
OPS.append(("QUAT_ROLL", [Q], S, "glm::roll(a)"))
OPS.append(("QUAT_EULER_ANGLES", [Q], V3, "glm::eulerAngles(a)"))
OPS.append(("QUAT_TO_MAT4", [Q], M4, "glm::mat4_cast(a)"))
OPS.append(("QUAT_TO_MAT3", [Q], M3, "glm::mat3_cast(a)"))
OPS.append(("MAT4_TO_QUAT", [M4], Q, "glm::quat_cast(a)"))
OPS.append(("MAT3_TO_QUAT", [M3], Q, "glm::quat_cast(a)"))
OPS.append(("QUAT_FROM", [S, S, S, S], Q, "glm::quat(s, t, u, v)"))


# ---------------------------------------------------------------------------
# C++ generation
# ---------------------------------------------------------------------------

def cpp_read(kind, name):
    if kind in READ:
        n = SIZE[kind]
        return READ[kind].format(n=name, s=n, b=n * 4)
    return ""


def cpp_write(kind, call):
    return WRITE[kind].format(call=call)


def gen_cpp(major, minor, patch):
    lines = []
    ap = lines.append
    ap("// Generated by tools/gen_ops.py -- do not edit by hand.")
    ap("")
    ap("#define GLM_VERSION_NUMBER (({m} << 16) | ({i} << 8) | {p})".format(m=major, i=minor, p=patch))
    ap("#include <cstring>")
    ap("#include <glm/glm.hpp>")
    ap("#include <glm/gtc/quaternion.hpp>")
    ap("#include <glm/ext/matrix_transform.hpp>")
    ap("#include <glm/ext/matrix_clip_space.hpp>")
    ap('#include "glm_wrapper.h"')
    ap("")
    ap("enum : int {")
    for name, *_ in OPS:
        ap("    OP_{n},".format(n=name))
    ap("    OP_COUNT,")
    ap("};")
    ap("")
    ap("namespace {")
    ap("struct Input {")
    ap("    const float* p;")
    ap("    explicit Input(const float* ptr) : p(ptr) {}")
    ap("    float next() { float v = *p; ++p; return v; }")
    ap("    const float* next(int n) { const float* r = p; p += n; return r; }")
    ap("};")
    ap("}")
    ap("")
    ap("extern \"C\" int glm_op_count(void) { return OP_COUNT; }")
    ap("")
    ap("extern \"C\" int glm_version(void) { return GLM_VERSION_NUMBER; }")
    ap("")
    ap("extern \"C\" void glm_call(int op, const float* args, int arg_count, float* out, int out_count) {")
    ap("    (void)arg_count; (void)out_count;")
    ap("    Input e(args);")
    ap("    switch (op) {")
    for name, args, out_kind, call in OPS:
        ap("    case OP_{n}: {{".format(n=name))
        vi = iter(["a", "b", "c", "d", "e2", "f2"])
        si = iter(["s", "t", "u", "v", "w", "x", "y", "z", "aa", "ab", "ac", "ad", "ae", "af", "ag", "ah"])
        for k in args:
            n = next(si) if k == S else next(vi)
            r = cpp_read(k, n)
            if r:
                ap("        " + r)
        ap("        " + cpp_write(out_kind, call))
        ap("        break;")
        ap("    }")
    ap("    default:")
    ap("        break;")
    ap("    }")
    ap("}")
    return "\n".join(lines) + "\n"


# ---------------------------------------------------------------------------
# Kotlin generation
# ---------------------------------------------------------------------------

def gen_kt():
    lines = []
    ap = lines.append
    ap("// Generated by tools/gen_ops.py -- do not edit by hand.")
    ap("package cn.enaium.glm")
    ap("")
    ap("/**")
    ap(" * Operation codes for the GLM C ABI dispatcher (glm_call).")
    ap(" *")
    ap(" * @property inSize number of input floats packed into the args array")
    ap(" * @property outSize number of output floats written into the out array")
    ap(" */")
    ap("internal enum class Op(val inSize: Int, val outSize: Int) {")
    for name, args, out_kind, _ in OPS:
        in_size = sum(SIZE[k] for k in args)
        out_size = SIZE[out_kind]
        ap("    {n}({i}, {o}),".format(n=name, i=in_size, o=out_size))
    ap("    ;")
    ap("}")
    return "\n".join(lines) + "\n"


def read_glm_version():
    setup = os.path.join(ROOT, "glm", "glm", "detail", "setup.hpp")
    with open(setup) as f:
        text = f.read()
    major = re.search(r"#define GLM_VERSION_MAJOR (\d+)", text).group(1)
    minor = re.search(r"#define GLM_VERSION_MINOR (\d+)", text).group(1)
    patch = re.search(r"#define GLM_VERSION_PATCH (\d+)", text).group(1)
    return major, minor, patch


def main():
    major, minor, patch = read_glm_version()
    os.makedirs(C_API_DIR, exist_ok=True)
    with open(os.path.join(C_API_DIR, "glm_wrapper.cpp"), "w") as f:
        f.write(
            "// GLM {m}.{i}.{p} ({n} ops) -- generated by tools/gen_ops.py.\n\n".format(
                m=major, i=minor, p=patch, n=len(OPS))
            + gen_cpp(major, minor, patch)
        )
    os.makedirs(os.path.dirname(OPS_KT), exist_ok=True)
    with open(OPS_KT, "w") as f:
        f.write(gen_kt())
    print("generated glm_wrapper.cpp ({} ops, GLM {}.{}.{}) and Ops.kt".format(
        len(OPS), major, minor, patch))


if __name__ == "__main__":
    main()
