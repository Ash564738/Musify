package com.example.musify.domain

import com.example.musify.data.remote.musicservice.SupportedJamendoTags

data class Genre(
    val id: String,
    val label: String,
    val genreType: GenreType
) {
    enum class GenreType {
        POP,
        ROCK,
        JAZZ,
        ELECTRONIC,
        AMBIENT,
        CHILL,
        CLASSICAL,
        DANCE,
        METAL,
        RAINY_DAY,
        PIANO,
        SLEEP,
        ENERGETIC,
        GUITAR,
        ELECTRIC_GUITAR,
        SAD,
        SYNTHESIZER,
        FUNK
    }
}

fun Genre.GenreType.toSupportedJamendoTags() = when (this) {
    Genre.GenreType.POP -> SupportedJamendoTags.POP
    Genre.GenreType.ROCK -> SupportedJamendoTags.ROCK
    Genre.GenreType.JAZZ -> SupportedJamendoTags.JAZZ
    Genre.GenreType.ELECTRONIC -> SupportedJamendoTags.ELECTRONIC
    else -> throw IllegalArgumentException("Unsupported GenreType: $this")
}