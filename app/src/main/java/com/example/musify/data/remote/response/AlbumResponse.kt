package com.example.musify.data.remote.response

import android.R.attr.duration
import com.example.musify.data.utils.MapperImageSize
import com.example.musify.data.utils.getImageResponseForImageSize
import com.example.musify.domain.SearchResult
import com.fasterxml.jackson.annotation.JsonProperty

data class AlbumResponse(
    val id: String,
    val name: String,
    @JsonProperty("album_type") val albumType: String,
    val artists: List<ArtistResponseWithNullableImagesAndFollowers>,
    val images: List<ImageResponse>,
    @JsonProperty("release_date") val releaseDate: String,
    @JsonProperty("release_date_precision") val releaseDatePrecision: String,
    @JsonProperty("total_tracks") val totalTracks: Int,
    val tracks: TracksWithoutAlbumMetadataListResponse
) {

    data class TracksWithoutAlbumMetadataListResponse(@JsonProperty("items") val value: List<TrackResponseWithoutAlbumMetadataResponse>)

    data class TrackResponseWithoutAlbumMetadataResponse(
        val id: String,
        val name: String,
        @JsonProperty("preview_url") val previewUrl: String?,
        @JsonProperty("is_playable") val isPlayable: Boolean,
        val explicit: Boolean,
        @JsonProperty("duration_ms") val durationInMillis: Int
    )

    data class ArtistResponseWithNullableImagesAndFollowers(
        val id: String,
        val name: String,
        val images: List<ImageResponse>?,
        val followers: ArtistResponse.Followers?
    )
}

fun AlbumResponse.toAlbumSearchResult() = SearchResult.AlbumSearchResult(
    id = id,
    name = name,
    artistsString = artists.joinToString(",") { it.name },
    yearOfReleaseString = releaseDate,
    albumArtUrlString = images.getImageResponseForImageSize(MapperImageSize.LARGE).url
)


fun AlbumResponse.getTracks(): List<SearchResult.TrackSearchResult> =
    tracks.value.map { trackResponse ->
        trackResponse.toTrackSearchResult(
            albumArtImageUrlString = images.getImageResponseForImageSize(MapperImageSize.LARGE).url,
            albumArtistsString = artists.joinToString(",") { it.name }
        )
    }

fun AlbumResponse.TrackResponseWithoutAlbumMetadataResponse.toTrackSearchResult(
    albumArtImageUrlString: String,
    albumArtistsString: String
) = SearchResult.TrackSearchResult(
    id = id,
    name = name,
    imageUrlString = albumArtImageUrlString,
    artistsString = albumArtistsString,
    trackUrlString = previewUrl,
    duration = durationInMillis / 1000,
    trackPosition = 0,
    audioDownloadAllowed = false,
    audioDownloadUrl = "",
    shareUrl = "",
    shortUrl = ""
)