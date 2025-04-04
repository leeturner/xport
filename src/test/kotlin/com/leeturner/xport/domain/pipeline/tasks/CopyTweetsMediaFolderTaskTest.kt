package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.createDataDirectory
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
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

@MicronautTest
class CopyTweetsMediaFolderTaskTest {
    @TempDir lateinit var currentDir: Path

    @TempDir lateinit var tempDir: Path

    private val worker by lazy { CopyTweetsMediaFolderTask() }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("CopyTweetsMediaFolderTask")
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
    fun `an error is returned when the tweets_media directory does not exist`() {
        val context =
            Context(
                mapOf(
                    "currentDirectory" to currentDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Create the data directory but not the tweets_media directory
        currentDir.createDataDirectory()

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets_media directory does not exist in the current directory"
    }

    @Test
    fun `the tweets_media folder is copied from the current directory to the tmp directory`() {
        val context =
            Context(
                mapOf(
                    "currentDirectory" to currentDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Create the tweets_media directory and some test files
        val tweetsMediaDir = currentDir.resolve("tweets_media").createDirectory()
        val testFile1 = File(tweetsMediaDir.toFile(), "test1.jpg")
        testFile1.writeText("test1 content")
        val testFile2 = File(tweetsMediaDir.toFile(), "test2.jpg")
        testFile2.writeText("test2 content")

        val result = worker.run(context)

        // Validate the tweets_media directory and its files are in the tempDir
        val tempMediaDir = tempDir.resolve("tweets_media")
        expectThat(result).isSuccess()
        expectThat(tempMediaDir.exists()).isEqualTo(true)

        val tempFile1 = File(tempMediaDir.toFile(), "test1.jpg")
        val tempFile2 = File(tempMediaDir.toFile(), "test2.jpg")

        expectThat(tempFile1.exists()).isEqualTo(true)
        expectThat(tempFile1.readText()).isEqualTo("test1 content")
        expectThat(tempFile2.exists()).isEqualTo(true)
        expectThat(tempFile2.readText()).isEqualTo("test2 content")
    }
}
