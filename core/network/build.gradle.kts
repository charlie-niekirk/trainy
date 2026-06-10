plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "com.example.trainy.core.network"
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
}
