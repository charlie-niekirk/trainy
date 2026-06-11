plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore)
}
