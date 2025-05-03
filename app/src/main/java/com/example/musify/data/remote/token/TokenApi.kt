package com.example.musify.data.remote.token

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun fetchAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): AccessTokenResponse
}