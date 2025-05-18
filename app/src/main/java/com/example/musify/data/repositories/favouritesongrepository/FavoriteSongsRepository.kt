package com.example.musify.data.repositories.favouritesongrepository

import android.util.Log
import com.example.musify.domain.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class SongRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val favoritesCollection: CollectionReference
        get() = firestore.collection("users/${auth.currentUser?.uid}/favorites")

    private suspend fun ensureAuthenticated() {
        try {
            if (auth.currentUser == null) {
                Log.d("SongRepository", "User not authenticated. Signing in anonymously...")
                auth.signInAnonymously().await()
                Log.d("SongRepository", "Anonymous sign-in successful.")
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error during authentication: ${e.message}", e)
            throw e
        }
    }

    val allFavorites: Flow<List<Song>> = callbackFlow {
        try {
            ensureAuthenticated()
            val listener = favoritesCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SongRepository", "Error fetching favorites: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val songs = snapshot?.toObjects(Song::class.java) ?: emptyList()
                Log.d("SongRepository", "Fetched ${songs.size} favorite songs.")
                trySend(songs).isSuccess
            }
            awaitClose { listener.remove() }
        } catch (e: Throwable) {
            if (e is CancellationException) {
                Log.d("SongRepository", "Flow cancelled: ${e.message}")
                throw e
            } else {
                Log.e("SongRepository", "Error in allFavorites flow: ${e.message}", e)
                close(e)
            }
        }
    }.distinctUntilChanged().shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    suspend fun addToFavorite(song: Song) {
        try {
            ensureAuthenticated()
            Log.d("SongRepository", "Adding song to favorites: ${song.id}")
            favoritesCollection.document(song.id).set(song.toMap()).await()
            Log.d("SongRepository", "Song added to favorites: ${song.id}")
        } catch (e: Exception) {
            Log.e("SongRepository", "Error adding song to favorites: ${e.message}", e)
            throw e
        }
    }

    suspend fun isFavorite(songId: String): Boolean {
        return try {
            ensureAuthenticated()
            val exists = favoritesCollection.document(songId).get().await().exists()
            Log.d("SongRepository", "Song $songId is favorite: $exists")
            exists
        } catch (e: Exception) {
            Log.e("SongRepository", "Error checking if song is favorite: ${e.message}", e)
            false
        }
    }

    suspend fun removeFromFavorite(song: Song) {
        try {
            ensureAuthenticated()
            Log.d("SongRepository", "Removing song from favorites: ${song.id}")
            favoritesCollection.document(song.id).delete().await()
            Log.d("SongRepository", "Song removed from favorites: ${song.id}")
        } catch (e: Exception) {
            Log.e("SongRepository", "Error removing song from favorites: ${e.message}", e)
            throw e
        }
    }

    fun isFavoriteFlow(songId: String): Flow<Boolean> = callbackFlow {
        try {
            ensureAuthenticated()
            val listener = favoritesCollection.document(songId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SongRepository", "Error in isFavoriteFlow: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val exists = snapshot?.exists() ?: false
                Log.d("SongRepository", "Song $songId is favorite: $exists")
                trySend(exists).isSuccess
            }
            awaitClose { listener.remove() }
        } catch (e: Throwable) {
            if (e is CancellationException) {
                Log.d("SongRepository", "Flow cancelled: ${e.message}")
                throw e
            } else {
                Log.e("SongRepository", "Error in isFavoriteFlow: ${e.message}", e)
                close(e)
            }
        }
    }.distinctUntilChanged().shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)
}