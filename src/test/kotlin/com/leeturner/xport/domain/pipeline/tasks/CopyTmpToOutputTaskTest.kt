package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.createDataDirectory
import com.leeturner.xport.createTweetsMediaDirectoryAndAddSampleData
import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.io.File
import java.nio.file.Path

@MicronautTest
class CopyTmpToOutputTaskTest {
    @TempDir lateinit var tmpDir: Path

    @TempDir lateinit var outputDir: Path

    private val worker by lazy { CopyTmpToOutputTask() }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("CopyTmpToOutputTask")
    }

    @Test
    fun `an error is returned when expected outputDirectory parameter are not in the context`() {
        val context = Context(mapOf("tmpDirectory" to "foo"))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called outputDirectory provided. Please provide a outputDirectory parameter in the context"
    }

    @Test
    fun `an error is returned when expected tmpDirectory parameter are not in the context`() {
        val context = Context(mapOf("outputDirectory" to "foo"))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called tmpDirectory provided. Please provide a tmpDirectory parameter in the context"
    }

    @Test
    fun `the tmp directory is copied to the output directory`() {
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tmpDir.toString(),
                    "outputDirectory" to outputDir.toString(),
                ),
            )

        // Create some test files in the tmp directory
        val dataDir = tmpDir.createDataDirectory().toPath()
        dataDir.createTweetsMediaDirectoryAndAddSampleData()

        // Create a test file directly in the tmp directory
        val testFile = File(tmpDir.toFile(), "test.txt")
        testFile.writeText("test content")

        val result = worker.run(context)

        expectThat(result).isSuccess()

        // Verify the files were copied to the output directory
        val outputTestFile = File(outputDir.toFile(), "test.txt")
        expectThat(outputTestFile.exists()).isEqualTo(true)
        expectThat(outputTestFile.readText()).isEqualTo("test content")

        // Verify the data directory was copied
        val outputDataDir = outputDir.resolve("data").toFile()
        expectThat(outputDataDir.exists()).isEqualTo(true)

        // Verify the tweets_media directory was copied
        val outputMediaDir = outputDir.resolve("data").resolve("tweets_media").toFile()
        expectThat(outputMediaDir.exists()).isEqualTo(true)

        // Verify the files in the tweets_media directory were copied
        val outputFile1 = File(outputMediaDir, "test1.jpg")
        val outputFile2 = File(outputMediaDir, "test2.jpg")
        expectThat(outputFile1.exists()).isEqualTo(true)
        expectThat(outputFile1.readText()).isEqualTo("test1 content")
        expectThat(outputFile2.exists()).isEqualTo(true)
        expectThat(outputFile2.readText()).isEqualTo("test2 content")
    }
}
