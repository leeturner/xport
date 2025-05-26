package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.model.TweetWrapper
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
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@MicronautTest
class ConvertTweetJsonToMarkdownTaskTest {
    @TempDir lateinit var tempDir: Path

    @Inject
    lateinit var objectMapper: ObjectMapper

    private val worker by lazy { ConvertTweetJsonToMarkdownTask(objectMapper) }

    @Test
    fun `task returns the correct name`() {
        expectThat(worker.getName()).isEqualTo("ConvertTweetJsonToMarkdownTask")
    }

    @Test
    fun `a failure is returned when expected tmpDirectory parameter is not in the context`() {
        val context = Context(mapOf())
        val result = worker.run(context)
        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "No parameter called tmpDirectory provided. Please provide a tmpDirectory parameter in the context"
    }

    @Test
    fun `a failure is returned when the tweet json file does not exist in the tmp directory`() {
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        val result = worker.run(context)

        expectThat(result)
            .isFailure()
            .get { result.get() }
            .isA<IllegalStateException>()
            .message isEqualTo
            "tweet json file does not exist in the tmp directory"
    }

    @Test
    fun `the tweet json file is converted to markdown files`(resourceLoader: ResourceLoader) {
        val context = Context(mapOf("tmpDirectory" to tempDir.toString()))

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-no-urls.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-with-no-urls.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files with custom author`(resourceLoader: ResourceLoader) {
        // Given a context with a custom author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                    "author" to "Custom Author",
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-no-urls.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-custom-author.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files without author when no author is specified`(resourceLoader: ResourceLoader) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-no-urls.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-no-author.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files and urls are replaced with their full version`(resourceLoader: ResourceLoader) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-url.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-with-url.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files and media urls are replaced with their full version`(
        resourceLoader: ResourceLoader,
    ) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-media-url.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-with-media-url.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files and media urls include the media path`(resourceLoader: ResourceLoader) {
        // Given a context with a media path
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                    "mediaPath" to "images",
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-media-url.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-with-media-path.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files and images are linked to videos if they are there`(
        resourceLoader: ResourceLoader,
    ) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-with-video.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-with-video.md",
        )
    }
    
    @Test
    fun `the tweet json file is converted to markdown files and reply to links are added`(
        resourceLoader: ResourceLoader,
    ) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-in-reply-to.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-in-reply-to.md",
        )
    }

    @Test
    fun `the tweet json file is converted to markdown files and reply to links are added`(resourceLoader: ResourceLoader) {
        // Given a context without an author
        val context =
            Context(
                mapOf(
                    "tmpDirectory" to tempDir.toString(),
                ),
            )

        // Given a tweets.json file exists in the tmp directory
        val tweetJsonResourceFile =
            resourceLoader.createTweetsJsonFile(
                "classpath:archive-content/tweet-json-file-single-tweet-in-reply-to.json",
            )

        // When
        val result = worker.run(context)

        // Then the result should be a markdown file saved in the output directory
        expectThat(result).isSuccess()

        // Parse the tweet date to get the expected filename
        resourceLoader.expectMarkdownMatches(
            tweetJsonResourceFile,
            "classpath:expected/tweet-markdown-file-single-tweet-in-reply-to.md",
        )
    }

    private fun parseTweetDate(dateString: String): ZonedDateTime {
        // Twitter date format: "Sun Dec 22 12:13:03 +0000 2024"
        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
        return ZonedDateTime.parse(dateString, formatter)
    }

    private fun formatDateForFileName(dateTime: ZonedDateTime) = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))

    private fun ResourceLoader.createTweetsJsonFile(resourcePath: String): File {
        val tweetsJsonResourceFile = toFile(resourcePath)
        val tweetsJsonFile = tempDir.resolve("tweets.json")
        tweetsJsonFile.writeText(tweetsJsonResourceFile.readText())
        return tweetsJsonResourceFile
    }

    private fun ResourceLoader.expectMarkdownMatches(
        tweetJsonResourceFile: File,
        expectedMarkdownFile: String,
    ) {
        val tweetJson = tweetJsonResourceFile.readText()
        val tweetWrapper = objectMapper.readValue(tweetJson, Array<TweetWrapper>::class.java)[0]
        val createdAt = parseTweetDate(tweetWrapper.tweet.createdAt)
        val expectedFileName = formatDateForFileName(createdAt) + ".md"

        val pathToMarkdownFile = Paths.get(tempDir.toString(), "markdown", expectedFileName)
        val markdownFile = tempDir.resolve(pathToMarkdownFile)
        expectThat(markdownFile.exists()).isEqualTo(true)

        val expectedMarkdown =
            toFile(expectedMarkdownFile)
                .readText()
        // Verify markdown content
        val markdownContent = markdownFile.readText()
        // Trim both strings to handle any newline differences
        expectThat(markdownContent.trim()).isEqualTo(expectedMarkdown.trim())
    }
}
