package com.leeturner.xport.domain.pipeline.tasks
import com.leeturner.xport.isLeft
import com.leeturner.xport.isRight
import com.leeturner.xport.testContext
import com.leeturner.xport.toFile
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.nio.file.Path
import kotlin.io.path.writeText

@MicronautTest
class DeleteTweetsJsonFileTaskTest {
    @TempDir lateinit var tmpDirectory: Path

    private val worker by lazy { DeleteTweetsJsonFileTask() }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("DeleteTweetsJsonFileTask")
    }

    @Test
    fun `an error is returned if the tweets json does not exist in the tmp directory`() {
        val context =
            testContext(tmpDirectory = tmpDirectory)

        val result = worker.run(context)

        expectThat(result)
            .isLeft()
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.json file does not exist in the tmp directory"
    }

    @Test
    fun `the tweets json file is deleted from the tmp directory`() {
        val context =
            testContext(tmpDirectory = tmpDirectory)

        // Create a sample tweets.json file in the tmp directory
        val sampleFile = tmpDirectory.resolve("tweets.json")
        sampleFile.writeText("[]") // Simple empty JSON array

        val result = worker.run(context)

        // validate the tweets.json file is deleted from the directory
        val tempFile = tmpDirectory.resolve("tweets.json").toFile()
        expectThat(result).isRight()
        expectThat(tempFile.exists()).isEqualTo(false)
    }
}
