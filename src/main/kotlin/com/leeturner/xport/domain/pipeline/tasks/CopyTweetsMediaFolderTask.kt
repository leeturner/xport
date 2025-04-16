package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import kotlin.io.path.exists

class CopyTweetsMediaFolderTask : Task {
    override fun getDescription() = "Copy the tweets_media folder and its contents from the current directory to the tmp directory"

    override fun run(context: Context): Result<Unit, Exception> {
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }
        val archiveDirectory = context.exists("archiveDirectory").onFailure { exception -> return exception }

        // Check if the tweets_media directory exists in the archiveDirectory data directory
        val sourceDir = get(archiveDirectory, "data", "tweets_media")
        if (!sourceDir.exists()) {
            return Failure(IllegalStateException("tweets_media directory does not exist in the current data directory"))
        }

        val destinationDir = get(tmpDirectory, "tweets_media")

        return try {
            // Create the destination directory if it doesn't exist
            if (!destinationDir.exists()) {
                Files.createDirectory(destinationDir)
            }

            // Copy all files from the source directory to the destination directory
            Files.walk(sourceDir).forEach { sourcePath ->
                if (!Files.isDirectory(sourcePath)) {
                    val relativePath = sourceDir.relativize(sourcePath)
                    val destinationPath = destinationDir.resolve(relativePath)

                    // Create parent directories if they don't exist
                    if (!Files.exists(destinationPath.parent)) {
                        Files.createDirectories(destinationPath.parent)
                    }

                    Files.copy(sourcePath, destinationPath)
                }
            }

            if (context.isVerbose()) {
                println("Successfully copied tweet media directory to tmp directory")
            }

            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
