package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.domain.pipeline.exists
import com.leeturner.xport.domain.pipeline.isVerbose
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get

class ConvertTweetJsToJsonTask : Task {
    override fun getDescription() = "Convert the tweet.js file to a JSON file"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.exists("tmpDirectory").getOrElse { exception -> return exception.left() }

        val tweetJsFile = get(tmpDirectory, "tweets.js")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsFile)) {
            return IllegalStateException("tweets.js file does not exist in the tmp directory").left()
        }

        val tweetJsonFile = get(tmpDirectory, "tweets.json")

        return try {
            val tweetJsContent = Files.readString(tweetJsFile)
            val updatedContent = tweetJsContent.dropWhile { it != '[' }
            Files.writeString(tweetJsonFile, updatedContent)
            if (context.isVerbose()) {
                println("Successfully converted tweet.js file to JSON")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
