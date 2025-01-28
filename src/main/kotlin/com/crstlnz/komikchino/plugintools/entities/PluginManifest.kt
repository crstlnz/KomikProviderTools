package com.crstlnz.komikchino.plugintools.entities

data class PluginManifest(
    val pluginClassName: String?,
    val name: String,
    val version: Int,
    val requiresResources: Boolean
)