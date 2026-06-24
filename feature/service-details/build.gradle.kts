plugins {
    id("trainy.android.library")
    id("trainy.android.compose")
    id("trainy.kotlin.serialization")
}

android {
    namespace = "me.cniekirk.trainy.feature.servicedetails"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation3.runtime)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
