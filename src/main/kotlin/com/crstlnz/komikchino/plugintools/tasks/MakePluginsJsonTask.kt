package com.crstlnz.komikchino.plugintools.tasks

import com.crstlnz.komikchino.plugintools.KomikExtension
import com.crstlnz.komikchino.plugintools.findKomik
import com.crstlnz.komikchino.plugintools.makePluginEntry
import com.crstlnz.komikchino.plugintools.entities.PluginEntry
import com.crstlnz.komikchino.plugintools.findProvider
import com.crstlnz.komikchino.plugintools.getKomik
import com.crstlnz.komikchino.plugintools.makeRepoJson
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.LinkedList

abstract class MakePluginsJsonTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:OutputFile
    abstract val repoOutputFile: RegularFileProperty

    @TaskAction
    fun makePluginsJson() {
        val lst = LinkedList<PluginEntry>();
        var komik: KomikExtension? = null

        for (subproject in project.allprojects) {
            subproject.extensions.findKomik() ?: continue
            komik = subproject.extensions.getKomik()

            lst.add(subproject.makePluginEntry())
        }

        outputFile.asFile.get().writeText(
            JsonBuilder(
                lst,
                JsonGenerator.Options()
                    .excludeNulls()
                    .build()
            ).toPrettyString()
        )

        logger.lifecycle("Created ${outputFile.asFile.get()}")

        // create repo.json file
        val providerInfo = project.extensions.findProvider()
        if (providerInfo == null) {
            logger.lifecycle("provider info not provided!")
        }
        if (komik == null) {
            logger.lifecycle("komik not provided!")
        }

        if (providerInfo != null && komik != null) {
            logger.lifecycle("Creating repo.json")
            try {
                val link = komik.repository!!.getRawLink(
                    repoOutputFile.get().asFile.name,
                    komik.buildBranch
                )

                val providerInfoData = project.makeRepoJson(project.extensions)
                providerInfoData.pluginLists = listOf(link)
                logger.lifecycle("Raw link : $link")

                repoOutputFile.asFile.get().writeText(
                    JsonBuilder(
                        providerInfoData,
                        JsonGenerator.Options()
                            .excludeNulls()
                            .build()
                    ).toPrettyString()
                )
                logger.lifecycle("Created ${repoOutputFile.asFile.get()}")
            } catch (e: Throwable) {
                logger.error(e.stackTraceToString())
                logger.lifecycle("Failed to create repo.json")
            }
        }
    }
}