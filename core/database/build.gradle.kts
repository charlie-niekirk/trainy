plugins {
    id("trainy.android.library")
    id("trainy.room")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.core.database"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
