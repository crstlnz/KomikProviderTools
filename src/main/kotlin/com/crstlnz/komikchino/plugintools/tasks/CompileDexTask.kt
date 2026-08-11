package com.crstlnz.komikchino.plugintools.tasks

import com.android.builder.dexing.ClassFileInputs
import com.android.builder.dexing.DexArchiveBuilder
import com.android.builder.dexing.DexParameters
import com.android.builder.dexing.r8.ClassFileProviderFactory
import com.crstlnz.komikchino.plugintools.getKomik
import com.google.common.io.Closer
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.CacheableTask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import com.android.build.gradle.internal.errors.MessageReceiverImpl
import com.android.build.gradle.options.SyncOptions.ErrorFormatMode
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.Arrays
import java.util.stream.Collectors

@CacheableTask
abstract class CompileDexTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    val input: ConfigurableFileCollection =
        project.objects.fileCollection()

    @get:InputFiles
    @get:Classpath
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    abstract val bootClasspath: ConfigurableFileCollection

    @get:Input
    abstract val minSdk: Property<Int>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:OutputFile
    abstract val pluginClassFile: RegularFileProperty

    @TaskAction
    fun compileDex() {
        val dexOutputDir = outputFile.get().asFile.parentFile

        Closer.create().use { closer ->

            val dexBuilder = DexArchiveBuilder.createD8DexBuilder(
                DexParameters(
                    minSdkVersion = minSdk.get(),
                    debuggable = true,
                    dexPerClass = false,
                    withDesugaring = true,

                    desugarBootclasspath = ClassFileProviderFactory(
                        bootClasspath.files.map(File::toPath)
                    ).also {
                        closer.register(it)
                    },

                    desugarClasspath = ClassFileProviderFactory(
                        listOf<Path>()
                    ).also {
                        closer.register(it)
                    },

                    coreLibDesugarConfig = null,

                    messageReceiver = MessageReceiverImpl(
                        ErrorFormatMode.HUMAN_READABLE,
                        LoggerFactory.getLogger(
                            CompileDexTask::class.java
                        )
                    ),

                    enableApiModeling = false
                )
            )

            val fileStreams = input
                .map { input ->
                    ClassFileInputs
                        .fromPath(input.toPath())
                        .use {
                            it.entries { _, _ -> true }
                        }
                }
                .toTypedArray()

            Arrays.stream(fileStreams)
                .flatMap { it }
                .use { classesInput ->

                    val files = classesInput.collect(
                        Collectors.toList()
                    )

                    dexBuilder.convert(
                        files.stream(),
                        dexOutputDir.toPath(),
                        null
                    )

                    for (file in files) {
                        val reader = ClassReader(
                            file.readAllBytes()
                        )

                        val classNode = ClassNode()

                        reader.accept(
                            classNode,
                            0
                        )

                        for (
                        annotation in
                        classNode.visibleAnnotations.orEmpty() +
                                classNode.invisibleAnnotations.orEmpty()
                        ) {
                            if (
                                annotation.desc ==
                                "Lcom/crstlnz/komikchino/plugins/KomikPlugin;"
                            ) {
                                val pluginClassName =
                                    classNode.name.replace('/', '.')

                                require(
                                    !pluginClassFile
                                        .asFile
                                        .get()
                                        .exists()
                                ) {
                                    "Only 1 active plugin class per project is supported"
                                }

                                pluginClassFile
                                    .asFile
                                    .get()
                                    .writeText(pluginClassName)

                                break
                            }
                        }
                    }
                }
        }

        logger.lifecycle(
            "Compiled dex to ${outputFile.get()}"
        )
    }
}