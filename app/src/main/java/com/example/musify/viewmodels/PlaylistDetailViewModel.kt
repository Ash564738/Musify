package com.example.musify.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.musify.data.repositories.tracksrepository.TracksRepository
import com.example.musify.domain.SearchResult
import com.example.musify.ui.navigation.MusifyNavigationDestinations
import com.example.musify.usecases.getCurrentlyPlayingTrackUseCase.GetCurrentlyPlayingTrackUseCase
import com.example.musify.usecases.getPlaybackLoadingStatusUseCase.GetPlaybackLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    tracksRepository: TracksRepository,
    getCurrentlyPlayingTrackUseCase: GetCurrentlyPlayingTrackUseCase,
    getPlaybackLoadingStatusUseCase: GetPlaybackLoadingStatusUseCase,
) : AndroidViewModel(application) {

    private val playlistId: String = savedStateHandle.get<String>(
        MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_PLAYLIST_ID
    ) ?: throw IllegalArgumentException("Playlist ID is missing")

    val playbackLoadingStateStream: Flow<Boolean> = try {
        getPlaybackLoadingStatusUseCase.loadingStatusStream
            .onEach { isLoading ->
                Log.d(TAG, "Playback loading status changed: $isLoading")
            }
            .catch { e ->
                Log.e(TAG, "Error in playbackLoadingStateStream: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error initializing playbackLoadingStateStream: ${e.message}", e)
        throw e
    }

    val currentlyPlayingTrackStream: Flow<SearchResult.TrackSearchResult?> = try {
        getCurrentlyPlayingTrackUseCase.currentlyPlayingTrackStream
            .onEach { track ->
                Log.d(TAG, "Currently playing track updated: $track")
            }
            .catch { e ->
                Log.e(TAG, "Error in currentlyPlayingTrackStream: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error initializing currentlyPlayingTrackStream: ${e.message}", e)
        throw e
    }

    val tracks: Flow<PagingData<SearchResult.TrackSearchResult>> = try {
        tracksRepository.getPaginatedStreamForPlaylistTracks(playlistId)
            .cachedIn(viewModelScope)
            .onEach { pagingData ->
                Log.d(TAG, "Tracks paging data updated")
            }
            .catch { e ->
                Log.e(TAG, "Error in tracks stream: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error initializing tracks stream: ${e.message}", e)
        throw e
    }

    companion object {
        private const val TAG = "PlaylistDetailViewModel"
    }
}