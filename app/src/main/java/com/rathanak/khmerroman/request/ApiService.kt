package com.rathanak.khmerroman.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/v1/spelling-check")
    fun spellCheckIng(@Body requestBody: SpellCheckRequestDTO): Call<SpellCheckRespondDTO>
}