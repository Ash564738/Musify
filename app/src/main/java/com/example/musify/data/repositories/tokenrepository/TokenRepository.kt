package com.example.musify.data.repositories.tokenrepository

import com.example.musify.data.remote.token.BearerToken

interface TokenRepository {
    suspend fun getValidBearerToken(): BearerToken
}