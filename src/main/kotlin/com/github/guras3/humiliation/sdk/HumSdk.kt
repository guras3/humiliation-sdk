package com.github.guras3.humiliation.sdk

import com.github.guras3.humiliation.sdk.api.humiliation.Humiliation

interface HumSdk {

    fun getHumiliations(limit: Int, allowObscene: Boolean, withEpithet: Boolean): List<Humiliation>

    fun destroy()

}
