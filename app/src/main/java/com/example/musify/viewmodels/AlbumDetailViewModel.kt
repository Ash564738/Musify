package com.example.musify.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.musify.data.repositories.playlistrepository.PlaylistRepository
import com.example.musify.data.repositories.tracksrepository.TracksRepository
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.Playlist
import com.example.musify.domain.SearchResult
import com.example.musify.ui.navigation.MusifyNavigationDestinations
import com.example.musify.usecases.getCurrentlyPlayingTrackUseCase.GetCurrentlyPlayingTrackUseCase
import com.example.musify.usecases.getPlaybackLoadingStatusUseCase.GetPlaybackLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AlbumDetailUiState {
    object Idle : AlbumDetailUiState()
    object Loading : AlbumDetailUiState()
    data class Error(private val message: String) : AlbumDetailUiState()
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    getCurrentlyPlayingTrackUseCase: GetCurrentlyPlayingTrackUseCase,
    getPlaybackLoadingStatusUseCase: GetPlaybackLoadingStatusUseCase,
    private val tracksRepository: TracksRepository,
    private val playlistRepository: PlaylistRepository
) : AndroidViewModel(application) {
    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()
    private val _tracks = mutableStateOf<List<SearchResult.TrackSearchResult>>(emptyList())
    val tracks = _tracks as State<List<SearchResult.TrackSearchResult>>
    private val _uiState = mutableStateOf<AlbumDetailUiState>(AlbumDetailUiState.Idle)
    val uiState = _uiState as State<AlbumDetailUiState>
    private val albumId = savedStateHandle.get<String>(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_ALBUM_ID)!!
    val currentlyPlayingTrackStream = getCurrentlyPlayingTrackUseCase.currentlyPlayingTrackStream

    init {
        try {
            fetchUserPlaylists()
            Log.d(TAG, "Initializing AlbumDetailViewModel")
            fetchAndAssignTrackList()
            getPlaybackLoadingStatusUseCase
                .loadingStatusStream
                .onEach { isPlaybackLoading ->
                    try {
                        Log.d(TAG, "Playback loading status changed: $isPlaybackLoading")
                        if (isPlaybackLoading && _uiState.value !is AlbumDetailUiState.Loading) {
                            _uiState.value = AlbumDetailUiState.Loading
                            return@onEach
                        }
                        if (!isPlaybackLoading && _uiState.value is AlbumDetailUiState.Loading) {
                            _uiState.value = AlbumDetailUiState.Idle
                            return@onEach
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling playback loading status: ${e.message}", e)
                    }
                }.launchIn(viewModelScope)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AlbumDetailViewModel: ${e.message}", e)
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
    private fun fetchAndAssignTrackList() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching track list for albumId: $albumId")
                _uiState.value = AlbumDetailUiState.Loading
                val result = tracksRepository.fetchTracksForAlbumWithId(albumId = albumId)
                if (result is FetchedResource.Success) {
                    Log.d(TAG, "Successfully fetched ${result.data.size} tracks")
                    _tracks.value = result.data
                    _uiState.value = AlbumDetailUiState.Idle
                } else {
                    Log.e(TAG, "Failed to fetch tracks: ${result}")
                    _uiState.value =
                        AlbumDetailUiState.Error("Unable to fetch tracks. Please check internet connection.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching track list: ${e.message}", e)
                _uiState.value =
                    AlbumDetailUiState.Error("An unexpected error occurred while fetching tracks.")
            }
        }
    }

    companion object {
        private const val TAG = "AlbumDetailViewModel"
    }
}