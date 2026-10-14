package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.model.Tweet
import com.leeturner.xport.domain.model.TweetWrapper
import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.domain.pipeline.exists
import com.leeturner.xport.domain.pipeline.isFilterReplies
import com.leeturner.xport.domain.pipeline.isVerbose
import io.micronaut.serde.ObjectMapper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ConvertTweetJsonToMarkdownTask(
    private val objectMapper: ObjectMapper,
) : Task {
    override fun getDescription() = "Convert tweet JSON to markdown files"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory =
            context.exists("tmpDirectory").getOrElse { exception ->
                return exception.left()
            }
        val outputDirectory = "$tmpDirectory/markdown"

        val tweetJsonFile = get(tmpDirectory, "tweets.json")
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
            val outputPath = get(outputDirectory)
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath)
            }

            // Create filtered directory if filter-replies is enabled
            val filteredDirectory = "$tmpDirectory/filtered"
            if (context.isFilterReplies()) {
                val filteredPath = get(filteredDirectory)
                if (!Files.exists(filteredPath)) {
                    Files.createDirectories(filteredPath)
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
                val directory = if (isReply && context.isFilterReplies()) filteredDirectory else outputDirectory

                // Tweets posted in the same minute would share a file name, so suffix later ones with the tweet id
                val markdownFile =
                    get(directory, "$fileName.md").takeUnless { Files.exists(it) }
                        ?: get(directory, "$fileName-${tweet.id}.md")
                Files.writeString(markdownFile, markdownContent)
            }

            if (context.isVerbose()) {
                println("Successfully converted tweet JSON file to markdown files")
            }
            Unit.right()
        } catch (e: IOException) {
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
        context.parameters["author"]?.let { author ->
            appendLine("author: $author")
        }

        appendLine("showtoc: false")
        appendLine("comments: true")
        appendLine("---")
        appendLine()

        var text = fullText
        // process the media urls in the tweet text and replace them with markdown images
        entities.media.forEach { mediaItem ->
            // the name of the image file is the id of the tweet and then the name of the media file
            val mediaFileName = mediaItem.mediaUrlHttps.substring(mediaItem.mediaUrlHttps.lastIndexOf('/') + 1)
            val localMediaFileName = "$id-$mediaFileName"

            // If mediaPath is provided in the context, prepend it to the image filename
            val mediaPath = context.parameters["mediaPath"]
            val imageReference = mediaPath?.let { "![]($it/$localMediaFileName)" } ?: "![]($localMediaFileName)"

            // let's see if there is a video associated to this tweet
            val extendedMediaItem =
                extendedEntities?.media?.firstOrNull { extendedMediaItem ->
                    extendedMediaItem.mediaUrlHttps == mediaItem.mediaUrlHttps
                }
            val mp4Reference =
                extendedMediaItem?.let { mediaItem ->
                    mediaItem.videoInfo
                        ?.variants
                        ?.firstOrNull { variant ->
                            variant.contentType == "video/mp4"
                        }?.let { mp4 ->
                            val mp4FileName = mp4.url.substring(mp4.url.lastIndexOf('/') + 1)
                            val localMp4FileName = "$id-$mp4FileName"
                            mediaPath?.let { "$it/$localMp4FileName" } ?: localMp4FileName
                        }
                }

            if (mp4Reference != null) {
                text =
                    text.replace(
                        mediaItem.url,
                        """<video controls="" src="$mp4Reference" style="width: 100%; height: 100%; background-color: black;"></video>""",
                    )
            } else {
                text = text.replace(mediaItem.url, imageReference)
            }
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
