package com.github.guras3.humiliation.sdk.impl

import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.auth.*
import com.github.guras3.humiliation.sdk.utils.JsonUtils
import mu.KLogging
import okhttp3.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

internal class TokenManager(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val clientId: String,
    private val clientSecret: String?,
    private val grantType: String,
    private val grantTypeDetails: Map<String, String>,
    token: TokenResponse? = null
) {

    private val listeners = CopyOnWriteArrayList<(state: String?) -> Unit>()

    private var tokenHolder = AtomicReference<TokenResponse>(token)

    fun addStateChangeListener(listener: (state: String?) -> Unit) {
        listeners.add(listener)
    }

    fun getToken(): TokenResponse {
        val token = tokenHolder.get()
        if (token != null) {

            if (!token.expired()) {
                return token
            }

            logger.info { "token expired, trying to refresh" }
            return doRefreshToken()
        }

        logger.info { "no token, requesting new" }
        return doGetToken()
    }

    private fun doGetToken(): TokenResponse {
        val (tokenResponse, securityErrorDescription) = getToken(grantType, grantTypeDetails)
        if (securityErrorDescription != null) throw HumSdkException(securityErrorDescription.errorDescription)
        tokenResponse!!

        tokenHolder.set(tokenResponse)
        listeners.forEach { it(JsonUtils.mapper.writeValueAsString(tokenResponse)) }
        return tokenResponse
    }

    fun refreshToken() {
        doRefreshToken()
    }

    private fun doRefreshToken(): TokenResponse {
        val token = tokenHolder.get() ?: throw HumSdkException("no token")
        val (tokenResponse, securityErrorDescription) = refreshToken(token.refreshToken)
        if (securityErrorDescription != null) throw HumSdkException(securityErrorDescription.errorDescription)
        tokenResponse!!

        tokenHolder.set(tokenResponse)
        listeners.forEach { it(JsonUtils.mapper.writeValueAsString(tokenResponse)) }
        return tokenResponse
    }

    fun revokeToken() {
        val token = tokenHolder.get() ?: throw HumSdkException("no token")
        revokeToken(token.refreshToken, TokenTypeHint.REFRESH_TOKEN)
        //tokenHolder.set(null)
        listeners.forEach { it(null) }
    }

    private fun revokeToken(token: String, tokenTypeHint: TokenTypeHint) {
        val revokeTokenRequest = RevokeTokenRequest(
            clientId = clientId,
            clientSecret = clientSecret,
            token = token,
            tokenTypeHint = tokenTypeHint
        )

        val request = Request.Builder()
            .cacheControl(CacheControl.FORCE_NETWORK)
            .url("$backendBaseUrl/auth/revoke_token")
            .post(
                RequestBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    JsonUtils.mapper.writeValueAsBytes(revokeTokenRequest)
                )
            )
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val errorResponse = JsonUtils.mapper.readValue(body.bytes(), SecurityErrorDescription::class.java)
                logger.error { "failed to revoke token: $errorResponse" }
            } else {
                logger.info { "token revoked" }
            }
        }

    }

    private fun refreshToken(refreshToken: String): Pair<TokenResponse?, SecurityErrorDescription?> {
        return getToken("refresh_token", mapOf("refresh_token" to refreshToken))
    }

    private fun getToken(grantType: String, grantTypeDetails: Map<String, String>): Pair<TokenResponse?, SecurityErrorDescription?> {

        val tokenRequest = TokenRequest(
            clientId = clientId,
            clientSecret = clientSecret,
            grantTypeName = grantType
        )

        grantTypeDetails.forEach { k, v ->
            tokenRequest.addGrantDetail(k, v)
        }

        val request = Request.Builder()
            .cacheControl(CacheControl.FORCE_NETWORK)
            .url("$backendBaseUrl/auth/token")
            .post(
                RequestBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    JsonUtils.mapper.writeValueAsBytes(tokenRequest)
                )
            )
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val errorResponse = JsonUtils.mapper.readValue(body.bytes(), SecurityErrorDescription::class.java)
                logger.error { "failed to get access token: $errorResponse" }
                return null to errorResponse
            }
            val tokenResponse = JsonUtils.mapper.readValue(body.bytes(), TokenResponse::class.java)
            logger.info { "new token: $tokenResponse" }
            return tokenResponse to null
        }
    }

    private companion object : KLogging()

}