package com.example.musify.data.remote.token

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class AccessTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val secondsUntilExpiration: Int,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null
)
fun AccessTokenResponse.toBearerToken() = BearerToken(
    accessToken = accessToken,
    timeOfCreation = LocalDateTime.now(),
    secondsUntilExpiration = secondsUntilExpiration,
    refreshToken = refreshToken
)