package com.example.musify.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.R
import com.example.musify.domain.PodcastEpisode
import com.example.musify.domain.SearchResult
import com.example.musify.domain.Streamable
import com.example.musify.musicplayer.MusicPlayerV2
import com.example.musify.usecases.downloadDrawableFromUrlUseCase.DownloadDrawableFromUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import com.example.musify.domain.Song


@HiltViewModel
class PlaybackViewModel @Inject constructor(
    application: Application,
    private val musicPlayer: MusicPlayerV2,
    private val downloadDrawableFromUrlUseCase: DownloadDrawableFromUrlUseCase
) : AndroidViewModel(application) {

    private val TAG = "PlaybackViewModel"

    private val defaultPlaceholderBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(
            application.resources,
            R.drawable.baseline_album_24
        ) ?: createBitmap(1, 1)
    }

    private val _totalDurationText = mutableStateOf("00:00")
    val totalDurationText: State<String> = _totalDurationText

    private val _playbackState = mutableStateOf<PlaybackState>(PlaybackState.Idle)
    val playbackState: State<PlaybackState> = _playbackState

    private val _eventChannel = Channel<Event?>()
    val playbackEventsFlow = _eventChannel.receiveAsFlow()

    val progressPercentFlow = mutableStateOf<Flow<Float>>(emptyFlow())
    val progressTextFlow = mutableStateOf<Flow<String>>(emptyFlow())

    private val genericErrorMessage = "An error occurred. Please check your internet connection."

    init {
        musicPlayer.currentPlaybackStateStream
            .onEach { state ->
                _playbackState.value = when (state) {
                    is MusicPlayerV2.PlaybackState.Loading ->
                        PlaybackState.Loading(state.previouslyPlayingStreamable)
                    is MusicPlayerV2.PlaybackState.Idle ->
                        PlaybackState.Idle
                    is MusicPlayerV2.PlaybackState.Playing -> {
                        _totalDurationText.value = convertMillisToTimestamp(state.totalDuration)
                        progressPercentFlow.value = state.currentPlaybackPositionInMillisFlow
                            .map { pos -> (pos.toFloat() / state.totalDuration) * 100f }
                        progressTextFlow.value = state.currentPlaybackPositionInMillisFlow
                            .map(::convertMillisToTimestamp)
                        PlaybackState.Playing(state.currentlyPlayingStreamable)
                    }
                    is MusicPlayerV2.PlaybackState.Paused ->
                        PlaybackState.Paused(state.currentlyPlayingStreamable)
                    is MusicPlayerV2.PlaybackState.Error -> {
                        viewModelScope.launch {
                            _eventChannel.send(Event.PlaybackError(genericErrorMessage))
                        }
                        PlaybackState.Error(genericErrorMessage)
                    }
                    is MusicPlayerV2.PlaybackState.Ended ->
                        PlaybackState.PlaybackEnded(state.streamable)
                }
            }
            .launchIn(viewModelScope)
    }

    fun resumeIfPausedOrPlay(streamable: Streamable) {
        try {
            Log.d(TAG, "resumeIfPausedOrPlay: $streamable")
            if (musicPlayer.tryResume()) {
                Log.d(TAG, "Resumed playback successfully.")
            } else {
                playStreamable(streamable)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in resumeIfPausedOrPlay", e)
            viewModelScope.launch {
                _eventChannel.send(Event.PlaybackError("Unexpected error while resuming playback."))
            }
        }
    }

    fun playStreamable(streamable: Streamable) {
        viewModelScope.launch {
            Log.d(TAG, "playStreamable: $streamable")

            if (streamable.streamInfo.streamUrl == null) {
                val type = when (streamable) {
                    is PodcastEpisode -> "podcast episode"
                    is SearchResult.TrackSearchResult -> "track"
                    is Song -> "song"
                }
                val msg = "This $type is unavailable for playback."
                Log.e(TAG, msg)
                _eventChannel.send(Event.PlaybackError(msg))
                return@launch
            }

            val streamableId = when (streamable) {
                is SearchResult.TrackSearchResult -> streamable.id
                is PodcastEpisode -> streamable.id
                is Song -> streamable.id
            }

            val imageUrl = streamable.streamInfo.imageUrl
            if (imageUrl.isBlank()) {
                Log.w(TAG, "No album art URL for $streamableId, using placeholder.")
                musicPlayer.playStreamable(streamable, defaultPlaceholderBitmap)
                return@launch
            }

            val downloadResult = downloadDrawableFromUrlUseCase.invoke(
                urlString = imageUrl,
                context = getApplication()
            )

            if (downloadResult.isSuccess) {
                val bitmap = downloadResult.getOrNull()!!.toBitmap()
                Log.d(TAG, "Downloaded album art successfully.")
                musicPlayer.playStreamable(streamable, bitmap)
            } else {
                val causeMsg = downloadResult.exceptionOrNull()?.message
                Log.e(TAG, "Album art download failed: $causeMsg")
                _eventChannel.send(Event.PlaybackError(genericErrorMessage))
                musicPlayer.playStreamable(streamable, defaultPlaceholderBitmap)
            }
        }
    }

    fun pauseCurrentlyPlayingTrack() {
        try {
            Log.d(TAG, "pauseCurrentlyPlayingTrack")
            musicPlayer.pauseCurrentlyPlayingTrack()
        } catch (e: Exception) {
            Log.e(TAG, "Error in pauseCurrentlyPlayingTrack", e)
            viewModelScope.launch {
                _eventChannel.send(Event.PlaybackError("Unexpected error while pausing playback."))
            }
        }
    }

    private fun convertMillisToTimestamp(millis: Long): String = with(TimeUnit.MILLISECONDS) {
        if (toHours(millis) == 0L) {
            "%02d:%02d".format(toMinutes(millis), toSeconds(millis) % 60)
        } else {
            "%02d:%02d:%02d".format(
                toHours(millis),
                toMinutes(millis) % 60,
                toSeconds(millis) % 60
            )
        }
    }

    companion object {
        val PLAYBACK_PROGRESS_RANGE = 0f..100f
    }

    sealed class PlaybackState(
        val currentlyPlayingStreamable: Streamable? = null,
        val previouslyPlayingStreamable: Streamable? = null
    ) {
        object Idle : PlaybackState()
        data class Loading(val previous: Streamable?) : PlaybackState(previouslyPlayingStreamable = previous)
        data class Playing(val streamable: Streamable) : PlaybackState(streamable)
        data class Paused(val streamable: Streamable) : PlaybackState(streamable)
        data class PlaybackEnded(val streamable: Streamable) : PlaybackState(streamable)
        data class Error(val errorMessage: String) : PlaybackState()
    }

    sealed class Event {
        data class PlaybackError(val errorMessage: String) : Event()
    }
}