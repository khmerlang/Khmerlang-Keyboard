package com.rathanak.khmerroman.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/v1/spelling-check")
    fun spellCheckIng(@Body requestBody: SpellCheckRequestDTO): Call<SpellCheckRespondDTO>
    @POST("/v2/words/selection")
    fun spellWordSelection(@Body requestBody: SpellSelectRequestDTO): Call<SpellSelectRespondDTO>
}