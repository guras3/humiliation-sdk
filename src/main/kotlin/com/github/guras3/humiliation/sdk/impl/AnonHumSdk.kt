package com.github.guras3.humiliation.sdk.impl

import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.HumSdk
import com.github.guras3.humiliation.sdk.api.humiliation.Bastard
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationAddRequest
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationRemoveRequest
import com.github.guras3.humiliation.sdk.utils.JsonUtils
import mu.KLogging
import okhttp3.*
import javax.naming.OperationNotSupportedException

internal class AnonHumSdk(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient
) : HumSdk {

    override fun addFavourites(humiliations: List<FavouriteHumiliationAddRequest>) {
        throw OperationNotSupportedException("unauthorized")
    }

    override fun deleteFavourites(humiliations: List<FavouriteHumiliationRemoveRequest>) {
        throw OperationNotSupportedException("unauthorized")
    }

    override fun getFavourites(): List<Humiliation> {
        throw OperationNotSupportedException("unauthorized")
    }

    override fun start() {
        // todo: use /ping
        getHumiliations(1, false, false)
    }

    override fun destroy() {

    }

    override fun addStateChangeListener(listener: (state: String?) -> Unit) {

    }

    override fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation> {
        val httpUrl = HttpUrl.get("$backendBaseUrl/api/v0/humiliations").newBuilder()
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("allowObscene", allowObscene.toString())
            .addQueryParameter("withEpithet", withEpithet.toString())
            .build()

        val request = Request.Builder().url(httpUrl).build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val rawError = body.string()
                logger.warn { "failed to get humiliations: $rawError" }
                throw HumSdkException(rawError)
            }

            return JsonUtils.mapper.readValue(
                body.bytes(),
                JsonUtils.arrayListOfHumiliationsType
            ) as List<Humiliation>
        }
    }

    override fun getBastardPhrase(allowObscene: Boolean): Bastard {
        val httpUrl = HttpUrl.get("$backendBaseUrl/bastard").newBuilder()
            .addQueryParameter("allowObscene", allowObscene.toString())
            .build()

        val request = Request.Builder().url(httpUrl).build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body()!!
            if (!response.isSuccessful) {
                val rawError = body.string()
                logger.warn { "failed to get humiliations: $rawError" }
                throw HumSdkException(rawError)
            }

            return JsonUtils.mapper.readValue(body.bytes(), Bastard::class.java)
        }
    }

    private companion object: KLogging()
}