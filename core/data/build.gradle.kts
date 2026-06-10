plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "com.example.trainy.core.data"
}

dependencies {
    api(project(":core:database"))
    api(project(":core:datastore"))
    api(project(":core:network"))

    implementation(libs.kotlinx.coroutines.core)
}
