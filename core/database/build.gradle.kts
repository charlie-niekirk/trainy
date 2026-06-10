plugins {
    id("trainy.android.library")
    id("trainy.room")
    id("trainy.metro")
}

android {
    namespace = "com.example.trainy.core.database"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
