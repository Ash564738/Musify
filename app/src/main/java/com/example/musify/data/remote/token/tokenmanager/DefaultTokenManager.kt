package com.example.musify.data.remote.token.tokenmanager

import com.example.musify.data.remote.token.AccessTokenResponse
import com.example.musify.data.remote.token.BearerToken
import com.example.musify.data.remote.token.TokenApi
import com.example.musify.data.remote.token.isExpired
import com.example.musify.data.remote.token.toBearerToken
import com.example.musify.data.repositories.tokenrepository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTokenManager @Inject constructor(
    private val tokenApi: TokenApi
) : TokenManager, TokenRepository {

    private var cachedBearerToken: BearerToken? = null

    override suspend fun getNewAccessToken(secret: String, grantType: String): AccessTokenResponse {
        return tokenApi.fetchAccessToken(secret, grantType)
    }

    override suspend fun getValidBearerToken(): BearerToken {
        val token = cachedBearerToken
        return if (token == null || token.isExpired) {
            val newTokenResponse = getNewAccessToken("your_authorization_header")
            val newToken = newTokenResponse.toBearerToken()
            cachedBearerToken = newToken
            newToken
        } else {
            token
        }
    }
}