package com.example.musify.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musify.R
import com.example.musify.utils.conditional
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer

enum class ListItemCardType { ALBUM, ARTIST, TRACK, PLAYLIST }

@ExperimentalMaterialApi
@Composable
fun MusifyCompactListItemCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    thumbnailImageUrlString: String? = null,
    thumbnailShape: Shape? = null,
    titleTextStyle: TextStyle = LocalTextStyle.current,
    subtitleTextStyle: TextStyle = LocalTextStyle.current,
    isLoadingPlaceHolderVisible: Boolean = false,
    onThumbnailLoading: (() -> Unit)? = null,
    onThumbnailImageLoadingFinished: ((Throwable?) -> Unit)? = null,
    errorPainter: Painter? = null,
    placeholderHighlight: PlaceholderHighlight = PlaceholderHighlight.shimmer(),
    contentPadding: PaddingValues = PaddingValues(all = 8.dp),
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .sizeIn(minHeight = 56.dp, minWidth = 250.dp, maxHeight = 80.dp)
            .then(modifier),
        shape = shape,
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            thumbnailImageUrlString?.let {
                AsyncImageWithPlaceholder(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f, true)
                        .conditional(thumbnailShape != null) { clip(thumbnailShape!!) },
                    model = it,
                    contentScale = ContentScale.Crop,
                    isLoadingPlaceholderVisible = isLoadingPlaceHolderVisible,
                    onImageLoading = { onThumbnailLoading?.invoke() },
                    onImageLoadingFinished = { onThumbnailImageLoadingFinished?.invoke(it) },
                    placeholderHighlight = placeholderHighlight,
                    errorPainter = errorPainter,
                    alpha = LocalContentAlpha.current,
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = titleTextStyle
                )
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = subtitleTextStyle
                )
            }
            trailingContent?.invoke()
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MusifyCompactListItemCard(
    cardType: ListItemCardType,
    title: String,
    subtitle: String,
    thumbnailImageUrlString: String?,
    onClick: () -> Unit,
    onAddToPlaylistClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    titleTextStyle: TextStyle = LocalTextStyle.current,
    subtitleTextStyle: TextStyle = LocalTextStyle.current,
    isLoadingPlaceHolderVisible: Boolean = false,
    onThumbnailLoading: (() -> Unit)? = null,
    onThumbnailImageLoadingFinished: ((Throwable?) -> Unit)? = null,
    errorPainter: Painter? = null,
    placeholderHighlight: PlaceholderHighlight = PlaceholderHighlight.shimmer(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val trailingContent: @Composable () -> Unit = {
        if (cardType == ListItemCardType.TRACK) {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, "Track options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (showRemoveButton) {
                        DropdownMenuItem(
                            onClick = {
                                onRemoveClick()
                                showMenu = false
                            }
                        ) {
                            Text("Remove from Playlist")
                        }
                    } else {
                        onAddToPlaylistClick?.let {
                            DropdownMenuItem(
                                onClick = {
                                    onAddToPlaylistClick.invoke()
                                    showMenu = false
                                }
                            ) {
                                Text("Add to Playlist")
                            }
                        }
                    }
                    DropdownMenuItem(onClick = {}) {
                        Text("Share")
                    }
                }
            }
        } else {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_chevron_right_24),
                    contentDescription = null
                )
            }
        }
    }

    MusifyCompactListItemCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        shape = shape,
        thumbnailImageUrlString = thumbnailImageUrlString,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailingContent = trailingContent,
        thumbnailShape = if (cardType == ListItemCardType.ARTIST) CircleShape else null,
        titleTextStyle = titleTextStyle,
        subtitleTextStyle = subtitleTextStyle,
        isLoadingPlaceHolderVisible = isLoadingPlaceHolderVisible,
        onThumbnailLoading = onThumbnailLoading,
        onThumbnailImageLoadingFinished = onThumbnailImageLoadingFinished,
        placeholderHighlight = placeholderHighlight,
        errorPainter = errorPainter,
        contentPadding = contentPadding
    )
}