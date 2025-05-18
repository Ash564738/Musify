package com.example.musify.data.repositories.homefeedrepository

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.example.musify.data.remote.musicservice.JamendoService
import com.example.musify.data.remote.musicservice.SupportedJamendoTags
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.MusifyErrorType
import com.example.musify.domain.SearchResult
import com.example.musify.utils.Constants.DEFAULT_PLAYLIST_IMAGE_URL
import dagger.hilt.android.internal.Contexts
import java.io.IOException
import javax.inject.Inject

class MusifyHomeFeedRepository @Inject constructor(
    private val jamendoService: JamendoService,
    private val context: Context
) : HomeFeedRepository {

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

    override suspend fun fetchNewlyReleasedAlbums(): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchNewlyReleasedAlbums()")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
            val response = jamendoService.getNewAlbums(offset = 0, limit = 20)
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
//            Log.d(TAG, "Response headers: ${response.headers()}")
//            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful && response.body()?.headers?.code == 0) {
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
                        Log.e(TAG, "Error parsing album: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                Log.d(TAG, "Successfully parsed ${albums.size} albums")
                FetchedResource.Success(albums)
            } else {
                val errorMessage = response.body()?.headers?.errorMessage ?: "Unknown error (code ${response.code()})"
                Log.e(TAG, "Failed to fetch newly released albums: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchNewlyReleasedAlbums: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE, emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchNewlyReleasedAlbums: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, emptyList())
        }
    }

    private fun parseReleaseYear(dateString: String): String {
        return try {
            dateString.substring(0, 4)
        } catch (e: Exception) {
            Log.w(TAG, "Invalid date format: $dateString", e)
            ""
        }
    }

    override suspend fun fetchAlbumsByGenre(genre: SupportedJamendoTags): FetchedResource<List<SearchResult.AlbumSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchAlbumsByGenre() for genre: $genre")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
            val response = jamendoService.getAlbumsByTag(tags = genre.toString(), offset = 0, limit = 20)
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
//            Log.d(TAG, "Response headers: ${response.headers()}")
//            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")
            if (response.isSuccessful && response.body()?.headers?.code == 0) {
                val albums = response.body()!!.results.mapNotNull { album ->
                    try {
                        val imageUrl = album.image.ifEmpty {
                            "https://usercontent.jamendo.com?type=artist&id=${album.artistId}&width=300"
                        }
                        SearchResult.AlbumSearchResult(
                            id = album.id,
                            name = album.name,
                            artistsString = album.artistName,
                            albumArtUrlString = imageUrl,
                            yearOfReleaseString = parseReleaseYear(album.releaseDate)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing album: ${e.message}", e)
                        null
                    }
                }
                Log.d(TAG, "Successfully parsed ${albums.size} albums")
                FetchedResource.Success(albums)
            } else {
                val errorMessage = response.body()?.headers?.errorMessage ?: "Unknown error (code ${response.code()})"
                Log.e(TAG, "Failed to fetch albums: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchAlbumsByGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE, emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchAlbumsByGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, emptyList())
        }
    }

    override suspend fun fetchPlaylistsByGenre(genre: SupportedJamendoTags): FetchedResource<List<SearchResult.PlaylistSearchResult>, MusifyErrorType> {
        Log.d(TAG, "Starting fetchPlaylistsByGenre() for genre: $genre")
        return try {
            if (!hasNetworkConnection()) {
                Log.e(TAG, "No internet connection available")
                return FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }

            val response = jamendoService.getPlaylistsByTag(
                tags = genre.toString(), offset = 0, limit = 20
            )
            Log.d(TAG, "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
//            Log.d(TAG, "Response headers: ${response.headers()}")
//            Log.d(TAG, "Response body: ${response.body()?.toString() ?: "null"}")

            if (response.isSuccessful && response.body()?.headers?.code == 0) {
                val playlists = response.body()!!.results.mapNotNull { playlist ->
                    try {
                        val fallbackImage: String? = playlist.image?.takeIf { it.isNotEmpty() }
                        val coverUrl = fallbackImage ?: run {
                            val tracksResp = jamendoService.getPlaylistTracks(
                                playlistId = playlist.id,
                                offset = 0,
                                limit = 1
                            )
                            if (tracksResp.isSuccessful) {
                                tracksResp.body()?.results
                                    ?.firstOrNull()?.tracks
                                    ?.firstOrNull()?.imageUrl
                            } else null
                        } ?: DEFAULT_PLAYLIST_IMAGE_URL

                        SearchResult.PlaylistSearchResult(
                            id = playlist.id,
                            name = playlist.name,
                            ownerName = playlist.userName ?: "Jamendo Community",
                            totalNumberOfTracks = "",
                            imageUrlString = coverUrl
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing playlist: ${e.message}", e)
                        null
                    }
                }
                Log.d(TAG, "Successfully parsed ${playlists.size} playlists")
                FetchedResource.Success(playlists)
            } else {
                val errorMessage = response.body()?.headers?.errorMessage ?: "Unknown error (code ${response.code()})"
                Log.e(TAG, "Failed to fetch playlists: $errorMessage")
                FetchedResource.Failure(MusifyErrorType.NETWORK_ERROR, emptyList())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error in fetchPlaylistsByGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.NETWORK_CONNECTION_FAILURE, emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in fetchPlaylistsByGenre: ${e.message}", e)
            FetchedResource.Failure(MusifyErrorType.UNKNOWN_ERROR, emptyList())
        }
    }

    companion object {
        private const val TAG = "MusifyHomeFeedRepository"
    }
}