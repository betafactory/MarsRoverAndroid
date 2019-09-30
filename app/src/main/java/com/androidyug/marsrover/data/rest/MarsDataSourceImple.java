package com.androidyug.marsrover.data.rest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.androidyug.marsrover.common.Constant;
import com.androidyug.marsrover.data.MarsDataSource;
import com.androidyug.marsrover.data.model.Dataset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by IAMONE on 12/23/2015.
 */
public class MarsDataSourceImple implements MarsDataSource {

    private static final String LOG_TAG = "MarsDataSourceImple";
    Retrofit client;
    Bus bus;
    Context mContext;


    public MarsDataSourceImple(Context ctx, Bus bus) {

        this.mContext = ctx;
        this.bus = bus;
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();


        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                return response;
            }
        });


        client = new Retrofit.Builder()
                .baseUrl(Constant.API_END_POINT)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Log.d(LOG_TAG, "from Constructor");

    }


    public void makeCallToService(int roverId, int page, String earthDate) {

        ApiEndpointInterface service = client.create(ApiEndpointInterface.class);

        String imageType = getImageType(roverId);
        if (imageType == null) {
            Toast.makeText(mContext, "This rover not in list", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Dataset> callCuriosity = service.getPhotoByType(imageType, earthDate, page, Constant.API_KEY);
        callCuriosity.enqueue(new Callback<Dataset>() {

            @Override
            public void onResponse(retrofit.Response<Dataset> response, Retrofit retrofit) {


                if (response.isSuccess()) {
                    bus.post(response.body());
                    Log.d(LOG_TAG, "retrofit success: " + " : " + response.raw().toString());

                } else {
                    Log.d(LOG_TAG, "retrofit failure: " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });


    }


    private String getImageType(int roverId) {
        switch (roverId) {
            case Constant.ROVER_CURIOUSITY:
                return Constant.IMAGE_CURIOUSITY;
            case Constant.ROVER_OPPORTUNITY:
                return Constant.IMAGE_OPPORTUNITY;
            case Constant.ROVER_SPIRIT:
                return Constant.IMAGE_SPIRIT;
            default:
                return null;
        }
    }


    /////////////////////// interface implemenatin ////////////////////////////////

    @Override
    public void getPhotos() {

    }

    @Override
    public void getDetail(String id) {

    }

    @Override
    public void getKuchKuch() {

    }
}
