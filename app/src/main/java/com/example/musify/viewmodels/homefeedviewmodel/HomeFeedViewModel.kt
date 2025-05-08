package com.example.musify.viewmodels.homefeedviewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.data.remote.musicservice.SupportedSpotifyGenres
import com.example.musify.data.repositories.homefeedrepository.HomeFeedRepository
import com.example.musify.data.utils.FetchedResource
import com.example.musify.domain.*
import com.example.musify.viewmodels.homefeedviewmodel.greetingphrasegenerator.GreetingPhraseGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    application: Application,
    greetingPhraseGenerator: GreetingPhraseGenerator,
    private val homeFeedRepository: HomeFeedRepository,
) : AndroidViewModel(application) {
    private val _homeFeedCarousels = mutableStateOf<List<HomeFeedCarousel>>(emptyList())
    private val _uiState = mutableStateOf<HomeFeedUiState>(HomeFeedUiState.IDLE)
    val uiState = _uiState as State<HomeFeedUiState>
    val homeFeedCarousels = _homeFeedCarousels as State<List<HomeFeedCarousel>>
    val greetingPhrase = greetingPhraseGenerator.generatePhrase()

    init {
        fetchAndAssignHomeFeedCarousels()
    }
    private fun fetchAndAssignHomeFeedCarousels() {
        viewModelScope.launch {
            _uiState.value = HomeFeedUiState.LOADING
            val carousels = mutableListOf<HomeFeedCarousel>()

            try {
                val albumsResult = homeFeedRepository.fetchNewlyReleasedAlbums()
                val playlistsResult = homeFeedRepository.fetchPlaylistsByGenre(
                    genre = SupportedSpotifyGenres.POP,
                    country = "US"
                )
                when (albumsResult) {
                    is FetchedResource.Success -> {
                        carousels.add(
                            HomeFeedCarousel(
                                id = "new_albums",
                                title = "New Releases",
                                associatedCards = albumsResult.data.map {
                                    toHomeFeedCarouselCardInfo(it)
                                }
                            )
                        )
                    }
                    is FetchedResource.Failure -> {
                        Log.e("HomeFeedViewModel", "Failed to fetch albums: ${albumsResult.cause}")
                    }
                }
                when (playlistsResult) {
                    is FetchedResource.Success -> {
                        carousels.add(
                            HomeFeedCarousel(
                                id = "featured_playlists",
                                title = "Featured Playlists",
                                associatedCards = playlistsResult.data.map {
                                    toHomeFeedCarouselCardInfo(it)
                                }
                            )
                        )
                    }
                    is FetchedResource.Failure -> {
                        Log.e("HomeFeedViewModel", "Failed to fetch playlists: ${playlistsResult.cause}")
                    }
                }
                _homeFeedCarousels.value = carousels
                _uiState.value = if (carousels.isNotEmpty()) HomeFeedUiState.IDLE
                else HomeFeedUiState.ERROR
            } catch (e: Exception) {
                Log.e("HomeFeedViewModel", "Fetch error", e)
                _uiState.value = HomeFeedUiState.ERROR
            }
        }
    }

    fun refreshFeed() {
        if (_uiState.value == HomeFeedUiState.LOADING) return
        fetchAndAssignHomeFeedCarousels()
    }

    private suspend fun <FetchedResourceType> Deferred<FetchedResource<FetchedResourceType, MusifyErrorType>>.awaitFetchedResourceUpdatingUiState(
        onSuccess: (FetchedResourceType) -> Unit
    ) {
        try {
            awaitFetchedResource(
                onError = { errorType ->
                    Log.e("HomeFeedViewModel", "Error fetching resource: $errorType")
                    if (_uiState.value != HomeFeedUiState.ERROR) {
                        _uiState.value = HomeFeedUiState.ERROR
                    }
                },
                onSuccess = { result ->
                    Log.d("HomeFeedViewModel", "Resource fetched successfully: $result")
                    onSuccess(result)
                    if (_uiState.value != HomeFeedUiState.IDLE) {
                        _uiState.value = HomeFeedUiState.IDLE
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("HomeFeedViewModel", "Unexpected error while fetching resource", e)
            _uiState.value = HomeFeedUiState.ERROR
        }
    }

    private suspend fun <FetchedResourceType> Deferred<FetchedResource<FetchedResourceType, MusifyErrorType>>.awaitFetchedResource(
        onError: (MusifyErrorType) -> Unit, onSuccess: (FetchedResourceType) -> Unit
    ) {
        try {
            val fetchedResourceResult = this.await()
            when (fetchedResourceResult) {
                is FetchedResource.Success -> {
                    Log.d("HomeFeedViewModel", "Fetched resource successfully: ${fetchedResourceResult.data}")
                    onSuccess(fetchedResourceResult.data)
                }
                is FetchedResource.Failure -> {
                    val error = fetchedResourceResult.cause
                    Log.e("HomeFeedViewModel", "Failed to fetch resource: $error, Partial data: ${fetchedResourceResult.data}")
                    onError(error)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFeedViewModel", "Unexpected exception while awaiting resource", e)
            onError(MusifyErrorType.UNKNOWN_ERROR)
        }
    }

    private fun toHomeFeedCarouselCardInfo(searchResult: SearchResult): HomeFeedCarouselCardInfo =
        when (searchResult) {
            is SearchResult.AlbumSearchResult -> {
                HomeFeedCarouselCardInfo(
                    id = searchResult.id,
                    imageUrlString = searchResult.albumArtUrlString,
                    caption = searchResult.name,
                    associatedSearchResult = searchResult
                )
            }
            is SearchResult.PlaylistSearchResult -> {
                HomeFeedCarouselCardInfo(
                    id = searchResult.id,
                    imageUrlString = searchResult.imageUrlString ?: "",
                    caption = searchResult.name,
                    associatedSearchResult = searchResult
                )
            }
            else -> throw java.lang.IllegalArgumentException("The method supports only the mapping of AlbumSearchResult and PlaylistSearchResult subclasses")
        }

    enum class HomeFeedUiState { IDLE, LOADING, ERROR }
}