package com.leeturner.xport.cli

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class XportCommandTest {
    @Test
    fun testWithCommandLineOption() {
        val ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)
        val baos = ByteArrayOutputStream()

        System.setOut(PrintStream(baos))
        val args = arrayOf("-v")
        PicocliRunner.run(XportCommand::class.java, ctx, *args)

        assertTrue(baos.toString().contains("Hi!"))
        ctx.close()
    }
}
