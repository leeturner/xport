package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.nio.file.Files
import java.nio.file.Paths.get

class ConvertTweetJsToJsonTask : Task {
    override fun getDescription() = "Convert the tweet.js file to a JSON file"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory =
            context.parameters["tmpDirectory"]
                ?: return Failure(
                    IllegalStateException(
                        "No temp directory provided. Please provide a tmpDirectory parameter",
                    ),
                )

        val tweetJsFile = get(tmpDirectory, "tweets.js")
        val tweetJsonFile = get(tmpDirectory, "tweets.json")

        return try {
            val tweetJsContent = Files.readString(tweetJsFile)
            val updatedContent = tweetJsContent.dropWhile { it != '[' }
            Files.writeString(tweetJsonFile, updatedContent)
            Success(Unit)
        } catch (e: java.io.IOException) {
            Failure(e)
        }
    }
}
