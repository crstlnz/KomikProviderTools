package com.crstlnz.komikchino.plugintools.configuration

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

// Deprecated
class ApkConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "apk"

    override fun provide(project: Project, dependency: Dependency) {
        KomikConfigurationProvider().provide(project, dependency)
    }
}