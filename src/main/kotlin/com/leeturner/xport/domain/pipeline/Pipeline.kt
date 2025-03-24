package com.leeturner.xport.domain.pipeline

import com.leeturner.xport.domain.pipeline.tasks.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import jakarta.inject.Singleton

@Singleton
class Pipeline(
    private val tasks: List<Task>,
) {
    fun execute(context: Context): Result<Unit, Exception> = executeTasksInOrder(tasks, context)

    private fun executeTasksInOrder(
        tasks: List<Task>,
        context: Context,
    ): Result<Unit, Exception> {
        tasks.forEach { task ->
            println("Executing task: ${task.getName()} - ${task.getDescription()}")
            val result = task.run(context)
            when (result) {
                is Failure -> {
                    println("Task ${task.getName()} failed: ${result.reason}")
                    return result
                }
                is Success -> {
                    // Continue to the next task
                }
            }
        }

        return Success(Unit)
    }
}
