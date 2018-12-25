package com.github.guras3.humiliation.sdk.api

import com.github.guras3.humiliation.sdk.api.humiliation.Bastard
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationAddRequest
import com.github.guras3.humiliation.sdk.api.humiliation.FavouriteHumiliationRemoveRequest
import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation

interface HumSdk {

    fun start()

    fun destroy()

    fun addStateChangeListener(listener: (state: String?) -> Unit)

    fun addFavourites(humiliations: List<FavouriteHumiliationAddRequest>)

    fun deleteFavourites(humiliations: List<FavouriteHumiliationRemoveRequest>)

    fun getFavourites(): List<Humiliation>

    fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation>

    fun getBastardPhrase(allowObscene: Boolean): Bastard

}
