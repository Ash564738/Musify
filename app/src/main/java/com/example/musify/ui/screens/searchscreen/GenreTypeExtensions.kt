package com.example.musify.ui.screens.searchscreen

import androidx.compose.ui.graphics.Color
import com.example.musify.R
import com.example.musify.domain.Genre
fun Genre.GenreType.getAssociatedImageResource(): Int = when (this) {
    Genre.GenreType.AMBIENT         -> R.drawable.genre_img_ambient
    Genre.GenreType.CHILL           -> R.drawable.genre_img_chill
    Genre.GenreType.CLASSICAL       -> R.drawable.genre_img_classical
    Genre.GenreType.DANCE           -> R.drawable.genre_img_dance
    Genre.GenreType.ELECTRONIC      -> R.drawable.genre_img_electronic
    Genre.GenreType.METAL           -> R.drawable.genre_img_metal
    Genre.GenreType.RAINY_DAY       -> R.drawable.genre_img_rainy_day
    Genre.GenreType.ROCK            -> R.drawable.genre_img_rock
    Genre.GenreType.PIANO           -> R.drawable.genre_img_piano
    Genre.GenreType.POP             -> R.drawable.genre_img_pop
    Genre.GenreType.SLEEP           -> R.drawable.genre_img_sleep
    Genre.GenreType.ENERGETIC       -> R.drawable.genre_img_energetic
    Genre.GenreType.GUITAR          -> R.drawable.genre_img_guitar
    Genre.GenreType.ELECTRIC_GUITAR -> R.drawable.genre_img_electric_guitar
    Genre.GenreType.SAD             -> R.drawable.genre_img_sad
    Genre.GenreType.SYNTHESIZER     -> R.drawable.genre_img_synthesizer
    Genre.GenreType.FUNK            -> R.drawable.genre_img_funk
    Genre.GenreType.JAZZ            -> R.drawable.genre_img_jazz
}

fun Genre.GenreType.getAssociatedBackgroundColor() = when (this) {
    Genre.GenreType.AMBIENT         -> Color(0, 48, 72)
    Genre.GenreType.CHILL           -> Color(71, 126, 149)
    Genre.GenreType.CLASSICAL       -> Color(141, 103, 171)
    Genre.GenreType.DANCE           -> Color(140, 25, 50)
    Genre.GenreType.ELECTRONIC      -> Color(186, 93, 7)
    Genre.GenreType.METAL           -> Color(119, 119, 119)
    Genre.GenreType.RAINY_DAY       -> Color(144, 168, 192)
    Genre.GenreType.ROCK            -> Color(230, 30, 50)
    Genre.GenreType.PIANO           -> Color(71, 125, 149)
    Genre.GenreType.POP             -> Color(141, 103, 171)
    Genre.GenreType.SLEEP           -> Color(30, 50, 100)
    Genre.GenreType.ENERGETIC       -> Color(200, 100, 20)
    Genre.GenreType.GUITAR          -> Color(80, 30, 10)
    Genre.GenreType.ELECTRIC_GUITAR -> Color(50, 0, 100)
    Genre.GenreType.SAD             -> Color(40, 40, 80)
    Genre.GenreType.SYNTHESIZER     -> Color(100, 0, 100)
    Genre.GenreType.FUNK            -> Color(255, 165, 0)
    Genre.GenreType.JAZZ            -> Color(0, 100, 0)
}
