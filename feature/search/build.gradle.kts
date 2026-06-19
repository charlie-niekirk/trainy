plugins {
    id("trainy.android.library")
    id("trainy.android.compose")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.feature.search"
}

dependencies {
    implementation(project(":feature:station-search"))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.metrox.viewmodel.compose)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
