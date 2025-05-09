package com.example.musify.domain

val fakeTrackSearchResult = SearchResult.TrackSearchResult(
    "testId",
    "Test Track",
    "",
    "Artist1,Artist2",
    "",
    duration ?: 0,
    position?.toIntOrNull() ?: 0,
    audioDownloadAllowed ?: false,
    audioDownload ?: "",
    shareUrl ?: "",
    shortUrl ?: ""
)