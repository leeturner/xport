package com.leeturner.xport.domain.pipeline

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.leeturner.xport.domain.pipeline.tasks.Task
import com.leeturner.xport.isLeft
import com.leeturner.xport.isRight
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@MicronautTest
class PipelineTest {
    @Test
    fun `pipeline leaves reporting a task failure to the caller`() {
        val context = Context(mapOf("verbose" to "false"))
        val failingTask = Task { IllegalStateException("boom").left() }
        val stdout = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(stdout))

        val result =
            try {
                Pipeline(listOf(failingTask)).execute(context)
            } finally {
                System.setOut(originalOut)
            }

        expectThat(result).isLeft()
        expectThat(stdout.toString()).isEqualTo("")
    }

    @Test
    fun `pipeline executes all tasks in order when all tasks succeed`() {
        // Given
        val context =
            Context(
                mapOf(
                    "currentDirectory" to "/path/to/archive",
                    "tmpDirectory" to "/path/to/tmp",
                    "outputDirectory" to "/path/to/output",
                    "verbose" to "false",
                ),
            )

        val executionOrder = mutableListOf<String>()

        val task1 =
            object : Task {
                override fun getName() = "Task1"

                override fun getDescription() = "First task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task1")
                    return Unit.right()
                }
            }

        val task2 =
            object : Task {
                override fun getName() = "Task2"

                override fun getDescription() = "Second task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task2")
                    return Unit.right()
                }
            }

        val task3 =
            object : Task {
                override fun getName() = "Task3"

                override fun getDescription() = "Third task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task3")
                    return Unit.right()
                }
            }

        val pipeline = Pipeline(listOf(task1, task2, task3))

        // When
        val result = pipeline.execute(context)

        // Then
        expectThat(result).isRight()
        expectThat(executionOrder).isEqualTo(mutableListOf("Task1", "Task2", "Task3"))
    }

    @Test
    fun `pipeline stops execution when a task fails`() {
        // Given
        val context =
            Context(
                mapOf(
                    "currentDirectory" to "/path/to/archive",
                    "tmpDirectory" to "/path/to/tmp",
                    "outputDirectory" to "/path/to/output",
                    "verbose" to "false",
                ),
            )

        val executionOrder = mutableListOf<String>()
        val exception = Exception("Task failed")

        val task1 =
            object : Task {
                override fun getName() = "Task1"

                override fun getDescription() = "First task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task1")
                    return Unit.right()
                }
            }

        val task2 =
            object : Task {
                override fun getName() = "Task2"

                override fun getDescription() = "Second task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task2")
                    return exception.left()
                }
            }

        val task3 =
            object : Task {
                override fun getName() = "Task3"

                override fun getDescription() = "Third task"

                override fun run(context: Context): Either<Exception, Unit> {
                    executionOrder.add("Task3")
                    return Unit.right()
                }
            }

        val pipeline = Pipeline(listOf(task1, task2, task3))

        // When
        val result = pipeline.execute(context)

        // Then
        expectThat(result).isLeft()
        expectThat(executionOrder).isEqualTo(mutableListOf("Task1", "Task2"))
    }
}
