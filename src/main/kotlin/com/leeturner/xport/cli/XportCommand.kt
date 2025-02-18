package com.leeturner.xport.cli

import io.micronaut.configuration.picocli.PicocliRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "xtomd", description = ["..."], mixinStandardHelpOptions = true)
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
            PicocliRunner.run(XportCommand::class.java, *args)
        }
    }
}
