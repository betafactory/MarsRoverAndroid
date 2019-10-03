package com.androidyug.marsrover.data.rest

import android.content.Context
import android.util.Log
import android.widget.Toast

import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.data.MarsDataSource
import com.androidyug.marsrover.data.model.Dataset
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Response
import com.squareup.otto.Bus

import java.io.IOException

import retrofit.Call
import retrofit.Callback
import retrofit.GsonConverterFactory
import retrofit.Retrofit

/**
 * Created by IAMONE on 12/23/2015.
 */
class MarsDataSourceImple(private var mContext: Context, private var bus: Bus) : MarsDataSource {
    private var client: Retrofit

    init {
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create()


        val okHttpClient = OkHttpClient()
        okHttpClient.interceptors().add(Interceptor { chain -> chain.proceed(chain.request()) })


        client = Retrofit.Builder()
                .baseUrl(Constant.API_END_POINT)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        Log.d(LOG_TAG, "from Constructor")

    }


    fun makeCallToService(roverId: Int, page: Int, earthDate: String) {

        val service = client.create(ApiEndpointInterface::class.java)

        val imageType = getImageType(roverId)
        if (imageType == null) {
            Toast.makeText(mContext, "This rover not in list", Toast.LENGTH_SHORT).show()
            return
        }

        val callCuriosity = service.getPhotoByType(imageType, earthDate, page, Constant.API_KEY)
        callCuriosity.enqueue(object : Callback<Dataset> {

            override fun onResponse(response: retrofit.Response<Dataset>, retrofit: Retrofit) {


                if (response.isSuccess) {
                    bus.post(response.body())
                    Log.d(LOG_TAG, "retrofit success: " + " : " + response.raw().toString())

                } else {
                    Log.d(LOG_TAG, "retrofit failure: " + response.raw().toString())
                }
            }

            override fun onFailure(t: Throwable) {

            }
        })


    }


    private fun getImageType(roverId: Int): String? {
        return when (roverId) {
            Constant.ROVER_CURIOUSITY -> Constant.IMAGE_CURIOUSITY
            Constant.ROVER_OPPORTUNITY -> Constant.IMAGE_OPPORTUNITY
            Constant.ROVER_SPIRIT -> Constant.IMAGE_SPIRIT
            else -> null
        }
    }


    /////////////////////// interface implemenatin ////////////////////////////////

    override fun getPhotos() {

    }

    override fun getDetail(id: String) {

    }

    override fun getKuchKuch() {

    }

    companion object {

        private val LOG_TAG = "MarsDataSourceImple"
    }
}
