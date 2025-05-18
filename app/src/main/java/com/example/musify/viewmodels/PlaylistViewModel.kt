package com.example.musify.viewmodels

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.data.repositories.playlistrepository.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: PlaylistRepository,
) : ViewModel() {

    val playlists = repository.getAllPlaylists()
        .catch { exception ->
            Log.e("PlaylistViewModel", "Error fetching playlists: ${exception.message}", exception)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun renamePlaylist(id: String, newName: String) {
        viewModelScope.launch {
            try {
                Log.d("PlaylistViewModel", "Renaming playlist $id to $newName")
                repository.renamePlaylist(id, newName)
                Log.d("PlaylistViewModel", "Playlist $id renamed successfully")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error renaming playlist $id: ${e.message}", e)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                Log.d("PlaylistViewModel", "Creating playlist with name: $name")
                repository.createPlaylist(name)
                Log.d("PlaylistViewModel", "Playlist created successfully: $name")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error creating playlist: ${e.message}", e)
            }
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            try {
                Log.d("PlaylistViewModel", "Deleting playlist with id: $id")
                repository.deletePlaylist(id)
                Log.d("PlaylistViewModel", "Playlist $id deleted successfully")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error deleting playlist $id: ${e.message}", e)
            }
        }
    }
}