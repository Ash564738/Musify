package com.example.musify.data.remote.response

import com.example.musify.data.utils.MapperImageSize
import com.example.musify.data.utils.getImageResponseForImageSize
import com.example.musify.domain.SearchResult.TrackSearchResult
import com.fasterxml.jackson.annotation.JsonProperty

data class TrackResponseWithAlbumMetadata(
    val id: String,
    val name: String,

    @JsonProperty("preview_url")
    val previewUrl: String?,

    @JsonProperty("is_playable")
    val isPlayable: Boolean,

    val explicit: Boolean,

    @JsonProperty("duration_ms")
    val durationInMillis: Int,

    @JsonProperty("album")
    val albumMetadata: AlbumMetadataResponse
)

fun TrackResponseWithAlbumMetadata.toTrackSearchResult(): TrackSearchResult {
    return TrackSearchResult(
        id = id,
        name = name,
        imageUrlString = albumMetadata.images.getImageResponseForImageSize(MapperImageSize.LARGE).url,
        artistsString = albumMetadata.artists.joinToString(",") { it.name },
        trackUrlString = previewUrl,
        duration = durationInMillis / 1000,
        trackPosition = 0,
        audioDownloadAllowed = false,
        audioDownloadUrl = "",
        shareUrl = "",
        shortUrl = ""
    )
}