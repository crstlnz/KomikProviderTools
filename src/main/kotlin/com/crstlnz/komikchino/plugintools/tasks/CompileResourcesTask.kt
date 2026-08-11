package com.crstlnz.komikchino.plugintools.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import java.io.File

@CacheableTask
abstract class CompileResourcesTask : Exec() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    abstract val input: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val aaptExecutable: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidJar: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    override fun exec() {
        val aapt = aaptExecutable.asFile.get()
        val android = androidJar.asFile.get()

        val tmpRes = File.createTempFile(
            "res",
            ".zip"
        )

        try {
            execActionFactory.newExecAction().apply {
                executable = aapt.path

                args("compile")
                args("--dir", input.asFile.get().path)
                args("-v")
                args("-o", tmpRes.path)

                execute()
            }

            execActionFactory.newExecAction().apply {
                executable = aapt.path

                args("link")
                args("-I", android.path)
                args("-R", tmpRes.path)
                args(
                    "--manifest",
                    manifestFile.asFile.get().path
                )
                args("--auto-add-overlay")
                args("--warn-manifest-validation")
                args("-v")
                args(
                    "-o",
                    outputFile.asFile.get().path
                )

                execute()
            }
        } finally {
            tmpRes.delete()
        }
    }
}