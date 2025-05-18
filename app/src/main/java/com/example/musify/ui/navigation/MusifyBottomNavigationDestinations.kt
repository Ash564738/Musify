package com.example.musify.ui.navigation

import com.example.musify.R

sealed class MusifyBottomNavigationDestinations(
    val route: String,
    val label: String,
    val outlinedIconVariantResourceId: Int,
    val filledIconVariantResourceId: Int
) {
    object Home : MusifyBottomNavigationDestinations(
        route = MusifyNavigationDestinations.HomeScreen.route,
        label = "Home",
        outlinedIconVariantResourceId = R.drawable.ic_outline_home_24,
        filledIconVariantResourceId = R.drawable.ic_filled_home_24
    )

    object Search : MusifyBottomNavigationDestinations(
        route = MusifyNavigationDestinations.SearchScreen.route,
        label = "Search",
        outlinedIconVariantResourceId = R.drawable.ic_outline_search_24,
        filledIconVariantResourceId = R.drawable.ic_outline_search_24
    )

    object Premium : MusifyBottomNavigationDestinations(
        route = "com.example.musify.ui.navigation.bottom.premium",
        label = "Premium",
        outlinedIconVariantResourceId = R.drawable.ic_spotify_premium,
        filledIconVariantResourceId = R.drawable.ic_spotify_premium
    )

    object Favorite : MusifyBottomNavigationDestinations(
        route = MusifyNavigationDestinations.FavoriteScreen.route,
        label = "Favorite",
        outlinedIconVariantResourceId = R.drawable.ic_outline_favorite_24,
        filledIconVariantResourceId = R.drawable.ic_filled_favorite_24
    )

    object Playlist : MusifyBottomNavigationDestinations(
        route = MusifyNavigationDestinations.PlaylistScreen.route,
        label = "Playlist",
        outlinedIconVariantResourceId = R.drawable.ic_outline_playlist_24,
        filledIconVariantResourceId = R.drawable.ic_filled_playlist_24
    )
}