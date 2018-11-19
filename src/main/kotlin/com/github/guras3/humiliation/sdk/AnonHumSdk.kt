package com.github.guras3.humiliation.sdk

import com.github.guras3.humiliation.sdk.api.HumSdkException
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KLogging
import okhttp3.*

class AnonHumSdk(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient
) : HumSdk {

    override fun start() {
        // todo: use /ping
        getHumiliations(1, false, false)
    }

    override fun destroy() {

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