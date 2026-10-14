package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.domain.pipeline.exists
import com.leeturner.xport.domain.pipeline.isVerbose
import java.io.IOException
import java.nio.file.Paths.get
import kotlin.io.path.exists

class CopyTweetsMediaFolderTask : Task {
    override fun getDescription() = "Copy the tweets_media folder and its contents from the current directory to the tmp directory"

    override fun run(context: Context): Either<Exception, Unit> {
        val tmpDirectory = context.exists("tmpDirectory").getOrElse { exception -> return exception.left() }
        val archiveDirectory = context.exists("archiveDirectory").getOrElse { exception -> return exception.left() }

        // Archives with no media have no tweets_media directory, so there is nothing to copy
        val sourceDir = get(archiveDirectory, "data", "tweets_media")
        if (!sourceDir.exists()) {
            if (context.isVerbose()) {
                println("No tweets_media directory in the data directory, skipping")
            }
            return Unit.right()
        }

        val destinationDir = get(tmpDirectory, "tweets_media")

        return try {
            // Copy all files from the source directory to the destination directory, creating directories as needed
            sourceDir.toFile().copyRecursively(destinationDir.toFile())

            if (context.isVerbose()) {
                println("Successfully copied tweet media directory to tmp directory")
            }

            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
