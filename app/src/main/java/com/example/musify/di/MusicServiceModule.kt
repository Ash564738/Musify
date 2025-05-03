package com.example.musify.di
//import com.example.musify.di.SpotifyRequestInterceptor
import android.util.Log
import com.example.musify.BuildConfig
import com.example.musify.data.remote.musicservice.SpotifyBaseUrls
import com.example.musify.data.remote.musicservice.SpotifyService
import com.example.musify.data.remote.token.TokenApi
import com.example.musify.data.remote.token.tokenmanager.TokenManager
import com.example.musify.utils.defaultMusifyJacksonConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MusicServiceModule {

    @Provides
    @Singleton
    fun provideSpotifyService(
        okHttpClient: OkHttpClient
    ): SpotifyService {
        Log.d("MusicServiceModule", "Initializing SpotifyService...")
        return try {
            Retrofit.Builder()
                .baseUrl(SpotifyBaseUrls.API_URL)
                .client(okHttpClient)
                .addConverterFactory(defaultMusifyJacksonConverterFactory)
                .build()
                .create(SpotifyService::class.java).also {
                    Log.d("MusicServiceModule", "SpotifyService initialized successfully.")
                }
        } catch (e: Exception) {
            Log.e("MusicServiceModule", "Error initializing SpotifyService: ${e.message}", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideTokenManager(): TokenManager {
        Log.d("MusicServiceModule", "Initializing TokenManager...")
        return try {
            Retrofit.Builder()
                .baseUrl(SpotifyBaseUrls.AUTHENTICATION_URL)
                .addConverterFactory(defaultMusifyJacksonConverterFactory)
                .build()
                .create(TokenManager::class.java).also {
                    Log.d("MusicServiceModule", "TokenManager initialized successfully.")
                }
        } catch (e: Exception) {
            Log.e("MusicServiceModule", "Error initializing TokenManager: ${e.message}", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideTokenApi(): TokenApi {
        Log.d("MusicServiceModule", "Initializing TokenApi...")
        return try {
            Retrofit.Builder()
                .baseUrl(SpotifyBaseUrls.AUTHENTICATION_URL)
                .addConverterFactory(defaultMusifyJacksonConverterFactory)
                .build()
                .create(TokenApi::class.java).also {
                    Log.d("MusicServiceModule", "TokenApi initialized successfully.")
                }
        } catch (e: Exception) {
            Log.e("MusicServiceModule", "Error initializing TokenApi: ${e.message}", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor
        // spotifyRequestInterceptor: SpotifyRequestInterceptor
    ): OkHttpClient {
        Log.d("MusicServiceModule", "Initializing OkHttpClient...")
        return try {
            OkHttpClient.Builder()
                .addInterceptor(logging)
                // .addInterceptor(spotifyRequestInterceptor)
                .build().also {
                    Log.d("MusicServiceModule", "OkHttpClient initialized successfully.")
                }
        } catch (e: Exception) {
            Log.e("MusicServiceModule", "Error initializing OkHttpClient: ${e.message}", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        Log.d("MusicServiceModule", "Initializing HttpLoggingInterceptor...")
        return try {
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }.also {
                Log.d("MusicServiceModule", "HttpLoggingInterceptor initialized successfully.")
            }
        } catch (e: Exception) {
            Log.e("MusicServiceModule", "Error initializing HttpLoggingInterceptor: ${e.message}", e)
            throw e
        }
    }

//    @Provides
//    @Singleton
//    fun provideSpotifyRequestInterceptor(
//        tokenRepository: TokenRepository
//    ): SpotifyRequestInterceptor {
//        return SpotifyRequestInterceptor(tokenRepository, "US", "en_US")
//    }
}