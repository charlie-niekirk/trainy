plugins {
    id("trainy.android.library")
    id("trainy.android.compose")
    id("trainy.kotlin.serialization")
}

android {
    namespace = "me.cniekirk.trainy.feature.search"
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
}
