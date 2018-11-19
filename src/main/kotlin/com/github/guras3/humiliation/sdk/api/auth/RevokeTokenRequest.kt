package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonProperty

internal data class RevokeTokenRequest(
    @JsonProperty("token")
    val token: String,
    @JsonProperty("token_type_hint")
    val tokenTypeHint: TokenTypeHint?,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String?
)
