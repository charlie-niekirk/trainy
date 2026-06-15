package me.cniekirk.trainy

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                parallel = true
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
                if (this@with != rootProject) {
                    source.setFrom(detektSources())
                }
            }

            tasks.withType<Detekt>().configureEach {
                include("**/*.kt")
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }

    private fun Project.detektSources() =
        if (this == rootProject) {
            files("build-logic/src/main/kotlin")
        } else {
            files(
                "src/main/java",
                "src/main/kotlin",
                "src/test/java",
                "src/test/kotlin",
                "src/androidTest/java",
                "src/androidTest/kotlin",
            )
        }
}
