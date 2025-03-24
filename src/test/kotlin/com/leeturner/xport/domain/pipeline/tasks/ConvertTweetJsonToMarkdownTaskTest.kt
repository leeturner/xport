package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.toFile
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import io.micronaut.core.io.ResourceLoader
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@MicronautTest
class ConvertTweetJsonToMarkdownTaskTest {
    @TempDir lateinit var tempDir: Path

    @TempDir lateinit var outputDir: Path

    @Inject
    lateinit var objectMapper: ObjectMapper

    private val worker by lazy { ConvertTweetJsonToMarkdownTask(objectMapper) }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("ConvertTweetJsonToMarkdownTask")
    }

    @Test
    fun `an error is returned when expected tmpDirectory parameter is not in the context`() {
        val context = Context(mapOf("outputDirectory" to outputDir.toString()))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called tmpDirectory provided. Please provide a tmpDirectory parameter in the context"
    }

    @Test
    fun `an error is returned when expected outputDirectory parameter is not in the context`() {
        val context = Context(mapOf("tmpDirectory" to tempDir.toString()))
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called outputDirectory provided. Please provide a outputDirectory parameter in the context"
    }

    @Test
    fun `the tweet json file is converted to markdown files`(resourceLoader: ResourceLoader) {
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                    "outputDirectory" to outputDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile = resourceLoader.toFile("classpath:archive-content/tweet-json-file-single-tweet.json")
        val tweetJsonFile = tempDir.resolve("tweets.json")
        tweetJsonFile.writeText(tweetJsonResourceFile.readText())

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        val tweetJson = tweetJsonResourceFile.readText()
        val tweetWrapper = objectMapper.readValue(tweetJson, Array<com.leeturner.xport.domain.model.TweetWrapper>::class.java)[0]
        val createdAt = parseTweetDate(tweetWrapper.tweet.createdAt)
        val expectedFileName = formatDateForFileName(createdAt) + ".md"

        val markdownFile = outputDir.resolve(expectedFileName)
        expectThat(markdownFile.exists()).isEqualTo(true)

        val expectedMarkdown = resourceLoader.toFile("classpath:archive-content/tweet-markdown-file-single-tweet.md").readText()
        // Verify markdown content
        val markdownContent = markdownFile.readText()
        expectThat(markdownContent).isEqualTo(expectedMarkdown)
//        expectThat(markdownContent).contains("---")
//        expectThat(markdownContent).contains("title: \"Tweet\"")
//        expectThat(markdownContent).contains("date: \"${tweetWrapper.tweet.createdAt}\"")
//        expectThat(markdownContent).contains(tweetWrapper.tweet.fullText)

        // If there's media, verify it's included
//        tweetWrapper.tweet.entities.media.forEach { media ->
//            expectThat(markdownContent).contains(media.mediaUrl)
//        }
    }

    private fun parseTweetDate(dateString: String): ZonedDateTime {
        // Twitter date format: "Sun Dec 22 12:13:03 +0000 2024"
        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
        return ZonedDateTime.parse(dateString, formatter)
    }

    private fun formatDateForFileName(dateTime: ZonedDateTime) = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))
}
