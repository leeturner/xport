package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.toFile
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.skyscreamer.jsonassert.JSONAssert
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@MicronautTest
class ConvertTweetJsToJsonTaskTest {
    @TempDir lateinit var tempDir: Path

    private val worker by lazy { ConvertTweetJsToJsonTask() }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("ConvertTweetJsToJsonTask")
    }

    @Test
    fun `an error is returned when expected tmpDirectory parameter are not in the context`() {
        val context = Context()
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called tmpDirectory provided. Please provide a tmpDirectory parameter in the context"
    }

    @Test
    fun `an error is returned if the tweet js file is not in the tmp directory`() {
        val context = Context(mapOf("tmpDirectory" to tempDir.toString()))

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.js file does not exist in the tmp directory"
    }

    @Test
    fun `the tweets file is updated and saved to a json file`(resourceLoader: ResourceLoader) {
        val context = Context(mapOf("tmpDirectory" to tempDir.toString()))

        // Given a tweet.js file exists in the tmp directory
        val tweetJsResourceFile = resourceLoader.toFile("classpath:archive-content/raw-tweet-file-single-tweet.js")
        val tweetJsFile = tempDir.resolve("tweets.js")
        tweetJsFile.writeText(tweetJsResourceFile.readText())

        // When
        val result = worker.run(context)

        // Then the result should be a json file saved in the tmp directory that is the same as the test resource
        val tweetJsonResourceFile = resourceLoader.toFile("classpath:archive-content/tweet-json-file-single-tweet-with-media-url.json")
        val tweetJsonFile = tempDir.resolve("tweets.json")
        val tweetJsonFileContent = tweetJsonFile.readText()

        expectThat(result).isSuccess()
        expectThat(tweetJsonFile.exists()).isEqualTo(true)
        JSONAssert.assertEquals(tweetJsonFileContent, tweetJsonResourceFile.readText(), true)
    }
}
