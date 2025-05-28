package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.domain.pipeline.exists
import com.leeturner.xport.domain.pipeline.isVerbose
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import kotlin.io.path.copyTo

class CopyTweetJsFileTask : Task {
    override fun getDescription() = "Copy the tweet.js file from the data directory to the tmp directory for processing"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }
        val archiveDirectory = context.exists("archiveDirectory").onFailure { exception -> return exception }

        // copy the file called tweet.js from the directory in the archiveDirectory to the tmpDirectory
        val tweetJsFile = get(archiveDirectory, "data", "tweets.js")
        // Check if the tweets js file exists in the current data directory
        if (!Files.exists(tweetJsFile)) {
            return Failure(IllegalStateException("tweets.js file does not exist in the data directory"))
        }

        val destinationFile = get(tmpDirectory, "tweets.js")

        return try {
            tweetJsFile.copyTo(destinationFile, overwrite = true)
            if (context.isVerbose()) {
                println("Successfully copied tweet.js file")
            }
            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
