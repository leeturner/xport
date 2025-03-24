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
import java.nio.file.Files
import kotlin.system.exitProcess

@Command(
    name = "xport",
    description = ["A tool to process Twitter data exports"],
    mixinStandardHelpOptions = true,
)
class XportCommand : Runnable {
    @Option(names = ["-v", "--verbose"], description = ["Enable verbose output"])
    private var verbose: Boolean = false

    @Parameters(
        index = "0",
        description = ["Path to the Twitter archive directory"],
    )
    private lateinit var archiveDirectory: File

    @Option(
        names = ["-o", "--output"],
        description = ["Output directory for processed files"],
    )
    private var outputDirectory: File = File("output")

    @Inject
    private lateinit var pipeline: Pipeline

    override fun run() {
        if (verbose) {
            println("Processing Twitter archive from: ${archiveDirectory.absolutePath}")
            println("Output directory: ${outputDirectory.absolutePath}")
        }

        // Create a temporary directory
        val tmpDir = Files.createTempDirectory("xport").toFile()
        if (verbose) {
            println("Created temporary directory: ${tmpDir.absolutePath}")
        }

        // Create the output directory if it doesn't exist
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
            if (verbose) {
                println("Created output directory: ${outputDirectory.absolutePath}")
            }
        }

        // Create the context with the necessary parameters
        val context =
            Context(
                mapOf(
                    "currentDirectory" to archiveDirectory.absolutePath,
                    "tmpDirectory" to tmpDir.absolutePath,
                    "outputDirectory" to outputDirectory.absolutePath,
                ),
            )

        // Execute the pipeline
        val result = pipeline.execute(context)
        when (result) {
            is Success -> {
                println("Successfully processed Twitter archive")
            }
            is Failure -> {
                System.err.println("Error processing Twitter archive: ${result.reason}")
                exitProcess(1)
            }
        }

        // Clean up the temporary directory
        tmpDir.deleteRecursively()
        if (verbose) {
            println("Cleaned up temporary directory")
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val exitCode = PicocliRunner.execute(XportCommand::class.java, *args)
            exitProcess(exitCode)
        }
    }
}
