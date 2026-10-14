package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.assertTweetMediaDirDataCopied
import com.leeturner.xport.assertTweetMediaDirExists
import com.leeturner.xport.createDataDirectory
import com.leeturner.xport.createTweetsMediaDirectoryAndAddSampleData
import com.leeturner.xport.isRight
import com.leeturner.xport.testContext
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Path
import kotlin.io.path.exists

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
    fun `nothing is copied when the tweets_media directory does not exist`() {
        val context =
            testContext(archiveDirectory = archiveDir, tmpDirectory = tempDir)

        // Create the data directory but not the tweets_media directory
        archiveDir.createDataDirectory()

        val result = worker.run(context)

        expectThat(result).isRight()
        expectThat(tempDir.resolve("tweets_media").exists()).isEqualTo(false)
    }

    @Test
    fun `the tweets_media folder is copied from the current directory to the tmp directory`() {
        val context =
            testContext(archiveDirectory = archiveDir, tmpDirectory = tempDir)

        // create the data directory
        val dataDir = archiveDir.createDataDirectory().toPath()
        // Create the tweets_media directory and some test files
        dataDir.createTweetsMediaDirectoryAndAddSampleData()

        val result = worker.run(context)

        expectThat(result).isRight()
        // Validate the tweets_media directory and its files are in the tempDir
        tempDir.assertTweetMediaDirExists()
        tempDir.assertTweetMediaDirDataCopied()
    }
}
