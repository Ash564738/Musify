package com.example.musify.data.repositories.searchrepository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.musify.data.paging.*
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.response.toSearchResults
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.example.musify.data.repositories.tokenrepository.runCatchingWithToken
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import com.example.musify.domain.SearchResults
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MusifySearchRepository @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val spotifyService: SpotifyService,
    private val pagingConfig: PagingConfig
) : SearchRepository {

    override suspend fun fetchSearchResultsForQuery(
        searchQuery: String,
        countryCode: String
    ): FetchedResource<SearchResults, MusifyErrorType> {
        Log.d("MusifySearchRepository", "fetchSearchResultsForQuery: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.search(searchQuery, countryCode, it).toSearchResults()
            }.also {
                Log.d("MusifySearchRepository", "fetchSearchResultsForQuery: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "fetchSearchResultsForQuery: Error | searchQuery=$searchQuery, error=${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override fun getPaginatedSearchStreamForAlbums(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.AlbumSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForAlbums: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyAlbumSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForAlbums: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForAlbums: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }

    override fun getPaginatedSearchStreamForArtists(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.ArtistSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForArtists: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyArtistSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForArtists: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForArtists: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }

    override fun getPaginatedSearchStreamForTracks(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForTracks: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyTrackSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForTracks: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForTracks: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }

    override fun getPaginatedSearchStreamForPlaylists(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.PlaylistSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPlaylists: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyPlaylistSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPlaylists: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForPlaylists: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }

    override fun getPaginatedSearchStreamForPodcasts(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.PodcastSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPodcasts: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyPodcastSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPodcasts: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForPodcasts: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }

    override fun getPaginatedSearchStreamForEpisodes(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.EpisodeSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForEpisodes: Start | searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            Pager(pagingConfig) {
                SpotifyEpisodeSearchPagingSource(
                    searchQuery = searchQuery,
                    countryCode = countryCode,
                    tokenRepository = tokenRepository,
                    spotifyService = spotifyService
                )
            }.flow.also {
                Log.d("MusifySearchRepository", "getPaginatedSearchStreamForEpisodes: Success | searchQuery=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "getPaginatedSearchStreamForEpisodes: Error | searchQuery=$searchQuery, error=${e.message}", e)
            throw e
        }
    }
}