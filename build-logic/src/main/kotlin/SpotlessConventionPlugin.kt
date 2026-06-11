package me.cniekirk.trainy

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            val ktlintVersion =
                extensions
                    .findByType(VersionCatalogsExtension::class.java)
                    ?.named("libs")
                    ?.findVersion("ktlint")
                    ?.map { it.requiredVersion }
                    ?.orElse(DEFAULT_KTLINT_VERSION)
                    ?: DEFAULT_KTLINT_VERSION

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target(*kotlinTargets())
                    targetExclude("**/build/**/*.kt")
                    ktlint(ktlintVersion).editorConfigOverride(KTLINT_EDITOR_CONFIG_OVERRIDES)
                    trimTrailingWhitespace()
                    endWithNewline()
                }

                kotlinGradle {
                    target(*kotlinGradleTargets())
                    ktlint(ktlintVersion).editorConfigOverride(KTLINT_EDITOR_CONFIG_OVERRIDES)
                    trimTrailingWhitespace()
                    endWithNewline()
                }

                format("xml") {
                    target("src/**/*.xml")
                    targetExclude("**/build/**/*.xml")
                    trimTrailingWhitespace()
                    endWithNewline()
                }

                format("misc") {
                    target(*miscTargets())
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }

    private fun Project.kotlinTargets(): Array<String> =
        if (this == rootProject) {
            arrayOf("src/**/*.kt", "build-logic/src/**/*.kt")
        } else {
            arrayOf("src/**/*.kt")
        }

    private fun Project.kotlinGradleTargets(): Array<String> =
        if (this == rootProject) {
            arrayOf("*.gradle.kts", "build-logic/*.gradle.kts")
        } else {
            arrayOf("*.gradle.kts")
        }

    private fun Project.miscTargets(): Array<String> =
        if (this == rootProject) {
            arrayOf(
                "*.md",
                ".gitignore",
                "*.properties",
                "gradle/**/*.properties",
                "gradle/**/*.toml",
            )
        } else {
            arrayOf("*.md", "*.properties")
        }

    private companion object {
        const val DEFAULT_KTLINT_VERSION = "1.8.0"
        val KTLINT_EDITOR_CONFIG_OVERRIDES =
            mapOf(
                "ktlint_code_style" to "ktlint_official",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            )
    }
}
