package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import java.io.IOException
import java.nio.file.Files

class ConvertTweetJsToJsonTask : Task {
    override fun getDescription() = "Convert the tweet.js file to a JSON file"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.tmpDirectory

        val tweetJsFile = tmpDirectory.resolve("tweets.js")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsFile)) {
            return IllegalStateException("tweets.js file does not exist in the tmp directory").left()
        }

        val tweetJsonFile = tmpDirectory.resolve("tweets.json")

        return try {
            val tweetJsContent = Files.readString(tweetJsFile)
            val updatedContent = tweetJsContent.dropWhile { it != '[' }
            Files.writeString(tweetJsonFile, updatedContent)
            if (context.verbose) {
                println("Successfully converted tweet.js file to JSON")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
