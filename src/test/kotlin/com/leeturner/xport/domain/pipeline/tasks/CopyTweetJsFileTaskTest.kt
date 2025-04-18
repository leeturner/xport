package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.createDataDirectory
import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.toFile
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.nio.file.Path

@MicronautTest
class CopyTweetJsFileTaskTest {
    @TempDir lateinit var archiveDir: Path

    @TempDir lateinit var tempDir: Path

    private val worker by lazy { CopyTweetJsFileTask() }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("CopyTweetJsFileTask")
    }

    @Test
    fun `a failure is returned when expected tmpDirectory parameter are not in the context`() {
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
    fun `a failure is returned when expected archiveDirectory parameter are not in the context`() {
        val context = Context(mapOf("currentDirectory" to "foo", "tmpDirectory" to "foo"))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called archiveDirectory provided. Please provide a archiveDirectory parameter in the context"
    }

    @Test
    fun `a failure is returned when the tweet js file does not exist in the current data directory`() {
        val context =
            Context(
                mapOf(
                    "archiveDirectory" to archiveDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        archiveDir.createDataDirectory()

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.js file does not exist in the data directory"
    }

    @Test
    fun `the tweets file is copied from the data directory to the tmp directory`(resourceLoader: ResourceLoader) {
        val context =
            Context(
                mapOf(
                    "archiveDirectory" to archiveDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // given a directory and a file inside current directory
        val dataDirectory = archiveDir.createDataDirectory()

        // Load the file from classpath
        val resourceFile = resourceLoader.toFile("classpath:archive-content/raw-tweet-file-single-tweet.js")
        val sampleFile = dataDirectory.resolve("tweets.js")
        sampleFile.writeText(resourceFile.readText())

        val result = worker.run(context)

        // validate the tweet.js file is in the tempDir directory
        val tempFile = tempDir.resolve("tweets.js").toFile()
        expectThat(result).isSuccess()
        expectThat(tempFile.exists()).isEqualTo(true)
        expectThat(tempFile.readText()).isEqualTo(resourceFile.readText())
    }
}
