package com.github.guras3.humiliation.sdk

import okhttp3.OkHttpClient

fun main(args: Array<String>) {

    val sdkFactory = SdkFactory(backendBaseUrl = "http://host:port", httpClient = OkHttpClient())

    anonymousExample(sdkFactory)
    authorizedExample(sdkFactory)
    stateChangeListenerExample(sdkFactory)
    saveAndRestoreStateExample(sdkFactory)
    bastardExample(sdkFactory)

}

private fun anonymousExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAnonymous()

    humSdk.start()

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val humiliations2 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

    val humiliations3 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations3)

    humSdk.destroy()

}

private fun bastardExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAnonymous()

    humSdk.start()

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val bastard = humSdk.getBastardPhrase(true)
    println(bastard)

    humSdk.destroy()

}

private fun authorizedExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAuthorized(
        clientId = "clientId",
        clientSecret = "clientSecret",
        grantType = "grantType",
        grantTypeDetails = mapOf("grantTypeDetails" to "grantTypeDetails")
    )

    humSdk.start()

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val humiliations2 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

    humSdk.destroy()

    val humiliations3 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations3)

}

private fun stateChangeListenerExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAuthorized(
        clientId = "clientId",
        clientSecret = "clientSecret",
        grantType = "grantType",
        grantTypeDetails = mapOf("grantTypeDetails" to "grantTypeDetails")
    )

    var state: String? = null

    humSdk.addStateChangeListener { newState ->
        state = newState
    }

    humSdk.start()

    assert(state != null)
    println(state)

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    humSdk.destroy()

    assert(state == null)

}

private fun saveAndRestoreStateExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAuthorized(
        clientId = "clientId",
        clientSecret = "clientSecret",
        grantType = "grantType",
        grantTypeDetails = mapOf("grantTypeDetails" to "grantTypeDetails")
    )

    var state: String? = null

    humSdk.addStateChangeListener { newState ->
        state = newState
    }

    humSdk.start()

    assert(state != null)
    println(state)

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)


    val restoredSdk = sdkFactory.restore(
        clientId = "clientId",
        clientSecret = "clientSecret",
        state = state!!
    )

    restoredSdk.addStateChangeListener { newState ->
        // new listener
    }

    val humiliations2 = restoredSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

}