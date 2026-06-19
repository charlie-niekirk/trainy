plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
}

android {
    namespace = "me.cniekirk.trainy.core.network"

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

    sourceSets {
        getByName("debug") {
            kotlin.srcDir("src/realNetwork/java")
        }
        getByName("release") {
            kotlin.srcDir("src/realNetwork/java")
        }
        getByName("benchmarkRelease") {
            kotlin.srcDir("src/benchmarkNetwork/java")
        }
        getByName("nonMinifiedRelease") {
            kotlin.srcDir("src/benchmarkNetwork/java")
        }
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
}
