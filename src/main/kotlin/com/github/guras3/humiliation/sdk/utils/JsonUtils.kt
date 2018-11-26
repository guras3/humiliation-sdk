package com.github.guras3.humiliation.sdk.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation
import java.util.ArrayList

object JsonUtils {
    val mapper = ObjectMapper().registerKotlinModule()
    val arrayListOfHumiliationsType: CollectionLikeType =
        TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList::class.java, Humiliation::class.java)
}