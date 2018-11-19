package com.github.guras3.humiliation.sdk.api.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class SecurityErrorDescription(
    val status: String,
    val error: SecurityErrorCode?,
    @JsonProperty("error_description")
    val errorDescription: String?
)
