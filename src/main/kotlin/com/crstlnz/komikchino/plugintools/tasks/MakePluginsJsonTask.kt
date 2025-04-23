package com.crstlnz.komikchino.plugintools.tasks

import com.crstlnz.komikchino.plugintools.KomikExtension
import com.crstlnz.komikchino.plugintools.findKomik
import com.crstlnz.komikchino.plugintools.makePluginEntry
import com.crstlnz.komikchino.plugintools.entities.PluginEntry
import com.crstlnz.komikchino.plugintools.findProvider
import com.crstlnz.komikchino.plugintools.getKomik
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.LinkedList
import java.lang.Thread

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
        val providerInfo = extensions.findProvider()
        if(providerInfo != null && komik != null) {
            providerInfo.pluginLists = listOf(
                komik.repository!!.getRawLink(repoOutputFile.get().asFile.name, komik.buildBranch)
            )
            repoOutputFile.asFile.get().writeText(
                JsonBuilder(
                    providerInfo,
                    JsonGenerator.Options()
                        .excludeNulls()
                        .build()
                ).toPrettyString()
            )
        }
    }
}