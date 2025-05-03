package com.example.musify.data.remote.response

import com.example.musify.domain.SearchResults
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

/**
 * A response that contains the results of a search operation.
 * All the nested lists may contain nulls, so we skip them at parse time.
 */
data class SearchResultsResponse(
    val tracks: Tracks?,
    val albums: Albums?,
    val artists: Artists?,
    val playlists: Playlists?,
    val shows: Shows?,
    val episodes: Episodes?
) {
    data class Tracks(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<TrackResponseWithAlbumMetadata>
    )
    data class Albums(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<AlbumMetadataResponse>
    )
    data class Artists(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<ArtistResponse>
    )
    data class Playlists(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<PlaylistMetadataResponse>
    )
    data class Shows(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<ShowMetadataResponse>
    )
    data class Episodes(
        @JsonProperty("items")
        @JsonSetter(contentNulls = Nulls.SKIP)
        val value: List<EpisodeMetadataResponse>
    )
}

/**
 * Maps the raw search response into your domain model.
 */
fun SearchResultsResponse.toSearchResults() = SearchResults(
    tracks    = tracks?.value?.map { it.toTrackSearchResult() }            ?: emptyList(),
    albums    = albums?.value?.map { it.toAlbumSearchResult() }            ?: emptyList(),
    artists   = artists?.value?.map { it.toArtistSearchResult() }          ?: emptyList(),
    playlists = playlists?.value?.map { it.toPlaylistSearchResult() }     ?: emptyList(),
    shows     = shows?.value?.map { it.toPodcastSearchResult() }          ?: emptyList(),
    episodes  = episodes?.value?.map { it.toEpisodeSearchResult() }       ?: emptyList()
)