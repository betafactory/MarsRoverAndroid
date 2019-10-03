package com.androidyug.marsrover.data.rest

import com.androidyug.marsrover.data.model.Dataset

import retrofit.Call
import retrofit.http.GET
import retrofit.http.Path
import retrofit.http.Query

/**
 * Created by IAMONE on 12/23/2015.
 */
interface ApiEndpointInterface {

    @GET("/mars-photos/api/v1/rovers/{imgType}/photos")
    fun getPhotoByType(@Path("imgType") photoType: String, @Query("earth_date") earthDate: String, @Query("page") page: Int, @Query("api_key") apiKey: String): Call<Dataset>

}
