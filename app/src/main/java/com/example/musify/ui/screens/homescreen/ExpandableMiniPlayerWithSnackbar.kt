package com.example.musify.ui.screens.homescreen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musify.domain.Streamable
import com.example.musify.ui.components.MusifyMiniPlayer
import com.example.musify.ui.screens.NowPlayingScreen
import com.example.musify.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.Flow

@ExperimentalAnimationApi
@Composable
fun ExpandableMiniPlayerWithSnackbar(
    streamable: Streamable,
    onPauseButtonClicked: () -> Unit,
    onPlayButtonClicked: (Streamable) -> Unit,
    isPlaybackPaused: Boolean,
    timeElapsedStringFlow: Flow<String>,
    playbackProgressFlow: Flow<Float>,
    totalDurationOfCurrentTrackText: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var isNowPlayingScreenVisible by remember { mutableStateOf(false) }
    AnimatedContent(
        modifier = modifier,
        targetState = isNowPlayingScreenVisible,
        transitionSpec = {
            slideInVertically(animationSpec = tween()).togetherWith(shrinkOut(animationSpec = tween()))
        }
    ) { isFullScreenVisible ->
        if (isFullScreenVisible) {
            Box {
                NowPlayingScreen(
                    streamable = streamable,
                    isPlaybackPaused = isPlaybackPaused,
                    timeElapsedStringFlow = timeElapsedStringFlow,
                    totalDurationOfCurrentTrackProvider = { totalDurationOfCurrentTrackText },
                    playbackDurationRange = PlaybackViewModel.PLAYBACK_PROGRESS_RANGE,
                    playbackProgressFlow = playbackProgressFlow,
                    onCloseButtonClicked = { isNowPlayingScreenVisible = false },
                    onShuffleButtonClicked = {},
                    onSkipPreviousButtonClicked = {},
                    onPlayButtonClicked = { onPlayButtonClicked(streamable) },
                    onPauseButtonClicked = onPauseButtonClicked,
                    onSkipNextButtonClicked = {},
                    onRepeatButtonClicked = {},
                    viewModel = viewModel()
                )
                SnackbarHost(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                    hostState = snackbarHostState
                )
            }

        } else {
            Column {
                SnackbarHost(
                    modifier = Modifier.padding(8.dp),
                    hostState = snackbarHostState
                )
                MusifyMiniPlayer(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { isNowPlayingScreenVisible = true },
                    isPlaybackPaused = isPlaybackPaused,
                    streamable = streamable,
                    onPlayButtonClicked = { onPlayButtonClicked(streamable) },
                    onPauseButtonClicked = onPauseButtonClicked
                )
            }
        }
    }
}

