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
        Log.d("MusifySearchRepository", "fetchSearchResultsForQuery called with searchQuery=$searchQuery, countryCode=$countryCode")
        return try {
            tokenRepository.runCatchingWithToken {
                spotifyService.search(searchQuery, countryCode, it).toSearchResults()
            }.also {
                Log.d("MusifySearchRepository", "Successfully fetched search results for query=$searchQuery")
            }
        } catch (e: Exception) {
            Log.e("MusifySearchRepository", "Error fetching search results for query=$searchQuery: ${e.message}", e)
            FetchedResource.Failure(cause = MusifyErrorType.NETWORK_ERROR, data = null)
        }
    }

    override fun getPaginatedSearchStreamForAlbums(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.AlbumSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForAlbums called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyAlbumSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for albums with query=$searchQuery")
        }
    }

    override fun getPaginatedSearchStreamForArtists(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.ArtistSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForArtists called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyArtistSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for artists with query=$searchQuery")
        }
    }

    override fun getPaginatedSearchStreamForTracks(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForTracks called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyTrackSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for tracks with query=$searchQuery")
        }
    }

    override fun getPaginatedSearchStreamForPlaylists(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.PlaylistSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPlaylists called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyPlaylistSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for playlists with query=$searchQuery")
        }
    }

    override fun getPaginatedSearchStreamForPodcasts(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.PodcastSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForPodcasts called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyPodcastSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for podcasts with query=$searchQuery")
        }
    }

    override fun getPaginatedSearchStreamForEpisodes(
        searchQuery: String,
        countryCode: String
    ): Flow<PagingData<SearchResult.EpisodeSearchResult>> {
        Log.d("MusifySearchRepository", "getPaginatedSearchStreamForEpisodes called with searchQuery=$searchQuery, countryCode=$countryCode")
        return Pager(pagingConfig) {
            SpotifyEpisodeSearchPagingSource(
                searchQuery = searchQuery,
                countryCode = countryCode,
                tokenRepository = tokenRepository,
                spotifyService = spotifyService
            )
        }.flow.also {
            Log.d("MusifySearchRepository", "Created paginated stream for episodes with query=$searchQuery")
        }
    }
}