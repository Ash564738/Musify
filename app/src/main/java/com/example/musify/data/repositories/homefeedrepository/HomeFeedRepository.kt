package com.example.musify.data.repositories.homefeedrepository

import com.example.musify.data.remote.musicservice.SupportedSpotifyGenres
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.FeaturedPlaylists
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.PlaylistsForCategory
import com.example.musify.domain.SearchResult

interface HomeFeedRepository {
//    suspend fun fetchFeaturedPlaylistsForCurrentTimeStamp(
//        timestampMillis: Long,
//        countryCode: String,
//        languageCode: ISO6391LanguageCode,
//    ): FetchedResource<FeaturedPlaylists, MusifyErrorType>
//
//    suspend fun fetchPlaylistsBasedOnCategoriesAvailableForCountry(
//        countryCode: String,
//        languageCode: ISO6391LanguageCode,
//    ): FetchedResource<List<PlaylistsForCategory>, MusifyErrorType>

    suspend fun fetchNewlyReleasedAlbums(
        countryCode: String
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType>

    suspend fun fetchPlaylistsByGenre(
        genre: SupportedSpotifyGenres,
        country: String
    ): FetchedResource<List<SearchResult.PlaylistSearchResult>, MusifyErrorType>
}