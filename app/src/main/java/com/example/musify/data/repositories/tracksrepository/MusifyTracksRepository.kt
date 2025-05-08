package com.example.musify.data.repositories.tracksrepository

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.paging.PagingData
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.Genre
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import com.example.musify.data.remote.musicservice.JamendoService
import dagger.hilt.android.internal.Contexts
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

class MusifyTracksRepository @Inject constructor(
    private val jamendoService: JamendoService,
    private val pagingConfig: androidx.paging.PagingConfig,
    @com.example.musify.data.remote.musicservice.ClientId private val clientId: String,
    private val context: Context
) : TracksRepository {

    private fun hasNetworkConnection(): Boolean {
        val connectivityManager = Contexts.getApplication(context)
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork?.let { true } ?: false
    }

    override suspend fun fetchTopTenTracksForArtistWithId(
        artistId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            val response = jamendoService.getArtistTracks(
                clientId = clientId,
                artistId = artistId,
                limit = 10
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val tracks = response.body()?.results?.map { it.toTrackSearchResult() } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${tracks.size} tracks")
                FetchedResource.Success(tracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch top 10 tracks: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchTopTenTracksForArtistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchTopTenTracksForArtistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    override suspend fun fetchTracksForGenre(
        genre: Genre
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            val response = jamendoService.getFeaturedPlaylists(
                clientId = clientId,
                offset = 0,
                limit = 20
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val tracks = response.body()?.results?.flatMap { playlist ->
                    listOf(SearchResult.TrackSearchResult(
                        id = playlist.id,
                        name = playlist.name,
                        imageUrlString = "https://usercontent.jamendo.com?type=playlist&id=${playlist.id}&width=300",
                        artistsString = playlist.userName ?: "Unknown Artist",
                        trackUrlString = ""
                    ))
                } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${tracks.size} tracks")
                FetchedResource.Success(tracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch tracks for genre: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchTracksForGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchTracksForGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    override suspend fun fetchTracksForAlbumWithId(
        albumId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            Log.d(TAG, "Fetching tracks for album: $albumId")

            val response = jamendoService.getAlbumTracks(
                clientId = clientId,
                albumId = albumId,
                offset = 0,
                limit = 20
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val rawTracks = response.body()?.results ?: emptyList()
                val playableTracks = rawTracks.mapNotNull { track ->
                    if (track.audioUrl != null && track.audioUrl.isNotBlank()) {
                        track.toTrackSearchResult()
                    } else {
                        Log.w(TAG, "Skipping track ${track.id} - Missing audio URL")
                        null
                    }
                }
                Log.d(TAG, "Successfully parsed ${playableTracks.size} tracks")
                FetchedResource.Success(playableTracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch tracks for album: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchTracksForAlbumWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchTracksForAlbumWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    override fun getPaginatedStreamForPlaylistTracks(
        playlistId: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>> {
        Log.d(TAG, "Fetching paginated stream for playlist with ID: $playlistId")
        return androidx.paging.Pager(pagingConfig) {
            com.example.musify.data.paging.JamendoPlaylistTracksPagingSource(
                playlistId = playlistId,
                jamendoService = jamendoService,
                clientId = clientId
            )
        }.flow
    }

    companion object {
        private const val TAG = "MusifyTracksRepository"
    }
}