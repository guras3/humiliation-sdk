package com.github.guras3.humiliation.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import mu.KLogging
import okhttp3.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class AuthHumSdk(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val tokenManager: TokenManager
) : HumSdk {

    override fun start() {
        tokenManager.getToken()
    }

    override fun destroy() {
        tokenManager.revokeToken()
    }

    override fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation> {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations").newBuilder()
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("allowObscene", allowObscene.toString())
            .addQueryParameter("withEpithet", withEpithet.toString())
            .build()

        val token = tokenManager.getToken()

        val request = Request.Builder()
            .url(httpUrl)
            .header("Authorization", "${token.tokenType.capitalize()} ${token.accessToken}")
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val rawError = body.string()
                logger.warn { "failed to get humiliations: $rawError" }
                if (response.code() == 401) {
                    logger.info { "token probably expired, trying to refresh.." }
                    tokenManager.refreshToken()
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
