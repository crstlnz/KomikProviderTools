package com.crstlnz.komikchino.plugintools.tasks

import com.crstlnz.komikchino.plugintools.getKomik
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.util.function.Function
import java.net.URI
import com.crstlnz.komikchino.plugintools.download
import com.crstlnz.komikchino.plugintools.createProgressLogger
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(
    because = "Downloads source artifacts into the plugin cache"
)
abstract class GenSourcesTask : DefaultTask() {
    @TaskAction
    fun genSources() {
        val extension = project.extensions.getKomik()
        val apkinfo = extension.apkinfo!!

        val sourcesJarFile = apkinfo.cache.resolve("komik-sources.jar")

        val url = URI(
            "${apkinfo.urlPrefix}/app-sources.jar"
        ).toURL()

        url.download(sourcesJarFile, createProgressLogger(project, "Download sources"))
    }
}