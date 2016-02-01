package com.androidyug.marsrover.data.rest;

import com.androidyug.marsrover.data.model.Dataset;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by IAMONE on 12/23/2015.
 */
public interface ApiEndpointInterface {

    @GET("/mars-photos/api/v1/rovers/curiosity/photos")
    Call<Dataset> getPhotoByCuriosity(@Query("earth_date") String earthDate, @Query("page") int page, @Query("api_key")String apiKey);

    @GET("/mars-photos/api/v1/rovers/spirit/photos")
    Call<Dataset> getPhotoBySpirit(@Query("earth_date") String earthDate, @Query("page") int page, @Query("api_key")String apiKey);

    @GET("/mars-photos/api/v1/rovers/opportunity/photos")
    Call<Dataset> getPhotoByOpportunity(@Query("earth_date") String earthDate, @Query("page") int page, @Query("api_key")String apiKey);
}
