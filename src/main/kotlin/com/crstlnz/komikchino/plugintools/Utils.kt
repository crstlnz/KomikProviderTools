package com.crstlnz.komikchino.plugintools

import org.gradle.api.Project
import com.crstlnz.komikchino.plugintools.entities.*
import org.gradle.api.plugins.ExtensionContainer

fun Project.makeManifest(): PluginManifest {
    val extension = this.extensions.getKomik()

    require(extension.pluginClassName != null) {
        "No plugin class found, make sure your plugin class is annotated with @KomikPlugin"
    }

    val version = this.version.toString().toIntOrNull(10)
    if (version == null) {
        logger.warn("'${project.version}' is not a valid version. Use an integer.")
    }

    return PluginManifest(
        pluginClassName = extension.pluginClassName,
        name = this.name,
        version = version ?: -1,
        requiresResources = extension.requiresResources
    )
}

fun Project.makePluginEntry(): PluginEntry {
    val extension = this.extensions.getKomik()
    val provider = this.extensions.getProvider()

    val version = this.version.toString().toIntOrNull(10)
    if (version == null) {
        logger.warn("'${project.version}' is not a valid version. Use an integer.")
    }

    val repo = extension.repository

    return PluginEntry(
        url = repo?.getRawLink("${this.name}.kc", extension.buildBranch) ?: "",
        status = extension.status,
        version = version ?: -1,
        name = this.name,
        internalName = this.name,
        authors = extension.authors,
        description = extension.description,
        repositoryUrl = (repo?.url),
        language = extension.language,
        iconUrl = extension.iconUrl,
        apiVersion = extension.apiVersion,
        fileSize = extension.fileSize,
        repoId = provider?.id ?: "",
    )
}

fun Project.makeRepoJson(extension: ExtensionContainer): ProviderInfoData {
    val providerInfo = extension.getProvider()

    return ProviderInfoData(
        name = providerInfo.name,
        description = providerInfo.description,
        manifestVersion = providerInfo.manifestVersion,
        pluginLists = providerInfo.pluginLists
    )
}