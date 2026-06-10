plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "com.example.trainy.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore)
}
