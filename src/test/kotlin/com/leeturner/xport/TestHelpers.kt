package com.leeturner.xport

import java.io.File
import java.nio.file.Path

fun Path.createDataDirectory(): File {
    val dataDirectory = this.resolve("data").toFile()
    dataDirectory.mkdir()
    return dataDirectory
}
