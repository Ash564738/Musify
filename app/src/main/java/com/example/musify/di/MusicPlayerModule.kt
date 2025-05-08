package com.example.musify.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.musify.musicplayer.MusicPlayerV2
import com.example.musify.musicplayer.MusifyBackgroundMusicPlayerV2
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MusicPlayerModule {
    @OptIn(UnstableApi::class)
    @Binds
    @Singleton
    abstract fun bindMusicPlayerV2(
        musifyBackgroundMusicPlayerV2: MusifyBackgroundMusicPlayerV2
    ): MusicPlayerV2
}