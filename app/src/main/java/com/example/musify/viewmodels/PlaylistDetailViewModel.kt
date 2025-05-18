package com.example.musify.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.musify.data.repositories.playlistrepository.PlaylistRepository
import com.example.musify.data.repositories.tracksrepository.TracksRepository
import com.example.musify.domain.Playlist
import com.example.musify.domain.SearchResult
import com.example.musify.ui.navigation.MusifyNavigationDestinations
import com.example.musify.usecases.getCurrentlyPlayingTrackUseCase.GetCurrentlyPlayingTrackUseCase
import com.example.musify.usecases.getPlaybackLoadingStatusUseCase.GetPlaybackLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val tracksRepository: TracksRepository,
    private val playlistRepository: PlaylistRepository,
    getCurrentlyPlayingTrackUseCase: GetCurrentlyPlayingTrackUseCase,
    getPlaybackLoadingStatusUseCase: GetPlaybackLoadingStatusUseCase,
) : AndroidViewModel(application) {
    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()
    private val source = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_SOURCE)!!
    private val playlistId = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_PLAYLIST_ID)!!
    val isUserPlaylist = source == "user"
    private val jamendoPlaylistName = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_PLAYLIST_NAME)
    private val jamendoOwnerName = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_OWNER_NAME)
    private val jamendoNumberOfTracks = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_NUMBER_OF_TRACKS)
    private val jamendoImageUrl = savedStateHandle.get<String>(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_ENCODED_IMAGE_URL_STRING)?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    private val _userPlaylist = MutableStateFlow<Playlist?>(null)
    val userPlaylist: StateFlow<Playlist?> = _userPlaylist.asStateFlow()
    private val _loadingError = MutableStateFlow(false)
    val loadingError: StateFlow<Boolean> = _loadingError.asStateFlow()
    val playbackLoadingStateStream: Flow<Boolean> = getPlaybackLoadingStatusUseCase.loadingStatusStream
    val currentlyPlayingTrackStream: Flow<SearchResult.TrackSearchResult?> = getCurrentlyPlayingTrackUseCase.currentlyPlayingTrackStream

    init {
        Log.d(TAG, "Initializing PlaylistDetailViewModel with source: $source, playlistId: $playlistId")
        fetchUserPlaylists()
        if (isUserPlaylist) {
            loadSpecificUserPlaylist()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tracks: Flow<PagingData<SearchResult.TrackSearchResult>> = when (source) {
        "user" -> _userPlaylist
            .filterNotNull()
            .flatMapLatest { playlist ->
                if (playlist.songIds.isEmpty()) flowOf(PagingData.empty())
                else tracksRepository.getPaginatedStreamForTrackIds(playlist.songIds)
            }
        "jamendo" -> tracksRepository.getPaginatedStreamForPlaylistTracks(playlistId) // Add this
        else -> emptyFlow()
    }.cachedIn(viewModelScope)

    private fun loadSpecificUserPlaylist() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading specific user playlist with ID: $playlistId")
                playlistRepository.getPlaylist(playlistId)
                    .collect { playlist ->
                        _userPlaylist.value = playlist
                        _loadingError.value = false
                        Log.d(TAG, "User playlist loaded successfully: $playlist")
                    }
            } catch (e: Exception) {
                _loadingError.value = true
                Log.e(TAG, "Error loading specific user playlist: ${e.message}", e)
            }
        }
    }

    private fun fetchUserPlaylists() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching all user playlists...")
                playlistRepository.getAllPlaylists().collect { playlists ->
                    _userPlaylists.value = playlists
                    Log.d(TAG, "User playlists fetched successfully: $playlists")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user playlists: ${e.message}", e)
            }
        }
    }

    suspend fun createNewPlaylistAndAddTrack(playlistName: String, trackId: String): String {
        return try {
            Log.d(TAG, "Creating new playlist with name: $playlistName and adding track ID: $trackId")
            val playlistId = playlistRepository.createPlaylist(playlistName)
            playlistRepository.addToPlaylist(playlistId, listOf(trackId))
            Log.d(TAG, "Playlist created successfully with ID: $playlistId")
            playlistId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating new playlist: ${e.message}", e)
            throw e
        }
    }

    fun addTrackToSelectedPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            if (trackId.toIntOrNull() == null) {
                Log.e(TAG, "Invalid track ID: $trackId. Must be an integer.")
                return@launch
            }
            try {
                Log.d(TAG, "Adding track ID: $trackId to playlist ID: $playlistId")
                playlistRepository.addToPlaylist(playlistId, listOf(trackId))
                Log.d(TAG, "Track added successfully to playlist ID: $playlistId")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to selected playlist: ${e.message}", e)
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                playlistRepository.removeFromPlaylist(playlistId, trackId)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing track: ${e.message}", e)
            }
        }
    }
    val currentPlaylistId = playlistId

    fun getPlaylistMetadata(): PlaylistMetadata {
        return try {
            Log.d(TAG, "Retrieving playlist metadata for source: $source")
            when (source) {
                "jamendo" -> PlaylistMetadata(
                    name = jamendoPlaylistName ?: "",
                    owner = jamendoOwnerName ?: "",
                    trackCount = jamendoNumberOfTracks ?: "0",
                    imageUrl = jamendoImageUrl
                )
                "user" -> {
                    val playlist = _userPlaylist.value
                    PlaylistMetadata(
                        name = playlist?.name ?: "",
                        owner = "Your Playlist",
                        trackCount = playlist?.songIds?.size?.toString() ?: "0",
                        imageUrl = null
                    )
                }
                else -> throw IllegalArgumentException("Invalid source")
            }.also {
                Log.d(TAG, "Playlist metadata retrieved successfully: $it")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving playlist metadata: ${e.message}", e)
            throw e
        }
    }
    data class PlaylistMetadata(
        val name: String,
        val owner: String,
        val trackCount: String,
        val imageUrl: String?
    )
    companion object {
        private const val TAG = "PlaylistDetailViewModel"
    }
}