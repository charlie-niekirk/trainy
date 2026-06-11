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
    implementation(libs.spotless.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "trainy.android.application"
            implementationClass = "me.cniekirk.trainy.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "trainy.android.library"
            implementationClass = "me.cniekirk.trainy.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "trainy.android.compose"
            implementationClass = "me.cniekirk.trainy.AndroidComposeConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "trainy.kotlin.serialization"
            implementationClass = "me.cniekirk.trainy.KotlinSerializationConventionPlugin"
        }
        register("metro") {
            id = "trainy.metro"
            implementationClass = "me.cniekirk.trainy.MetroConventionPlugin"
        }
        register("room") {
            id = "trainy.room"
            implementationClass = "me.cniekirk.trainy.RoomConventionPlugin"
        }
        register("spotless") {
            id = "trainy.spotless"
            implementationClass = "me.cniekirk.trainy.SpotlessConventionPlugin"
        }
    }
}
