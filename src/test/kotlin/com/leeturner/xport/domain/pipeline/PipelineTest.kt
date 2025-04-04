package com.leeturner.xport.domain.pipeline

import com.leeturner.xport.domain.pipeline.tasks.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.strikt.isFailure
import dev.forkhandles.result4k.strikt.isSuccess
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@MicronautTest
class PipelineTest {
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

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task1")
                    return Success(Unit)
                }
            }

        val task2 =
            object : Task {
                override fun getName() = "Task2"

                override fun getDescription() = "Second task"

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task2")
                    return Success(Unit)
                }
            }

        val task3 =
            object : Task {
                override fun getName() = "Task3"

                override fun getDescription() = "Third task"

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task3")
                    return Success(Unit)
                }
            }

        val pipeline = Pipeline(listOf(task1, task2, task3))

        // When
        val result = pipeline.execute(context)

        // Then
        expectThat(result).isSuccess()
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

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task1")
                    return Success(Unit)
                }
            }

        val task2 =
            object : Task {
                override fun getName() = "Task2"

                override fun getDescription() = "Second task"

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task2")
                    return Failure(exception)
                }
            }

        val task3 =
            object : Task {
                override fun getName() = "Task3"

                override fun getDescription() = "Third task"

                override fun run(context: Context): Result<Unit, Exception> {
                    executionOrder.add("Task3")
                    return Success(Unit)
                }
            }

        val pipeline = Pipeline(listOf(task1, task2, task3))

        // When
        val result = pipeline.execute(context)

        // Then
        expectThat(result).isFailure()
        expectThat(executionOrder).isEqualTo(mutableListOf("Task1", "Task2"))
    }
}
