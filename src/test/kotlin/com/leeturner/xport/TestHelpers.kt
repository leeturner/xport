package com.leeturner.xport

import io.micronaut.core.io.ResourceLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File
import java.nio.file.Path

fun Path.createDataDirectory(): File {
    val dataDirectory = this.resolve("data").toFile()
    dataDirectory.mkdir()
    return dataDirectory
}

fun Path.createTweetsMediaDirectoryAndAddSampleData(): File {
    val tweetsMediaDir = this.resolve("tweets_media").toFile()
    tweetsMediaDir.mkdir()
    val testFile1 = File(tweetsMediaDir, "test1.jpg")
    testFile1.writeText("test1 content")
    val testFile2 = File(tweetsMediaDir, "test2.jpg")
    testFile2.writeText("test2 content")

    return tweetsMediaDir
}

fun Path.assertTweetMediaDirExists() {
    val tempMediaDir = this.resolve("tweets_media").toFile()
    expectThat(tempMediaDir.exists()).isEqualTo(true)

    val tempFile1 = File(tempMediaDir, "test1.jpg")
    val tempFile2 = File(tempMediaDir, "test2.jpg")

    expectThat(tempFile1.exists()).isEqualTo(true)
    expectThat(tempFile1.readText()).isEqualTo("test1 content")
    expectThat(tempFile2.exists()).isEqualTo(true)
    expectThat(tempFile2.readText()).isEqualTo("test2 content")
}

fun Path.assertTweetMediaDirDataCopied() {
    val tempMediaDir = this.resolve("tweets_media").toFile()

    val tempFile1 = File(tempMediaDir, "test1.jpg")
    val tempFile2 = File(tempMediaDir, "test2.jpg")

    expectThat(tempFile1.exists()).isEqualTo(true)
    expectThat(tempFile1.readText()).isEqualTo("test1 content")
    expectThat(tempFile2.exists()).isEqualTo(true)
    expectThat(tempFile2.readText()).isEqualTo("test2 content")
}

fun ResourceLoader.toFile(resource: String): File = File(this.getResource(resource).get().toURI())
