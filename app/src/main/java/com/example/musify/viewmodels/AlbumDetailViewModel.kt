package com.example.musify.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.musify.data.repositories.tracksrepository.TracksRepository
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.SearchResult
import com.example.musify.ui.navigation.MusifyNavigationDestinations
import com.example.musify.usecases.getCurrentlyPlayingTrackUseCase.GetCurrentlyPlayingTrackUseCase
import com.example.musify.usecases.getPlaybackLoadingStatusUseCase.GetPlaybackLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : AndroidViewModel(application) {

    private val _tracks = mutableStateOf<List<SearchResult.TrackSearchResult>>(emptyList())
    val tracks = _tracks as State<List<SearchResult.TrackSearchResult>>

    private val _uiState = mutableStateOf<AlbumDetailUiState>(AlbumDetailUiState.Idle)
    val uiState = _uiState as State<AlbumDetailUiState>

    private val albumId = savedStateHandle.get<String>(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_ALBUM_ID)!!
    val currentlyPlayingTrackStream = getCurrentlyPlayingTrackUseCase.currentlyPlayingTrackStream

    init {
        try {
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