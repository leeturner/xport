package com.leeturner.xport.domain.pipeline.workers

import com.leeturner.xport.domain.pipeline.Context

fun interface Worker {
    fun run(context: Context)
}
