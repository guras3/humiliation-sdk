package com.github.guras3.humiliation.sdk

import okhttp3.OkHttpClient

class SdkFactory(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient
) {

    fun createAnonymous(): HumSdk {
        return AnonHumSdk(backendBaseUrl, httpClient)
    }

    fun createAuthorized(clientId: String, clientSecret: String?, grantType: String, grantTypeDetails: Map<String, String>): HumSdk {
        val ts = DefaultTokenService(backendBaseUrl, httpClient, clientId, clientSecret, grantType, grantTypeDetails)
        return AuthHumSdk(backendBaseUrl, httpClient, ts)
    }

    fun restore(state: Map<String, Any>): HumSdk {
        TODO()
    }

}