package com.crstlnz.komikchino.plugintools.tasks

import com.crstlnz.komikchino.plugintools.getKomik
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(
    because = "This task deletes the cached APK JAR file"
)
abstract class CleanCacheTask : DefaultTask() {

    @TaskAction
    fun cleanCache() {
        val extension = project.extensions.getKomik()
        val apkinfo = extension.apkinfo ?: return

        if (apkinfo.jarFile.exists()) {
            apkinfo.jarFile.delete()
        }
    }
}