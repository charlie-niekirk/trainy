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

            val ktfmtVersion =
                extensions
                    .findByType(VersionCatalogsExtension::class.java)
                    ?.named("libs")
                    ?.findVersion("ktfmt")
                    ?.map { it.requiredVersion }
                    ?.orElse(DEFAULT_KTFMT_VERSION) ?: DEFAULT_KTFMT_VERSION

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target(*kotlinTargets())
                    targetExclude("**/build/**/*.kt")
                    ktfmt(ktfmtVersion).kotlinlangStyle()
                    trimTrailingWhitespace()
                    endWithNewline()
                }

                kotlinGradle {
                    target(*kotlinGradleTargets())
                    ktfmt(ktfmtVersion).kotlinlangStyle()
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
        const val DEFAULT_KTFMT_VERSION = "0.63"
    }
}
