package com.example.musify.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.musify.R
import com.example.musify.domain.Playlist
import com.example.musify.domain.PodcastEpisode
import com.example.musify.domain.SearchResult
import com.example.musify.domain.Streamable
import com.example.musify.ui.components.DefaultMusifyErrorMessage
import com.example.musify.ui.components.DefaultMusifyLoadingAnimation
import com.example.musify.ui.screens.detailscreens.AlbumDetailScreen
import com.example.musify.ui.screens.detailscreens.ArtistDetailScreen
import com.example.musify.ui.screens.detailscreens.PlaylistDetailScreen
import com.example.musify.ui.screens.detailscreens.PodcastEpisodeDetailScreen
import com.example.musify.ui.screens.playlistscreen.PlaylistScreen
import com.example.musify.ui.screens.podcastshowdetailscreen.PodcastShowDetailScreen
import com.example.musify.viewmodels.*
import com.example.musify.viewmodels.artistviewmodel.ArtistDetailScreenUiState
import com.example.musify.viewmodels.artistviewmodel.ArtistDetailViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@ExperimentalMaterialApi
fun NavGraphBuilder.navGraphWithDetailScreens(
    navGraphRoute: String,
    navController: NavHostController,
    playStreamable: (Streamable) -> Unit,
    onPausePlayback: () -> Unit,
    startDestination: String,
    builder: NavGraphBuilder.(nestedController: NavGraphWithDetailScreensNestedController) -> Unit
) {
    val onBackButtonClicked = {
        navController.popBackStack()
        Unit
    }
    val nestedController = NavGraphWithDetailScreensNestedController(
        navController = navController,
        associatedNavGraphRoute = navGraphRoute,
        playTrack = playStreamable
    )
    navigation(
        route = navGraphRoute,
        startDestination = startDestination
    ) {
        builder(nestedController)
        artistDetailScreen(
            route = MusifyNavigationDestinations
                .ArtistDetailScreen
                .prefixedWithRouteOfNavGraphRoute(navGraphRoute),
            arguments = listOf(
                navArgument(MusifyNavigationDestinations.ArtistDetailScreen.NAV_ARG_ENCODED_IMAGE_URL_STRING) {
                    nullable = true
                }
            ),
            onBackButtonClicked = onBackButtonClicked,
            onAlbumClicked = nestedController::navigateToDetailScreen,
            onPlayTrack = playStreamable
        )
        albumDetailScreen(
            route = MusifyNavigationDestinations
                .AlbumDetailScreen
                .prefixedWithRouteOfNavGraphRoute(navGraphRoute),
            onBackButtonClicked = onBackButtonClicked,
            onPlayTrack = playStreamable
        )
        playlistDetailScreen(
            route = MusifyNavigationDestinations
                .PlaylistDetailScreen
                .prefixedWithRouteOfNavGraphRoute(navGraphRoute),
            onBackButtonClicked = onBackButtonClicked,
            onPlayTrack = playStreamable
        )
        podcastEpisodeDetailScreen(
            route = MusifyNavigationDestinations
                .PodcastEpisodeDetailScreen
                .prefixedWithRouteOfNavGraphRoute(navGraphRoute),
            onBackButtonClicked = onBackButtonClicked,
            onPlayButtonClicked = playStreamable,
            onPauseButtonClicked = onPausePlayback,
            navigateToPodcastShowDetailScreen = nestedController::navigateToDetailScreen
        )

        podcastShowDetailScreen(
            route = MusifyNavigationDestinations
                .PodcastShowDetailScreen
                .prefixedWithRouteOfNavGraphRoute(navGraphRoute),
            onEpisodePlayButtonClicked = playStreamable,
            onEpisodePauseButtonClicked = { onPausePlayback() },
            onEpisodeClicked = playStreamable,
            onBackButtonClicked = onBackButtonClicked
        )

    }
}

