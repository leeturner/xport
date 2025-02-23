package com.leeturner.xport.domain.pipeline.tasks

import com.leeturner.xport.domain.pipeline.Context
import dev.forkhandles.result4k.Result

fun interface Task {
    fun getName(): String = this.javaClass.simpleName

    fun getDescription(): String = ""

    fun run(context: Context): Result<Unit, Exception>
}
