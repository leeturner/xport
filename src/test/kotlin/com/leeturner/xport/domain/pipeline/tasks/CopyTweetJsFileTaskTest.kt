package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.createDataDirectory
import com.leeturner.xport.isLeft
import com.leeturner.xport.isRight
import com.leeturner.xport.testContext
import com.leeturner.xport.toFile
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
    fun `a failure is returned when the tweet js file does not exist in the current data directory`() {
        val context =
            testContext(archiveDirectory = archiveDir, tmpDirectory = tempDir)

        archiveDir.createDataDirectory()

        val result = worker.run(context)

        expectThat(result)
            .isLeft()
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.js file does not exist in the data directory"
    }

    @Test
    fun `the tweets file is copied from the data directory to the tmp directory`(resourceLoader: ResourceLoader) {
        val context =
            testContext(archiveDirectory = archiveDir, tmpDirectory = tempDir)

        // given a directory and a file inside current directory
        val dataDirectory = archiveDir.createDataDirectory()

        // Load the file from classpath
        val resourceFile = resourceLoader.toFile("classpath:archive-content/raw-tweet-file-single-tweet.js")
        val sampleFile = dataDirectory.resolve("tweets.js")
        sampleFile.writeText(resourceFile.readText())

        val result = worker.run(context)

        // validate the tweet.js file is in the tempDir directory
        val tempFile = tempDir.resolve("tweets.js").toFile()
        expectThat(result).isRight()
        expectThat(tempFile.exists()).isEqualTo(true)
        expectThat(tempFile.readText()).isEqualTo(resourceFile.readText())
    }
}
