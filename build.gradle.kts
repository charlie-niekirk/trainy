// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("trainy.spotless")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.room) apply false
}

subprojects {
    apply(plugin = "trainy.spotless")
}