@ExperimentalMaterialApi
fun NavGraphBuilder.playlistScreen(
    route: String,
    onNavigateToPlaylistSongs: (Playlist) -> Unit
) {
    composable(route) {
        val playlistViewModel: PlaylistViewModel = hiltViewModel()
        PlaylistScreen(
            viewModel = playlistViewModel,
            onNavigateToPlaylistSongs = onNavigateToPlaylistSongs
        )
    }
}

@ExperimentalMaterialApi
private fun NavGraphBuilder.artistDetailScreen(
    route: String,
    onBackButtonClicked: () -> Unit,
    onPlayTrack: (SearchResult.TrackSearchResult) -> Unit,
    onAlbumClicked: (SearchResult.AlbumSearchResult) -> Unit,
    arguments: List<NamedNavArgument> = emptyList()
) {
    composable(route, arguments) { backStackEntry ->
        val viewModel = hiltViewModel<ArtistDetailViewModel>(backStackEntry)
        val arguments = backStackEntry.arguments!!
        val artistName =
            arguments.getString(MusifyNavigationDestinations.ArtistDetailScreen.NAV_ARG_ARTIST_NAME)!!
        val artistImageUrlString =
            arguments.getString(MusifyNavigationDestinations.ArtistDetailScreen.NAV_ARG_ENCODED_IMAGE_URL_STRING)
                ?.run { URLDecoder.decode(this, StandardCharsets.UTF_8.toString()) }
        val releases = viewModel.albumsOfArtistFlow.collectAsLazyPagingItems()
        val uiState by viewModel.uiState
        val currentlyPlayingTrack by viewModel.currentlyPlayingTrackStream.collectAsState(initial = null)
        ArtistDetailScreen(
            artistName = artistName,
            artistImageUrlString = artistImageUrlString,
            popularTracks = viewModel.popularTracks.value,
            releases = releases,
            currentlyPlayingTrack = currentlyPlayingTrack,
            onBackButtonClicked = onBackButtonClicked,
            onPlayButtonClicked = {},
            onTrackClicked = onPlayTrack,
            onAlbumClicked = onAlbumClicked,
            isLoading = uiState is ArtistDetailScreenUiState.Loading,
            fallbackImageRes = R.drawable.ic_outline_account_circle_24,
            isErrorMessageVisible = uiState is ArtistDetailScreenUiState.Error
        )
    }
}

@ExperimentalMaterialApi
private fun NavGraphBuilder.albumDetailScreen(
    route: String,
    onBackButtonClicked: () -> Unit,
    onPlayTrack: (SearchResult.TrackSearchResult) -> Unit
) {
    composable(route) { backStackEntry ->
        val arguments = backStackEntry.arguments!!
        val viewModel = hiltViewModel<AlbumDetailViewModel>()
        val albumArtUrl =
            arguments.getString(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_ENCODED_IMAGE_URL_STRING)!!
        val albumName =
            arguments.getString(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_ALBUM_NAME)!!
        val artists =
            arguments.getString(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_ARTISTS_STRING)!!
        val yearOfRelease =
            arguments.getString(MusifyNavigationDestinations.AlbumDetailScreen.NAV_ARG_YEAR_OF_RELEASE_STRING)!!
        val currentlyPlayingTrack by viewModel.currentlyPlayingTrackStream.collectAsState(initial = null)
        AlbumDetailScreen(
            albumName = albumName,
            artistsString = artists,
            yearOfRelease = yearOfRelease,
            albumArtUrlString = albumArtUrl,
            trackList = viewModel.tracks.value,
            onTrackItemClick = onPlayTrack,
            onBackButtonClicked = onBackButtonClicked,
            isLoading = viewModel.uiState.value is AlbumDetailUiState.Loading,
            isErrorMessageVisible = viewModel.uiState.value is AlbumDetailUiState.Error,
            currentlyPlayingTrack = currentlyPlayingTrack
        )
    }
}

