package com.leeturner.xport.domain.pipeline

import arrow.core.Either
import arrow.core.right
import com.leeturner.xport.domain.pipeline.tasks.Task
import jakarta.inject.Singleton

@Singleton
class Pipeline(
    private val tasks: List<Task>,
) {
    fun execute(context: Context): Either<Exception, Unit> {
        tasks.forEach { task ->
            if (context.verbose) {
                println("Executing task: ${task.getName()} - ${task.getDescription()}")
            }
            // Stop at the first failure; the caller reports it
            val result = task.run(context)
            if (result.isLeft()) return result
        }
        return Unit.right()
    }
}
