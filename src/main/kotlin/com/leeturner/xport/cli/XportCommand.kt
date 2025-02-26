package com.leeturner.xport.cli

import io.micronaut.configuration.picocli.PicocliRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import kotlin.system.exitProcess

@Command(name = "xport", description = ["..."], mixinStandardHelpOptions = true)
class XportCommand : Runnable {
    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose: Boolean = false

    override fun run() {
        // business logic here
        if (verbose) {
            println("Hi!")
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val exitCode = PicocliRunner.execute(XportCommand::class.java, *args)
            exitProcess(exitCode)
        }
    }
}
