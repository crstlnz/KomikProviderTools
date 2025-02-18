package com.crstlnz.komikchino.plugintools.configuration

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

interface IConfigurationProvider {
    val name: String

    fun provide(project: Project, dependency: Dependency)
}