package com.leeturner.xport.domain.pipeline.tasks

import arrow.core.Either
import com.leeturner.xport.domain.pipeline.Context

fun interface Task {
    fun getName(): String = this.javaClass.simpleName

    fun getDescription(): String = ""

    fun run(context: Context): Either<Exception, Unit>
}
