package com.github.guras3.humiliation.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.auth.*
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import mu.KLogging
import okhttp3.*
import java.util.concurrent.CopyOnWriteArrayList

class AuthHumSdk(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val tokenManager: TokenManager
) : HumSdk {

    private lateinit var latestTokenResponse: TokenResponse

    private val listeners = CopyOnWriteArrayList<SdkStateChangeListener>()

    override fun init() {
        latestTokenResponse = tokenManager.getToken()
    }

    override fun destroy() {
        tokenManager.revokeToken()
    }

    override fun addStateChangeListener(listener: SdkStateChangeListener) {
        listeners.add(listener)
    }

    override fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation> {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations").newBuilder()
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("allowObscene", allowObscene.toString())
            .addQueryParameter("withEpithet", withEpithet.toString())
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .header("Authorization", "${latestTokenResponse.tokenType.capitalize()} ${latestTokenResponse.accessToken}")
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val rawError = body.string()
                logger.warn { "failed to get humiliations: $rawError" }
                if (response.code() == 401) {
                    logger.info { "trying to refresh expired token.." }
                    latestTokenResponse = tokenManager.refreshToken()
                    return getHumiliations(limit, allowObscene, withEpithet)
                } else {
                    throw HumSdkException(rawError)
                }
            }

            return mapper.readValue(
                body.bytes(),
                arrayListOfHumiliationsType
            ) as List<Humiliation>
        }
    }

    private companion object : KLogging() {
        val mapper = ObjectMapper().registerKotlinModule()
        val arrayListOfHumiliationsType: CollectionLikeType =
            TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList::class.java, Humiliation::class.java)
    }
}

interface TokenManager {
    fun getToken(): Pair<TokenResponse?, SecurityErrorDescription?>
    fun refreshToken(): Pair<TokenResponse?, SecurityErrorDescription?>
    fun revokeToken()
}

abstract class AbstractTokenManager(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val clientId: String,
    private val clientSecret: String?
) : TokenManager {

    protected fun revokeToken(token: String, tokenTypeHint: TokenTypeHint) {
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

    protected fun refreshToken(refreshToken: String): Pair<TokenResponse?, SecurityErrorDescription?> {
        return getToken("refresh_token", mapOf("refresh_token" to refreshToken))
    }

    protected fun getToken(grantType: String, grantTypeDetails: Map<String, String>): Pair<TokenResponse?, SecurityErrorDescription?> {

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

class PreAuthTokenManager(
    backendBaseUrl: String,
    httpClient: OkHttpClient,
    clientId: String,
    clientSecret: String?,
    private var token: TokenResponse
) : AbstractTokenManager(backendBaseUrl, httpClient, clientId, clientSecret) {

    override fun getToken(): Pair<TokenResponse?, SecurityErrorDescription?> {
        return token to null
    }

    override fun refreshToken(): Pair<TokenResponse?, SecurityErrorDescription?> {
        return super.refreshToken(token.refreshToken)
    }

    override fun revokeToken() {
        super.revokeToken(token.refreshToken, TokenTypeHint.REFRESH_TOKEN)
    }

}

class DefaultTokenService(
    backendBaseUrl: String,
    httpClient: OkHttpClient,
    clientId: String,
    clientSecret: String?,
    private val grantType: String,
    private val grantTypeDetails: Map<String, String>
) : AbstractTokenManager(backendBaseUrl, httpClient, clientId, clientSecret) {

    private var token: TokenResponse? = null

    override fun getToken(): Pair<TokenResponse?, SecurityErrorDescription?> {
        if (token != null) {
            return token to null
        }
        return super.getToken(grantType, grantTypeDetails)
    }

    override fun refreshToken(): Pair<TokenResponse?, SecurityErrorDescription?> {
        return super.refreshToken(token!!.refreshToken)
    }

    override fun revokeToken() {
        super.revokeToken(token!!.refreshToken, TokenTypeHint.REFRESH_TOKEN)
    }


}