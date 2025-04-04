package com.leeturner.xport.cli

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.env.Environment
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@Suppress("detekt:ForbiddenComment")
class XportCommandTest {
    val environments = listOf(Environment.CLI, Environment.TEST)

    @Test
    @Disabled("Not implemented yet")
    fun testWithCommandLineOption() {
        val outputStream = ByteArrayOutputStream()

        System.setOut(PrintStream(outputStream))
        val args = arrayOf("-v")
        val exitCode = PicocliRunner.execute(XportCommand::class.java, environments, *args)

        expectThat(exitCode).isEqualTo(0)
        expectThat(outputStream.toString()).contains("Hi!")
    }

    // TODO: test verbose is passed in the context to the pipeline
}
