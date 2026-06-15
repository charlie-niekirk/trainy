plugins {
    id("trainy.android.library")
    id("trainy.kotlin.serialization")
    id("trainy.metro")
    id("org.openapi.generator") version "7.23.0"
}

val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")
val generatedOpenApiSources =
    layout.buildDirectory.dir("generated/openapi/src/main/kotlin").get().asFile

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
        getByName("main") {
            kotlin.srcDir(generatedOpenApiSources)
        }
        getByName("debug") {
            java.srcDir("src/realNetwork/java")
        }
        getByName("release") {
            java.srcDir("src/realNetwork/java")
        }
        getByName("benchmarkRelease") {
            java.srcDir("src/benchmarkNetwork/java")
        }
        getByName("nonMinifiedRelease") {
            java.srcDir("src/benchmarkNetwork/java")
        }
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("https://api.cniekirk.online/docs/json")
    outputDir.set(generatedOpenApiDir)
    packageName.set("me.cniekirk.trainy.core.network.generated")
    modelPackage.set("me.cniekirk.trainy.core.network.generated.model")
    typeMappings.set(
        mapOf(
            "AnyType" to "JsonElement",
            "object" to "JsonObject",
        )
    )
    importMappings.set(
        mapOf(
            "JsonElement" to "kotlinx.serialization.json.JsonElement",
            "JsonObject" to "kotlinx.serialization.json.JsonObject",
        )
    )
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    globalProperties.set(
        mapOf(
            "apis" to "false",
            "models" to "",
            "supportingFiles" to "false",
        )
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "string",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "kotlinx_serialization",
            "sourceFolder" to "src/main/kotlin",
        )
    )
}

tasks
    .matching {
        (it.name.startsWith("compile") && it.name.endsWith("Kotlin")) ||
            (it.name.startsWith("extract") && it.name.endsWith("Annotations"))
    }
    .configureEach {
        dependsOn(tasks.named("openApiGenerate"))
    }

dependencies {
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
}
