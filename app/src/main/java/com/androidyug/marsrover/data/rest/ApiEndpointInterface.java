package com.androidyug.marsrover.data.rest;

import com.androidyug.marsrover.data.model.Dataset;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by IAMONE on 12/23/2015.
 */
public interface ApiEndpointInterface {

    @GET("/mars-photos/api/v1/rovers/{imgType}/photos")
    Call<Dataset> getPhotoByType(@Path("imgType") String photoType, @Query("earth_date") String earthDate, @Query("page") int page, @Query("api_key")String apiKey);

}
