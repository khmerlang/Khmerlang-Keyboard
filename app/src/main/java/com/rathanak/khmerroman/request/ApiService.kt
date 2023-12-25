package com.rathanak.khmerroman.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/spelling-check")
    fun spellCheckIng(@Path("id") postId: Int): Call<SpellCheckResultDTO>
}