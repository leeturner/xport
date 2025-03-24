package com.leeturner.xport.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TweetWrapper(
    val tweet: Tweet,
)

@Serdeable
data class Tweet(
    val entities: Entities,
    val truncated: Boolean,
    val id: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("full_text")
    val fullText: String,
    val lang: String,
    @JsonProperty("extended_entities")
    val extendedEntities: ExtendedEntities,
)

@Serdeable
data class Entities(
    val media: List<Media>,
)

@Serdeable
data class Media(
    val url: String,
    @JsonProperty("media_url")
    val mediaUrl: String,
    @JsonProperty("media_url_https")
    val mediaUrlHttps: String,
    @JsonProperty("id_str")
    val idStr: String,
)

@Serdeable
data class ExtendedEntities(
    val media: List<Media>,
)
