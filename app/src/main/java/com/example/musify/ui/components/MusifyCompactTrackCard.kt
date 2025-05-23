package com.example.musify.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.musify.domain.SearchResult

@ExperimentalMaterialApi
@Composable
fun MusifyCompactTrackCard(
    track: SearchResult.TrackSearchResult,
    onClick: (SearchResult.TrackSearchResult) -> Unit,
    isLoadingPlaceholderVisible: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = MaterialTheme.colors.surface,
    isCurrentlyPlaying: Boolean = false,
    isAlbumArtVisible: Boolean = true,
    onImageLoading: ((SearchResult.TrackSearchResult) -> Unit)? = null,
    onImageLoadingFinished: ((SearchResult.TrackSearchResult, Throwable?) -> Unit)? = null,
    titleTextStyle: TextStyle = LocalTextStyle.current,
    subtitleTextStyle: TextStyle = LocalTextStyle.current,
    contentPadding: PaddingValues = MusifyCompactTrackCardDefaults.defaultContentPadding,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
    onAddToPlaylistClick: (() -> Unit)? = null
) {
    val trackPlayingTextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colors.primary
    )
    CompositionLocalProvider(
        LocalContentAlpha.provides(if (track.trackUrlString == null) 0.5f else 1f)
    ) {
        MusifyCompactListItemCard(
            modifier = modifier,
            backgroundColor = backgroundColor,
            shape = shape,
            cardType = ListItemCardType.TRACK,
            thumbnailImageUrlString = if (isAlbumArtVisible) track.imageUrlString else null,
            title = track.name,
            subtitle = track.artistsString,
            onClick = { onClick(track) },
            isLoadingPlaceHolderVisible = isLoadingPlaceholderVisible,
            onThumbnailLoading = { onImageLoading?.invoke(track) },
            onThumbnailImageLoadingFinished = { throwable ->
                onImageLoadingFinished?.invoke(track, throwable)
            },
            titleTextStyle = if (isCurrentlyPlaying) trackPlayingTextStyle else titleTextStyle,
            subtitleTextStyle = subtitleTextStyle,
            contentPadding = contentPadding,
            showRemoveButton = showRemoveButton,
            onRemoveClick = onRemoveClick,
            onAddToPlaylistClick = if (!showRemoveButton) onAddToPlaylistClick else null
        )
    }
}

object MusifyCompactTrackCardDefaults {
    val defaultContentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 8.dp
    )
}