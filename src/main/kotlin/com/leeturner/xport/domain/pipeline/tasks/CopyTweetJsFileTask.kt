package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.copyTo

class CopyTweetJsFileTask : Task {
    override fun getDescription() = "Copy the tweet.js file from the data directory to the tmp directory for processing"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.tmpDirectory
        val archiveDirectory = context.archiveDirectory

        // copy the file called tweet.js from the directory in the archiveDirectory to the tmpDirectory
        val tweetJsFile = archiveDirectory.resolve("data").resolve("tweets.js")
        // Check if the tweets js file exists in the current data directory
        if (!Files.exists(tweetJsFile)) {
            return IllegalStateException("tweets.js file does not exist in the data directory").left()
        }

        val destinationFile = tmpDirectory.resolve("tweets.js")

        return try {
            tweetJsFile.copyTo(destinationFile, overwrite = true)
            if (context.verbose) {
                println("Successfully copied tweet.js file")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
