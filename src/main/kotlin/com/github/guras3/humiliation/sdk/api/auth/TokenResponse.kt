package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.*
import java.util.*

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String = "bearer",
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonIgnore
    val issuedAt: Long = OffsetDateTime.now().toEpochSecond()// todo UTC
) {

    fun expired(): Boolean {// todo UTC
        return (LocalDateTime.ofInstant(Instant.ofEpochSecond(issuedAt), ZoneId.systemDefault()) + Duration.ofSeconds(expiresIn)) < LocalDateTime.now()
    }

}
