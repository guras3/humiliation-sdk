package com.github.guras3.humiliation.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.auth.*
import mu.KLogging
import okhttp3.*

class TokenManager(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val clientId: String,
    private val clientSecret: String?,
    private val grantType: String,
    private val grantTypeDetails: Map<String, String>,
    private var token: TokenResponse? = null
) {

    fun getToken(): TokenResponse {
        if (token != null) {

            if (token!!.expired()) {
                logger.info { "token expired, trying to refresh.." }
                this.refreshToken()
            }

            return token!!
        }

        val (tokenResponse, securityErrorDescription) = getToken(grantType, grantTypeDetails)
        if (securityErrorDescription != null) throw HumSdkException(securityErrorDescription.errorDescription)
        token = tokenResponse
        return token!!
    }

    fun refreshToken() {
        val (tokenResponse, securityErrorDescription) = refreshToken(token!!.refreshToken)
        if (securityErrorDescription != null) throw HumSdkException(securityErrorDescription.errorDescription)
        token = tokenResponse
    }

    fun revokeToken() {
        revokeToken(token!!.refreshToken, TokenTypeHint.REFRESH_TOKEN)
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
                    mapper.writeValueAsBytes(revokeTokenRequest)
                )
            )
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val errorResponse = mapper.readValue(body.bytes(), SecurityErrorDescription::class.java)
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
                    mapper.writeValueAsBytes(tokenRequest)
                )
            )
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val errorResponse = mapper.readValue(body.bytes(), SecurityErrorDescription::class.java)
                logger.error { "failed to get access token: $errorResponse" }
                return null to errorResponse
            }
            val tokenResponse = mapper.readValue(body.bytes(), TokenResponse::class.java)
            logger.info { "new token: $tokenResponse" }
            return tokenResponse to null
        }
    }

    private companion object : KLogging() {
        val mapper = ObjectMapper().registerKotlinModule()
    }

}