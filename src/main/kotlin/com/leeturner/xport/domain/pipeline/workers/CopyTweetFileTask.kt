package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.nio.file.Files.copy
import java.nio.file.Paths.get
import java.nio.file.StandardCopyOption

class CopyTweetFileTask : Task {
    override fun getDescription() = "Copy the tweet.js file from the data directory to the tmp directory for processing"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory =
            context.parameters["tmpDirectory"]
                ?: return Failure(
                    IllegalStateException(
                        "No temp directory provided. Please provide a tmpDirectory parameter",
                    ),
                )

        val currentDirectory =
            context.parameters["currentDirectory"]
                ?: return Failure(
                    IllegalStateException(
                        "No current directory provided. Please provide a currentDirectory parameter",
                    ),
                )

        // copy the file called tweet.js from the directory in the currentDirectory to the tmpDirectory
        val sourceFile = get(currentDirectory, "data", "tweets.js")
        val destinationFile = get(tmpDirectory, "tweets.js")

        return try {
            copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING)
            Success(Unit)
        } catch (e: java.io.IOException) {
            Failure(e)
        }
    }
}
