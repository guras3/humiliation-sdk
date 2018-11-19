package com.github.guras3.humiliation.sdk

import com.github.guras3.humiliation.sdk.api.HumSdk
import com.github.guras3.humiliation.sdk.api.auth.TokenResponse
import com.github.guras3.humiliation.sdk.impl.AnonHumSdk
import com.github.guras3.humiliation.sdk.impl.AuthHumSdk
import com.github.guras3.humiliation.sdk.impl.TokenManager
import com.github.guras3.humiliation.sdk.utils.JsonUtils
import okhttp3.OkHttpClient

class SdkFactory(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient
) {

    fun createAnonymous(): HumSdk {
        return AnonHumSdk(backendBaseUrl, httpClient)
    }

    fun createAuthorized(clientId: String, clientSecret: String?, grantType: String, grantTypeDetails: Map<String, String>): HumSdk {
        val tm = TokenManager(backendBaseUrl, httpClient, clientId, clientSecret, grantType, grantTypeDetails)
        return AuthHumSdk(backendBaseUrl, httpClient, tm)
    }

    fun restore(clientId: String, clientSecret: String?, state: String): HumSdk {
        val tokenResponse = JsonUtils.mapper.readValue(state, TokenResponse::class.java)
        val tm = TokenManager(backendBaseUrl, httpClient, clientId, clientSecret, "restore", emptyMap(), tokenResponse)
        return AuthHumSdk(backendBaseUrl, httpClient, tm)
    }

}
