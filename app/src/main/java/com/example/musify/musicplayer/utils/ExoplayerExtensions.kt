package com.example.musify.musicplayer.utils

import android.util.Log
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch

fun Player.getCurrentPlaybackProgressFlow() = flow {
    if (!playWhenReady || playbackState != Player.STATE_READY) return@flow
    while (currentPosition <= duration) {
        emit(currentPosition)
        delay(1_000)
    }
    emit(currentPosition)
}.catch { e ->
    if (e is CancellationException) {
        Log.e(TAG, "Error in getCurrentPlaybackProgressFlow: ${e.message}", e)
    } else {
        Log.e(TAG, "Unexpected error in playback flow", e)
        throw e
    }
}.distinctUntilChanged()

private const val TAG = "ExoplayerExtensions"