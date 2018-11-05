package com.github.guras3.humiliation.sdk

fun main(args: Array<String>) {

//    authorizedExample()

    anonymousExample()

}

//private fun authorizedExample() {
//    val humSdk = HumSdk("http://asd")
//
//    try {
//        humSdk.authorize(

//        )
//    } catch (e: HumSdkAuthException) {
//        // failed to authenticate or authorize
//        return
//    }
//
//    try {
//        val humiliations = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
//        println(humiliations)
//    } catch (e: HumSdkException) {
//        println(e.message)
//        // business error
//    } catch (e: HumSdkAuthException) {
//        println(e.message)
//        // FORCE LOGOUT USER HERE
//    }
//
//    humSdk.deauthorize()
//
//    try {
//        val humiliations = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
//    } catch (e: HumSdkException) {
//        println(e.message)
//        // business error
//    } catch (e: HumSdkAuthException) {
//        println(e.message)
//        // FORCE LOGOUT USER HERE
//    }
//}

private fun anonymousExample() {
    val humSdk = HumSdk("http://asd")

    humSdk.asAnonymous()

    val humiliations1 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations1)

    val humiliations2 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations2)

    val humiliations3 = humSdk.getHumiliations(limit = 1, allowObscene = true, withEpithet = true)
    println(humiliations3)

}
