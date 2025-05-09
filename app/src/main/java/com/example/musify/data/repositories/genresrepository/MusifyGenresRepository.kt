package com.example.musify.data.repositories.genresrepository

import com.example.musify.data.remote.musicservice.SupportedJamendoTags
import com.example.musify.data.remote.musicservice.toGenre
import com.example.musify.domain.Genre
import javax.inject.Inject

class MusifyGenresRepository @Inject constructor() : GenresRepository {
    override fun fetchAvailableGenres(): List<Genre> = SupportedJamendoTags.values().map {
        it.toGenre()
    }
}