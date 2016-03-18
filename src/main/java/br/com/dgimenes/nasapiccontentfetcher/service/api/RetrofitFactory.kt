package br.com.dgimenes.nasapiccontentfetcher.service.api

import com.squareup.okhttp.Cache
import com.squareup.okhttp.OkHttpClient
import retrofit.Retrofit
import retrofit.GsonConverterFactory
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute

object RetrofitFactory {
    val HTTP_CACHE_SIZE_IN_BYTES = 2048L;
    var retrofit : Retrofit? = null

    private fun createOKHttpClient() : OkHttpClient {
        val okHttpClient = OkHttpClient();
        okHttpClient.cache = createHttpClientCache();
        return okHttpClient;
    }

    private fun createHttpClientCache() : Cache {
        return Cache(Files.createTempDirectory("nasapic",
                *arrayOf<FileAttribute<String>>()).toFile(), HTTP_CACHE_SIZE_IN_BYTES);
    }

    fun get(apiBaseUrl : String) : Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .client(createOKHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit!!;
    }
}
