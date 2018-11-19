package com.github.guras3.humiliation.sdk.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {
    val mapper = ObjectMapper().registerKotlinModule()
}