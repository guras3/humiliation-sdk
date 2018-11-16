package com.github.guras3.humiliation.sdk

import okhttp3.OkHttpClient

fun main(args: Array<String>) {

    val sdkFactory = SdkFactory("http://95.216.172.51:8888", OkHttpClient())

    anonymousExample(sdkFactory)
    authorizedExample(sdkFactory)

}

private fun anonymousExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAnonymous()

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val humiliations2 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

    val humiliations3 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations3)

    humSdk.destroy()

}

private fun authorizedExample(sdkFactory: SdkFactory) {

    val humSdk = sdkFactory.createAuthorized(
        clientId = "clientId",
        clientSecret = "clientSecret",
        grantType = "grantType",
        grantTypeDetails = mapOf("grantTypeDetails" to "grantTypeDetails")
    )

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val humiliations2 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

    Thread.sleep(65000)

    //humSdk.destroy()

    val humiliations3 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations3)

//    Thread.sleep(125000)
//
//    val humiliations4 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
//    println(humiliations4)


}