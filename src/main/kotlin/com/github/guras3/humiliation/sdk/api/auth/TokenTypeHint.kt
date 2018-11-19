package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonCreator

internal enum class TokenTypeHint(private val code: String) {
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token");

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(code: String): TokenTypeHint {
            return when (code) {
                ACCESS_TOKEN.code -> ACCESS_TOKEN
                REFRESH_TOKEN.code -> REFRESH_TOKEN
                else -> throw IllegalArgumentException("Unknown TokenTypeHint: $code")
            }
        }
    }
}