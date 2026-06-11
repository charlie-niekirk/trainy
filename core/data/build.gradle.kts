plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.core.data"

    buildTypes {
        create("benchmarkRelease") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
        }
        create("nonMinifiedRelease") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
        }
    }
}

dependencies {
    api(project(":core:database"))
    api(project(":core:datastore"))
    api(project(":core:network"))

    implementation(libs.kotlinx.coroutines.core)
}
