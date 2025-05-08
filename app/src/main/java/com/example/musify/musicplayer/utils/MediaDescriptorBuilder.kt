package com.example.musify.musicplayer.utils

import android.app.PendingIntent
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager

@OptIn(UnstableApi::class)
fun MediaDescriptionAdapter(
    getCurrentContentTitle: (Player) -> CharSequence,
    getCurrentContentText: (Player) -> CharSequence,
    getCurrentLargeIcon: (Player, PlayerNotificationManager.BitmapCallback) -> Bitmap?,
    createCurrentContentIntent: ((Player) -> PendingIntent?)? = null,
): PlayerNotificationManager.MediaDescriptionAdapter {
    return object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return try {
                Log.d(TAG, "Fetching current content title")
                getCurrentContentTitle(player)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current content title: ${e.message}", e)
                "Unknown Title"
            }
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return try {
                Log.d(TAG, "Creating current content intent")
                createCurrentContentIntent?.invoke(player)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating current content intent: ${e.message}", e)
                null
            }
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return try {
                Log.d(TAG, "Fetching current content text")
                getCurrentContentText(player)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current content text: ${e.message}", e)
                "Unknown Content"
            }
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return try {
                Log.d(TAG, "Fetching current large icon")
                getCurrentLargeIcon(player, callback)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current large icon: ${e.message}", e)
                null
            }
        }
    }
}

private const val TAG = "MediaDescriptionAdapter"