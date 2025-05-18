package com.example.musify.domain

import com.example.musify.domain.SearchResult.TrackSearchResult

fun Song.toTrackSearchResult() = TrackSearchResult(
    id = id,
    name = title,
    imageUrlString = imageUrl,
    artistsString = artist,
    trackUrlString = streamUrl,
    duration = duration.toInt(),
    trackPosition = 0,
    audioDownloadAllowed = false,
    audioDownloadUrl = "",
    shareUrl = "",
    shortUrl = ""
)