package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

/**
 * Extension function to retrieve the value of a parameter from the context.
 * Returns a Result wrapping either the value (if present) or an exception (if missing).
 */
fun Context.exists(parameterName: String): Result<String, IllegalStateException> =
    parameters[parameterName]
        ?.let { Success(it) }
        ?: Failure(
            IllegalStateException(
                "No parameter called $parameterName provided. Please provide a $parameterName parameter in the context",
            ),
        )
