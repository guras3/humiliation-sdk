package com.github.guras3.humiliation.sdk

import com.github.guras3.humiliation.sdk.api.HumSdkAuthException
import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.auth.*
import mu.KLogging
import okhttp3.*
import java.util.concurrent.atomic.AtomicBoolean

class HumSdk(private val backendBaseUrl: String) {

    private var latestTokenResponse: TokenResponse? = null

    private val apiClient: OkHttpClient
    private val authClient: OkHttpClient

    private lateinit var clientId: String
    private var clientSecret: String? = null
    private lateinit var grantType: String
    private lateinit var grantTypeDetails: Map<String, String>

    private val initialized = AtomicBoolean(false)
    private val authorized = AtomicBoolean(false)

    init {
        apiClient = OkHttpClient.Builder().build()
        authClient = OkHttpClient.Builder().build()
    }

    fun asAnonymous() {
        if (initialized.get()) {
            throw HumSdkException("already initialized")
        }
        initialized.set(true)
        authorized.set(false)
    }

    fun authorize(clientId: String, clientSecret: String?, grantType: String, grantTypeDetails: Map<String, String>) {
        if (initialized.get()) {
            throw HumSdkException("already initialized")
        }
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.grantType = grantType
        this.grantTypeDetails = grantTypeDetails

        val (tokenResponse, error) = getToken(grantType, grantTypeDetails)
        if (error != null) throw HumSdkAuthException("${error.error}: ${error.errorDescription}")
        latestTokenResponse = tokenResponse!!
        authorized.set(true)
        initialized.set(true)
    }

    fun deauthorize() {
        if (!initialized.get()) {
            throw HumSdkException("uninitialized")
        }
        if (!authorized.get()) {
            throw HumSdkException("sdk in anonymous mode")
        }
        initialized.set(false)
        val revokeTokenRequest = RevokeTokenRequest(
            clientId = clientId,
            clientSecret = clientSecret,
            token = latestTokenResponse!!.refreshToken,
            tokenTypeHint = TokenTypeHint.REFRESH_TOKEN
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

        authClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val errorResponse = mapper.readValue(body.bytes(), SecurityErrorDescription::class.java)
                logger.error { "failed to revoke token: $errorResponse" }
                return
            }
            logger.info { "token revoked" }
        }

    }

    fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation> {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations").newBuilder()
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("allowObscene", allowObscene.toString())
            .addQueryParameter("withEpithet", withEpithet.toString())
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .let { builder ->
                if (authorized.get()) {
                    builder.header("Authorization", "${latestTokenResponse!!.tokenType.capitalize()} ${latestTokenResponse!!.accessToken}")
                }
                builder
            }
            .build()

        val humiliations = apiClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val rawError = body.string()
                logger.warn { "failed to get humiliations: $rawError" }
                if (response.code() == 401) {
                    logger.info { "trying to refresh expired token.." }
                    val (newTokenResponse, newError) = refreshToken(latestTokenResponse!!.refreshToken)
                    if (newError != null) {
                        logger.warn { "failed to refresh token: $newError" }
                        throw HumSdkAuthException("${newError.error}: ${newError.errorDescription}")
                    }
                    latestTokenResponse = newTokenResponse!!
                    return@use getHumiliations(limit, allowObscene, withEpithet)
                } else {
                    throw HumSdkException(rawError)
                }
            }

            return@use mapper.readValue(body.bytes(),
                arrayListOfHumiliationsType
            ) as List<Humiliation>
        }

        return humiliations
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

        authClient.newCall(request).execute().use { response ->
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
        val arrayListOfHumiliationsType: CollectionLikeType =
            TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList::class.java, Humiliation::class.java)
    }
}