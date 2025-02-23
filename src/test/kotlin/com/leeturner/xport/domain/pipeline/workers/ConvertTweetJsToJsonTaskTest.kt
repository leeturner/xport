package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context
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
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@MicronautTest
class ConvertTweetJsToJsonTaskTest {
    
    @TempDir lateinit var tempDir: Path
    
    private val worker = ConvertTweetJsToJsonTask()

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
            .message isEqualTo "No temp directory provided. Please provide a tmpDirectory parameter"
    }

    @Test
    fun `the tweets file is updated and saved to a json file`(
        resourceLoader: ResourceLoader,
    ) {
        val contextParameters = mapOf("tmpDirectory" to tempDir.toString())
        val context = Context(contextParameters)

        // Given a tweet.js file exists in the tmp directory
        val tweetJsResource =
            resourceLoader.getResource("classpath:archive-content/raw-tweet-file-single-tweet.js")
                ?: throw IllegalArgumentException("File could not be found on the classpath")
        val tweetJsResourceFile = File(tweetJsResource.get().toURI())
        val tweetJsFile = tempDir.resolve("tweets.js")
        tweetJsFile.writeText(tweetJsResourceFile.readText())

        // When
        val result = worker.run(context)

        // Then the result should be a json file saved in the tmp directory that is the same as the test resource
        val tweetJsonResource =
            resourceLoader.getResource("classpath:archive-content/tweet-json-file-single-tweet.json")
                ?: throw IllegalArgumentException("File could not be found on the classpath")
        val tweetJsonResourceFile = File(tweetJsonResource.get().toURI())
        val tweetJsonFile = tempDir.resolve("tweets.json")
        val tweetJsonFileContent = tweetJsonFile.readText()

        expectThat(result).isSuccess()
        expectThat(tweetJsonFile.exists()).isEqualTo(true)
        JSONAssert.assertEquals(tweetJsonFileContent, tweetJsonResourceFile.readText(), true)
    }
}
