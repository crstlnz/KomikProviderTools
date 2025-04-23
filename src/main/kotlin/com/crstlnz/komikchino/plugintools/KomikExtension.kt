package com.crstlnz.komikchino.plugintools

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonProperty
import javax.inject.Inject

abstract class ProviderInfo @Inject constructor(project: Project) {
    @JsonProperty("name") val name: String = ""
    @JsonProperty("description") val description: String = ""
    @JsonProperty("manifestVersion") val manifestVersion: Int = 1
    @JsonProperty("pluginLists")
    var pluginLists: List<String> = listOf()
}

abstract class KomikExtension @Inject constructor(project: Project) {
    val userCache = project.gradle.gradleUserHomeDir.resolve("caches").resolve("komik")

    val apiVersion = 1

    var apkinfo: ApkInfo? = null
        internal set

    var repository: Repo? = null
        internal set

    var buildBranch: String = "builds"

    fun overrideUrlPrefix(url: String) {
        if (apkinfo == null) {
            apkinfo = ApkInfo(this, "pre-release")
        }
        apkinfo!!.urlPrefix = url
    }

    fun setRepo(user: String, repo: String, url: String, rawLinkFormat: String) {
        repository = Repo(user, repo, url, rawLinkFormat)
    }
    fun setRepo(user: String, repo: String, type: String) {
        when {
            type == "github" -> setRepo(user, repo, "https://github.com/${user}/${repo}", "https://raw.githubusercontent.com/${user}/${repo}/%branch%/%filename%")
            type == "gitlab" -> setRepo(user, repo, "https://gitlab.com/${user}/${repo}", "https://gitlab.com/${user}/${repo}/-/raw/%branch%/%filename%")
            type == "codeberg" -> setRepo(user, repo, "https://codeberg.org/${user}/${repo}", "https://codeberg.org/${user}/${repo}/raw/branch/%branch%/%filename%")
            type.startsWith("gitlab-") -> {
                val domain = type.removePrefix("gitlab-")
                setRepo(user, repo, "https://${domain}/${user}/${repo}", "https://${domain}/${user}/${repo}/-/raw/%branch%/%filename%")
            }
            type.startsWith("gitea-") -> {
                val domain = type.removePrefix("gitea-")
                setRepo(user, repo, "https://${domain}/${user}/${repo}", "https://${domain}/${user}/${repo}/raw/branch/%branch%/%filename%")
            }
            else -> throw IllegalArgumentException("Unknown type ${type}. Use github, gitlab, gitlab-<domain> or gitea-<domain> or set repository via setRepo(user, repo, url, rawLinkFormat)")
        }
    }
    fun setRepo(url: String) {
        var type: String? = null

        var split = when {
             url.startsWith("https://github.com") -> {
                type = "github"
                   url
                    .removePrefix("https://")
                    .removePrefix("github.com")
            }
            url.startsWith("https://gitlab.com") -> {
                type = "gitlab"
                url
                    .removePrefix("https://")
                    .removePrefix("gitlab.com")
            }
            url.startsWith("https://codeberg.org") -> {
                type = "codeberg"
                url
                    .removePrefix("https://")
                    .removePrefix("codeberg.org")
            }
            !url.startsWith("https://") -> { // assume default as github
                type = "github"
                url
            }
            else -> throw IllegalArgumentException("Unknown domain, please set repository via setRepo(user, repo, type)")
        }
            .removePrefix("/")
            .removeSuffix("/")
            .split("/")

        setRepo(split[0], split[1], type)
    }

    internal var pluginClassName: String? = null
    internal var fileSize: Long? = null

    var requiresResources = false
    var description: String? = null
    var authors = listOf<String>()
    var status = 3
    var language: String? = null
    var iconUrl: String? = null
}

class ApkInfo(extension: KomikExtension, release: String) {
    val cache = extension.userCache.resolve("komik")

    var urlPrefix = "https://github.com/lockby/crx/releases/download/${release}"
    val jarFile = cache.resolve("komik.jar")
}

class Repo(val user: String, val repo: String, val url: String, val rawLinkFormat: String) {
    fun getRawLink(filename: String, branch: String): String {
        return rawLinkFormat
            .replace("%filename%", filename)
            .replace("%branch%", branch)
    }
}

fun ExtensionContainer.getKomik(): KomikExtension {
    return getByName("komik") as KomikExtension
}

fun ExtensionContainer.findKomik(): KomikExtension? {
    return findByName("komik") as KomikExtension?
}

fun ExtensionContainer.getProvider(): ProviderInfo {
    return getByName("komik") as ProviderInfo
}

fun ExtensionContainer.findProvider(): ProviderInfo? {
    return findByName("komik") as ProviderInfo?
}

