package com.example.musify.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.musify.domain.HomeFeedCarouselCardInfo
import com.example.musify.domain.HomeFeedFilters
import com.example.musify.domain.SearchResult
import com.example.musify.domain.Streamable
import com.example.musify.ui.dynamicTheme.dynamicbackgroundmodifier.DynamicBackgroundResource
import com.example.musify.ui.dynamicTheme.dynamicbackgroundmodifier.dynamicBackground
import com.example.musify.ui.screens.GetPremiumScreen
import com.example.musify.ui.screens.favoritescreen.FavoriteScreen
import com.example.musify.ui.screens.homescreen.HomeScreen
import com.example.musify.ui.screens.searchscreen.PagingItemsForSearchScreen
import com.example.musify.ui.screens.searchscreen.SearchScreen
import com.example.musify.viewmodels.AuthViewModel
import com.example.musify.viewmodels.FavoriteSongsViewModel
import com.example.musify.viewmodels.PlaybackViewModel
import com.example.musify.viewmodels.homefeedviewmodel.HomeFeedViewModel
import com.example.musify.viewmodels.searchviewmodel.SearchFilter
import com.example.musify.viewmodels.searchviewmodel.SearchScreenUiState
import com.example.musify.viewmodels.searchviewmodel.SearchViewModel

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun MusifyNavigation(
    navController: NavHostController,
    playStreamable: (Streamable) -> Unit,
    onPausePlayback: () -> Unit,
    isFullScreenNowPlayingOverlayScreenVisible: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = "${MusifyBottomNavigationDestinations.Home.route}.graph"
    ) {
        navGraphWithDetailScreens(
            navGraphRoute = "${MusifyBottomNavigationDestinations.Home.route}.graph",
            startDestination = MusifyNavigationDestinations.HomeScreen.route,
            navController = navController,
            playStreamable = playStreamable,
            onPausePlayback = onPausePlayback
        ) { nestedController ->
            homeScreen(
                route = MusifyNavigationDestinations.HomeScreen.route,
                onCarouselCardClicked = { cardInfo ->
                    nestedController.navigateToDetailScreen(cardInfo.associatedSearchResult)
                }
            )
        }

        navGraphWithDetailScreens(
            navGraphRoute = "${MusifyBottomNavigationDestinations.Search.route}.graph",
            startDestination = MusifyNavigationDestinations.SearchScreen.route,
            navController = navController,
            playStreamable = playStreamable,
            onPausePlayback = onPausePlayback
        ) { nestedController ->
            searchScreen(
                route = MusifyNavigationDestinations.SearchScreen.route,
                onSearchResultClicked = nestedController::navigateToDetailScreen,
                isFullScreenNowPlayingScreenOverlayVisible = isFullScreenNowPlayingOverlayScreenVisible
            )
        }

        navGraphWithDetailScreens(
            navGraphRoute = "${MusifyBottomNavigationDestinations.Playlist.route}.graph",
            startDestination = MusifyNavigationDestinations.PlaylistScreen.route,
            navController = navController,
            playStreamable = playStreamable,
            onPausePlayback = onPausePlayback
        ) { nestedController ->
            playlistScreen(
                route = MusifyNavigationDestinations.PlaylistScreen.route,
                onNavigateToPlaylistSongs = { playlist ->
                    val route = MusifyNavigationDestinations.PlaylistDetailScreen.buildUserRoute(playlist.id)
                    nestedController.navigateToDetailScreen(route)
                }
            )
        }

        composable(MusifyBottomNavigationDestinations.Premium.route) {
            GetPremiumScreen()
        }

        composable(MusifyBottomNavigationDestinations.Favorite.route) {
            val favoriteSongsViewModel: FavoriteSongsViewModel = hiltViewModel()
            val playbackViewModel: PlaybackViewModel = hiltViewModel()
            FavoriteScreen(
                viewModel = favoriteSongsViewModel,
                playbackViewModel = playbackViewModel
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
private fun NavGraphBuilder.homeScreen(
    route: String,
    onCarouselCardClicked: (HomeFeedCarouselCardInfo) -> Unit
) {
    composable(route) {
        val authViewModel: AuthViewModel = hiltViewModel()
        val homeFeedViewModel = hiltViewModel<HomeFeedViewModel>()
        val filters = remember {
            listOf(
                HomeFeedFilters.Music,
                HomeFeedFilters.PodcastsAndShows
            )
        }
        HomeScreen(
            timeBasedGreeting = homeFeedViewModel.greetingPhrase,
            homeFeedFilters = filters,
            currentlySelectedHomeFeedFilter = HomeFeedFilters.None,
            onHomeFeedFilterClick = {},
            carousels = homeFeedViewModel.homeFeedCarousels.value,
            onHomeFeedCarouselCardClick = onCarouselCardClicked,
            onErrorRetryButtonClick = homeFeedViewModel::refreshFeed,
            isLoading = homeFeedViewModel.uiState.value == HomeFeedViewModel.HomeFeedUiState.LOADING,
            isErrorMessageVisible = homeFeedViewModel.uiState.value == HomeFeedViewModel.HomeFeedUiState.ERROR,
            onSignOut = { authViewModel.logout() }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
private fun NavGraphBuilder.searchScreen(
    route: String,
    onSearchResultClicked: (SearchResult) -> Unit,
    isFullScreenNowPlayingScreenOverlayVisible: Boolean,
) {
    composable(route = route) {
        val viewModel = hiltViewModel<SearchViewModel>()
        val albums = viewModel.albumListForSearchQuery.collectAsLazyPagingItems()
        val artists = viewModel.artistListForSearchQuery.collectAsLazyPagingItems()
        val playlists = viewModel.playlistListForSearchQuery.collectAsLazyPagingItems()
        val tracks = viewModel.trackListForSearchQuery.collectAsLazyPagingItems()
        val podcasts = viewModel.podcastListForSearchQuery.collectAsLazyPagingItems()
        val episodes = viewModel.episodeListForSearchQuery.collectAsLazyPagingItems()
        val pagingItems = remember {
            PagingItemsForSearchScreen(
                albums,
                artists,
                tracks,
                playlists,
                podcasts,
                episodes
            )
        }
        val uiState by viewModel.uiState
        val isLoadingError by remember {
            derivedStateOf {
                tracks.loadState.refresh is LoadState.Error || tracks.loadState.append is LoadState.Error || tracks.loadState.prepend is LoadState.Error
            }
        }
        val controller = LocalSoftwareKeyboardController.current
        val genres = remember { viewModel.getAvailableGenres() }
        val filters = remember { SearchFilter.values().toList() }
        val currentlySelectedFilter by viewModel.currentlySelectedFilter
        val dynamicBackgroundResource by remember {
            derivedStateOf {
                val imageUrl = when (currentlySelectedFilter) {
                    SearchFilter.ALBUMS -> albums.itemSnapshotList.firstOrNull()?.albumArtUrlString
                    SearchFilter.TRACKS -> tracks.itemSnapshotList.firstOrNull()?.imageUrlString
                    SearchFilter.ARTISTS -> artists.itemSnapshotList.firstOrNull()?.imageUrlString
                    SearchFilter.PLAYLISTS -> playlists.itemSnapshotList.firstOrNull()?.imageUrlString
                    SearchFilter.PODCASTS -> podcasts.itemSnapshotList.firstOrNull()?.imageUrlString
                }
                if (imageUrl == null) DynamicBackgroundResource.Empty
                else DynamicBackgroundResource.FromImageUrl(imageUrl)
            }
        }
        val currentlyPlayingTrack by viewModel.currentlyPlayingTrackStream.collectAsState(initial = null)
        Box(modifier = Modifier.dynamicBackground(dynamicBackgroundResource)) {
            SearchScreen(
                genreList = genres,
                searchScreenFilters = filters,
                onGenreItemClick = {},
                onSearchTextChanged = viewModel::search,
                isLoading = uiState == SearchScreenUiState.LOADING,
                pagingItems = pagingItems,
                onSearchQueryItemClicked = onSearchResultClicked,
                currentlySelectedFilter = viewModel.currentlySelectedFilter.value,
                onSearchFilterChanged = viewModel::updateSearchFilter,
                isSearchErrorMessageVisible = isLoadingError,
                onImeDoneButtonClicked = {
                    if (isLoadingError) viewModel.search(it)
                    controller?.hide()
                },
                currentlyPlayingTrack = currentlyPlayingTrack,
                isFullScreenNowPlayingOverlayScreenVisible = isFullScreenNowPlayingScreenOverlayVisible,
                onErrorRetryButtonClick = viewModel::search
            )
        }
    }
}