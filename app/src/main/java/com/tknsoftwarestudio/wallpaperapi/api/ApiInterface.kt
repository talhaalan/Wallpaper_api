package com.tknsoftwarestudio.wallpaperapi.api

import com.tknsoftwarestudio.wallpaperapi.api.ApiUtils.Companion.API_KEY
import com.tknsoftwarestudio.wallpaperapi.models.Photo
import com.tknsoftwarestudio.wallpaperapi.models.Search
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Authorization: Client-ID " + API_KEY)
    @GET("/photos")
    fun getImages(@Query("page") page : Int,@Query("per_page") perPage : Int) : Call<List<Photo>>

    @Headers("Authorization: Client-ID " + API_KEY)
    @GET("/search/photos")
    fun searchImage(@Query("query") query: String) : Call<Search>


}