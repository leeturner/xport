package com.leeturner.xport.domain.pipeline

import com.leeturner.xport.domain.pipeline.tasks.ConvertTweetJsToJsonTask
import com.leeturner.xport.domain.pipeline.tasks.ConvertTweetJsonToMarkdownTask
import com.leeturner.xport.domain.pipeline.tasks.CopyTweetJsFileTask
import com.leeturner.xport.domain.pipeline.tasks.CopyTweetsMediaFolderTask
import com.leeturner.xport.domain.pipeline.tasks.DeleteTweetJsFileTask
import com.leeturner.xport.domain.pipeline.tasks.Task
import io.micronaut.context.annotation.Factory
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton

@Factory
class TaskFactory {
    @Singleton
    fun tasks(objectMapper: ObjectMapper): List<Task> =
        listOf(
            CopyTweetJsFileTask(),
            ConvertTweetJsToJsonTask(),
            ConvertTweetJsonToMarkdownTask(objectMapper),
            CopyTweetsMediaFolderTask(),
            DeleteTweetJsFileTask(),
        )
}
