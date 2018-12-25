package com.github.guras3.humiliation.sdk.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.HumSdk
import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.humiliation.Bastard
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationAddRequest
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationRemoveRequest
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import com.github.guras3.humiliation.sdk.utils.JsonUtils
import mu.KLogging
import okhttp3.*
import java.util.*

internal class AuthHumSdk(
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

    override fun addStateChangeListener(listener: (state: String?) -> Unit) {
        tokenManager.addStateChangeListener(listener)
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

            return JsonUtils.mapper.readValue(
                body.bytes(),
                JsonUtils.arrayListOfHumiliationsType
            ) as List<Humiliation>
        }
    }

    override fun addFavourites(humiliations: List<FavouriteHumiliationAddRequest>) {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations/favourites/add").newBuilder().build()

        val token = tokenManager.getToken()

        val request = Request.Builder()
            .url(httpUrl)
            .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), JsonUtils.mapper.writeValueAsBytes(humiliations)))
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
                    addFavourites(humiliations)
                } else {
                    throw HumSdkException(rawError)
                }
            }
        }

    }

    override fun deleteFavourites(humiliations: List<FavouriteHumiliationRemoveRequest>) {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations/favourites/delete").newBuilder().build()

        val token = tokenManager.getToken()

        val request = Request.Builder()
            .url(httpUrl)
            .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), JsonUtils.mapper.writeValueAsBytes(humiliations)))
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
                    deleteFavourites(humiliations)
                } else {
                    throw HumSdkException(rawError)
                }
            }
        }

    }

    override fun getFavourites(): List<Humiliation> {

        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations/favourites").newBuilder().build()

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
                    return getFavourites()
                } else {
                    throw HumSdkException(rawError)
                }
            }

            return JsonUtils.mapper.readValue(
                body.bytes(),
                JsonUtils.arrayListOfHumiliationsType
            ) as List<Humiliation>
        }
    }

    override fun getBastardPhrase(allowObscene: Boolean): Bastard {
        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/bastard").newBuilder()
            .addQueryParameter("allowObscene", allowObscene.toString())
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
                logger.warn { "failed to get bastard phrase: $rawError" }
                if (response.code() == 401) {
                    logger.info { "token probably expired, trying to refresh.." }
                    tokenManager.refreshToken()
                    return getBastardPhrase(allowObscene)
                } else {
                    throw HumSdkException(rawError)
                }
            }

            return JsonUtils.mapper.readValue(
                body.bytes(),
                Bastard::class.java
            )
        }
    }

    private companion object : KLogging()
}
