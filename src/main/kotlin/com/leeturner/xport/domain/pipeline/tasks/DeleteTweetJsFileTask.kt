package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import kotlin.io.path.deleteExisting

class DeleteTweetJsFileTask : Task {
    override fun getDescription() = "Delete the tweet.js file from the tmp dir as it will no longer be needed"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }

        val tweetJsFile = get(tmpDirectory, "tweets.js")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsFile)) {
            return Failure(IllegalStateException("tweets.js file does not exist in the tmp directory"))
        }

        return try {
            tweetJsFile.deleteExisting()
            if (context.isVerbose()) {
                println("Successfully deleted tweet js file from the tmp dir")
            }
            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
