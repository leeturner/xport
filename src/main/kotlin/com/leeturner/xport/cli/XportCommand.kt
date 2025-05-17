package com.leeturner.xport.cli

import com.leeturner.xport.domain.pipeline.Context
import com.leeturner.xport.domain.pipeline.Pipeline
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.micronaut.configuration.picocli.PicocliRunner
import jakarta.inject.Inject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.lang.System.err
import java.util.concurrent.Callable
import kotlin.io.path.createTempDirectory
import kotlin.system.exitProcess

@Command(
    name = "xport",
    description = ["A tool to process X/Twitter data exports"],
    version = ["0.1"],
    mixinStandardHelpOptions = true,
)
class XportCommand
    @Inject
    constructor(
        private val pipeline: Pipeline,
    ) : Callable<Int> {
        @Option(names = ["-v", "--verbose"], description = ["Enable verbose output"])
        private var verbose: Boolean = false

        @Option(names = ["-a", "--author"], description = ["Author name for generated markdown files"])
        private var author: String? = null

        @Option(names = ["-m", "--media-path"], description = ["Path to be appended to media URLs in generated markdown files"])
        private var mediaPath: String? = null

        @Parameters(
            index = "0",
            description = ["Path to the Twitter archive directory"],
        )
        private lateinit var archiveDirectory: File

        @Parameters(
            index = "1",
            description = ["Output directory for processed files"],
        )
        private lateinit var outputDirectory: File

        override fun call(): Int {
            // Validate that the output directory exists.  Exit if not
            if (outputDirectory.doesNotExist() || outputDirectory.isNotEmpty()) {
                err.println("The output directory does not exist. This directory must exist and be empty: $outputDirectory")
                return 1
            }

            // Validate that the data directory exists in the archive directory.  Exit if not
            if (archiveDirectory.doesNotExist() || archiveDirectory.doesNotContainsDataDirectory()) {
                err.println(
                    buildString {
                        append("The archive directory does not exist. ")
                        append("This directory must exist and contain the data directory: $archiveDirectory")
                    },
                )
                return 1
            }

            if (verbose) {
                println("Processing Twitter archive from: ${archiveDirectory.absolutePath}")
                println("Output directory: ${outputDirectory.absolutePath}")
            }

            // Create a temporary directory
            val tmpDir = createTempDirectory("xport").toFile()
            if (verbose) {
                println("Created temporary directory. This will be deleted on exit: ${tmpDir.absolutePath}")
            }

            try {
                // Create the context with the necessary parameters
                val contextParams =
                    buildMap {
                        put("archiveDirectory", archiveDirectory.absolutePath)
                        put("tmpDirectory", tmpDir.absolutePath)
                        put("outputDirectory", outputDirectory.absolutePath)
                        put("verbose", verbose.toString())
                        author?.let { put("author", it) }
                        mediaPath?.let { put("mediaPath", it) }
                    }

                val context = Context(contextParams)

                // Execute the pipeline
                val result = pipeline.execute(context)
                when (result) {
                    is Success -> {
                        println("Successfully processed X/Twitter archive")
                        return 0
                    }
                    is Failure -> {
                        err.println("Error processing X/Twitter archive: ${result.reason}")
                        return 1
                    }
                }
            } finally {
                // Clean up the temporary directory
                tmpDir.deleteRecursively()
                if (verbose) {
                    println("Cleaned up temporary directory")
                }
            }
        }

        companion object {
            @JvmStatic fun main(args: Array<String>) {
                val exitCode = PicocliRunner.call(XportCommand::class.java, *args)
                exitProcess(exitCode ?: 0)
            }
        }

        private fun File.doesNotExist(): Boolean = !exists()

        private fun File.isNotEmpty(): Boolean = !list().isNullOrEmpty()

        private fun File.doesNotContainsDataDirectory(): Boolean = list()?.contains("data") == false
    }
