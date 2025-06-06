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
import kotlin.io.path.deleteExisting

class DeleteTweetsJsonFileTask : Task {
    override fun getDescription() = "Delete the tweets.json file from the tmp dir as it will no longer be needed"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }

        val tweetsJsonFile = get(tmpDirectory, "tweets.json")
        // Check if the tweets json file exists in the tmp directory
        if (!Files.exists(tweetsJsonFile)) {
            return Failure(IllegalStateException("tweets.json file does not exist in the tmp directory"))
        }

        return try {
            tweetsJsonFile.deleteExisting()
            if (context.isVerbose()) {
                println("Successfully deleted tweets json file from the tmp dir")
            }
            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
