package com.example.musify.data.repositories.homefeedrepository

import com.example.musify.data.remote.musicservice.SupportedJamendoTags
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult

interface HomeFeedRepository {
    suspend fun fetchNewlyReleasedAlbums(
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType>

    suspend fun fetchAlbumsByGenre(
        genre: SupportedJamendoTags
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType>

    suspend fun fetchPlaylistsByGenre(
        genre: SupportedJamendoTags
    ): FetchedResource<List<SearchResult.PlaylistSearchResult>, MusifyErrorType>
}