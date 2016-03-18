package br.com.dgimenes.nasapiccontentfetcher.service.api

import br.com.dgimenes.nasapicserver.model.APOD
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface NasaAPODWebservice {

    @GET("/planetary/apod")
    fun getAPOD(@Query("api_key") nasaApiKey: String,
                @Query("concept_tags") conceptTags: Boolean,
                @Query("date") formattedDate: String): Call<APOD>
}
