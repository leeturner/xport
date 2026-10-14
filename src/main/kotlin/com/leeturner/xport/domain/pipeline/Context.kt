package com.leeturner.xport.domain.pipeline

import java.nio.file.Path

data class Context(
    val archiveDirectory: Path,
    val tmpDirectory: Path,
    val outputDirectory: Path,
    val verbose: Boolean = false,
    val filterReplies: Boolean = false,
    val author: String? = null,
    val mediaPath: String? = null,
)
