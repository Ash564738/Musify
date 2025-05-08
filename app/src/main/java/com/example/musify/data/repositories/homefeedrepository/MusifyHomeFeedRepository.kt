package com.example.musify.data.repositories.homefeedrepository

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.example.musify.data.remote.musicservice.JamendoService
import com.example.musify.data.remote.musicservice.SupportedSpotifyGenres
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import dagger.hilt.android.internal.Contexts
import java.io.IOException
import javax.inject.Inject

class MusifyHomeFeedRepository @Inject constructor(
    private val jamendoService: JamendoService,
    private val context: Context
) : HomeFeedRepository {

    private fun hasNetworkConnection(): Boolean {
        val connectivityManager = Contexts.getApplication(context)
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetwork?.let { true } ?: false
    }

    override suspend fun fetchNewlyReleasedAlbums(
    ): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType> {
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
            val response = jamendoService.getNewAlbums(
                offset = 0,
                limit = 20
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful && response.body()?.headers?.code == 0) {
                Log.d(TAG, "Parsing response body for newly released albums")
                val albums = response.body()?.results?.mapNotNull { album ->
                    try {
                        val validImageUrl = if (album.image.isNotEmpty()) album.image else
                            "https://usercontent.jamendo.com?type=artist&id=${album.artistId}&width=300"

                        SearchResult.AlbumSearchResult(
                            id = album.id,
                            name = album.name,
                            artistsString = album.artistName,
                            albumArtUrlString = validImageUrl,
                            yearOfReleaseString = parseReleaseYear(album.releaseDate),
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${albums.size} albums")
                FetchedResource.Success(albums)
            } else {
                val errorMessage = response.body()?.headers?.errorMessage ?: "Unknown error (code ${response.code()})"
                Log.e(TAG, "Failed to fetch newly released albums: $errorMessage")
                FetchedResource.Failure(
                    MusifyErrorType.NETWORK_ERROR,
                    emptyList()
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchNewlyReleasedAlbums: ${e.message}", e)
            FetchedResource.Failure(
                MusifyErrorType.NETWORK_CONNECTION_FAILURE,
                emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchNewlyReleasedAlbums: ${e.message}", e)
            FetchedResource.Failure(
                MusifyErrorType.UNKNOWN_ERROR,
                emptyList()
            )
        }
    }
    private fun parseReleaseYear(dateString: String): String {
        return try {
            dateString.substring(0, 4)
        } catch (e: Exception) {
            Log.w(TAG, "Invalid date format: $dateString")
            ""
        }
    }
    override suspend fun fetchPlaylistsByGenre(
        genre: SupportedSpotifyGenres,
        country: String
    ): FetchedResource<List<SearchResult.PlaylistSearchResult>, MusifyErrorType> {
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
            Log.d(TAG, "Starting fetchPlaylistsByGenre with genre: ${genre.name}, country: $country")

            val response = jamendoService.getFeaturedPlaylists(
                offset = 0,
                limit = 20
            )

            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful && response.body()?.headers?.code == 0) {
                Log.d(TAG, "Parsing response body for playlists")
                val playlists = response.body()?.results?.mapNotNull { playlist ->
                    try {
                        SearchResult.PlaylistSearchResult(
                            id = playlist.id,
                            name = playlist.name,
                            ownerName = playlist.userName ?: "Jamendo Community",
                            totalNumberOfTracks = "",
                            imageUrlString = "https://usercontent.jamendo.com?type=user&id=${playlist.userId}&width=300"
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing playlist data: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${playlists.size} playlists")
                FetchedResource.Success(playlists)
            } else {
                val errorMessage = response.body()?.headers?.errorMessage ?: "Unknown error (code ${response.code()})"
                Log.e(TAG, "Failed to fetch playlists: $errorMessage")
                FetchedResource.Failure(
                    MusifyErrorType.NETWORK_ERROR,
                    emptyList()
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchPlaylistsByGenre: ${e.message}", e)
            FetchedResource.Failure(
                MusifyErrorType.NETWORK_CONNECTION_FAILURE,
                emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchPlaylistsByGenre: ${e.message}", e)
            FetchedResource.Failure(
                MusifyErrorType.UNKNOWN_ERROR,
                emptyList()
            )
        }
    }

    companion object {
        private const val TAG = "MusifyHomeFeedRepository"
    }
}