package com.leeturner.xport.domain.pipeline.tasks
import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.toFile
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
    fun `an error is returned if the tweets json does not exist in the tmp directory`() {
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tmpDirectory.toString(),
                ),
            )

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.json file does not exist in the tmp directory"
    }

    @Test
    fun `the tweets json file is deleted from the tmp directory`() {
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tmpDirectory.toString(),
                ),
            )

        // Create a sample tweets.json file in the tmp directory
        val sampleFile = tmpDirectory.resolve("tweets.json")
        sampleFile.writeText("[]") // Simple empty JSON array

        val result = worker.run(context)

        // validate the tweets.json file is deleted from the directory
        val tempFile = tmpDirectory.resolve("tweets.json").toFile()
        expectThat(result).isSuccess()
        expectThat(tempFile.exists()).isEqualTo(false)
    }
}
