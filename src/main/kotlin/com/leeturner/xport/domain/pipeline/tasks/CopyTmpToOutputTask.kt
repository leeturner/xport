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

class CopyTmpToOutputTask : Task {
    override fun getDescription() = "Copy everything from the tmp directory to the output directory"

    override fun run(context: Context): Either<Exception, Unit> {
        val outputDirectory = context.exists("outputDirectory").getOrElse { exception -> return exception.left() }
        val tmpDirectory = context.exists("tmpDirectory").getOrElse { exception -> return exception.left() }

        val sourceDir = get(tmpDirectory)
        val destinationDir = get(outputDirectory)

        return try {
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
                println("Successfully copied tmp directory to output directory")
            }

            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
