package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.createDataDirectory
import com.leeturner.xport.domain.pipeline.Context
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
import java.io.File
import java.nio.file.Path

@MicronautTest
class CopyTweetFileTaskTest {
    @TempDir lateinit var currentDir: Path

    @TempDir lateinit var tempDir: Path

    private val worker = CopyTweetFileTask()

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("CopyTweetFileTask")
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
    fun `an error is returned when expected currentDirectory parameter are not in the context`() {
        val context = Context(mapOf("outputDirectory" to "foo", "tmpDirectory" to "foo"))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called currentDirectory provided. Please provide a currentDirectory parameter in the context"
    }

    @Test
    fun `the tweets file is copied from the data directory to the tmp directory`(resourceLoader: ResourceLoader) {
        val contextParameters =
            mapOf(
                "currentDirectory" to currentDir.toString(),
                "tmpDirectory" to tempDir.toString(),
            )
        val context = Context(contextParameters)

        // given a directory and a file inside current directory
        val dataDirectory = currentDir.createDataDirectory()

        // Load the file from classpath
        val resource =
            resourceLoader.getResource("classpath:archive-content/raw-tweet-file-single-tweet.js")
                ?: throw IllegalArgumentException("File could not be found on the classpath")
        val resourceFile = File(resource.get().toURI())
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
