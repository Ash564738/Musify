package com.example.musify.di

import com.example.musify.data.encoder.AndroidBase64Encoder
import com.example.musify.data.encoder.Base64Encoder
import com.example.musify.data.repositories.favouritesongrepository.SongRepository
import com.example.musify.data.repositories.playlistrepository.PlaylistRepository
import com.example.musify.data.repositories.tokenrepository.SpotifyTokenRepository
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    @Binds
    abstract fun bindBase64Encoder(
        androidBase64Encoder: AndroidBase64Encoder
    ): Base64Encoder

    @Binds
    @Singleton
    abstract fun bindTokenRepository(
        spotifyTokenRepository: SpotifyTokenRepository
    ): TokenRepository

}

@Module
@InstallIn(SingletonComponent::class)
object AppProviderModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideSongRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): SongRepository = SongRepository(firestore, auth)

    @Provides
    fun providePlaylistRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): PlaylistRepository = PlaylistRepository(firestore, auth)
}