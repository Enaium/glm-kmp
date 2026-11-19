plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = "cn.enaium.glm"
    // <module-version>-fixed.<original-library-version>; the module version
    // follows its own semver line (last: 1.0.1, revision bump: 1.0.2).
    version = "1.0.2-fixed.1.0.3"
}
