package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.*
import java.util.*

internal data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String = "bearer",
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("issued_at")
    val issuedAt: Long = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
) {

    fun expired(): Boolean {
        return (LocalDateTime.ofInstant(Instant.ofEpochSecond(issuedAt), ZoneOffset.UTC) + Duration.ofSeconds(expiresIn)) < LocalDateTime.now(ZoneOffset.UTC)
    }

}
