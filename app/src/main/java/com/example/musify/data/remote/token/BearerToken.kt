package com.example.musify.data.remote.token

import java.time.LocalDateTime
data class BearerToken(
    val accessToken: String,
    val timeOfCreation: LocalDateTime,
    val secondsUntilExpiration: Int,
    val refreshToken: String? = null
) {
    val value get() = "Bearer $accessToken"
    override fun toString(): String = "Bearer $accessToken"
}

val BearerToken.isExpired: Boolean
    get() {
        val timeOfExpiration = timeOfCreation.plusSeconds(secondsUntilExpiration.toLong())
        return LocalDateTime.now() > timeOfExpiration
    }