@ExperimentalMaterialApi
private fun NavGraphBuilder.playlistDetailScreen(
    route: String,
    onBackButtonClicked: () -> Unit,
    onPlayTrack: (SearchResult.TrackSearchResult) -> Unit,
    navigationArguments: List<NamedNavArgument> = emptyList()
) {
    composable(
        route = route,
        arguments = listOf(
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_SOURCE) { type = NavType.StringType },
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_PLAYLIST_ID) { type = NavType.StringType },
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_PLAYLIST_NAME) { nullable = true },
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_OWNER_NAME) { nullable = true },
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_NUMBER_OF_TRACKS) { nullable = true },
            navArgument(MusifyNavigationDestinations.PlaylistDetailScreen.NAV_ARG_ENCODED_IMAGE_URL_STRING) { nullable = true }
        )
    ) { backStackEntry ->
        val viewModel = hiltViewModel<PlaylistDetailViewModel>()
        val metadata by remember { derivedStateOf { viewModel.getPlaylistMetadata() } }
        val tracks = viewModel.tracks.collectAsLazyPagingItems()
        val currentlyPlayingTrack by viewModel.currentlyPlayingTrackStream.collectAsState(initial = null)
        val isPlaybackLoading by viewModel.playbackLoadingStateStream.collectAsState(initial = false)

        val isErrorMessageVisible by remember {
            derivedStateOf {
                tracks.loadState.refresh is LoadState.Error ||
                        tracks.loadState.append is LoadState.Error ||
                        tracks.loadState.prepend is LoadState.Error
            }
        }

        PlaylistDetailScreen(
            playlistName = metadata.name,
            playlistImageUrlString = metadata.imageUrl,
            nameOfPlaylistOwner = metadata.owner,
            totalNumberOfTracks = metadata.trackCount,
            imageResToUseWhenImageUrlStringIsNull = R.drawable.ic_outline_account_circle_24,
            tracks = tracks,
            currentlyPlayingTrack = currentlyPlayingTrack,
            onBackButtonClicked = onBackButtonClicked,
            onTrackClicked = onPlayTrack,
            isLoading = tracks.loadState.refresh is LoadState.Loading || isPlaybackLoading,
            isErrorMessageVisible = isErrorMessageVisible
        )
    }
}

class NavGraphWithDetailScreensNestedController(
    val navController: NavHostController,
    val associatedNavGraphRoute: String,
    private val playTrack: (SearchResult.TrackSearchResult) -> Unit
) {
    fun navigateToDetailScreen(podcastEpisode: PodcastEpisode) {
        val route = MusifyNavigationDestinations
            .PodcastShowDetailScreen
            .buildRoute(podcastEpisode.podcastShowInfo.id)
        navController.navigate(associatedNavGraphRoute + route) { launchSingleTop = true }
    }
    fun navigateToDetailScreen(route: String) {
        val fullRoute = "$associatedNavGraphRoute/$route"
        navController.navigate(fullRoute) {
            launchSingleTop = true
        }
    }
    fun navigateToDetailScreen(searchResult: SearchResult) {
        val route = when (searchResult) {
            is SearchResult.AlbumSearchResult -> MusifyNavigationDestinations
                .AlbumDetailScreen
                .buildRoute(searchResult)
            is SearchResult.ArtistSearchResult -> MusifyNavigationDestinations
                .ArtistDetailScreen
                .buildRoute(searchResult)

            is SearchResult.PlaylistSearchResult -> MusifyNavigationDestinations
                .PlaylistDetailScreen
                .buildJamendoRoute(searchResult)

            is SearchResult.TrackSearchResult -> {
                playTrack(searchResult)
                return
            }
            is SearchResult.PodcastSearchResult -> {
                MusifyNavigationDestinations.PodcastShowDetailScreen.buildRoute(searchResult.id)
            }
            is SearchResult.EpisodeSearchResult -> {
                MusifyNavigationDestinations.PodcastEpisodeDetailScreen.buildRoute(searchResult.id)
            }
        }
        navController.navigate("$associatedNavGraphRoute/$route") {
            launchSingleTop = true
        }
    }
}

