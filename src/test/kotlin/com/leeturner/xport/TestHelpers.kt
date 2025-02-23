package com.leeturner.xport

import io.micronaut.core.io.ResourceLoader
import java.io.File
import java.nio.file.Path

fun Path.createDataDirectory(): File {
    val dataDirectory = this.resolve("data").toFile()
    dataDirectory.mkdir()
    return dataDirectory
}

fun ResourceLoader.toFile(resource: String): File = File(this.getResource(resource).get().toURI())
