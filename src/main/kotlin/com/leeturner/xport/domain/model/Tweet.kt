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
    val extendedEntities: ExtendedEntities? = null,
)

@Serdeable
data class Entities(
    val media: List<Media>,
    val urls: List<Url>,
)

@Serdeable
data class ExtendedEntities(
    val media: List<Media>,
)

@Serdeable
data class Url(
    val url: String,
    @JsonProperty("expanded_url")
    val expandedUrl: String,
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
    @JsonProperty("expanded_url")
    val expandedUrl: String? = null,
//    @JsonProperty("source_status_id")
//    val sourceStatusId: Long? = null,
//    val indices: List<String>? = null,
    @JsonProperty("video_info")
    val videoInfo: VideoInfo? = null,
//    @JsonProperty("source_user_id")
//    val sourceUserId: Long? = null,
//    val sizes: Sizes? = null,
//    val type: String? = null,
//    @JsonProperty("source_status_id_str")
//    val sourceStatusIdStr: String? = null,
//    @JsonProperty("display_url")
//    val displayUrl: String? = null,
//    @JsonProperty("source_user_id_str")
//    val sourceUserIdStr: String? = null,
)

@Serdeable
data class VideoInfo(
    @JsonProperty("aspect_ratio")
    val aspectRatio: List<String>,
    val variants: List<VideoVariant>,
)

@Serdeable
data class VideoVariant(
    val bitrate: String? = null,
    @JsonProperty("content_type")
    val contentType: String,
    val url: String,
)

// @Serdeable
// data class Sizes(
//    val medium: Size? = null,
//    val large: Size? = null,
//    val small: Size? = null,
//    val thumb: Size? = null,
// )

// @Serdeable
// data class Size(
//    val w: String? = null,
//    val h: String? = null,
//    val resize: String? = null,
// )
