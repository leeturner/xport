package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.model.Media
import com.leeturner.xport.domain.model.Tweet
import com.leeturner.xport.domain.model.TweetWrapper
import com.leeturner.xport.domain.pipeline.Context
import io.micronaut.serde.ObjectMapper
import java.io.IOException
import java.nio.file.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class ConvertTweetJsonToMarkdownTask(
    private val objectMapper: ObjectMapper,
) : Task {
    override fun getDescription() = "Convert tweet JSON to markdown files"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.tmpDirectory
        val outputDirectory = tmpDirectory.resolve("markdown")

        val tweetJsonFile = tmpDirectory.resolve("tweets.json")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsonFile)) {
            return IllegalStateException("tweet json file does not exist in the tmp directory").left()
        }

        return try {
            val tweetJsonContent = Files.readString(tweetJsonFile)
            val tweets =
                objectMapper.readValue(tweetJsonContent, Array<TweetWrapper>::class.java)
                    ?: return IllegalStateException("tweet json file could not be parsed").left()

            // Create output directory if it doesn't exist
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory)
            }

            // Create filtered directory if filter-replies is enabled
            val filteredDirectory = tmpDirectory.resolve("filtered")
            if (context.filterReplies) {
                if (!Files.exists(filteredDirectory)) {
                    Files.createDirectories(filteredDirectory)
                }
            }

            // Generate markdown for each tweet
            tweets.forEach { tweetWrapper ->
                val tweet = tweetWrapper.tweet
                val createdAt = parseTweetDate(tweet.createdAt)
                val fileName = formatDateForFileName(createdAt)
                val markdownContent = tweet.generateMarkdown(context)

                // Check if the tweet is a reply and if we should filter it
                val isReply = tweet.inReplyToUserId != null
                val directory = if (isReply && context.filterReplies) filteredDirectory else outputDirectory

                // Tweets posted in the same minute would share a file name, so suffix later ones with the tweet id
                val markdownFile =
                    directory.resolve("$fileName.md").takeUnless { Files.exists(it) }
                        ?: directory.resolve("$fileName-${tweet.id}.md")
                Files.writeString(markdownFile, markdownContent)
            }

            if (context.verbose) {
                println("Successfully converted tweet JSON file to markdown files")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        } catch (e: DateTimeParseException) {
            e.left()
        }
    }
}

fun formatDateForFileName(dateTime: ZonedDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))

fun formatDateTimeWithSeconds(dateTime: ZonedDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"))

fun parseTweetDate(dateString: String): ZonedDateTime {
    // Twitter date format: "Sun Dec 22 12:13:03 +0000 2024"
    val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
    return ZonedDateTime.parse(dateString, formatter)
}

fun formatDateForTitle(dateTime: ZonedDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm"))

fun Tweet.generateMarkdown(context: Context) =
    buildString {
        // Add Hugo front matter
        appendLine("---")
        appendLine("title: ${formatDateForTitle(parseTweetDate(createdAt))}")
        appendLine("date: ${formatDateTimeWithSeconds(parseTweetDate(createdAt))}")

        // Only include author if provided in the context
        context.author?.let { author ->
            appendLine("author: $author")
        }

        appendLine("showtoc: false")
        appendLine("comments: true")
        appendLine("---")
        appendLine()

        var text = fullText
        // extended_entities lists every photo/video in the tweet, entities only the first. They all share one t.co url,
        // so replace that url once with all of them
        val mediaPath = context.mediaPath
        extendedEntities?.media.orEmpty().ifEmpty { entities.media }.groupBy { it.url }.forEach { (url, mediaItems) ->
            text = text.replace(url, mediaItems.joinToString(" ") { it.toMarkdown(id, mediaPath) })
        }

        // process the generic urls in the tweet text and replace them with the full urls
        entities.urls.forEach { urlItem ->
            text = text.replace(urlItem.url, urlItem.expandedUrl)
        }

        // Add tweet content
        appendLine(text)
        appendLine()
        if (inReplyToScreenName != null && inReplyToStatusId != null) {
            appendLine("In reply to [this post](https://x.com/$inReplyToScreenName/status/$inReplyToStatusId) by @$inReplyToScreenName")
        }
    }

private fun Media.toMarkdown(
    tweetId: String,
    mediaPath: String?,
): String {
    val mp4 = videoInfo?.variants?.firstOrNull { it.contentType == "video/mp4" }
    // the local media file is named with the id of the tweet and then the name of the remote file
    val localFileName = "$tweetId-${(mp4?.url ?: mediaUrlHttps).substringAfterLast('/')}"
    val reference = mediaPath?.let { "$it/$localFileName" } ?: localFileName
    return if (mp4 != null) {
        """<video controls="" src="$reference" style="width: 100%; height: 100%; background-color: black;"></video>"""
    } else {
        "![]($reference)"
    }
}
