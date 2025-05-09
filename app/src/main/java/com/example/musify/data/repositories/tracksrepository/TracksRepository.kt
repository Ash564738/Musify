package com.example.musify.data.repositories.tracksrepository

import androidx.paging.PagingData
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.Genre
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    suspend fun fetchTracksForArtistWithId(
        artistId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType>

    suspend fun fetchTracksForPlaylistWithId(
        playlistId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType>

    suspend fun fetchTracksForAlbumWithId(
        albumId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType>

    fun getPaginatedStreamForPlaylistTracks(
        playlistId: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>>
}