package com.example.musify.ui.screens.favoritescreen

import com.example.musify.viewmodels.FavoriteSongsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musify.domain.Song
import com.example.musify.domain.toTrackSearchResult
import com.example.musify.ui.components.MusifyCompactTrackCard
import com.example.musify.viewmodels.PlaybackViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteSongsViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val favoriteTracks by viewModel.favoriteSongs.observeAsState(emptyList())
    val playbackState = playbackViewModel.playbackState.value
    val currentlyPlayingTrack = when (playbackState) {
        is PlaybackViewModel.PlaybackState.Playing -> playbackState.currentlyPlayingStreamable as? Song
        is PlaybackViewModel.PlaybackState.Paused -> playbackState.currentlyPlayingStreamable as? Song
        else -> null
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h5
                    )
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (favoriteTracks.isEmpty()) {
                Text(text = "Your favorite tracks will appear here.")
            } else {
                LazyColumn {
                    items(favoriteTracks) { song ->
                        val isFavorite by viewModel.isFavoriteFlow(song.id).collectAsState(initial = false)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MusifyCompactTrackCard(
                                modifier = Modifier.weight(1f),
                                track = song.toTrackSearchResult(),
                                onClick = { playbackViewModel.resumeIfPausedOrPlay(song) },
                                isLoadingPlaceholderVisible = false,
                                isCurrentlyPlaying = song.id == currentlyPlayingTrack?.id,
                                isAlbumArtVisible = true,
                                contentPadding = PaddingValues(16.dp)
                            )
                            FavoriteToggleButton(
                                isFavorite = isFavorite,
                                onToggle = { viewModel.toggleFavorite(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun FavoriteToggleButton(
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) MaterialTheme.colors.primary else LocalContentColor.current
        )
    }
}