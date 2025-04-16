package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.model.Tweet
import com.leeturner.xport.domain.model.TweetWrapper
import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import io.micronaut.serde.ObjectMapper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ConvertTweetJsonToMarkdownTask(
    private val objectMapper: ObjectMapper,
) : Task {
    override fun getDescription() = "Convert tweet JSON to markdown files"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory =
            context.exists("tmpDirectory").onFailure { exception ->
                return exception
            }
        val outputDirectory = "$tmpDirectory/markdown"

        val tweetJsonFile = get(tmpDirectory, "tweets.json")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsonFile)) {
            return Failure(IllegalStateException("tweet json file does not exist in the tmp directory"))
        }

        return try {
            val tweetJsonContent = Files.readString(tweetJsonFile)
            val tweets = objectMapper.readValue(tweetJsonContent, Array<TweetWrapper>::class.java)

            // Create output directory if it doesn't exist
            val outputPath = get(outputDirectory)
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath)
            }

            // Generate markdown for each tweet
            tweets.forEach { tweetWrapper ->
                val tweet = tweetWrapper.tweet
                val createdAt = parseTweetDate(tweet.createdAt)
                val fileName = formatDateForFileName(createdAt)
                val markdownContent = tweet.generateMarkdown()

                val markdownFile = get(outputDirectory, "$fileName.md")
                Files.writeString(markdownFile, markdownContent)
            }

            if (context.isVerbose()) {
                println("Successfully converted tweet JSON file to markdown files")
            }
            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}

fun formatDateForFileName(dateTime: ZonedDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))

fun parseTweetDate(dateString: String): ZonedDateTime {
    // Twitter date format: "Sun Dec 22 12:13:03 +0000 2024"
    val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
    return ZonedDateTime.parse(dateString, formatter)
}

fun formatDateForTitle(dateTime: ZonedDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))

fun Tweet.generateMarkdown() =
    buildString {
        // Add Hugo front matter
        appendLine("---")
        appendLine("title: ${formatDateForTitle(parseTweetDate(createdAt))}")
        appendLine("date: ${parseTweetDate(createdAt)}")
        appendLine("author: Lee Turner")
        appendLine("showtoc: false")
        appendLine("comments: true")
        appendLine("---")
        appendLine()

        // Add tweet content
        appendLine(fullText)
        appendLine()

        // Add media if present
        entities.media.forEach { mediaItem -> appendLine("![](${mediaItem.mediaUrlHttps})") }
    }
