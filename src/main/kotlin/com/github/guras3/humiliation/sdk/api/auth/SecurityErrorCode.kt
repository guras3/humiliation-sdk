package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonCreator

enum class SecurityErrorCode(val code: String) {
    INVALID_REQUEST("invalid_request"),
    INVALID_CLIENT("invalid_client"),
    INVALID_GRANT("invalid_grant"),
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
    UNAUTHORIZED("unauthorized"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(code: String): SecurityErrorCode {
            return when (code) {
                INVALID_REQUEST.code -> INVALID_REQUEST
                INVALID_CLIENT.code -> INVALID_CLIENT
                INVALID_GRANT.code -> INVALID_GRANT
                UNAUTHORIZED_CLIENT.code -> UNAUTHORIZED_CLIENT
                UNSUPPORTED_GRANT_TYPE.code -> UNSUPPORTED_GRANT_TYPE
                UNAUTHORIZED.code -> UNAUTHORIZED
                else -> throw IllegalArgumentException("Unknown SecurityErrorCode: $code")
            }
        }
    }
}
