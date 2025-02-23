package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import kotlin.io.path.copyTo

class ConvertTweetJsToJsonTask : Task {
    override fun getDescription() = "Convert the tweet.js file to a JSON file"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }

        val tweetJsFile = get(tmpDirectory, "tweets.js")
        val tweetJsonFile = get(tmpDirectory, "tweets.json")

        return try {
            tweetJsFile.copyTo(tweetJsonFile, overwrite = true)
            val tweetJsContent = Files.readString(tweetJsFile)
            val updatedContent = tweetJsContent.dropWhile { it != '[' }
            Files.writeString(tweetJsonFile, updatedContent)
            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
