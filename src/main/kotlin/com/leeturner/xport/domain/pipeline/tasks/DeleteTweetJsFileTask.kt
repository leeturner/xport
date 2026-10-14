package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.deleteExisting

class DeleteTweetJsFileTask : Task {
    override fun getDescription() = "Delete the tweet.js file from the tmp dir as it will no longer be needed"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.tmpDirectory

        val tweetJsFile = tmpDirectory.resolve("tweets.js")
        // Check if the tweets js file exists in the tmp directory
        if (!Files.exists(tweetJsFile)) {
            return IllegalStateException("tweets.js file does not exist in the tmp directory").left()
        }

        return try {
            tweetJsFile.deleteExisting()
            if (context.verbose) {
                println("Successfully deleted tweet js file from the tmp dir")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
