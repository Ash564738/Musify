package com.example.musify.data.repositories.tracksrepository

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.paging.PagingData
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import com.example.musify.data.remote.musicservice.JamendoService
import com.example.musify.utils.Constants.DEFAULT_ALBUM_IMAGE_URL
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
        return try {
            val connectivityManager = Contexts.getApplication(context)
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isConnected = connectivityManager.activeNetwork != null
            Log.d(TAG, "Network connection status: $isConnected")
            isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network connection: ${e.message}", e)
            false
        }
    }

    override suspend fun fetchTracksForPlaylistWithId(
        playlistId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchTracksForPlaylistWithId() for playlistId: $playlistId")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            val response = jamendoService.getPlaylistTracks(
                clientId = clientId,
                playlistId = playlistId,
                offset = 0,
                limit = 200
            )
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val body = response.body() ?: return FetchedResource.Failure(MusifyErrorType.EMPTY_RESPONSE)
                if (body.headers.code != 0) {
                    Log.e(TAG, "API error: ${body.headers.errorMessage}")
                    return FetchedResource.Failure(MusifyErrorType.API_ERROR)
                }
                val tracks = body.results.flatMap { playlist -> playlist.tracks }
                    .mapNotNull { jamendoTrack ->
                        if (jamendoTrack.audioUrl.isNullOrEmpty()) {
                            Log.w(TAG, "Skipping track ${jamendoTrack.id} – no audio URL")
                            null
                        } else {
                            jamendoTrack.toTrackSearchResult().copy(
                                trackPosition = jamendoTrack.position?.toIntOrNull() ?: 0
                            )
                        }
                    }
                Log.d(TAG, "Successfully parsed ${tracks.size} tracks")
                FetchedResource.Success(tracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch tracks: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchTracksForPlaylistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchTracksForPlaylistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    override suspend fun fetchTracksForAlbumWithId(
        albumId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchTracksForAlbumWithId() for albumId: $albumId")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            val response = jamendoService.getAlbumTracks(
                albumId = albumId,
                offset = 0,
                limit = 200
            )
            if (!response.isSuccessful) {
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful) {
                val body = response.body() ?: return FetchedResource.Failure(MusifyErrorType.EMPTY_RESPONSE)
                if (body.headers.code != 0) {
                    Log.e(TAG, "API error: ${body.headers.errorMessage}")
                    return FetchedResource.Failure(MusifyErrorType.API_ERROR)
                }
                val album = body.results.firstOrNull() ?: return FetchedResource.Failure(MusifyErrorType.EMPTY_RESPONSE)
                val albumCover = album.image.ifEmpty {
                    "https://usercontent.jamendo.com?type=album&id=${album.id}&width=300"
                }
                val tracks = album.tracks.map { jamendoTrack ->
                    jamendoTrack.toTrackSearchResult().copy(
                        imageUrlString = albumCover
                    )
                }
                Log.d(TAG, "Successfully parsed ${tracks.size} tracks")
                FetchedResource.Success(tracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch tracks: $errorMessage")
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

    override suspend fun fetchTracksForArtistWithId(
        artistId: String
    ): FetchedResource<List<SearchResult.TrackSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchTracksForArtistWithId() for artistId: $artistId")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
            }
            val response = jamendoService.getArtistTracks(clientId, artistId, limit = 10)
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            if (response.isSuccessful) {
                val tracks = response.body()?.results?.map { it.toTrackSearchResult() } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${tracks.size} tracks")
                FetchedResource.Success(tracks)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch tracks: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchTracksForArtistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchTracksForArtistWithId: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    override fun getPaginatedStreamForPlaylistTracks(
        playlistId: String
    ): Flow<PagingData<SearchResult.TrackSearchResult>> {
        Log.d(TAG, "Starting getPaginatedStreamForPlaylistTracks() for playlistId: $playlistId")
        return try {
            androidx.paging.Pager(pagingConfig) {
                com.example.musify.data.paging.JamendoPlaylistTracksPagingSource(
                    playlistId = playlistId,
                    jamendoService = jamendoService,
                    clientId = clientId
                )
            }.flow
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getPaginatedStreamForPlaylistTracks: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "MusifyTracksRepository"
    }
}