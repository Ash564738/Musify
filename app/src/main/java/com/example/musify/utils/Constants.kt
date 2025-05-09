package com.example.musify.utils

import com.example.musify.BuildConfig

object Constants {
    const val REDIRECT_URI = "musify://callback"
    const val JAMENDO_CLIENT_ID = BuildConfig.JAMENDO_CLIENT_ID
    const val JAMENDO_CLIENT_SECRET = BuildConfig.JAMENDO_CLIENT_SECRET
    const val JAMENDO_API_URL = "https://api.jamendo.com/v3.0/"
    const val DEFAULT_PLAYLIST_IMAGE_URL = "https://i.pinimg.com/736x/cd/04/5d/cd045d2f60aadbbc0532d398f780c361.jpg"
    const val DEFAULT_ALBUM_IMAGE_URL = "https://i.pinimg.com/736x/23/e6/23/23e623ade541ced591501b1e8d11fb12.jpg"
    const val DEFAULT_TRACK_IMAGE_URL = "https://i.pinimg.com/736x/9a/95/d1/9a95d17c580534117215b0f6a50ec96c.jpg"
}