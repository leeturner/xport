package com.leeturner.xport.domain.pipeline

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

data class Context(
    val parameters: Map<String, String> = emptyMap(),
)

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

/**
 * Extension function to retrieve the value of the 'verbose' parameter.
 * Returns true if the 'verbose' parameter is present and equal to 'true', false otherwise
 */
fun Context.isVerbose(): Boolean = parameters["verbose"].toBoolean()

/**
 * Extension function to retrieve the value of the 'filterReplies' parameter.
 * Returns true if the 'filterReplies' parameter is present and equal to 'true', false otherwise
 */
fun Context.isFilterReplies(): Boolean = parameters["filterReplies"].toBoolean()
