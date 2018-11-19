package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonProperty

internal data class RefreshTokenRequest(
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String?
)
