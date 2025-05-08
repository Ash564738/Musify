package com.example.musify.data.remote.token.tokenmanager

import android.content.SharedPreferences
import android.util.Log
import com.example.musify.data.remote.token.AccessTokenResponse
import com.example.musify.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : TokenManager {

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TAG = "DefaultTokenManager"
    }

    override suspend fun getNewAccessToken(secret: String, grantType: String): AccessTokenResponse =
        throw UnsupportedOperationException("Not used in this flow")

    private fun getStoredRefreshToken(): String? =
        sharedPreferences.getString(REFRESH_TOKEN_KEY, null)

}
