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
import kotlin.io.path.copyTo

class CopyTweetJsFileTask : Task {
    override fun getDescription() = "Copy the tweet.js file from the data directory to the tmp directory for processing"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.exists("tmpDirectory").getOrElse { exception -> return exception.left() }
        val archiveDirectory = context.exists("archiveDirectory").getOrElse { exception -> return exception.left() }

        // copy the file called tweet.js from the directory in the archiveDirectory to the tmpDirectory
        val tweetJsFile = get(archiveDirectory, "data", "tweets.js")
        // Check if the tweets js file exists in the current data directory
        if (!Files.exists(tweetJsFile)) {
            return IllegalStateException("tweets.js file does not exist in the data directory").left()
        }

        val destinationFile = get(tmpDirectory, "tweets.js")

        return try {
            tweetJsFile.copyTo(destinationFile, overwrite = true)
            if (context.isVerbose()) {
                println("Successfully copied tweet.js file")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
