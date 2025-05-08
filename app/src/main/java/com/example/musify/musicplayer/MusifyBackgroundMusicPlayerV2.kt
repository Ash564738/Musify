package com.example.musify.musicplayer

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import com.example.musify.R
import com.example.musify.domain.Streamable
import com.example.musify.musicplayer.utils.MediaDescriptionAdapter
import com.example.musify.musicplayer.utils.getCurrentPlaybackProgressFlow
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import android.util.Log

@UnstableApi
class MusifyBackgroundMusicPlayerV2 @Inject constructor(
    @ApplicationContext context: Context,
    private val exoPlayer: ExoPlayer
) : MusicPlayerV2 {
    private var currentlyPlayingStreamable: Streamable? = null
    private val notificationManagerBuilder by lazy {
        PlayerNotificationManager.Builder(context, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
    }

    override val currentPlaybackStateStream: Flow<MusicPlayerV2.PlaybackState> = callbackFlow {
        val listener = createEventsListener { player, events ->
            try {
                Log.d(TAG, "Player events received: $events")
                if (!events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAYER_ERROR,
                        Player.EVENT_IS_PLAYING_CHANGED,
                        Player.EVENT_IS_LOADING_CHANGED
                    )
                ) return@createEventsListener

                val isPlaying =
                    events.contains(Player.EVENT_IS_PLAYING_CHANGED) && player.playbackState == Player.STATE_READY && player.playWhenReady
                val isPaused =
                    events.contains(Player.EVENT_IS_PLAYING_CHANGED) && player.playbackState == Player.STATE_READY && !player.playWhenReady
                val newPlaybackState = when {
                    events.contains(Player.EVENT_PLAYER_ERROR) -> {
                        Log.e(TAG, "Player error occurred")
                        MusicPlayerV2.PlaybackState.Error
                    }
                    isPlaying -> {
                        Log.d(TAG, "Player is playing")
                        currentlyPlayingStreamable?.let { buildPlayingState(it, player) }
                    }
                    isPaused -> {
                        Log.d(TAG, "Player is paused")
                        currentlyPlayingStreamable?.let(MusicPlayerV2.PlaybackState::Paused)
                    }
                    player.playbackState == Player.STATE_IDLE -> {
                        Log.d(TAG, "Player is idle")
                        MusicPlayerV2.PlaybackState.Idle
                    }
                    player.playbackState == Player.STATE_ENDED -> {
                        Log.d(TAG, "Playback ended")
                        currentlyPlayingStreamable?.let(MusicPlayerV2.PlaybackState::Ended)
                    }
                    player.isLoading -> {
                        Log.d(TAG, "Player is loading")
                        MusicPlayerV2.PlaybackState.Loading(previouslyPlayingStreamable = currentlyPlayingStreamable)
                    }
                    else -> null
                } ?: return@createEventsListener
                trySend(newPlaybackState)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing player events: ${e.message}", e)
            }
        }
        exoPlayer.addListener(listener)

        awaitClose {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                exoPlayer.removeListener(listener)
                Log.d(TAG, "Player listener removed")
            }
        }
    }.distinctUntilChanged()
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(500),
            initialValue = MusicPlayerV2.PlaybackState.Idle
        )

    private fun createEventsListener(onEvents: (Player, Player.Events) -> Unit) =
        object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                try {
                    onEvents(player, events)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in event listener: ${e.message}", e)
                }
            }
        }

    private fun buildPlayingState(
        streamable: Streamable,
        player: Player,
    ) = MusicPlayerV2.PlaybackState.Playing(
        currentlyPlayingStreamable = streamable,
        totalDuration = player.duration,
        currentPlaybackPositionInMillisFlow = player.getCurrentPlaybackProgressFlow()
    )

    @OptIn(UnstableApi::class)
    override fun playStreamable(
        streamable: Streamable,
        associatedAlbumArt: Bitmap
    ) {
        try {
            with(exoPlayer) {
                if (streamable.streamInfo.streamUrl == null) {
                    Log.w(TAG, "Stream URL is null, cannot play streamable")
                    return@with
                }
                if (currentlyPlayingStreamable == streamable) {
                    Log.d(TAG, "Restarting playback for the same streamable")
                    seekTo(0)
                    playWhenReady = true
                    return@with
                }
                if (isPlaying) {
                    Log.d(TAG, "Stopping current playback before starting new streamable")
                    exoPlayer.stop()
                }
                currentlyPlayingStreamable = streamable
                setMediaItem(MediaItem.fromUri(streamable.streamInfo.streamUrl!!))
                prepare()
                val mediaDescriptionAdapter = MediaDescriptionAdapter(
                    getCurrentContentTitle = { streamable.streamInfo.title },
                    getCurrentContentText = { streamable.streamInfo.subtitle },
                    getCurrentLargeIcon = { _, _ -> associatedAlbumArt }
                )
                notificationManagerBuilder
                    .setMediaDescriptionAdapter(mediaDescriptionAdapter)
                    .build().setPlayer(exoPlayer)
                Log.d(TAG, "Starting playback for streamable: ${streamable.streamInfo.title}")
                play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing streamable: ${e.message}", e)
        }
    }

    override fun pauseCurrentlyPlayingTrack() {
        try {
            Log.d(TAG, "Pausing currently playing track")
            exoPlayer.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing track: ${e.message}", e)
        }
    }

    override fun stopPlayingTrack() {
        try {
            Log.d(TAG, "Stopping playback")
            exoPlayer.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback: ${e.message}", e)
        }
    }

    override fun tryResume(): Boolean {
        return try {
            val hasPlaybackEnded = exoPlayer.currentPosition > exoPlayer.duration
            if (hasPlaybackEnded) {
                Log.d(TAG, "Cannot resume, playback has ended")
                return false
            }
            if (exoPlayer.isPlaying) {
                Log.d(TAG, "Cannot resume, player is already playing")
                return false
            }
            currentlyPlayingStreamable?.let {
                Log.d(TAG, "Resuming playback")
                exoPlayer.playWhenReady = true
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error trying to resume playback: ${e.message}", e)
            false
        }
    }

    companion object {
        private const val TAG = "MusifyBackgroundMusicPlayerV2"
        private const val NOTIFICATION_CHANNEL_ID =
            "com.example.musify.musicplayer.MusicPlayerV2Service.NOTIFICATION_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
    }
}