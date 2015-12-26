package br.com.dgimenes.nasapiccontentfetcher.service.api;

import br.com.dgimenes.nasapicserver.model.APOD;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface NasaAPODWebservice {

    @GET("/planetary/apod")
    Call<APOD> getAPOD(@Query("api_key") String nasaApiKey,
                       @Query("concept_tags") boolean conceptTags,
                       @Query("date") String formattedDate);
}
