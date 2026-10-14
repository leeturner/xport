package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.Context
import java.io.IOException

class CopyTmpToOutputTask : Task {
    override fun getDescription() = "Copy everything from the tmp directory to the output directory"

    override fun run(context: Context): Either<Exception, Unit> {
        val sourceDir = context.tmpDirectory
        val destinationDir = context.outputDirectory

        return try {
            // Copy all files from the source directory to the destination directory, creating directories as needed
            sourceDir.toFile().copyRecursively(destinationDir.toFile())

            if (context.verbose) {
                println("Successfully copied tmp directory to output directory")
            }

            Unit.right()
        } catch (e: IOException) {
            e.left()
        }
    }
}
