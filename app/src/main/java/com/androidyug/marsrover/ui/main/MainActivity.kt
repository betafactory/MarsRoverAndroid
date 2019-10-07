package com.androidyug.marsrover.ui.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.androidyug.marsrover.R
import com.androidyug.marsrover.data.model.Dataset
import com.androidyug.marsrover.data.model.Photo
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.squareup.otto.ThreadEnforcer
import com.squareup.picasso.Picasso

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnMakeRqst: Button
    private lateinit var tvResult: TextView
    private lateinit var ivResult: ImageView
    private val bus: Bus = Bus(ThreadEnforcer.ANY)

    init {
        bus.register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById<View>(R.id.tv_result) as TextView
        ivResult = findViewById<View>(R.id.iv_result) as ImageView

        btnMakeRqst = findViewById<View>(R.id.btn_make_rqst) as Button
        btnMakeRqst.setOnClickListener(this)

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()
        bus.unregister(this)
    }

    override fun onClick(v: View) {
        val id = v.id

        if (id == R.id.btn_make_rqst) {
            Log.d(LOG_TAG, "clicked")

        }
    }

    @Subscribe
    fun onRespponse(dataset: Dataset) {
        // String res = response.raw().toString();
        //Toast.makeText(this, "from onResponse: " + res, Toast.LENGTH_SHORT).show();
        val data = dataset.photos
        for (p in data) {
            Log.d(LOG_TAG, "" + p.getImgSrc())
            Log.d(LOG_TAG, "" + p.getEarthDate())
            Log.d(LOG_TAG, "=======================================")
        }
        val imgToLoad = data[0].getImgSrc()
        Picasso.with(this).load(imgToLoad).into(ivResult)
        tvResult.text = "" + data.size

    }

    @Subscribe
    fun onResponseTest(txt: String) {
        Toast.makeText(this, "from onResponse: $txt", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val LOG_TAG = "MainActivity"
    }


}
