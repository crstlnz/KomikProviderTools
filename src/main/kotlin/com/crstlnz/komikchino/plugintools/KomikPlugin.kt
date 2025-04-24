package com.crstlnz.komikchino.plugintools

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.crstlnz.komikchino.plugintools.tasks.registerTasks
import com.crstlnz.komikchino.plugintools.configuration.registerConfigurations

abstract class KomikPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("komik", KomikExtension::class.java, project)
        project.extensions.create("providerinfo", ProviderInfo::class.java, project)

        registerTasks(project)
        registerConfigurations(project)
    }
}