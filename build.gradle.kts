// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("trainy.spotless")
    id("trainy.detekt")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.room) apply false
}

subprojects {
    apply(plugin = "trainy.spotless")
    apply(plugin = "trainy.detekt")
}

tasks.register("qualityCheck") {
    description = "Runs Kotlin formatting and lint checks."
    group = "verification"
    dependsOn(allprojects.map { it.tasks.named("spotlessCheck") })
    dependsOn(allprojects.map { it.tasks.named("detekt") })
}

tasks.register<Exec>("installGitHooks") {
    description = "Configures Git to use the repository-managed hooks in .githooks."
    group = "verification"
    commandLine("git", "config", "core.hooksPath", ".githooks")
}
