plugins {
    id("trainy.android.library")
    id("trainy.android.compose")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.feature.stationdetails"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.metrox.viewmodel.compose) {
        exclude(group = "org.jetbrains.compose.ui")
        exclude(group = "org.jetbrains.compose.runtime")
    }
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
