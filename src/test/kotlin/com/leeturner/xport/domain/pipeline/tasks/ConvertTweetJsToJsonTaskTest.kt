package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.isLeft
import com.leeturner.xport.isRight
import com.leeturner.xport.testContext
import com.leeturner.xport.toFile
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
    fun `an error is returned if the tweet js file is not in the tmp directory`() {
        val context = testContext(tmpDirectory = tempDir)

        val result = worker.run(context)

        expectThat(result)
            .isLeft()
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweets.js file does not exist in the tmp directory"
    }

    @Test
    fun `the tweets file is updated and saved to a json file`(resourceLoader: ResourceLoader) {
        val context = testContext(tmpDirectory = tempDir)

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

        expectThat(result).isRight()
        expectThat(tweetJsonFile.exists()).isEqualTo(true)
        JSONAssert.assertEquals(tweetJsonFileContent, tweetJsonResourceFile.readText(), true)
    }
}
