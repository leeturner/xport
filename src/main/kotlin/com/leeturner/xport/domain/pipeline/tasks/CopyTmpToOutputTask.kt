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

class CopyTmpToOutputTask : Task {
    override fun getDescription() = "Copy everything from the tmp directory to the output directory"

    override fun run(context: Context): Result<Unit, Exception> {
        val outputDirectory = context.exists("outputDirectory").onFailure { exception -> return exception }
        val tmpDirectory = context.exists("tmpDirectory").onFailure { exception -> return exception }

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

            Success(Unit)
        } catch (e: IOException) {
            Failure(e)
        }
    }
}
