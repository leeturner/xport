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
import kotlin.io.path.deleteExisting

class DeleteTweetsJsonFileTask : Task {
    override fun getDescription() = "Delete the tweets.json file from the tmp dir as it will no longer be needed"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.exists("tmpDirectory").getOrElse { exception -> return exception.left() }

        val tweetsJsonFile = get(tmpDirectory, "tweets.json")
        // Check if the tweets json file exists in the tmp directory
        if (!Files.exists(tweetsJsonFile)) {
            return IllegalStateException("tweets.json file does not exist in the tmp directory").left()
        }

        return try {
            tweetsJsonFile.deleteExisting()
            if (context.isVerbose()) {
                println("Successfully deleted tweets json file from the tmp dir")
            }
            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
