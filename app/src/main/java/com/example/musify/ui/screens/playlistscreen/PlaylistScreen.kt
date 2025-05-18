package com.example.musify.ui.screens.playlistscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musify.domain.Playlist
import com.example.musify.viewmodels.PlaylistViewModel

@ExperimentalMaterialApi
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    onNavigateToPlaylistSongs: (Playlist) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.offset(y = (-100).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create playlist")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Playlist",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(y = (20).dp)
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(playlists) { playlist ->
                    PlaylistItemRow(
                        playlist = playlist,
                        onRename = { playlistToEdit = it },
                        onDelete = { viewModel.deletePlaylist(it.id) },
                        onClick = { onNavigateToPlaylistSongs(playlist) }
                    )
                }
            }
        }

        if (showCreateDialog) {
            TextFieldDialog(
                title = "Create Playlist",
                initialValue = "",
                onDismiss = { showCreateDialog = false },
                onConfirm = { name -> viewModel.createPlaylist(name) }
            )
        }

        playlistToEdit?.let { playlist ->
            TextFieldDialog(
                title = "Rename Playlist",
                initialValue = playlist.name,
                onDismiss = { playlistToEdit = null },
                onConfirm = { newName ->
                    viewModel.renamePlaylist(playlist.id, newName)
                    playlistToEdit = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PlaylistItemRow(
    playlist: Playlist,
    onRename: (Playlist) -> Unit,
    onDelete: (Playlist) -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(10.dp)
            .offset(y = (20).dp),
        text = { Text(playlist.name) },
        trailing = {
            Row {
                IconButton(onClick = { onRename(playlist) }) { Icon(Icons.Default.Edit, null) }
                IconButton(onClick = { onDelete(playlist) }) { Icon(Icons.Default.Delete, null) }
            }
        }
    )
}

@Composable
fun TextFieldDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}