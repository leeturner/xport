package com.leeturner.xport.cli

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.env.Environment
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.writeText

@Suppress("detekt:ForbiddenComment")
class XportCommandTest {
    val environments = listOf(Environment.CLI, Environment.TEST)

    @TempDir lateinit var archiveDir: Path

    @TempDir lateinit var outputDir: Path

    @Test
    fun `cli returns an error if the output directory does not exist`() {
        val outputStream = ByteArrayOutputStream()
        System.setErr(PrintStream(outputStream))

        val args = arrayOf(archiveDir.absolutePathString(), "${outputDir.absolutePathString()}/does-not-exist", "-v")
        val exitCode = PicocliRunner.call(XportCommand::class.java, *args)

        expectThat(exitCode).isEqualTo(1)
        expectThat(outputStream.toString()).contains("The output directory does not exist. This directory must exist and be empty")
    }

    @Test
    fun `cli returns an error if the output directory exists but is not empty`() {
        val outputStream = ByteArrayOutputStream()
        System.setErr(PrintStream(outputStream))

        val testFile = outputDir.resolve("text.txt").createFile()
        testFile.writeText("text")

        val args = arrayOf(archiveDir.absolutePathString(), outputDir.absolutePathString(), "-v")
        val exitCode = PicocliRunner.call(XportCommand::class.java, *args)

        expectThat(exitCode).isEqualTo(1)
        expectThat(outputStream.toString()).contains("The output directory does not exist. This directory must exist and be empty")
    }

    @Test
    fun `cli returns an error if the archive directory does not exist`() {
        val outputStream = ByteArrayOutputStream()
        System.setErr(PrintStream(outputStream))

        val args = arrayOf("${archiveDir.absolutePathString()}/does-not-exist", outputDir.absolutePathString(), "-v")
        val exitCode = PicocliRunner.call(XportCommand::class.java, *args)

        expectThat(exitCode).isEqualTo(1)
        expectThat(
            outputStream.toString(),
        ).contains("The archive directory does not exist. This directory must exist and contain the data directory")
    }

    @Test
    fun `cli returns an error if the archive directory does not contain the data directory`() {
        val outputStream = ByteArrayOutputStream()
        System.setErr(PrintStream(outputStream))

        val args = arrayOf(archiveDir.absolutePathString(), outputDir.absolutePathString(), "-v")
        val exitCode = PicocliRunner.call(XportCommand::class.java, *args)

        expectThat(exitCode).isEqualTo(1)
        expectThat(
            outputStream.toString(),
        ).contains("The archive directory does not exist. This directory must exist and contain the data directory")
    }

    @Test
    @Disabled("Not implemented yet")
    fun testWithCommandLineOption() {
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        val args = arrayOf(archiveDir.absolutePathString(), outputDir.absolutePathString(), "-v")
        val exitCode = PicocliRunner.execute(XportCommand::class.java, environments, *args)

        expectThat(exitCode).isEqualTo(0)
        expectThat(outputStream.toString()).contains("Hi!")
    }

    // TODO: test verbose is passed in the context to the pipeline
}
