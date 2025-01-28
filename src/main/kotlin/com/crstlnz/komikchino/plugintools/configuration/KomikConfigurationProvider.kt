package com.crstlnz.komikchino.plugintools.configuration

import com.crstlnz.komikchino.plugintools.ApkInfo
import com.crstlnz.komikchino.plugintools.createProgressLogger
import com.crstlnz.komikchino.plugintools.download
import com.crstlnz.komikchino.plugintools.getKomik
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.net.URI

class KomikConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "komik"

    override fun provide(project: Project, dependency: Dependency) {
        val extension = project.extensions.getKomik()
        if (extension.apkinfo == null) {
            extension.apkinfo = ApkInfo(extension, dependency.version ?: "pre-release")
        }
        val apkinfo = extension.apkinfo!!

        apkinfo.cache.mkdirs()

        if (!apkinfo.jarFile.exists()) {
            project.logger.lifecycle("Fetching JAR")

            val url = URI("${apkinfo.urlPrefix}/classes.jar").toURL()
            url.download(apkinfo.jarFile, createProgressLogger(project, "Download JAR"))
        }

        project.dependencies.add("compileOnly", project.files(apkinfo.jarFile))
    }
}