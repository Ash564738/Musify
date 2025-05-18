package com.example.musify.ui.navigation

import com.example.musify.domain.SearchResult
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class MusifyNavigationDestinations(val route: String) {
    object SearchScreen : MusifyNavigationDestinations("MusifyNavigationDestinations.SearchScreen")
    object ArtistDetailScreen : MusifyNavigationDestinations(
        route = "MusifyNavigationDestinations.ArtistDetailScreen/{artistId}/{artistName}?encodedUrlString={encodedImageUrlString}"
    ) {
        const val NAV_ARG_ARTIST_ID = "artistId"
        const val NAV_ARG_ARTIST_NAME = "artistName"
        const val NAV_ARG_ENCODED_IMAGE_URL_STRING = "encodedImageUrlString"
        fun buildRoute(artistSearchResult: SearchResult.ArtistSearchResult): String {
            val baseRoute = "MusifyNavigationDestinations.ArtistDetailScreen/${artistSearchResult.id}/${artistSearchResult.name}"
            val encodedImageUrl = artistSearchResult.imageUrlString?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            }
            return if (encodedImageUrl != null) "$baseRoute?encodedUrlString=$encodedImageUrl" else baseRoute
        }
    }
    object AlbumDetailScreen : MusifyNavigationDestinations(
        route = "MusifyNavigationDestinations.AlbumDetailScreen/{albumId}/{albumName}/{artistsString}/{yearOfReleaseString}/{encodedImageUrlString}"
    ) {
        const val NAV_ARG_ALBUM_ID = "albumId"
        const val NAV_ARG_ALBUM_NAME = "albumName"
        const val NAV_ARG_ARTISTS_STRING = "artistsString"
        const val NAV_ARG_ENCODED_IMAGE_URL_STRING = "encodedImageUrlString"
        const val NAV_ARG_YEAR_OF_RELEASE_STRING = "yearOfReleaseString"
        fun buildRoute(albumSearchResult: SearchResult.AlbumSearchResult): String {
            val encodedName = URLEncoder.encode(albumSearchResult.name, "UTF-8")
            val encodedArtists = URLEncoder.encode(albumSearchResult.artistsString, "UTF-8")
            val encodedImageUrl = URLEncoder.encode(albumSearchResult.albumArtUrlString, "UTF-8")
            return "MusifyNavigationDestinations.AlbumDetailScreen/" +
                    "${albumSearchResult.id}/" +
                    "$encodedName/" +
                    "$encodedArtists/" +
                    "${albumSearchResult.yearOfReleaseString.substringBefore("-")}/" +
                    encodedImageUrl
        }
    }
    object PlaylistDetailScreen : MusifyNavigationDestinations(
        route = "MusifyNavigationDestinations.PlaylistDetailScreen/{source}/{playlistId}?" +
                "playlistName={playlistName}&ownerName={ownerName}" +
                "&numberOfTracks={numberOfTracks}&encodedImageUrlString={encodedImageUrlString}"
    ) {
        const val NAV_ARG_SOURCE = "source"
        const val NAV_ARG_PLAYLIST_ID = "playlistId"
        const val NAV_ARG_PLAYLIST_NAME = "playlistName"
        const val NAV_ARG_OWNER_NAME = "ownerName"
        const val NAV_ARG_NUMBER_OF_TRACKS = "numberOfTracks"
        const val NAV_ARG_ENCODED_IMAGE_URL_STRING = "encodedImageUrlString"
        fun buildJamendoRoute(playlistSearchResult: SearchResult.PlaylistSearchResult): String {
            val encodedImage = URLEncoder.encode(playlistSearchResult.imageUrlString, "UTF-8")
            return "MusifyNavigationDestinations.PlaylistDetailScreen/" +
                    "jamendo/${playlistSearchResult.id}" +
                    "?playlistName=${playlistSearchResult.name}" +
                    "&ownerName=${playlistSearchResult.ownerName}" +
                    "&numberOfTracks=${playlistSearchResult.totalNumberOfTracks}" +
                    "&encodedImageUrlString=$encodedImage"
        }
        fun buildUserRoute(userPlaylistId: String): String {
            return "MusifyNavigationDestinations.PlaylistDetailScreen/user/$userPlaylistId" +
                    "?playlistName=My%20Playlist&ownerName=Me&numberOfTracks=0"
        }
    }
    object HomeScreen : MusifyNavigationDestinations("MusifyNavigationDestinations.HomeScreen")
    object PodcastEpisodeDetailScreen : MusifyNavigationDestinations(
        route = "MusifyNavigationDestinations.PodcastEpisodeDetailScreen/{episodeId}"
    ) {
        const val NAV_ARG_PODCAST_EPISODE_ID = "episodeId"

        fun buildRoute(episodeId: String): String {
            return "MusifyNavigationDestinations.PodcastEpisodeDetailScreen/$episodeId"
        }
    }
    object PodcastShowDetailScreen : MusifyNavigationDestinations(
        route = "MusifyNavigationDestinations.PodcastShowDetailScreen/{showId}"
    ) {
        const val NAV_ARG_PODCAST_SHOW_ID = "showId"

        fun buildRoute(showId: String): String {
            return "MusifyNavigationDestinations.PodcastShowDetailScreen/$showId"
        }
    }
    object FavoriteScreen : MusifyNavigationDestinations("MusifyNavigationDestinations.FavoriteScreen")
    object PlaylistScreen : MusifyNavigationDestinations("MusifyNavigationDestinations.PlaylistScreen")
}