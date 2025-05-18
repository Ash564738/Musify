package com.example.musify.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.musify.data.repositories.favouritesongrepository.SongRepository
import com.example.musify.domain.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteSongsViewModel @Inject constructor(
    private val repository: SongRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "FavoriteSongsViewModel"
    }

    val favoriteSongs: LiveData<List<Song>> = repository.allFavorites
        .catch { e ->
            Log.e(TAG, "Error fetching favorite songs", e)
            emit(emptyList())
        }
        .asLiveData()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Toggling favorite status for song: ${song.id}")
                val isFav = try {
                    repository.isFavorite(song.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking if song is favorite: ${song.id}", e)
                    false
                }

                if (isFav) {
                    try {
                        Log.d(TAG, "Removing song from favorites: ${song.id}")
                        repository.removeFromFavorite(song)
                        Log.d(TAG, "Song removed from favorites successfully: ${song.id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing song from favorites: ${song.id}", e)
                    }
                } else {
                    try {
                        Log.d(TAG, "Adding song to favorites: ${song.id}")
                        repository.addToFavorite(song)
                        Log.d(TAG, "Song added to favorites successfully: ${song.id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding song to favorites: ${song.id}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite status for song: ${song.id}", e)
            }
        }
    }

    fun isFavoriteFlow(songId: String): Flow<Boolean> {
        return try {
            Log.d(TAG, "Fetching favorite status flow for song: $songId")
            repository.isFavoriteFlow(songId)
                .catch { e ->
                    Log.e(TAG, "Error in isFavoriteFlow for song: $songId", e)
                    emit(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching favorite status flow for song: $songId", e)
            throw e
        }
    }
}