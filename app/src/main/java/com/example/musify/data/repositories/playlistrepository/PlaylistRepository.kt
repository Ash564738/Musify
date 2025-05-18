package com.example.musify.data.repositories.playlistrepository

import android.util.Log
import com.example.musify.domain.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure

class PlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun currentUserId(): String {
        return try {
            Log.d(TAG, "Fetching current user ID")
            auth.currentUser?.uid ?: throw Exception("Not authenticated")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID: ${e.message}", e)
            throw e
        }
    }

    private val playlistsCollection: CollectionReference
        get() = firestore.collection("users/${currentUserId()}/playlists")
    @OptIn(FlowPreview::class, DelicateCoroutinesApi::class)
    fun getAllPlaylists(): Flow<List<Playlist>> = callbackFlow {
        Log.d(TAG, "getAllPlaylists() - Start fetching playlists")
        var listener: ListenerRegistration? = null

        try {
            listener = playlistsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (isClosedForSend) {
                        Log.d(TAG, "Channel already closed, ignoring snapshot update")
                        return@addSnapshotListener
                    }

                    when {
                        error != null -> {
                            Log.e(TAG, "Error fetching playlists", error)
                            close(error)
                        }
                        snapshot != null -> {
                            val playlists = snapshot.toObjects(Playlist::class.java)
                            Log.d(TAG, "Successfully fetched ${playlists.size} playlists")
                            trySend(playlists).onFailure {
                                Log.e(TAG, "Failed to send playlists", it)
                            }
                        }
                        else -> Log.w(TAG, "Snapshot is null")
                    }
                }

            awaitClose {
                Log.d(TAG, "Awaiting close - removing listener")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in flow", e)
            close(e)
        } finally {
            Log.d(TAG, "Finally block - cleaning up")
            listener?.remove()
        }
    }

    suspend fun createPlaylist(name: String): String {
        try {
            val id = playlistsCollection.document().id
            val playlist = Playlist(
                id = id,
                name = name,
                songIds = emptyList(),
                createdAt = Date()
            )
            playlistsCollection.document(id).set(playlist).await()
            return id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun renamePlaylist(id: String, newName: String) {
        try {
            Log.d(TAG, "Renaming playlist $id to $newName")
            playlistsCollection.document(id).update("name", newName).await()
            Log.d(TAG, "Playlist $id renamed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming playlist $id: ${e.message}", e)
            throw e
        }
    }

    suspend fun deletePlaylist(id: String) {
        try {
            Log.d(TAG, "Deleting playlist with ID: $id")
            playlistsCollection.document(id).delete().await()
            Log.d(TAG, "Playlist $id deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting playlist $id: ${e.message}", e)
            throw e
        }
    }

    suspend fun addToPlaylist(playlistId: String, songIds: List<String>) {
        try {
            if (songIds.any { it.toIntOrNull() == null }) {
                Log.e(TAG, "Invalid track IDs: $songIds. All IDs must be integers.")
                return
            }
            Log.d(TAG, "Adding songs $songIds to playlist $playlistId")
            val document = playlistsCollection.document(playlistId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val current = snapshot.toObject(Playlist::class.java)?.songIds ?: emptyList()
                transaction.update(document, "songIds", current + songIds)
            }.await()
            Log.d(TAG, "Songs $songIds added to playlist $playlistId successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding songs to playlist $playlistId: ${e.message}", e)
            throw e
        }
    }

    suspend fun removeFromPlaylist(playlistId: String, songId: String) {
        try {
            Log.d(TAG, "Removing song $songId from playlist $playlistId")
            val document = playlistsCollection.document(playlistId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val current = snapshot.toObject(Playlist::class.java)?.songIds ?: emptyList()
                transaction.update(document, "songIds", current - songId)
            }.await()
            Log.d(TAG, "Song $songId removed from playlist $playlistId successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing song $songId from playlist $playlistId: ${e.message}", e)
            throw e
        }
    }

    fun getPlaylist(id: String): Flow<Playlist?> = callbackFlow {
        Log.d(TAG, "getPlaylist() - Start fetching playlist with ID: $id")
        try {
            val listener = playlistsCollection.document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching playlist $id: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val playlist = snapshot?.toObject(Playlist::class.java)
                    Log.d(TAG, "Fetched playlist: $playlist")
                    trySend(playlist).isSuccess
                }
            awaitClose {
                Log.d(TAG, "getPlaylist() - Listener removed for ID: $id")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getPlaylist flow: ${e.message}", e)
            close(e)
        }
    }
    companion object {
        private const val TAG = "PlaylistRepository"
    }
}