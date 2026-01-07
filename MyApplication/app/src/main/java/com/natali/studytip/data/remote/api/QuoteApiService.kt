package com.natali.studytip.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QuoteApiService {

    @Headers("X-Api-Key: nfLJYEmiPp7RUEMKCbiZa2LqPPKyiyS9VCWa2K9B")
    @GET("v2/randomquotes")
    suspend fun getRandomQuote(@Query("categories") category: String = "success"): List<QuoteResponse>
}

data class QuoteResponse(
    val quote: String,
    val author: String,
    val categories: List<String>? = null
)