private fun NavGraphBuilder.podcastEpisodeDetailScreen(
    route: String,
    onPlayButtonClicked: (PodcastEpisode) -> Unit,
    onPauseButtonClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    navigateToPodcastShowDetailScreen: (PodcastEpisode) -> Unit
) {
    composable(route = route) {
        val viewModel = hiltViewModel<PodcastEpisodeDetailViewModel>()

        val uiState = viewModel.uiState
        val isEpisodeCurrentlyPlaying = viewModel.isEpisodeCurrentlyPlaying
        if (viewModel.podcastEpisode == null) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState == PodcastEpisodeDetailViewModel.UiSate.LOADING) {
                    DefaultMusifyLoadingAnimation(
                        modifier = Modifier.align(Alignment.Center),
                        isVisible = true
                    )
                }
                if (uiState == PodcastEpisodeDetailViewModel.UiSate.ERROR) {
                    DefaultMusifyErrorMessage(
                        modifier = Modifier.align(Alignment.Center),
                        title = "Oops! Something doesn't look right",
                        subtitle = "Please check the internet connection",
                        onRetryButtonClicked = viewModel::retryFetchingEpisode
                    )
                }
            }
        } else {
            PodcastEpisodeDetailScreen(
                podcastEpisode = viewModel.podcastEpisode!!,
                isEpisodeCurrentlyPlaying = isEpisodeCurrentlyPlaying,
                isPlaybackLoading = uiState == PodcastEpisodeDetailViewModel.UiSate.PLAYBACK_LOADING,
                onPlayButtonClicked = {
                    onPlayButtonClicked(viewModel.podcastEpisode!!)
                },
                onPauseButtonClicked = { onPauseButtonClicked() },
                onShareButtonClicked = {},
                onAddButtonClicked = {},
                onDownloadButtonClicked = {},
                onBackButtonClicked = onBackButtonClicked,
                navigateToPodcastDetailScreen = {
                    viewModel.podcastEpisode?.let { navigateToPodcastShowDetailScreen(it) }
                }
            )
        }
    }
}

@ExperimentalMaterialApi
private fun NavGraphBuilder.podcastShowDetailScreen(
    route: String,
    onEpisodePlayButtonClicked: (PodcastEpisode) -> Unit,
    onEpisodePauseButtonClicked: (PodcastEpisode) -> Unit,
    onEpisodeClicked: (PodcastEpisode) -> Unit,
    onBackButtonClicked: () -> Unit
) {
    composable(route = route) {
        val viewModel = hiltViewModel<PodcastShowDetailViewModel>()
        val uiState = viewModel.uiState
        val episodesForShow = viewModel.episodesForShowStream.collectAsLazyPagingItems()
        if (viewModel.podcastShow == null) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState == PodcastShowDetailViewModel.UiState.LOADING) {
                    DefaultMusifyLoadingAnimation(
                        modifier = Modifier.align(Alignment.Center),
                        isVisible = true
                    )
                }
                if (uiState == PodcastShowDetailViewModel.UiState.ERROR) {
                    DefaultMusifyErrorMessage(
                        modifier = Modifier.align(Alignment.Center),
                        title = "Oops! Something doesn't look right",
                        subtitle = "Please check the internet connection",
                        onRetryButtonClicked = viewModel::retryFetchingShow
                    )
                }
            }
        } else {
            PodcastShowDetailScreen(
                podcastShow = viewModel.podcastShow!!,
                onBackButtonClicked = onBackButtonClicked,
                onEpisodePlayButtonClicked = onEpisodePlayButtonClicked,
                onEpisodePauseButtonClicked = onEpisodePauseButtonClicked,
                currentlyPlayingEpisode = viewModel.currentlyPlayingEpisode,
                isCurrentlyPlayingEpisodePaused = viewModel.isCurrentlyPlayingEpisodePaused,
                isPlaybackLoading = uiState == PodcastShowDetailViewModel.UiState.PLAYBACK_LOADING,
                onEpisodeClicked = onEpisodeClicked,
                episodes = episodesForShow
            )
        }
    }
}

private fun MusifyNavigationDestinations.prefixedWithRouteOfNavGraphRoute(routeOfNavGraph: String) =
    "$routeOfNavGraph/${this.route}"