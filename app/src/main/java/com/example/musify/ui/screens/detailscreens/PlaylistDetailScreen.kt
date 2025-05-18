package com.example.musify.ui.screens.detailscreens

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.example.musify.domain.Playlist
import com.example.musify.domain.SearchResult
import com.example.musify.ui.components.*
import com.example.musify.ui.dynamicTheme.dynamicbackgroundmodifier.DynamicBackgroundResource
import com.example.musify.ui.dynamicTheme.dynamicbackgroundmodifier.dynamicBackground
import com.example.musify.viewmodels.PlaylistDetailViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("StateFlowValueCalledInComposition", "UnrememberedMutableState")
@ExperimentalMaterialApi
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    playlistImageUrlString: String?,
    nameOfPlaylistOwner: String,
    totalNumberOfTracks: String,
    @DrawableRes imageResToUseWhenImageUrlStringIsNull: Int,
    tracks: LazyPagingItems<SearchResult.TrackSearchResult>,
    currentlyPlayingTrack: SearchResult.TrackSearchResult?,
    onBackButtonClicked: () -> Unit,
    onTrackClicked: (SearchResult.TrackSearchResult) -> Unit,
    isLoading: Boolean,
    isErrorMessageVisible: Boolean
) {
    val viewModel = hiltViewModel<PlaylistDetailViewModel>()
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackId by remember { mutableStateOf<String?>(null) }
    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    val isEmptyStateVisible by remember {
        derivedStateOf {
            !isLoading && tracks.itemCount == 0 && !isErrorMessageVisible
        }
    }
    val isPlaylistLoading = viewModel.userPlaylist.value == null && viewModel.isUserPlaylist
    val areTracksLoading = tracks.loadState.refresh is LoadState.Loading
    val isLoading = isPlaylistLoading || areTracksLoading || viewModel.loadingError.value
    var isLoadingPlaceholderForAlbumArtVisible by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val isAppBarVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }
    val dynamicBackgroundResource = remember {
        if (playlistImageUrlString == null) DynamicBackgroundResource.Empty
        else DynamicBackgroundResource.FromImageUrl(playlistImageUrlString)

    }
    val coroutineScope = rememberCoroutineScope()
    val loadState = tracks.loadState.refresh
    if (loadState is LoadState.Error) {
        ErrorMessageComponent(modifier = Modifier.fillMaxSize())
    }
    val validTrackCount by remember { derivedStateOf { tracks.itemCount } }
    val expectedTrackCount = viewModel.userPlaylist.value?.songIds?.size ?: 0
    if (validTrackCount < expectedTrackCount) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.error.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            Text(
                text = "${expectedTrackCount - validTrackCount} tracks unavailable",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = MusifyBottomNavigationConstants.navigationHeight + MusifyMiniPlayerConstants.miniPlayerHeight
            ),
            state = lazyListState
        ) {
            headerWithImageItem(
                dynamicBackgroundResource = dynamicBackgroundResource,
                playlistName = playlistName,
                playlistImageUrlString = playlistImageUrlString,
                imageResToUseWhenImageUrlStringIsNull = imageResToUseWhenImageUrlStringIsNull,
                nameOfPlaylistOwner = nameOfPlaylistOwner,
                totalNumberOfTracks = totalNumberOfTracks,
                isLoadingPlaceholderForAlbumArtVisible = isLoadingPlaceholderForAlbumArtVisible,
                onImageLoading = { isLoadingPlaceholderForAlbumArtVisible = true },
                onImageLoaded = { isLoadingPlaceholderForAlbumArtVisible = false },
                onBackButtonClicked = onBackButtonClicked
            )
            if (isErrorMessageVisible) {
                item {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oops! Something doesn't look right",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Please check the internet connection",
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            } else {
                items(tracks) { track ->
                    track?.let { track ->
                        MusifyCompactTrackCard(
                            track = track,
                            onClick = { onTrackClicked(track) },
                            isLoadingPlaceholderVisible = false,
                            isCurrentlyPlaying = track == currentlyPlayingTrack,
                            isAlbumArtVisible = true,
                            subtitleTextStyle = LocalTextStyle.current.copy(
                                fontWeight = FontWeight.Thin,
                                color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
                            ),
                            contentPadding = PaddingValues(16.dp),
                            onAddToPlaylistClick = {
                                if (viewModel.isUserPlaylist) {
                                    viewModel.removeTrackFromPlaylist(viewModel.currentPlaylistId, track.id)
                                } else {
                                    selectedTrackId = track.id
                                    showAddToPlaylistDialog = true
                                }
                            },
                            showRemoveButton = viewModel.isUserPlaylist,
                            onRemoveClick = {
                                viewModel.removeTrackFromPlaylist(
                                    playlistId = viewModel.currentPlaylistId,
                                    trackId = track.id
                                )
                            }
                        )
                    }
                }
            }
            if (isEmptyStateVisible) {
                item {
                    EmptyStateComponent(
                        modifier = Modifier.fillParentMaxSize(),
                        message = "This playlist is empty"
                    )
                }
            }
            item {
                Spacer(
                    modifier = Modifier
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .padding(bottom = 16.dp)
                )
            }
        }
        if (showAddToPlaylistDialog) {
            AddToPlaylistDialog(
                userPlaylistsFlow = viewModel.userPlaylists,
                selectedTrackId = selectedTrackId ?: "",
                onDismiss = { showAddToPlaylistDialog = false },
                onAddToPlaylist = { playlistId, trackId ->
                    viewModel.addTrackToSelectedPlaylist(playlistId, trackId)
                },
                onRemoveFromPlaylist = { playlistId, trackId ->
                    viewModel.removeTrackFromPlaylist(playlistId, trackId)
                },
                onCreateNewPlaylist = { playlistName ->
                    selectedTrackId?.let { trackId ->
                        coroutineScope.launch {
                            viewModel.createNewPlaylistAndAddTrack(playlistName, trackId)
                        }
                    }
                }
            )
        }
        DefaultMusifyLoadingAnimation(
            modifier = Modifier.align(Alignment.Center),
            isVisible = isLoading
        )
        AnimatedVisibility(
            visible = isAppBarVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DetailScreenTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                title = playlistName,
                onBackButtonClicked = onBackButtonClicked,
                dynamicBackgroundResource = dynamicBackgroundResource,
                onClick = {
                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                }
            )
        }
    }
}
@Composable
fun AddToPlaylistDialog(
    userPlaylistsFlow: StateFlow<List<Playlist>>,
    selectedTrackId: String,
    onDismiss: () -> Unit,
    onAddToPlaylist: (String, String) -> Unit,
    onRemoveFromPlaylist: (String, String) -> Unit,
    onCreateNewPlaylist: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userPlaylists by userPlaylistsFlow.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = { errorMessage = null }
                ) { Text("OK") }
            }
        )
    }
    if (showCreateDialog) {
        CreateNewPlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { playlistName ->
                coroutineScope.launch {
                    try {
                        isLoading = true
                        onCreateNewPlaylist(playlistName)
                        showCreateDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Failed to create playlist: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "Create new playlist",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateDialog = true }
                            .padding(16.dp),
                        color = MaterialTheme.colors.primary
                    )
                }

                if (userPlaylists.isEmpty()) {
                    item {
                        Text("No playlists found", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(userPlaylists) { playlist ->
                        val isTrackInPlaylist = playlist.songIds.contains(selectedTrackId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        try {
                                            isLoading = true
                                            if (isTrackInPlaylist) {
                                                onRemoveFromPlaylist(playlist.id, selectedTrackId)
                                            } else {
                                                onAddToPlaylist(playlist.id, selectedTrackId)
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Operation failed: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isTrackInPlaylist,
                                onCheckedChange = null
                            )
                            Text(text = playlist.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable
fun CreateNewPlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist") },
        text = {
            Column {
                TextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(playlistName) },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun ErrorMessageComponent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Failed to load tracks. Please try again.")
    }
}
@Composable
fun EmptyStateComponent(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.h6
        )
    }
}
private fun LazyListScope.headerWithImageItem(
    dynamicBackgroundResource: DynamicBackgroundResource,
    playlistName: String,
    playlistImageUrlString: String?,
    @DrawableRes imageResToUseWhenImageUrlStringIsNull: Int,
    nameOfPlaylistOwner: String,
    totalNumberOfTracks: String,
    isLoadingPlaceholderForAlbumArtVisible: Boolean,
    onImageLoading: () -> Unit,
    onImageLoaded: (Throwable?) -> Unit,
    onBackButtonClicked: () -> Unit
) {
    item {
        Column(
            modifier = Modifier
                .dynamicBackground(dynamicBackgroundResource)
                .statusBarsPadding()
        ) {
            ImageHeaderWithMetadata(
                title = playlistName,
                headerImageSource = if (playlistImageUrlString == null)
                    HeaderImageSource.ImageFromDrawableResource(
                        resourceId = imageResToUseWhenImageUrlStringIsNull
                    )
                else HeaderImageSource.ImageFromUrlString(playlistImageUrlString),
                subtitle = "by $nameOfPlaylistOwner • $totalNumberOfTracks tracks",
                onBackButtonClicked = onBackButtonClicked,
                isLoadingPlaceholderVisible = isLoadingPlaceholderForAlbumArtVisible,
                onImageLoading = onImageLoading,
                onImageLoaded = onImageLoaded,
                additionalMetadataContent = { }
            )
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}