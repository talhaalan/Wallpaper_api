package com.tknsoftwarestudio.wallpaperapi.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiUtils {
    companion object {
        const val BASE_URL = "https://api.unsplash.com"
        const val API_KEY = "QYjZsHr97vrpdPaw9CQQpdm5BZDFiX0z76FNj77ZRIs"
        var retrofit : Retrofit? = null
    }

    object ApiUtils {
        fun getApiInterface() : ApiInterface {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiInterface::class.java)
        }
    }

}