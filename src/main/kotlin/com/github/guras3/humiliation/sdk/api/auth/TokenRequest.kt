package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

internal data class TokenRequest(
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String?,
    @JsonProperty("grant_type")
    val grantTypeName: String
) {

    val grantDetails: Map<String, String>
        @JsonAnyGetter
        get() = _grantDetails

    @JsonIgnore
    private val _grantDetails: MutableMap<String, String> = mutableMapOf()

    @JsonAnySetter
    fun addGrantDetail(name: String, value: String) {
        _grantDetails[name] = value
    }

}