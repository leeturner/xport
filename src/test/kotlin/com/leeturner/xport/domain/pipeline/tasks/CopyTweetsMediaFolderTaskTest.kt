package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.assertTweetMediaDirDataCopied
import com.leeturner.xport.assertTweetMediaDirExists
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
import java.nio.file.Path

@MicronautTest
class CopyTweetsMediaFolderTaskTest {
    @TempDir lateinit var archiveDir: Path

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
    fun `an error is returned when expected archiveDirectory parameter are not in the context`() {
        val context = Context(mapOf("outputDirectory" to "foo", "tmpDirectory" to "foo"))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called archiveDirectory provided. Please provide a archiveDirectory parameter in the context"
    }

    @Test
    fun `an error is returned when the tweets_media directory does not exist`() {
        val context =
            Context(
                mapOf(
                    "archiveDirectory" to archiveDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Create the data directory but not the tweets_media directory
        archiveDir.createDataDirectory()

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets_media directory does not exist in the current data directory"
    }

    @Test
    fun `the tweets_media folder is copied from the current directory to the tmp directory`() {
        val context =
            Context(
                mapOf(
                    "archiveDirectory" to archiveDir.toString(),
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // create the data directory
        val dataDir = archiveDir.createDataDirectory().toPath()
        // Create the tweets_media directory and some test files
        dataDir.createTweetsMediaDirectoryAndAddSampleData()

        val result = worker.run(context)

        expectThat(result).isSuccess()
        // Validate the tweets_media directory and its files are in the tempDir
        tempDir.assertTweetMediaDirExists()
        tempDir.assertTweetMediaDirDataCopied()
    }
}
