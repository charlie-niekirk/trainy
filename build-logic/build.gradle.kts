plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.androidx.room.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.metro.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "trainy.android.application"
            implementationClass = "com.example.trainy.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "trainy.android.library"
            implementationClass = "com.example.trainy.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "trainy.android.compose"
            implementationClass = "com.example.trainy.AndroidComposeConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "trainy.kotlin.serialization"
            implementationClass = "com.example.trainy.KotlinSerializationConventionPlugin"
        }
        register("metro") {
            id = "trainy.metro"
            implementationClass = "com.example.trainy.MetroConventionPlugin"
        }
        register("room") {
            id = "trainy.room"
            implementationClass = "com.example.trainy.RoomConventionPlugin"
        }
    }
}
